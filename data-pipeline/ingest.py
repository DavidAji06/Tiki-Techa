import requests
import psycopg2
from decimal import Decimal

FPL_BASE_URL = "https://fantasy.premierleague.com/api"

def fetch_bootstrap_data():
    response = requests.get(f"{FPL_BASE_URL}/bootstrap-static/")
    response.raise_for_status()
    return response.json()

def upsert_teams(cursor, teams):
    for team in teams:
        team_id = team["id"]  # PK — fail loudly and immediately if missing, nothing works without it
        name = team.get("name", "Unknown")
        short_name = team.get("short_name", "")

        cursor.execute(
            """
            INSERT INTO teams (id, name, short_name)
            VALUES (%s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                short_name = EXCLUDED.short_name
            """,
            (team_id, name, short_name)
        )

def upsert_players(cursor, players):
    for player in players:
        player_id = player["id"]        # PK — fail loudly
        team_id = player["team"]        # FK — also fail loudly, since a player with no team is meaningless data

        first_name = player.get("first_name", "")
        second_name = player.get("second_name", "")
        name = f"{first_name} {second_name}".strip()

        position_id = player.get("element_type")
        total_points = player.get("total_points", 0)
        now_cost = Decimal(player.get("now_cost", 0)) / Decimal(10) # must convert from integer to decimal since FPL API returns cost in tenths of a million
        selected_by_percent = float(player.get("selected_by_percent", 0))
        minutes = player.get("minutes", 0)
        goals_scored = player.get("goals_scored", 0)
        assists = player.get("assists", 0)
        clean_sheets = player.get("clean_sheets", 0)
        goals_conceded = player.get("goals_conceded", 0)
        own_goals = player.get("own_goals", 0)
        penalties_saved = player.get("penalties_saved", 0)
        penalties_missed = player.get("penalties_missed", 0)
        yellow_cards = player.get("yellow_cards", 0)
        red_cards = player.get("red_cards", 0)

        cursor.execute(
            """
            INSERT INTO players (
                id, name, team_id, position_id, total_points, now_cost,
                selected_by_percent, minutes, goals_scored, assists,
                clean_sheets, goals_conceded, own_goals, penalties_saved,
                penalties_missed, yellow_cards, red_cards
            )
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                team_id = EXCLUDED.team_id,
                position_id = EXCLUDED.position_id,
                total_points = EXCLUDED.total_points,
                now_cost = EXCLUDED.now_cost,
                selected_by_percent = EXCLUDED.selected_by_percent,
                minutes = EXCLUDED.minutes,
                goals_scored = EXCLUDED.goals_scored,
                assists = EXCLUDED.assists,
                clean_sheets = EXCLUDED.clean_sheets,
                goals_conceded = EXCLUDED.goals_conceded,
                own_goals = EXCLUDED.own_goals,
                penalties_saved = EXCLUDED.penalties_saved,
                penalties_missed = EXCLUDED.penalties_missed,
                yellow_cards = EXCLUDED.yellow_cards,
                red_cards = EXCLUDED.red_cards
            """,
            (
                player_id, name, team_id, position_id, total_points, now_cost,
                selected_by_percent, minutes, goals_scored, assists,
                clean_sheets, goals_conceded, own_goals, penalties_saved,
                penalties_missed, yellow_cards, red_cards
            )
        )

def upsert_fixtures(cursor, fixtures):
    for fixture in fixtures:
        fixture_id = fixture["id"]           # PK — fail loudly
        home_team_id = fixture["team_h"]     # FK — fail loudly
        away_team_id = fixture["team_a"]     # FK — fail loudly
        kickoff_time = fixture.get("kickoff_time")
        gameweek = fixture.get("event")      # genuinely nullable in the API — defaulting 

        cursor.execute(
            """
            INSERT INTO fixtures (id, home_team_id, away_team_id, kickoff_time, gameweek)
            VALUES (%s, %s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                home_team_id = EXCLUDED.home_team_id,
                away_team_id = EXCLUDED.away_team_id,
                kickoff_time = EXCLUDED.kickoff_time,
                gameweek = EXCLUDED.gameweek
            """,
            (fixture_id, home_team_id, away_team_id, kickoff_time, gameweek)
        )

def main():
    print("Fetching bootstrap data (teams + players)...")
    bootstrap = fetch_bootstrap_data()
    teams = bootstrap["teams"]
    players = bootstrap["elements"]  # FPL calls players "elements"

    print("Fetching fixtures...")
    fixtures_response = requests.get(f"{FPL_BASE_URL}/fixtures/")
    fixtures_response.raise_for_status()
    fixtures = fixtures_response.json()

    conn = psycopg2.connect(
        host="localhost",
        port=5432,
        dbname="tikitecha",
        user="postgres",
        password="password123"
    )

    try:
        with conn.cursor() as cursor:
            print(f"Upserting {len(teams)} teams...")
            upsert_teams(cursor, teams)

            print(f"Upserting {len(players)} players...")
            upsert_players(cursor, players)

            print(f"Upserting {len(fixtures)} fixtures...")
            upsert_fixtures(cursor, fixtures)

        conn.commit()
        print("Done — all data committed.")
    except Exception as e:
        conn.rollback()
        print(f"Something went wrong, rolled back: {e}")
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()