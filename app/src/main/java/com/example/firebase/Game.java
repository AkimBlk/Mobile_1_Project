package com.example.firebase;

import java.util.ArrayList;

public class Game {

    private String name;
    private String imageUrl;
    private int metacritic;
    private boolean isCaptured = false;
    private ArrayList<String> screenshotsUrl;

    public Game(String name, String imageUrl, int metacritic, ArrayList<String> screenshotsUrl){
        this.name = name;
        this.imageUrl = imageUrl;
        this.metacritic = metacritic;
        this.screenshotsUrl = screenshotsUrl;
    }

    public String getName() {
        return name;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public int getMetacritic() {
        return metacritic;
    }
    public ArrayList<String> getScreenshotsUrl() {
        return screenshotsUrl;
    }

    public boolean isCaptured() {
        return isCaptured;
    }
    public void setCaptured(boolean captured) {
        isCaptured = captured;
    }

}
