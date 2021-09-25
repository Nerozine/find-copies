package com.networkcourse;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class FindCopies {
    private final String myName = ManagementFactory.getRuntimeMXBean().getName();
    private final MulticastSocket socket;
    private final InetAddress group;
    private final int port;

    private static final int timeoutDeleteValue = 5;
    private static final int maxBufferSize = 10_000;
    private final HashMap<SimpleEntry<String, String>, Integer> clientsTimeout = new HashMap<>();

    public FindCopies(String ipAddress, int port) throws IOException, IllegalArgumentException {
        this.port = port;
        this.group = InetAddress.getByName(ipAddress);
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(group);
    }

    private void updateClientsTimeout() {
        clientsTimeout.entrySet().removeIf(entry -> entry.getValue() >= timeoutDeleteValue);
        clientsTimeout.replaceAll((client, currentTimeoutValue) -> currentTimeoutValue + 1);
    }

    private void printClientsIp() {
        System.out.println("now " + clientsTimeout.size() + " copies on socket");
        for (var entry : clientsTimeout.keySet()) {
            System.out.println(entry.getValue());
        }
        System.out.println();
    }

    public void runApp() {
        DatagramPacket sendDatagram = new DatagramPacket(myName.getBytes(), myName.length(), group, port);
        TimerTask sendMyName = new MessageSender(socket, sendDatagram);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(sendMyName, 0, 1000);

        while (true) {
            byte[] buf = new byte[maxBufferSize];
            DatagramPacket recvDatagram = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(recvDatagram);
                String message = new String(recvDatagram.getData(), 0, recvDatagram.getLength());
                String recvIpAndPort = recvDatagram.getAddress() + ":" + recvDatagram.getPort();
                var currentIdAndIp = new SimpleEntry<>(message, recvIpAndPort);

                if (myName.equals(message)) {
                    int oldSize = clientsTimeout.size();
                    updateClientsTimeout();
                    if (oldSize != clientsTimeout.size()) {
                        printClientsIp();
                    }
                }
                else {
                    if (clientsTimeout.containsKey(currentIdAndIp)) {
                        clientsTimeout.replace(currentIdAndIp, 0);
                    }
                    else {
                        clientsTimeout.put(currentIdAndIp, 0);
                        printClientsIp();
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error receiving from socket");
                System.exit(1);
            }
        }
    }
}