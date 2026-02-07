import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
import joblib
import os
import re

TRAIN_PATH = "data/train_clean.csv"
MODEL_DIR = "ml/model"

# create directory if missing
os.makedirs(MODEL_DIR, exist_ok=True)

# clean text
def clean(text):
    return re.sub(r"[^a-zA-Z0-9\s]", "", text.lower())


def train():
    print("Loading training data...")
    df = pd.read_csv(TRAIN_PATH)

    df["clean_text"] = df["question"].astype(str).apply(clean)

    print("Vectorizing text...")
    tfidf = TfidfVectorizer(max_features=5000)
    X = tfidf.fit_transform(df["clean_text"])
    y = df["label"]

    print("Training model...")
    model = LogisticRegression(max_iter=300)
    model.fit(X, y)

    print("Saving model files...")
    joblib.dump(model, os.path.join(MODEL_DIR, "agri_svm_model.pkl"))
    joblib.dump(tfidf, os.path.join(MODEL_DIR, "tfidf_vectorizer.pkl"))

    print("Model Saved Successfully!")
    print("✔ agri_svm_model.pkl")
    print("✔ tfidf_vectorizer.pkl")

if __name__ == "__main__":
    train()
