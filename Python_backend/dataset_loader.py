# api/dataset_loader.py

import pandas as pd
import os

file_path = os.path.join(os.path.dirname(__file__), "data", "crop_timetable_dataset.csv")
crop_dataset = pd.read_csv(file_path)


def parse_normal_timetable(value: str):
    """Convert 'Day1:Task|Day2:Task' into ['Day1:Task', 'Day2:Task']"""
    if isinstance(value, str):
        return [v.strip() for v in value.split("|")]
    return []


def parse_weather_rules(rules: str):
    """
    Convert 'Rainy: x; Hot: y; Cold: z' into:
    rain = [...]
    hot = [...]
    cold = [...]
    """
    rain = []
    hot = []
    cold = []

    if isinstance(rules, str):
        rule_list = [r.strip() for r in rules.split(";")]

        for r in rule_list:
            lower = r.lower()

            if "rain" in lower:
                rain.append(r)

            elif "hot" in lower:
                hot.append(r)

            elif "cold" in lower:
                cold.append(r)

    # fallback defaults
    if not rain:
        rain = ["Rainy day: adjust schedule"]

    if not hot:
        hot = ["Hot day: increase water or give shade"]

    if not cold:
        cold = ["Cold day: protect seedlings"]

    return rain, hot, cold


def get_crop_rules(crop_name: str):
    crop = crop_dataset[crop_dataset['crop'].str.lower() == crop_name.lower()]
    if crop.empty:
        return None

    row = crop.iloc[0].to_dict()

    normal_list = parse_normal_timetable(row.get("normal_timetable", ""))
    rain_list, hot_list, cold_list = parse_weather_rules(row.get("weather_rules", ""))

    return {
        "normal": normal_list,
        "rain": rain_list,
        "hot": hot_list,
        "cold": cold_list
    }
