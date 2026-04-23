package com.mycompany.smartcampus.resources;

import com.mycompany.smartcampus.models.Room;
import com.mycompany.smartcampus.service.RoomService;
import com.mycompany.smartcampus.Errormodels.ErrorResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import javax.ws.rs.core.Context;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

@Path("/rooms") // This maps to /api/v1/rooms
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    // Initialize the service 
    private final RoomService roomService = new RoomService();

    @GET
    
    public Response getRooms(@Context UriInfo uriInfo) {
        List<Room> allRooms = roomService.getAllRooms();
        List<Map<String, Object>> summaryList = new ArrayList<>();

        for (Room room : allRooms) {
            Map<String, Object> roomMap = new HashMap<>();
            roomMap.put("id", room.getId());
            roomMap.put("name", room.getName());
            
            // HATEOAS Link
            String href = uriInfo.getBaseUriBuilder()
                                .path(SensorRoomResource.class)
                                .path(room.getId())
                                .build()
                                .toString();
            roomMap.put("href", href);
            
            summaryList.add(roomMap);
        }
        return Response.ok(summaryList).build();
    }

    @POST
    public Response createRoom(Room newRoom, @Context UriInfo uriInfo) {
       
        if (newRoom == null || newRoom.getId() == null || newRoom.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(new ErrorResponse("Room ID is required", 400))
                           .build();
        }

        
        roomService.addRoom(newRoom);

        java.net.URI location = uriInfo.getAbsolutePathBuilder()
                                      .path(newRoom.getId())
                                      .build();

        return Response.created(location).entity(newRoom).build();
    }
    
    //getting a specifc room by roomID
    @GET
    @Path("/{roomID}")
   
    public Response getRoom(@PathParam("roomID") String roomID) {
     //doing a input validation
    if (roomID == null || roomID.trim().isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(new ErrorResponse("Room ID is required", 400)).build();
    }else{
        Room room = roomService.getRoom(roomID);
        return Response.ok(room).build();
        
    }
    }
    
    
    @DELETE
    @Path("/{roomID}")
    public Response deleteRoom(@PathParam("roomID") String roomID) {
    
    if (roomID == null || roomID.trim().isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse("Room ID required", 400)).build();
    }

    
    // the buisness logic will happen in the servivce
    roomService.deleteRoom(roomID);

    
    return Response.noContent().build(); 
}
}