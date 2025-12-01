package com.example.newsapplication.model.error;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ValidationError {
    @SerializedName("loc")
    private List<Object> loc;

    @SerializedName("msg")
    private String msg;

    @SerializedName("type")
    private String type;

    public List<Object> getLoc() { return loc; }
    public void setLoc(List<Object> loc) { this.loc = loc; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}