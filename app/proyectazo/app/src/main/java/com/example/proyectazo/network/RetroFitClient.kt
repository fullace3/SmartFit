package com.example.proyectazo.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

/**
 * Singleton that provides the single Retrofit instance used across the entire app.
 * Using an object ensures the HTTP client is created only once and reused,
 * which avoids the overhead of rebuilding the connection pool on every request.
 */
object RetrofitClient {

    // Base URL of the FastAPI server deployed on AWS EC2
    // Port 8000 is where uvicorn is listening (configured on the EC2 instance)
    private const val BASE_URL = "http://100.26.177.220:8000/"

    /**
     * Lazily initialized ApiService instance.
     * 'by lazy' means Retrofit is only built the first time it is accessed,
     * not at app startup — improving launch performance.
     * GsonConverterFactory handles automatic JSON serialization and deserialization.
     */
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Converts JSON to Kotlin data classes and vice versa
            .build()
            .create(ApiService::class.java) // Generates the ApiService implementation at runtime
    }
}