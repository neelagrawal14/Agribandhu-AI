import pandas as pd
import numpy as np
import re
import joblib

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC
from sklearn.metrics import accuracy_score

print("\n🚀 Training Model Started...\n")

# -----------------------------
# 1. Load dataset
# -----------------------------
train = pd.read_csv("train_clean.csv")
test = pd.read_csv("test_clean.csv")

X_train = train["clean_text"]
y_train = train["label"]

X_test = test["clean_text"]
y_test = test["label"]

print("✔ Dataset Loaded!")
print("Train samples:", len(X_train))
print("Test samples:", len(X_test))


# -----------------------------
# 2. TF-IDF Vectorizer
# -----------------------------
vectorizer = TfidfVectorizer(
    max_features=50000,
    ngram_range=(1, 2),
    sublinear_tf=True
)

print("\n⏳ Fitting TF-IDF...")
X_train_tfidf = vectorizer.fit_transform(X_train)
X_test_tfidf = vectorizer.transform(X_test)

print("✔ TF-IDF Completed!")


# -----------------------------
# 3. Train Model (SVM)
# -----------------------------
print("\n⏳ Training Linear SVM Model...")
model = LinearSVC()
model.fit(X_train_tfidf, y_train)

print("✔ Model Training Completed!")


# -----------------------------
# 4. Accuracy
# -----------------------------
pred = model.predict(X_test_tfidf)
acc = accuracy_score(y_test, pred)

print(f"\n🎉 Model Accuracy: {acc*100:.2f}%\n")


# -----------------------------
# 5. Save Model + Vectorizer
# -----------------------------
joblib.dump(model, "agri_svm_model.pkl")
joblib.dump(vectorizer, "tfidf_vectorizer.pkl")

print("✔ Model Saved as: agri_svm_model.pkl")
print("✔ Vectorizer Saved as: tfidf_vectorizer.pkl")


# -----------------------------
# 6. Testing Function
# -----------------------------
def predict_agri(query):
    clean = re.sub(r"[^a-zA-Z0-9\s]", "", query.lower())
    vec = vectorizer.transform([clean])
    return model.predict(vec)[0]

print("\n🔥 Example Test:")
print("Input: How much fertilizer should I use for wheat?")
print("Prediction:", predict_agri("How much fertilizer should I use for wheat?"))

print("\n🚀 Training Complete!\n")
