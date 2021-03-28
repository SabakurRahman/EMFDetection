package com.example.datastoreapp;

public class Artist {
    String emfDataId;
    String emfValue;


    public Artist(){

    }

    public Artist(String emfDataId, String emfValue) {
        this.emfDataId = emfDataId;
        this.emfValue = emfValue;

    }

    public String getemfDataId() {
        return emfDataId;
    }

    public String getemfValue() {
        return emfValue;
    }


}
