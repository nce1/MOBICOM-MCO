package com.mobdeve.s18.group5.bayanihanspots.model;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Spots {
    private long id;
    private String name;
    private String type;
    private String status;
    private GeoPoint coordinates;

    public Spots() {

    }

    public Spots(String name, String type, String status, GeoPoint coordinates, long id) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.coordinates = coordinates;
        this.id = id;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public GeoPoint getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(GeoPoint coordinates) {
        this.coordinates = coordinates;
    }

}

