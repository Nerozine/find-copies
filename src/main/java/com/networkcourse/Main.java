package com.networkcourse;

import java.io.IOException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) {
        // default values
        String ipAddress = "228.5.6.7";
        int port = 6789;

        switch (args.length) {
            case 0 -> { }
            case 2 -> {
                ipAddress = args[0];
                try {
                    port = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Illegal argument for port!");
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
        catch (UnknownHostException | IllegalArgumentException e) {
            System.err.println("Illegal argument for ip or port!");
            System.exit(1);
        }
        catch (IOException e) {
            System.err.println("Error creating multicast socket!");
            System.exit(1);
        }
    }
}