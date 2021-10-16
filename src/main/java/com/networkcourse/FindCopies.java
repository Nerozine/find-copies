package com.networkcourse;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindCopies {
    private final MulticastSocket socket;
    private final InetAddress group;
    private final int port;

    private static final String MY_NAME = ManagementFactory.getRuntimeMXBean().getName();
    private static final long TIMEOUT_DELETE_VALUE = 5_000;
    private static final int MAX_BUFFER_SIZE = 10_000;
    private static final int SEND_NAME_PERIOD = 1_000;
    private static final int SEND_NAME_DELAY = 0;
    private static final int FIRST_BYTE_TO_DECODE_INDEX = 0;
    private static final Pattern VALID_MESSAGE_PATTERN = Pattern.compile("^[1-9]+[0-9]*@(.)*$");
    private final HashMap<SimpleEntry<String, String>, Long> clientsTimeout = new HashMap<>();

    public FindCopies(InetAddress ipAddress, int port) throws IOException {
        this.port = port;
        this.group = ipAddress;
        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(group);
    }

    private void printClientsIp() {
        System.out.println("now " + clientsTimeout.size() + " copies on socket");
        for (var entry : clientsTimeout.keySet()) {
            System.out.println(entry.getValue());
        }
        System.out.println();
    }

    private boolean isValidMessage(String message) {
        Matcher matcher = VALID_MESSAGE_PATTERN.matcher(message);
        return matcher.find();
    }

    private void updateConnections(boolean newClientConnected) {
        int oldSize = clientsTimeout.size();
        clientsTimeout.entrySet().removeIf(
                entry -> System.currentTimeMillis() - entry.getValue() >= TIMEOUT_DELETE_VALUE);
        if (oldSize != clientsTimeout.size() || newClientConnected) {
            printClientsIp();
        }
    }

    public void runApp() {
        DatagramPacket sendDatagram = new DatagramPacket(MY_NAME.getBytes(), MY_NAME.length(), group, port);
        TimerTask sendNameTask = new MessageSender(socket, sendDatagram);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(sendNameTask, SEND_NAME_DELAY, SEND_NAME_PERIOD);

        byte[] buf = new byte[MAX_BUFFER_SIZE];
        DatagramPacket recvDatagram = new DatagramPacket(buf, buf.length);

        while (true) {
            try {
                socket.receive(recvDatagram);
                String message = new String(recvDatagram.getData(), FIRST_BYTE_TO_DECODE_INDEX, recvDatagram.getLength());
                if (!isValidMessage(message)) {
                    continue;
                }

                if (MY_NAME.equals(message)) {
                    updateConnections(false);
                    continue;
                }

                String recvIpAndPort = recvDatagram.getAddress() + ":" + recvDatagram.getPort();
                var currentIdAndIp = new SimpleEntry<>(message, recvIpAndPort);

                boolean isNewClient = !clientsTimeout.containsKey(currentIdAndIp);
                clientsTimeout.put(currentIdAndIp, System.currentTimeMillis());
                updateConnections(isNewClient);
            }
            catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error receiving from socket");
                System.exit(1);
            }
        }
    }
}