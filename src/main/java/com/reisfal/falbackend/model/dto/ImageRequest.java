package com.reisfal.falbackend.model.dto;

public class ImageRequest {
    private String imageBase64;
    private String category; // opsiyonel

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}

