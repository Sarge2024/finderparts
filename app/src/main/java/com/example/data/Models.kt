package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brand: String,
    val model: String,
    val modification: String,
    val year: Int
)

@Entity(tableName = "scanned_parts")
data class ScannedPart(
    @PrimaryKey val barcode: String,
    val name: String,
    val originalReference: String,
    val specification: String,
    val imageUrl: String
)

@Entity(tableName = "part_associations")
data class PartAssociation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val barcode: String,
    val primaryVehicleId: Int,
    val compatibleVehicleIds: List<Int>,
    val timestamp: Long = System.currentTimeMillis()
)

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<Int> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }
}
