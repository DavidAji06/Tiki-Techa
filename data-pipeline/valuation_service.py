from fastapi import FastAPI
from pydantic import BaseModel
import joblib
import pandas as pd

class PlayerStats(BaseModel):
    minutes: int
    total_points: int
    goals_scored: int
    assists: int
    clean_sheets: int

app = FastAPI()
model = joblib.load("valuation_model.joblib")

@app.post("/predict")
def predict(stats: PlayerStats):
    features = pd.DataFrame([{
        "minutes": stats.minutes,
        "total_points": stats.total_points,
        "goals_scored": stats.goals_scored,
        "assists": stats.assists,
        "clean_sheets": stats.clean_sheets,
    }])
    predicted_points = model.predict(features)[0]
    return {"predictedNextPoints": round(predicted_points, 2)}