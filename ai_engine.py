import joblib
import json
import numpy as np

# File paths
MODEL_PATH = "model/agri_svm_model.pkl"
VECTORIZER_PATH = "model/tfidf_vectorizer.pkl"
RESPONSES_PATH = "api/responses.json"

# Load model and vectorizer
model = joblib.load(MODEL_PATH)
vectorizer = joblib.load(VECTORIZER_PATH)

# Load responses JSON
with open(RESPONSES_PATH, "r", encoding="utf-8") as f:
    responses = json.load(f)

def predict_label(question: str):
    """
    Predicts the label + confidence from the user's question
    """
    cleaned = question.lower().strip()
    vector = vectorizer.transform([cleaned])

    # get probabilities
    probabilities = model.predict_proba(vector)[0]

    # highest probability index
    index = np.argmax(probabilities)

    # final output
    label = model.classes_[index]
    confidence = float(probabilities[index])

    return label, confidence


def get_response_for_label(label: str):
    """
    Returns AI reply text from responses.json
    """
    return responses.get(label, "Sorry, I don’t understand this question.")
