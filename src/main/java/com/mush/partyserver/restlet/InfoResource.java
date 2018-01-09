/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.restlet;

import com.mush.partyserver.Main;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 *
 * @author Cic
 */
public class InfoResource extends ServerResource {

    @Get
    public StringRepresentation represent() {
        int guests = Main.getInstance().getRoomServer().getGuestCount();
        int rooms = Main.getInstance().getRoomServer().getGuestHandler().getRooms().getRoomCount();
        
        StringBuilder sb =  new StringBuilder();
        
        sb.append("PartyServer 0.0.1").append("\n");
        sb.append("-----").append("\n");
        sb.append("Rooms: ").append(rooms).append("\n");
        sb.append("Guests: ").append(guests).append("\n");
        
        return new StringRepresentation(sb.toString());
    }
}
