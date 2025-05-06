package com.example.summitdiary.network

object Config {
    const val BASE_IP = "192.168.8.105"
    const val BASE_PORT = 8888

    val BASE_URL get() = "http://$BASE_IP:$BASE_PORT"
}
