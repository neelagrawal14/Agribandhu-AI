import random
import pandas as pd

intents = {
    "CROP_DISEASE": [
        "How to protect {crop} from {disease}?",
        "{crop} me {disease} ka ilaj kya hai?",
        "{crop} ला {disease} झाला तर काय करावे?",
        "Symptoms of {disease} in {crop}",
        "{crop} disease {disease} treatment?",
    ],
    "FERTILIZER_SUGGESTION": [
        "Which fertilizer is best for {crop}?",
        "{crop} ke liye kaunsa fertilizer sahi hai?",
        "{crop} साठी कोणते खत वापरावे?",
        "How much NPK for {crop}?",
        "Best nutrients for {crop}",
    ],
    "IRRIGATION_GUIDE": [
        "When should I irrigate {crop}?",
        "{crop} me paani kab dena chahiye?",
        "{crop} ला पाणी कधी द्यावे?",
        "How often to water {crop}?",
        "Is drip irrigation good for {crop}?",
    ],
    "WEATHER_QUERY": [
        "What is today's weather?",
        "Kal barsaat hogi kya?",
        "आज पाऊस येईल का?",
        "Humidity level today?",
        "Will it rain today in {location}?",
    ],
    "SOIL_HEALTH": [
        "How to improve soil health?",
        "Mitti ki upj kaise badhaye?",
        "मातीची सुपीकता कशी वाढवावी?",
        "How to reduce soil acidity?",
        "Best compost for healthy soil",
    ],
    "STORAGE_GUIDE": [
        "How to store {crop}?",
        "{crop} ko kaise store kare?",
        "{crop} कसा साठवावा?",
        "Storage method for {crop}",
        "How to avoid insects while storing {crop}?",
    ],
    "SEED_GUIDE": [
        "Best seed variety for {crop}?",
        "{crop} ke liye beej kaunsa le?",
        "{crop} साठी सर्वोत्तम बियाणे कोणते?",
        "High yield variety of {crop}",
        "Which hybrid seeds are good for {crop}?",
    ]
}

crops = [
    "wheat", "rice", "cotton", "sugarcane", "maize", "soybean", "onion", "potato",
    "tomato", "banana", "millet", "pulses", "groundnut", "sunflower", "grapes"
]

diseases = [
    "rust", "blast", "blight", "leaf spot", "root rot", "stem borer",
    "bollworm", "aphids", "whitefly", "fungal infection"
]

locations = [
    "Delhi", "Mumbai", "Pune", "Nagpur", "Bhopal", "Nashik", "Hyderabad", "Lucknow"
]

rows = []

for _ in range(10000):
    intent = random.choice(list(intents.keys()))
    template = random.choice(intents[intent])

    question = template.format(
        crop=random.choice(crops),
        disease=random.choice(diseases),
        location=random.choice(locations)
    )

    rows.append([question, intent])

df = pd.DataFrame(rows, columns=["question", "label"])
df.to_csv("agri_data_10000.csv", index=False)

print("Generated 10,000 questions successfully!")
