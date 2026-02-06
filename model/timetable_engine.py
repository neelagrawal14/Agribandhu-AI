from lstm_service import predict_next_step

def generate_timetable(recent_data):
    days = predict_next_step(recent_data)

    if days <= 5:
        task = "Irrigation"
    elif days <= 20:
        task = "Fertilizer Application"
    elif days <= 45:
        task = "Pesticide Spraying"
    else:
        task = "Harvest Preparation"

    return {
        "after_days": days,
        "task": task
    }
