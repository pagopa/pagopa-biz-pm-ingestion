import requests
import time
from datetime import datetime, timedelta
import os
from concurrent.futures import ThreadPoolExecutor, as_completed
import sys
import threading

# Configurazioni
SUBSCRIPTION_KEY = os.getenv("OCP_APIM_SUBSCRIPTION_KEY")
PM_INGESTION_URL = os.getenv("PM_INGESTION_URL")

if not SUBSCRIPTION_KEY:
    raise EnvironmentError("La variabile di ambiente 'OCP_APIM_SUBSCRIPTION_KEY' non è configurata.")

if not PM_INGESTION_URL:
    raise EnvironmentError("La variabile di ambiente 'PM_INGESTION_URL' non è configurata.")

BASE_URL = f"{PM_INGESTION_URL}/extraction/data"
HEADERS = {"Ocp-Apim-Subscription-Key": SUBSCRIPTION_KEY}
current_date = datetime(2023, 1, 1)  # Data di partenza
end_date = datetime(2022, 12, 28)  # Data finale


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


# Loop principale
print(f"Avvio dello script. Data iniziale: {current_date.strftime('%Y-%m-%d')} Data finale: {end_date.strftime('%Y-%m-%d')}")

def spinner():
    characters = ['|', '/', '-', '\\']
    while True:
        for char in characters:
            sys.stdout.write(f'\r{char} Waiting... ')
            sys.stdout.flush()
            time.sleep(0.1)

    # Avvia l'animazione in un thread separato
thread = threading.Thread(target=spinner)
thread.daemon = True  # Imposta il thread come demone per terminare con lo script
thread.start()

while current_date >= end_date:
    print(f"Ingestion Data: {(current_date-timedelta(days=1)).strftime('%Y-%m-%d')}")
    make_post_request('CARD', current_date)
    current_date -= timedelta(days=1)  # Riduci la data di 1 giorno
    time.sleep(5 * 60)  # Attendi l'intervallo configurato

print("Script terminato.")
