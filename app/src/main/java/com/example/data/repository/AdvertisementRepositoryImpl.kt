package com.example.data.repository

import android.content.Context
import com.example.data.local.QueueFuelDatabase
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.domain.model.AdCategory
import com.example.domain.model.Advertisement
import com.example.domain.repository.AdvertisementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AdvertisementRepositoryImpl(context: Context) : AdvertisementRepository {

    private val dao = QueueFuelDatabase.getInstance(context).dao()

    override val activeAds: Flow<List<Advertisement>> =
        dao.getActiveAds().map { list -> list.map { it.toDomain() } }

    override fun getAdsByCity(city: String): Flow<List<Advertisement>> =
        dao.getAdsByCity(city).map { list -> list.map { it.toDomain() } }

    override fun getAdsByCategory(category: AdCategory): Flow<List<Advertisement>> =
        dao.getAdsByCategory(category.name).map { list -> list.map { it.toDomain() } }

    override suspend fun insertAd(ad: Advertisement) = withContext(Dispatchers.IO) {
        dao.insertAd(ad.toEntity())
        Unit
    }

    override suspend fun deleteAd(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteAdById(id)
    }

    override suspend fun incrementViewCount(id: Int) = withContext(Dispatchers.IO) {
        dao.incrementAdViewCount(id)
    }

    override suspend fun incrementClickCount(id: Int) = withContext(Dispatchers.IO) {
        dao.incrementAdClickCount(id)
    }
}
