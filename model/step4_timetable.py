def generate_task(days):
    if days <= 5:
        return "Irrigation"
    elif days <= 20:
        return "Fertilizer Application"
    elif days <= 45:
        return "Pesticide Spraying"
    else:
        return "Harvest Preparation"

predicted_days = 10   # example input
task = generate_task(predicted_days)

print("AI Timetable Task:", task)
