import requests
import time
from datetime import datetime, timedelta
import os


SUBSCRIPTION_KEY = os.getenv("OCP_APIM_SUBSCRIPTION_KEY")
PM_INGESTION_URL = os.getenv("PM_INGESTION_URL")

if not SUBSCRIPTION_KEY:
    raise EnvironmentError("La variabile di ambiente 'OCP_APIM_SUBSCRIPTION_KEY' non è configurata.")

if not PM_INGESTION_URL:
    raise EnvironmentError("La variabile di ambiente 'PM_INGESTION_URL' non è configurata.")

# Configurazioni
BASE_URL = f"{PM_INGESTION_URL}/extraction/data"
PM_EXTRACTION_TYPES = ["CARD", "BPAY", "PAYPAL"]
HEADERS = {
    "accept": "*/*",
    "Content-Type": "application/json",
    "Ocp-Apim-Subscription-Key": SUBSCRIPTION_KEY,  # Ottieni la subkey dall'ambiente
}
INTERVAL_SECONDS = 5 * 60  # Configura qui l'intervallo in secondi (5 minuti = 300 secondi)
current_date = datetime(2023, 3, 31)  # Data di partenza
end_date = datetime(2018, 1, 1)  # Data finale

def post_requests():
    """Esegue le chiamate POST per ogni tipo di estrazione."""
    creation_date = current_date.strftime("%Y-%m-%d")
    for pm_type in PM_EXTRACTION_TYPES:
        payload = {
            "taxCodes": [],
            "creationDateFrom": creation_date,
            "creationDateTo": creation_date,
        }
        url = f"{BASE_URL}?pmExtractionType={pm_type}"
        try:
            response = requests.post(url, headers=HEADERS, json=payload)
            print(f"POST to {url} with payload {payload}: {response.status_code} - {response.text}")
        except Exception as e:
            print(f"Errore durante la richiesta POST per {pm_type}: {e}")

# Loop principale
print(f"Avvio dello script. Data iniziale: {current_date.strftime('%Y-%m-%d')}")

while current_date >= end_date:
    post_requests()
    current_date -= timedelta(days=1)  # Riduci la data di 1 giorno
    print(f"Data successiva: {current_date.strftime('%Y-%m-%d')}")
    time.sleep(INTERVAL_SECONDS)  # Attendi l'intervallo configurato

print("Script terminato correttamente.")
