package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PartDao {
    @Query("SELECT * FROM vehicles ORDER BY brand ASC, model ASC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)

    @Query("SELECT * FROM scanned_parts")
    fun getAllScannedParts(): Flow<List<ScannedPart>>

    @Query("SELECT * FROM scanned_parts WHERE barcode = :barcode LIMIT 1")
    suspend fun getScannedPartByBarcode(barcode: String): ScannedPart?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedParts(parts: List<ScannedPart>)

    @Query("SELECT * FROM part_associations ORDER BY timestamp DESC")
    fun getAllAssociations(): Flow<List<PartAssociation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssociation(association: PartAssociation)

    @Query("DELETE FROM part_associations WHERE id = :id")
    suspend fun deleteAssociationById(id: Int)
}
