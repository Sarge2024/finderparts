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
    @field:Json(name = "id") val id: String = UUID.randomUUID().toString(),

    @field:Json(name = "marca") val brand: String = "",
    
    @field:Json(name = "modelo") val model: String = "",
    
    @field:Json(name = "modificacao_ano") val modification: String = "",
    
    @field:Json(name = "regiao") val region: String = "BR",
    
    @Transient val year: Int = 0 // Campo transiente para manter compatibilidade com UI caso não venha do banco
)

@Entity(tableName = "scanned_parts")
@JsonClass(generateAdapter = true)
data class ScannedPart(
    @PrimaryKey 
    @field:Json(name = "codigo_barras") val barcode: String,
    
    @field:Json(name = "nome_comercial") val name: String,
    
    @field:Json(name = "fabricante") val originalReference: String,
    
    // Como a specification era local, podemos usar um default 
    @Transient val specification: String = "",
    
    @field:Json(name = "imagem_url") val imageUrl: String? = null
)

@Entity(tableName = "part_associations")
@JsonClass(generateAdapter = true)
data class PartAssociation(
    @PrimaryKey
    @field:Json(name = "id") val id: String = UUID.randomUUID().toString(),

    @field:Json(name = "codigo_barras") val barcode: String,
    
    @field:Json(name = "veiculo_id") val primaryVehicleId: String,
    
    @Transient val compatibleVehicleIds: List<String> = emptyList(), // O Supabase na spec é Many-to-One por associação
    
    @Transient val timestamp: Long = System.currentTimeMillis()
)

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
