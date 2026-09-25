import requests
import csv
import time

FPL_BASE_URL = "https://fantasy.premierleague.com/api"
OUTPUT_FILE = "player_gameweek_history.csv"

FIELDS = [
    "player_id", "round", "minutes", "total_points",
    "goals_scored", "assists", "clean_sheets", "bonus",
    "value", "transfers_balance", "was_home"
]

def fetch_player_ids():
    response = requests.get(f"{FPL_BASE_URL}/bootstrap-static/")
    response.raise_for_status()
    return [player["id"] for player in response.json()["elements"]]

def fetch_player_history(player_id):
    response = requests.get(f"{FPL_BASE_URL}/element-summary/{player_id}/")
    response.raise_for_status()
    return response.json().get("history", [])

def main():
    player_ids = fetch_player_ids()
    print(f"Fetching history for {len(player_ids)} players...")

    with open(OUTPUT_FILE, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=FIELDS)
        writer.writeheader()

        for i, player_id in enumerate(player_ids):
            try:
                history = fetch_player_history(player_id)
                for gw in history:
                    writer.writerow({
                        "player_id": player_id,
                        "round": gw["round"],
                        "minutes": gw["minutes"],
                        "total_points": gw["total_points"],
                        "goals_scored": gw["goals_scored"],
                        "assists": gw["assists"],
                        "clean_sheets": gw["clean_sheets"],
                        "bonus": gw["bonus"],
                        "value": gw["value"],
                        "transfers_balance": gw["transfers_balance"],
                        "was_home": int(gw["was_home"]),
                    })
            except Exception as e:
                print(f"Skipped player {player_id}: {e}")

            if (i + 1) % 50 == 0:
                print(f"  {i + 1}/{len(player_ids)} done")

            time.sleep(0.05)  # to not spam API too much

    print(f"Done. Saved to {OUTPUT_FILE}")

if __name__ == "__main__":
    main()