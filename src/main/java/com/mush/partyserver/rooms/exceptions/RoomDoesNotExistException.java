/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms.exceptions;

/**
 *
 * @author cic
 */
public class RoomDoesNotExistException extends RoomsException {

    public RoomDoesNotExistException(String name) {
        super("Room does not exist: " + name);
    }
}
