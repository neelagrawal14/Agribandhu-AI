import pandas as pd
from sklearn.ensemble import RandomForestClassifier
import joblib

# Load dataset
data = pd.read_csv("data/agribandhu_training_data.csv")

X = data[["temp", "humidity", "rain", "wind", "crop_stage"]]
y = data["action"]

# Train Random Forest
model = RandomForestClassifier(    

    n_estimators=100,
    max_depth=8,
    random_state=42
)
model.fit(X, y)

# Save model
joblib.dump(model, "agribandhu_rf_model.pkl")

print("✅ Random Forest model trained & saved")
