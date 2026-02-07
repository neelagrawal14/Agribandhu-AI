from fastapi import FastAPI
import joblib
import pandas as pd

app = FastAPI()

model = joblib.load("agribandhu_rf_model.pkl")

ACTIONS = {
    0: "Sowing",
    1: "Irrigation",
    2: "Fertilizer",
    3: "Pesticide",
    4: "Wait",
    5: "Harvest"
}

STAGES = {
    0: "Seedling",
    1: "Vegetative Growth",
    2: "Flowering",
    3: "Harvest Ready"
}

def get_next_stage(current_stage, action):
    # 🌱 Stage changes ONLY on specific actions
    if current_stage == 0 and action == "Sowing":
        return 1
    if current_stage == 1 and action == "Fertilizer":
        return 2
    if current_stage == 2 and action == "Harvest":
        return 3

    # WAIT / Irrigation / Pesticide → stage unchanged
    return current_stage

@app.post("/predict")
def predict(
    temp: float,
    humidity: float,
    rain: float,
    wind: float,
    crop_stage: int,
    date: str
):
    # 🔹 Random Forest prediction
    X = pd.DataFrame(
        [[temp, humidity, rain, wind, crop_stage]],
        columns=["temp", "humidity", "rain", "wind", "crop_stage"]
    )

    pred = model.predict(X)[0]
    action = ACTIONS[pred]

    # 🔹 Stage logic
    next_stage = get_next_stage(crop_stage, action)

    # 🔹 Explanation
    if action == "Wait":
        explanation = "Weather conditions are stable. No farming activity is required today."
    else:
        explanation = f"{action} is recommended based on current weather and crop condition."

    return {
        "date": date,
        "action": action,
        "currentStage": STAGES.get(crop_stage, "Unknown"),
        "nextStage": STAGES.get(next_stage, "Unknown"),
        "stageChanged": next_stage != crop_stage,
        "explanation": explanation
    }

