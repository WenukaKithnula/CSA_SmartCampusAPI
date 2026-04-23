/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.exceptions;

/**
 *
 * @author wenuk
 */
public class RoomNotEmptyException extends RuntimeException {
    
    //defualt constructer 
     public RoomNotEmptyException() {
        super("Room cannot be deleted because it still contains sensors.");
    }
     
     
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
