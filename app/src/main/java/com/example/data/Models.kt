package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@Entity(tableName = "vehicles")
@JsonClass(generateAdapter = true)
data class Vehicle(
    @PrimaryKey
    @field:Json(name = "IDVeiculo") val id: String = UUID.randomUUID().toString(),

    @field:Json(name = "Marca") val brand: String = "",
    
    @field:Json(name = "Modelo") val model: String = "",
    
    @field:Json(name = "Versao") val modification: String = "",
    
    @field:Json(name = "ano") val yearStr: String? = null
) {
    @Transient val year: Int = yearStr?.toIntOrNull() ?: 0
}

@Entity(tableName = "scanned_parts")
@JsonClass(generateAdapter = true)
data class ScannedPart(
    @PrimaryKey 
    @field:Json(name = "IDComp") val barcode: String,
    
    @field:Json(name = "Descricao") val name: String = "",
    
    @field:Json(name = "Fabricante") val originalReference: String = "",
    
    @field:Json(name = "CodFabr") val codFabr: String = "",
    
    @field:Json(name = "Grupo") val group: String = "",
    
    @field:Json(name = "Subgrupo") val subgroup: String = ""
) {
    @Transient val specification: String = ""
    @Transient val imageUrl: String? = null
}

@Entity(tableName = "part_associations")
@JsonClass(generateAdapter = true)
data class PartAssociation(
    @PrimaryKey
    @field:Json(name = "id") val id: String = UUID.randomUUID().toString(),

    @field:Json(name = "IDComp") val barcode: String,
    
    @field:Json(name = "IDVeiculo") val primaryVehicleId: String
) {
    @Transient val compatibleVehicleIds: List<String> = emptyList()
    @Transient val timestamp: Long = System.currentTimeMillis()
}

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").map { it.trim() }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}
