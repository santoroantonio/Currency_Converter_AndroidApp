package com.example.currency_converter

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    //private val db = Firebase.firestore

    private lateinit var amountInput: EditText
    private lateinit var fromCurrencyList: Spinner
    private lateinit var toCurrencyList: Spinner
    private lateinit var resultText: TextView
    private lateinit var switchButton: ImageButton

    private val currencyFormatter = NumberFormat.getNumberInstance()

    private var usdToEurRate = 0.85  // default
    private var eurToUsdRate = 1.18 // default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // FirebaseApp.initializeApp(this)

        // Inizializza la View
        amountInput = findViewById(R.id.input)
        fromCurrencyList = findViewById(R.id.from)
        toCurrencyList = findViewById(R.id.to)
        resultText = findViewById(R.id.textView2)
        switchButton = findViewById(R.id.imageButton)

        currencyFormatter.minimumFractionDigits = 2
        currencyFormatter.maximumFractionDigits = 2


        // Configura le liste delle valute
        val adapter = CurrencyAdapter(this, CurrencyData.list)
        fromCurrencyList.setAdapter(adapter)
        toCurrencyList.setAdapter(adapter)

        fromCurrencyList.setPopupBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_dropdown_background))
        toCurrencyList.setPopupBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_dropdown_background))


        // Carica i tassi di cambio aggiornati all'avvio
        fetchExchangeRates()

        // Chiamata alla funzione ad ogni aggiornamento della cifra inserita
        amountInput.doAfterTextChanged {
            convertCurrency()
        }

        fromCurrencyList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Questa funzione viene chiamata quando un elemento viene selezionato
                convertCurrency()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Questa funzione viene chiamata quando non viene selezionato nulla.
            }
        }

        toCurrencyList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Questa funzione viene chiamata quando un elemento viene selezionato
                convertCurrency()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Questa funzione viene chiamata quando non viene selezionato nulla.
            }
        }

        switchButton.setOnClickListener {
            switchCurrency()
            convertCurrency()
        }


    }

    private fun switchCurrency() {
        val fromCurrency = fromCurrencyList.selectedItemPosition
        val toCurrency = toCurrencyList.selectedItemPosition

        fromCurrencyList.setSelection(toCurrency)
        toCurrencyList.setSelection(fromCurrency)

    }

    private fun fetchExchangeRates(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()

                // Richiesta per USD -> EUR
                val rqstUsdEur = Request.Builder().url("https://antoniosantoro.pythonanywhere.com/getUSDEUR").build()
                val rspUsdEur = client.newCall(rqstUsdEur).execute()
                usdToEurRate = rspUsdEur.body?.string()?.toDouble() ?: 0.85

                // Richiesta per EUR -> USD
                val rqstEurUsd = Request.Builder().url("https://antoniosantoro.pythonanywhere.com/getEURUSD").build()
                val rspEurUsd = client.newCall(rqstEurUsd).execute()
                eurToUsdRate = rspEurUsd.body?.string()?.toDouble() ?: 1.18

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Tassi di cambio aggiornati", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception){
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Errore nel recupero dei tassi: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    private fun convertCurrency(){
        try {
            if(amountInput.text.toString().isBlank()){
                resultText.text = ""
                return
            }

            val amount = amountInput.text.toString().toDouble()
            val fromCurrency = fromCurrencyList.selectedItem.toString()
            val toCurrency = toCurrencyList.selectedItem.toString()
            resultText.movementMethod = ScrollingMovementMethod()

            if(fromCurrency == toCurrency){
                resultText.text = currencyFormatter.format(amount)
                return
            }

            val result: Double

            if(fromCurrency == "USD" && toCurrency == "EUR"){
                result = amount * usdToEurRate
                resultText.text = currencyFormatter.format(result)
            } else {
                result = amount * eurToUsdRate
                resultText.text = currencyFormatter.format(result)
            }


        } catch (e: NumberFormatException){
            if(!amountInput.text.isEmpty()) {
                Toast.makeText(this, "Inserisci un importo valido: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

}

