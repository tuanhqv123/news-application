package com.example.newsapplication.model.error;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HTTPValidationError {
    @SerializedName("detail")
    private List<ValidationError> detail;

    public List<ValidationError> getDetail() { return detail; }
    public void setDetail(List<ValidationError> detail) { this.detail = detail; }
}