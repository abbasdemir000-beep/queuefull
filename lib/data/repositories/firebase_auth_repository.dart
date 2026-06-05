import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';

import '../../core/constants/app_constants.dart';
import '../../domain/models/app_user.dart';
import '../../domain/repositories/auth_repository.dart';

/// Sentinel value returned by [sendOtp] when Android auto-verifies the SMS
/// without user input. Callers should check for this value and skip the manual
/// OTP entry screen.
const String kAutoVerifiedSentinel = '__AUTO_VERIFIED__';

/// Firebase implementation of [AuthRepository].
/// Uses FirebaseAuth for phone OTP authentication and Firestore for user data.
class FirebaseAuthRepository implements AuthRepository {
  FirebaseAuthRepository({
    FirebaseAuth? firebaseAuth,
    FirebaseFirestore? firestore,
  })  : _auth = firebaseAuth ?? FirebaseAuth.instance,
        _firestore = firestore ?? FirebaseFirestore.instance;

  final FirebaseAuth _auth;
  final FirebaseFirestore _firestore;

  CollectionReference<Map<String, dynamic>> get _usersCollection =>
      _firestore.collection(AppConstants.usersCollection);

  @override
  Future<String> sendOtp(String phoneNumber) async {
    final completer = Completer<String>();

    await _auth.verifyPhoneNumber(
      phoneNumber: phoneNumber,
      verificationCompleted: (PhoneAuthCredential credential) async {
        // Auto-verification on Android - sign in and complete the future
        await _auth.signInWithCredential(credential);
        if (!completer.isCompleted) {
          completer.complete(kAutoVerifiedSentinel);
        }
      },
      verificationFailed: (FirebaseAuthException e) {
        if (!completer.isCompleted) {
          completer.completeError(e);
        }
      },
      codeSent: (String verificationId, int? resendToken) {
        if (!completer.isCompleted) {
          completer.complete(verificationId);
        }
      },
      codeAutoRetrievalTimeout: (String verificationId) {
        // Timeout - verificationId can still be used manually
      },
    );

    return completer.future;
  }

  @override
  Future<AppUser> verifyOtp(String verificationId, String otp) async {
    final credential = PhoneAuthProvider.credential(
      verificationId: verificationId,
      smsCode: otp,
    );

    final userCredential = await _auth.signInWithCredential(credential);
    final firebaseUser = userCredential.user;

    if (firebaseUser == null) {
      throw Exception('Authentication failed: no user returned');
    }

    // Check if user document exists
    final doc = await _usersCollection.doc(firebaseUser.uid).get();

    if (doc.exists) {
      // Existing user - return from Firestore
      final data = doc.data()!;
      data['uid'] = doc.id;
      return AppUser.fromJson(data);
    } else {
      // New user - create document
      final newUser = AppUser(
        uid: firebaseUser.uid,
        phoneNumber: firebaseUser.phoneNumber ?? '',
        name: '',
        role: UserRole.user,
        points: AppConstants.welcomePoints,
        banned: false,
        createdAt: DateTime.now(),
      );

      await _usersCollection.doc(firebaseUser.uid).set(newUser.toJson());
      return newUser;
    }
  }

  @override
  Future<AppUser?> getCurrentUser() async {
    final firebaseUser = _auth.currentUser;
    if (firebaseUser == null) return null;

    final doc = await _usersCollection.doc(firebaseUser.uid).get();
    if (!doc.exists) return null;

    final data = doc.data()!;
    data['uid'] = doc.id;
    return AppUser.fromJson(data);
  }

  @override
  Future<void> signOut() async {
    await _auth.signOut();
  }

  @override
  Future<void> deleteAccount() async {
    final firebaseUser = _auth.currentUser;
    if (firebaseUser == null) return;

    // Delete user document from Firestore first
    await _usersCollection.doc(firebaseUser.uid).delete();

    // Then delete the Firebase Auth user
    await firebaseUser.delete();
  }

  @override
  Stream<AppUser?> authStateChanges() {
    return _auth.authStateChanges().asyncMap((firebaseUser) async {
      if (firebaseUser == null) return null;

      final doc = await _usersCollection.doc(firebaseUser.uid).get();
      if (!doc.exists) return null;

      final data = doc.data()!;
      data['uid'] = doc.id;
      return AppUser.fromJson(data);
    });
  }
}
