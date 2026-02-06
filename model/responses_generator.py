import json
import joblib
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer

# Paths
MODEL_PATH = "model/agri_svm_model.pkl"
VECTORIZER_PATH = "model/tfidf_vectorizer.pkl"
RESPONSES_PATH = "api/responses.json"

# Load model + vectorizer + responses
model = joblib.load(MODEL_PATH)
vectorizer = joblib.load(VECTORIZER_PATH)

with open(RESPONSES_PATH, "r", encoding="utf-8") as f:
    responses = json.load(f)

def get_ai_response(user_question):
    # Preprocess
    user_question = user_question.lower()

    # Convert to vector
    vector = vectorizer.transform([user_question])

    # Predict label
    predicted_label = model.predict(vector)[0]

    # get response
    response = responses.get(predicted_label, "Sorry, I couldn't understand your query. Please try again.")

    return predicted_label, response

# Test run
if __name__ == "__main__":
    q = input("Ask something: ")
    label, res = get_ai_response(q)
    print("Predicted Label:", label)
    print("AI Response:", res)
