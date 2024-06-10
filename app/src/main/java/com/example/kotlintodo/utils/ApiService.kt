package com.example.kotlintodo.utils

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("todos")
    fun getTodoItems(): Call<TodoResponse>
}