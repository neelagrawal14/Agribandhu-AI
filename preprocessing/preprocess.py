import pandas as pd
import re
import string
from sklearn.model_selection import train_test_split

# ---------------------------------------------------------------------------------------------------
# 1. LOAD DATA
# ---------------------------------------------------------------------------------------------------
def load_dataset(path="data/agri_data.csv"):
    df = pd.read_csv(path)
    print("Dataset Loaded:", df.shape)
    return df

# ---------------------------------------------------------------------------------------------------
# 2. CLEAN TEXT (Hindi, Marathi, English)
# ---------------------------------------------------------------------------------------------------
def clean_text(text):
    if pd.isna(text):
        return ""

    text = text.lower()
    
    # Remove punctuations
    text = re.sub(r"[^\w\s]", "", text)

    # Remove digits
    text = re.sub(r"\d+", "", text)

    # Remove extra spaces
    text = re.sub(r"\s+", " ", text).strip()

    return text

# ---------------------------------------------------------------------------------------------------
# 3. APPLY CLEANING TO DATASET
# ---------------------------------------------------------------------------------------------------
def preprocess_dataset(df):
    df["question"] = df["question"].astype(str).apply(clean_text)
    df["answer"] = df["label"].astype(str).apply(clean_text)

    print("Cleaning complete!")
    return df

# ---------------------------------------------------------------------------------------------------
# 4. TRAIN-TEST SPLIT
# ---------------------------------------------------------------------------------------------------
def split_dataset(df):
    train, test = train_test_split(df, test_size=0.05, random_state=42)
    print("Train size:", train.shape, " Test size:", test.shape)
    return train, test

# ---------------------------------------------------------------------------------------------------
# MAIN PIPELINE
# ---------------------------------------------------------------------------------------------------
def preprocessing_pipeline():

    df = load_dataset()
    df = preprocess_dataset(df)
    
    train, test = split_dataset(df)

    # Save output files
    train.to_csv("data/train_clean.csv", index=False)
    test.to_csv("data/test_clean.csv", index=False)

    print("\n✔️ Preprocessing Done!")
    print("✔️ Files saved as: train_clean.csv & test_clean.csv")


if __name__ == "__main__":
    preprocessing_pipeline()
