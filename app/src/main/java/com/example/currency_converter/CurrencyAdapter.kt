package com.example.currency_converter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class CurrencyAdapter(
    context: Context,
    private val items: List<CurrencyItem>
) : ArrayAdapter<CurrencyItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.dropdown_item, parent, false)

        val currency = items[position]
        val icon = view.findViewById<ImageView>(R.id.imageView)
        val label = view.findViewById<TextView>(R.id.textView)

        icon.setImageResource(currency.iconId)
        label.text = currency.valute

        return view
    }
}