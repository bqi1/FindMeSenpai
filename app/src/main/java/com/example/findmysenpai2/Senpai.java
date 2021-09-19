package com.example.findmysenpai2;


import java.util.UUID;
// Have a firebase connection
public class Senpai {
    private String roomCode = "";
    private String name = "";
    public void setRoomCode(String roomCode){
        this.roomCode = roomCode;
    }
    public String getRoomCode(){
        return this.roomCode;
    }
    public void setPicture(){

    }
    public void getPicture(){

    }
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

}
