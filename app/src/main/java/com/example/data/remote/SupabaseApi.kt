package com.example.data.remote

import com.example.data.PartAssociation
import com.example.data.ScannedPart
import com.example.data.Vehicle
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {
    @GET("rest/v1/veiculos?select=*")
    suspend fun getVehicles(): List<Vehicle>

    // Utilizando o eq. para filtrar no PostgREST
    @GET("rest/v1/produtos_fisicos?select=*")
    suspend fun getScannedPart(@Query("codigo_barras") barcodeQuery: String): List<ScannedPart>

    @POST("rest/v1/compatibilidade_pecas")
    suspend fun insertAssociation(@Body association: Map<String, Any>)
}
