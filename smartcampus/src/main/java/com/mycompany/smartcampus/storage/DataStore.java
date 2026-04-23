/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.storage;
import com.mycompany.smartcampus.models.Room;
import com.mycompany.smartcampus.models.Sensor;
import com.mycompany.smartcampus.models.SensorReading;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author wenuk
 */
public class DataStore {
    
   
    
    private  static DataStore instance; 
    
    
    //Store the room objcet 
    private final Map<String , Room> roomStore = new ConcurrentHashMap<>(); 
    
    //Store the Sensor objects 
    private final Map<String , Sensor> sensorStore = new ConcurrentHashMap<>();
    
    //Store the     SensorReadings
    private final Map<String , List<SensorReading>> sensorReadingStore = new ConcurrentHashMap<>();
    
    
    //private constrcuter to prevent many instnace creating
    private DataStore(){
     
    //  Create the Rooms
    Room r1 = new Room("LIB-301", "Library Quiet Study", 50, new ArrayList<>());
    Room r2 = new Room("WBS-G01", "Lecture Theatre", 200, new ArrayList<>());
    
    //  Create the Sensors
    Sensor s1 = new Sensor("01", "TEMP", "ACTIVE", 2.0002, "LIB-301");
    Sensor s2 = new Sensor("02", "Light", "MAINTENANCE", 2.0032, "LIB-301");
         List<SensorReading> s1Readings = Collections.synchronizedList(new ArrayList<>());
        List<SensorReading> s2Readings = Collections.synchronizedList(new ArrayList<>());
        
      
        s1Readings.add(new SensorReading("R-101", 21.0));
        s1Readings.add(new SensorReading("R-102", 22.5));
        
        // Seeding Sensor 02 (Light)
        s2Readings.add(new SensorReading("R-201", 400.0));
        s2Readings.add(new SensorReading("R-202", 450.0));
        
       
        sensorReadingStore.put(s1.getId(), s1Readings);
        sensorReadingStore.put(s2.getId(), s2Readings);
    
   
    r1.getSensorIds().add(s1.getId());
    r1.getSensorIds().add(s2.getId());
    
    // Put them in the stores
    roomStore.put(r1.getId(), r1);
    roomStore.put(r2.getId(), r2);
    
    sensorStore.put(s1.getId(), s1);
    sensorStore.put(s2.getId(), s2);
}
    
    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    } 
    
    //getters
    public Map<String, Room> getRoomStore() {
        return roomStore;
    }

    public Map<String, Sensor> getSensorStore() {
        return sensorStore;
    }

    public Map<String, List<SensorReading>> getSensorReadingStore() {
        return sensorReadingStore;
    }
}
