package com.example.data

import com.example.data.remote.NetworkModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class PartRepository(private val partDao: PartDao) {
    
    val allVehicles: Flow<List<Vehicle>> = partDao.getAllVehicles().onStart {
        try {
            val remoteVehicles = NetworkModule.supabaseApi.getVehicles()
            if (remoteVehicles.isNotEmpty()) {
                partDao.insertVehicles(remoteVehicles)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val allScannedParts: Flow<List<ScannedPart>> = partDao.getAllScannedParts()
    
    val allAssociations: Flow<List<PartAssociation>> = partDao.getAllAssociations()

    suspend fun getScannedPartByBarcode(barcode: String): ScannedPart? {
        try {
            val remoteParts = NetworkModule.supabaseApi.getScannedPart(barcode)
            val remotePart = remoteParts.firstOrNull()
            if (remotePart != null) {
                partDao.insertScannedParts(listOf(remotePart))
                return remotePart
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return partDao.getScannedPartByBarcode(barcode)
    }

    suspend fun insertAssociation(association: PartAssociation) {
        try {
            val map = mapOf(
                "IDComp" to association.barcode,
                "IDVeiculo" to association.primaryVehicleId
            )
            NetworkModule.supabaseApi.insertAssociation(map)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        partDao.insertAssociation(association)
    }

    suspend fun deleteAssociation(id: String) {
        partDao.deleteAssociationById(id)
    }

    suspend fun initDefaultDataIfNeeded() {
        // Remoção da lógica Mock.
        // Os veículos agora são baixados nativamente através do fluxo `allVehicles`.
    }
}
