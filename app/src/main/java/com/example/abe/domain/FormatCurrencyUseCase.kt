package com.example.abe.domain

import java.text.NumberFormat
import java.util.Currency

class FormatCurrencyUseCase {
    private val numberFormat = NumberFormat.getCurrencyInstance().apply {
        setMaximumFractionDigits(0)
        currency = Currency.getInstance("IDR")
    }

    operator fun invoke(value: Int): String {
        return numberFormat.format(value).toString()
    }
}