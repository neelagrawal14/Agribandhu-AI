# api/timetable_engine.py

from datetime import datetime, timedelta
from dataset_loader import get_crop_rules


def generate_timetable(crop, start_date, weather):
    """Generate a weather-aware farming timetable with normal/rain/hot/cold."""

    rules = get_crop_rules(crop)
    if not rules:
        return [{"day": 1, "task": "No rules found for this crop"}]

    # Extract rule categories (NEW → hot, cold)
    normal_tasks = rules.get("normal", [])
    rain_tasks = rules.get("rain", [])
    hot_tasks = rules.get("hot", [])
    cold_tasks = rules.get("cold", [])

    # Fallback defaults
    if not normal_tasks: normal_tasks = ["Perform regular field work"]
    if not rain_tasks: rain_tasks = ["Rain expected – avoid heavy field work"]
    if not hot_tasks: hot_tasks = ["Very hot – water crops early, avoid mid-day work"]
    if not cold_tasks: cold_tasks = ["Cold weather – protect seedlings and young plants"]

    timetable = []

    date_obj = datetime.strptime(start_date, "%Y-%m-%d")

    for i, day_data in enumerate(weather):

        condition = day_data.get("condition", "").lower()
        rain_mm = day_data.get("rain_mm", 0)
        temp_c = day_data.get("temp_c", None)  # will use if available later

        # -------- WEATHER DECISION LOGIC --------

        # 🌧 RAIN DETECTION
        if "rain" in condition or rain_mm > 10:
            task = rain_tasks[min(i, len(rain_tasks) - 1)]

        # 🔥 HOT WEATHER DETECTION
        elif "hot" in condition or (temp_c is not None and temp_c > 32):
            task = hot_tasks[min(i, len(hot_tasks) - 1)]

        # ❄️ COLD WEATHER DETECTION
        elif "cold" in condition or (temp_c is not None and temp_c < 15):
            task = cold_tasks[min(i, len(cold_tasks) - 1)]

        # 🌤 DEFAULT NORMAL
        else:
            task = normal_tasks[min(i, len(normal_tasks) - 1)]

        # ----------------------------------------

        timetable.append({
            "day": i + 1,
            "date": (date_obj + timedelta(days=i)).strftime("%Y-%m-%d"),
            "task": task
        })

    return timetable
