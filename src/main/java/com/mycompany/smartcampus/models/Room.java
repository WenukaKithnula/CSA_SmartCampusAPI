/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.models;
import java.util.*;

/**
 *
 * @author wenuk
 */
public class Room {
    
    private String id;
    private String name ;
    private int capacity;
    //to store the sensors that are in the Room
    private List<String>  sensorIds = new ArrayList<>();
    
   // default Constructer
   public Room(){
   }
   
   public Room(String id , String name  , int capacity , List<String> sensorIds){
      this.id = id;
      this.name = name;
      this.capacity = capacity;
      this.sensorIds = sensorIds;
   }
   //Getters
   public String getName(){
   return this.name ;
   }
   
   public String getId(){
   return this.id;
   }
   
   public int getCapacity(){
   return capacity;
   }
   
   public List<String> getSensorIds(){
   return this.sensorIds;
   }
   
   //Setters
   public void setName(String name){
   this.name = name ;
   }
   
   public void setId(String id){
   this.id = id ;
   }
   
   public void setCapacity(int capacity){
   this.capacity = capacity;
   }
   
   public void setSensorIds(List<String> sensorIds){
   this.sensorIds = sensorIds;
   }
   
   
   
   
   
  
   
}
