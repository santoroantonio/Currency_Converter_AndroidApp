from flask import Flask, jsonify
import requests 
from datetime import datetime, timedelta
import threading
import time


app = Flask(__name__)

API_KEY= "077b9686f5ad2bd185ac4067"
BASE_URL = f"https://v6.exchangerate-api.com/v6/{API_KEY}/latest/USD"

exchange_rates = {
    "USD_EUR": 0.85,
    "EUR_USD": 1.18,
    "last_updated": None,
    "expires_at": None
}

def fetch_latest_exchange_rates():
    try:
        response = requests.get(BASE_URL)
        data = response.json()

        if data.get("result") == "success":
            usd_to_eur = data["conversion_rates"]["EUR"]
            eur_to_usd = 1 / usd_to_eur
            
            exchange_rates["USD_EUR"] = usd_to_eur
            exchange_rates["EUR_USD"] = eur_to_usd
            exchange_rates["last_updated"] = datetime.now().isoformat()
            exchange_rates["expires_at"] = (datetime.now() + timedelta(hours=1)).isoformat()
            
            print(f"Tassi aggiornati: 1 USD = {usd_to_eur} EUR, 1 EUR = {eur_to_usd} USD")
        else:
            print(f"Errore nel recupero dei tassi: {data.get('error-type', 'Unknown error')}")
    
    except Exception as e:
        print(f"Errore durante l'aggiornamento dei tassi: {str(e)}")


def background_rate_updater():
    """Thread per aggiornare periodicamente i tassi di cambio"""
    while True:
        fetch_latest_exchange_rates()
        # Aggiorna ogni ora (3600 secondi) o secondo le condizioni dell'API
        time.sleep(3600)

@app.route('/getUSDEUR', methods=['GET'])
def get_usd_eur():
    """Endpoint per ottenere il tasso USD -> EUR"""
    # Verifica se i tassi sono scaduti
    if (exchange_rates["expires_at"] is None or 
        datetime.fromisoformat(exchange_rates["expires_at"]) < datetime.now()):
        fetch_latest_exchange_rates()
    
    return str(exchange_rates["USD_EUR"])

@app.route('/getEURUSD', methods=['GET'])
def get_eur_usd():
    """Endpoint per ottenere il tasso EUR -> USD"""
    # Verifica se i tassi sono scaduti
    if (exchange_rates["expires_at"] is None or 
        datetime.fromisoformat(exchange_rates["expires_at"]) < datetime.now()):
        fetch_latest_exchange_rates()
    
    return str(exchange_rates["EUR_USD"])

@app.route('/status', methods=['GET'])
def status():
    """Endpoint per verificare lo stato del server"""
    return jsonify({
        "status": "running",
        "last_updated": exchange_rates["last_updated"],
        "rates": {
            "USD_EUR": exchange_rates["USD_EUR"],
            "EUR_USD": exchange_rates["EUR_USD"]
        }
    })

if __name__ == '__main__':
    # Avvia il thread per l'aggiornamento periodico
    updater_thread = threading.Thread(target=background_rate_updater)
    updater_thread.daemon = True
    updater_thread.start()
    
    # Recupera i tassi iniziali
    fetch_latest_exchange_rates()
    
    # Avvia il server Flask
    app.run(host='0.0.0.0', port=5000, debug=True)

