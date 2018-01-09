/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mush.partyserver.rooms;

/**
 *
 * @author cic
 */
public class LoginTimeoutChecker implements Runnable {

    private final long checkIntervalMillis;
    private final GuestHandler guestHandler;
    private boolean running = true;

    public LoginTimeoutChecker(GuestHandler handler, long checkMillis) {
        guestHandler = handler;
        checkIntervalMillis = checkMillis;
    }

    @Override
    public void run() {
        while (running) {
            guestHandler.checkGuestsPendingLogin();
            try {
                Thread.sleep(checkIntervalMillis);
            } catch (InterruptedException ex) {
                running = false;
            }
        }
    }

}
