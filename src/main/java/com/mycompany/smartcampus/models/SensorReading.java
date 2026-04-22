/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.models;
import java.util.UUID;

/**
 *
 * @author wenuk
 */
public class SensorReading {
    
    private String id;
    private long timestamp;
    private double value;
    
    public SensorReading() {
        this.timestamp = System.currentTimeMillis();
        this.id = UUID.randomUUID().toString();
    }
    
     public SensorReading(String id, double value) {
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }
     
     public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }
     
     //getters
     public String getId() {
        return id;
    }
     
     public long getTimestamp() {
        return timestamp;
    }
      
    public double getValue() {
        return value;
    }
    
    //setters
     public void setId(String id) {
        this.id = id;
    }
     
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    

}
