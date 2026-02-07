from flask import Flask, request, jsonify
from timetable_engine import generate_timetable

app = Flask(__name__)

@app.route("/predict-timetable", methods=["POST"])
def predict():
    data = request.json
    recent_data = data["recent_data"]

    result = generate_timetable(recent_data)
    return jsonify(result)

if __name__ == "__main__":
    app.run(debug=True)
