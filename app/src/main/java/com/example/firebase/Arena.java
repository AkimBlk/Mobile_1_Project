package com.example.firebase;

public class Arena {
    private String name;
    private double latitude;
    private double longitude;
    private Game game;

    public Arena(String name, double latitude, double longitude, Game game) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.game = game;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Game getGame() { return game; }
}