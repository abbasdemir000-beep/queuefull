package com.example.domain.repository

import com.example.domain.model.AdCategory
import com.example.domain.model.Advertisement
import kotlinx.coroutines.flow.Flow

interface AdvertisementRepository {
    val activeAds: Flow<List<Advertisement>>
    fun getAdsByCity(city: String): Flow<List<Advertisement>>
    fun getAdsByCategory(category: AdCategory): Flow<List<Advertisement>>
    suspend fun insertAd(ad: Advertisement)
    suspend fun deleteAd(id: Int)
    suspend fun incrementViewCount(id: Int)
    suspend fun incrementClickCount(id: Int)
}
