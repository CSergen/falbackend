package com.reisfal.falbackend.model.dto;

import com.reisfal.falbackend.model.enums.FortuneCategory;
import org.springframework.web.multipart.MultipartFile;

public class FortuneUploadRequest {
    private MultipartFile image;
    private FortuneCategory category;

    public MultipartFile getImage() {
        return image;
    }
    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public FortuneCategory getCategory() {
        return category;
    }
    public void setCategory(FortuneCategory category) {
        this.category = category;
    }
}
