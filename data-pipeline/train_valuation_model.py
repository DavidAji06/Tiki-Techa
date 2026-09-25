import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error
import joblib

FEATURES = ["minutes", "total_points", "goals_scored", "assists", "clean_sheets"]

def build_training_data(df):
    df = df.sort_values(["player_id", "round"])

    # predict next gameweek's points from this gameweek's stats
    df["next_points"] = df.groupby("player_id")["total_points"].shift(-1)
    df = df.dropna(subset=["next_points"])

    return df

def main():
    df = pd.read_csv("player_gameweek_history.csv")
    df = build_training_data(df)

    X = df[FEATURES]
    y = df["next_points"]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )

    model = LinearRegression()
    model.fit(X_train, y_train)

    predictions = model.predict(X_test)
    mae = mean_absolute_error(y_test, predictions)

    baseline_prediction = y_train.mean()
    baseline_mae = mean_absolute_error(y_test, [baseline_prediction] * len(y_test))

    print(f"Trained on {len(X_train)} examples, tested on {len(X_test)}")
    print(f"Model MAE: {mae:.3f} points")
    print(f"Baseline MAE: {baseline_mae:.3f} points")

    joblib.dump(model, "valuation_model.joblib")
    print("Model saved to valuation_model.joblib")

if __name__ == "__main__":
    main()