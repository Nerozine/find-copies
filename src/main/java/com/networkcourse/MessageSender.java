package com.networkcourse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.TimerTask;

public class MessageSender extends TimerTask {
    private final MulticastSocket socket;
    private final DatagramPacket sendDatagram;

    MessageSender(MulticastSocket socket, DatagramPacket sendDatagram) {
        this.socket = socket;
        this.sendDatagram = sendDatagram;
    }

    @Override
    public void run() {
        try {
            socket.send(sendDatagram);
        } catch (IOException e) {
            System.err.println("Can't send message to socket!");
            System.exit(1);
        }
    }
}