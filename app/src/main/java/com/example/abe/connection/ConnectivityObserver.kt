package com.example.abe.connection

import kotlinx.coroutines.flow.Flow


interface ConnectivityObserver {

    fun observe(): Flow<NetworkState>

    enum class NetworkState {
        AVAILABLE, UNAVAILABLE, LOSING, LOST
    }
}