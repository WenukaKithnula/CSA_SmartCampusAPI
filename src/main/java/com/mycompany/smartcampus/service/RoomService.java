package com.mycompany.smartcampus.service;

import com.mycompany.smartcampus.models.Room;
import com.mycompany.smartcampus.storage.DataStore;
import com.mycompany.smartcampus.exceptions.RoomNotEmptyException;
import com.mycompany.smartcampus.exceptions.ResourceConflictException;
import javax.ws.rs.NotFoundException;
// import com.mycompany.smartcampus.exceptions.ResourceConflictException;

import java.util.ArrayList;
import java.util.List;

public class RoomService {

    private final DataStore dataStore = DataStore.getInstance();

    // Get all rooms
    public List<Room> getAllRooms() {
        return new ArrayList<>(dataStore.getRoomStore().values());
    }

    // Add a new room
    public void addRoom(Room room) {
        // Prevent overwriting an existing room
        if (dataStore.getRoomStore().containsKey(room.getId())) {
            throw new ResourceConflictException("Room with ID " + room.getId() + " already exists.");
            
        }
        
        dataStore.getRoomStore().put(room.getId(), room);
    }

    // Get a specific room by ID (returns null if not found)
    public Room getRoom(String roomId) {
    Room room = dataStore.getRoomStore().get(roomId);
    
    
    if (room == null) {
        throw new NotFoundException("Room with ID " + roomId + " was not found.");
    }
    
    return room;
}

    // Delete a room only if it has no sensors
    public void deleteRoom(String roomId) {
        Room room = dataStore.getRoomStore().get(roomId);
        
        if (room == null) {
            
            throw new NotFoundException("Room with ID " + roomId + " does not exist.");
        }

       
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " cannot be deleted because it still contains sensors.");
        }

        dataStore.getRoomStore().remove(roomId);
    }
}