package com.networkcourse;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) {
        // default values
        String stringIpAddress = "228.5.6.7";
        InetAddress ipAddress = null;
        int port = 6789;

        switch (args.length) {
            case 0 -> {
                try {
                    ipAddress = InetAddress.getByName(stringIpAddress);
                }
                catch (UnknownHostException ignored) { }
            }
            case 2 -> {
                stringIpAddress = args[0];
                try {
                    ipAddress = Inet4Address.getByName(stringIpAddress);
                    port = Integer.parseInt(args[1]);

                    if (port < 0 || port > 65_353) {
                        throw new NumberFormatException();
                    }
                    if (!ipAddress.isMulticastAddress()) {
                        throw new IOException();
                    }
                }
                catch (NumberFormatException e) {
                    System.err.println("Illegal argument for port!");
                    System.exit(1);
                }
                catch (IOException e) {
                    System.err.println("Illegal argument for ip or ip address isn't a multicast address!");
                    System.exit(1);
                }
            }
            default -> {
                System.out.println("Wrong amount of parameters!");
                System.exit(0);
            }
        }

        try {
            FindCopies app = new FindCopies(ipAddress, port);
            app.runApp();
        }
        catch (IOException e) {
            System.err.println("Error creating multicast socket!");
        }
    }
}