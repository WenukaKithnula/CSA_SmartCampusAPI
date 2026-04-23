/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.service;
import com.mycompany.smartcampus.models.Sensor;
import com.mycompany.smartcampus.models.Room;
import com.mycompany.smartcampus.models.SensorReading;
import com.mycompany.smartcampus.storage.DataStore;
import com.mycompany.smartcampus.exceptions.ResourceConflictException;
import com.mycompany.smartcampus.exceptions.SensorUnavailableException;
import com.mycompany.smartcampus.exceptions.LinkedResourceNotFoundException;
import javax.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
/**
 *
 * @author wenuk
 */

public class SensorService {
    
     private final DataStore dataStore = DataStore.getInstance();

    
    public void registerSensor(Sensor sensor) {
    //  Check if the "Foreign Key"  exists in the Room Map
    if (!dataStore.getRoomStore().containsKey(sensor.getRoomId())) {
        throw new LinkedResourceNotFoundException("Cannot link sensor: Room ID " 
                + sensor.getRoomId() + " does not exist.");
    }

    //  Check for Duplicate Sensor ID (Part 5.1 logic style)
    if (dataStore.getSensorStore().containsKey(sensor.getId())) {
        throw new ResourceConflictException("Sensor ID " + sensor.getId() + " already exists.");
    }

    //  Save the Sensor
    dataStore.getSensorStore().put(sensor.getId(), sensor);
    
    //Initialize an empty reading list for this sensor
    dataStore.getSensorReadingStore().put(sensor.getId(), Collections.synchronizedList(new ArrayList<>()));
    // Adding sensor's ID to the room's sensor list
    Room targetRoom = dataStore.getRoomStore().get(sensor.getRoomId());
    targetRoom.getSensorIds().add(sensor.getId());
}
    
    //get the sensors and check for query 
    public List<Sensor> getSensors(String type) {
    List<Sensor> allSensors = new ArrayList<>(dataStore.getSensorStore().values());
    
    // If no type is provided, return everything 
    if (type == null || type.trim().isEmpty()) {
        return allSensors;
    }
    
    // Filter the list based on the 'type' parameter
    List<Sensor> filteredSensors = new ArrayList<>();
    for (Sensor s : allSensors) {
        if (s.getType().equalsIgnoreCase(type)) {
            filteredSensors.add(s);
        }
    }
    return filteredSensors;
}
    // Get a single sensor by ID
public Sensor getSensorById(String id) {
    Sensor sensor = dataStore.getSensorStore().get(id);
    if (sensor == null) {
        
        throw new NotFoundException("Sensor with ID " + id + " not found.");
    }
    return sensor;
}

// Delete a sensor by ID
public void deleteSensor(String id) {
    if (!dataStore.getSensorStore().containsKey(id)) {
        
        throw new NotFoundException("Sensor with ID " + id + " not found.");
    }
    
    
    Sensor removedSensor = dataStore.getSensorStore().remove(id);
    
    
    String roomId = removedSensor.getRoomId();
    Room room = dataStore.getRoomStore().get(roomId);
    if (room != null) {
        room.getSensorIds().remove(id);
    }
}
    
    
   

}
