import joblib
import numpy as np

# Load trained model
model = joblib.load("agribandhu_rf_model.pkl")

# Example input (from your app)
# temp, humidity, rain, wind, crop_stage
sample = np.array([[29.5, 62, 0.0, 8.1, 1]])

prediction = model.predict(sample)

actions = {
    0: "Sowing",
    1: "Irrigation",
    2: "Fertilizer",
    3: "Pesticide",
    4: "Wait",
    5: "Harvest"
}

print("🌾 Predicted Action:", actions[prediction[0]])

