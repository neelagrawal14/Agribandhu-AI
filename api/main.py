# api/main.py

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware

from ai_engine import predict_label, get_response_for_label
from timetable_engine import generate_timetable


app = FastAPI(title="AgriBandhu AI", version="1.0")


# --------------------------- CORS ---------------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],      
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# --------------------- MODELS ---------------------

class Query(BaseModel):
    question: str


class TimetableRequest(BaseModel):
    crop: str
    start_date: str
    weather: list
    # Example weather list:
    # [{"condition":"Rain", "rain_mm":12}, {"condition":"Sunny", "rain_mm":0}]


# ---------------------- ROOT ----------------------

@app.get("/")
def root():
    return {"message": "AgriBandhu AI API running"}


# ---------------------- /ask ----------------------

@app.post("/ask")
def ask(query: Query):

    if not query.question.strip():
        raise HTTPException(status_code=400, detail="question is required")

    label, score = predict_label(query.question)
    reply = get_response_for_label(label)

    return {
        "query": query.question,
        "label": label,
        "confidence": float(score),
        "reply": reply
    }


# ---------------------- /ai_timetable ----------------------

@app.post("/ai_timetable")
def ai_timetable(req: TimetableRequest):

    timetable = generate_timetable(
        crop=req.crop,
        start_date=req.start_date,
        weather=req.weather
    )

    return {
        "crop": req.crop,
        "start_date": req.start_date,
        "timetable": timetable
    }
