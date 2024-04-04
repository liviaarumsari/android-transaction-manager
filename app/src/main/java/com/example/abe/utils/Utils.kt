package com.example.abe.utils

import com.example.abe.connection.ConnectivityObserver
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

fun isConnected(networkState: ConnectivityObserver.NetworkState?): Boolean {
    return (networkState != null && (networkState == ConnectivityObserver.NetworkState.AVAILABLE || networkState == ConnectivityObserver.NetworkState.LOSING))
}

fun isNumeric(input: String): Boolean {
    return input.matches("\\d+".toRegex())
}

fun sanitizeHtml(input: String): String {
    return Jsoup.clean(input, Safelist.basic())
}
