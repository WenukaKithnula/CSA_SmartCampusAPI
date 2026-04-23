/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.service;

import com.mycompany.smartcampus.exceptions.SensorUnavailableException;
import com.mycompany.smartcampus.models.Sensor;
import com.mycompany.smartcampus.models.SensorReading;
import com.mycompany.smartcampus.storage.DataStore;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author wenuk
 */
public class SensorReadingService {
    
     private final DataStore dataStore = DataStore.getInstance();
     
     
     public List<SensorReading> getReadingsForSensor(String sensorId) {
        // First, check if the sensor exists
        if (!dataStore.getSensorStore().containsKey(sensorId)) {
            throw new NotFoundException("Sensor with ID " + sensorId + " not found.");
        }

        // Return the list from the map. If null, return empty list to avoid NPE.
        List<SensorReading> history = dataStore.getSensorReadingStore().get(sensorId);
        return (history != null) ? history : new ArrayList<>();
    }
    
    public void addReadingToSensor(String sensorId, SensorReading reading) {
    Sensor sensor = dataStore.getSensorStore().get(sensorId);
    
    // checking if the sensor exist 
    if (sensor == null) {
        throw new NotFoundException("Sensor " + sensorId + " not found.");
   }

    // Business rule: Sensors under maintenance cannot accept new readings
    if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
        throw new SensorUnavailableException( "Sensor " + sensorId + " is in maintenance and cannot accept readings.");
    }
    if (reading.getId() == null || reading.getId().isBlank()) {
        reading.setId(UUID.randomUUID().toString());
    }
    if (reading.getTimestamp() == 0) {
        reading.setTimestamp(System.currentTimeMillis());
    }

    // Update the parent sensor's live value
    sensor.setCurrentValue(reading.getValue());

    // Append to the list 
    List<SensorReading> history = dataStore.getSensorReadingStore().get(sensorId);
    if (history != null) {
        history.add(reading);
    }
}
}
