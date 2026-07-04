package com.example.data.remote

import com.example.data.ScannedPart
import com.example.data.Vehicle
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {
    @GET("rest/v1/veiculos?select=*")
    suspend fun getVehicles(): List<Vehicle>

    // "Tabela para receber o scanner será a tabela de componentes"
    @GET("rest/v1/componentes?select=*")
    suspend fun getScannedPart(@Query("IDComp") barcodeQuery: String): List<ScannedPart>

    @POST("rest/v1/aplica")
    suspend fun insertAssociation(@Body association: Map<String, Any>)
}
