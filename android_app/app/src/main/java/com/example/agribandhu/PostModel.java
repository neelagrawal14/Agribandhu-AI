package com.example.agribandhu;

public class PostModel {
    private String farmerName;
    private String description;
    private String imageUri;

    public PostModel() {}

    public PostModel(String farmerName, String description, String imageUri) {
        this.farmerName = farmerName;
        this.description = description;
        this.imageUri = imageUri;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUri() {
        return imageUri;
    }
}
