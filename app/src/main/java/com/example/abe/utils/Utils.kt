package com.example.abe.utils

import com.example.abe.connection.ConnectivityObserver

fun isConnected(networkState: ConnectivityObserver.NetworkState?): Boolean {
    return (networkState != null && (networkState == ConnectivityObserver.NetworkState.AVAILABLE || networkState == ConnectivityObserver.NetworkState.LOSING))
}
