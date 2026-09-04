package com.kreation.onionquality.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OnionApiService {

    @GET("api/v1/health")
    suspend fun checkHealth(): Response<HealthResponse>

    @Multipart
    @POST("api/v1/inspect")
    suspend fun inspectOnion(
        @Part image: MultipartBody.Part,
        @Part("sample_count") sampleCount: RequestBody? = null,
        @Part("lot_id") lotId: RequestBody? = null
    ): Response<InspectionResultDto>
}
