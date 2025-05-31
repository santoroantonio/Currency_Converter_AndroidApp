package com.example.currency_converter

data class CurrencyItem(val valute: String, val iconId: Int )
{
    override fun toString(): String = valute // Ciò che viene mostrato nello Spinner
}
