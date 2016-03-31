package com.ruben.rma.prettynotes.model;

import java.math.BigDecimal;

/**
 * Created by inftel12 on 29/3/16.
 */
public class Note {
    private BigDecimal noteID;
    private String tittle;
    private String content;
    private String date;
    private String image;
    private String latitude;
    private String longitude;

    public Note() {}

    public Note(String tittle, String content, String date, String image, String latitude, String longitude) {
        this.tittle = tittle;
        this.content = content;
        this.date = date;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public BigDecimal getIdNote() {
        return noteID;
    }

    public void setIdNote(BigDecimal noteID) {
        this.noteID = noteID;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) { this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
