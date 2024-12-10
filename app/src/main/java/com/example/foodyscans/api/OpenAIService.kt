package com.example.foodyscans.api

import com.example.foodyscans.models.ChatRequest
import com.example.foodyscans.models.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIService {

    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    fun sendPrompt(
        @Body request: ChatRequest
    ): Call<ChatResponse>

}