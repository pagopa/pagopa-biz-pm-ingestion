import requests
import time
from datetime import datetime, timedelta
import os
from concurrent.futures import ThreadPoolExecutor, as_completed

# Configurazioni
SUBSCRIPTION_KEY = os.getenv("OCP_APIM_SUBSCRIPTION_KEY")
PM_INGESTION_URL = os.getenv("PM_INGESTION_URL")

if not SUBSCRIPTION_KEY:
    raise EnvironmentError("La variabile di ambiente 'OCP_APIM_SUBSCRIPTION_KEY' non è configurata.")

if not PM_INGESTION_URL:
    raise EnvironmentError("La variabile di ambiente 'PM_INGESTION_URL' non è configurata.")

BASE_URL = f"{PM_INGESTION_URL}/extraction/data"
PM_EXTRACTION_TYPES = ["CARD", "BPAY", "PAYPAL"]
HEADERS = {"Ocp-Apim-Subscription-Key": SUBSCRIPTION_KEY}
current_date = datetime(2023, 4, 1)  # Data di partenza
end_date = datetime(2018, 1, 1)  # Data finale

def calculate_interval(elements):
    """Calcola il tempo di attesa in base al numero di elementi."""
    return (elements // 20000) * 240  # Ogni 20000 elementi = 4 minuti (240 secondi)

def make_post_request(pm_type, creation_date):
    """Esegue una chiamata POST per un determinato tipo di estrazione."""
    payload = {
        "taxCodes": [],
        "creationDateFrom": (creation_date - timedelta(days=1)).strftime("%Y-%m-%d"),
        "creationDateTo": creation_date.strftime("%Y-%m-%d"),
    }
    url = f"{BASE_URL}?pmExtractionType={pm_type}"
    try:
        # print(f"POST {url} \n {HEADERS} \n {payload}")
        response = requests.post(url, headers=HEADERS, json=payload)
        if response.status_code == 200:
            result = response.json()
            elements = result.get("elements", 0)  # Estrae il numero di elementi
            # print(f"POST to {url} - max elements: {elements}, response: {response.text}")
            return elements
        else:
            print(f"Errore nella richiesta POST per {pm_type}: {response.status_code} - {response.text}")
            return 0
    except Exception as e:
        print(f"Errore durante la richiesta POST per {pm_type}: {e}")
        return 0

def post_requests():
    """Esegue le chiamate POST in parallelo per ogni tipo di estrazione."""
    creation_date = current_date
    total_elements = 0

    with ThreadPoolExecutor() as executor:
        futures = [executor.submit(make_post_request, pm_type, creation_date) for pm_type in PM_EXTRACTION_TYPES]
        for future in as_completed(futures):
            total_elements = max(total_elements, future.result())

    return calculate_interval(total_elements)

# Loop principale
print(f"Avvio dello script. Data iniziale: {current_date.strftime('%Y-%m-%d')}")

while current_date >= end_date:
    INTERVAL_SECONDS = post_requests()
    current_date -= timedelta(days=1)  # Riduci la data di 1 giorno
    print(f"Data successiva: {current_date.strftime('%Y-%m-%d')}")
    time.sleep(INTERVAL_SECONDS)  # Attendi l'intervallo configurato

print("Script terminato correttamente.")
