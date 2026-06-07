// GENERATED FILE - ANDROID CONFIGURED MANUALLY
// Android values were derived from android/app/google-services.json.

import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;
import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, kIsWeb, TargetPlatform;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      throw UnsupportedError(
        'DefaultFirebaseOptions have not been configured for web.',
      );
    }

    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
      case TargetPlatform.macOS:
      case TargetPlatform.windows:
      case TargetPlatform.linux:
      case TargetPlatform.fuchsia:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for this platform.',
        );
    }
  }

  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'AIzaSyAxDO6J9sPaW33jN32ly4X1jOKh9ktHdGs',
    appId: '1:450639289776:android:b8056e6c33a2375bc38a75',
    messagingSenderId: '450639289776',
    projectId: 'queue-6a58d',
    storageBucket: 'queue-6a58d.firebasestorage.app',
  );
}
