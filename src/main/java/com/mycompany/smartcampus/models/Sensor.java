/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.models;

/**
 *
 * @author wenuk
 */
public class Sensor {
    
    private String id;          
    private String type;     
    private String status;      
    private double currentValue; 
    private String roomId;   
    
     public Sensor() {
    }
     
    public Sensor(String id, String type , String status , double currentValue,   String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId; 
    }
    
    //Getters
    public String getId() {
        return id;
    }
    
     public String getType() {
        return type;
    }
     
    public String getStatus() {
        return status;
    }
    
    public double getCurrentValue() {
        return currentValue;
    }
    
     public String getRoomId() {
        return roomId;
    }
     
     //Setters
     
     public void setId(String id) {
        this.id = id;
    }
     
    public void setType(String type) {
        this.type = type;
    }
    
     public void setStatus(String status) {
        this.status = status;
    }
     
    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
}
