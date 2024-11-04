/*
 * Author: Matěj Šťastný
 * Date created: 10/28/2024
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Client side of the app.
 * 
 */
public class Client {

    /////////////////
    // Constants
    ////////////////

    private static final int TCP_PORT = 54321;

    /////////////////
    // VarIables
    ////////////////

    private static int udpPort;
    private static int clientId;
    private static InetAddress serverAddress;

    /////////////////
    // Main
    ////////////////

    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);

        try {
            // Replace "127.0.0.1" with the actual IP address of the server
            serverAddress = InetAddress.getByName("10.85.120.15"); // Replace with server's IP

            try (
                    Socket socket = new Socket(serverAddress, TCP_PORT);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // Send client name to the server
                System.out.print("Enter your display name: ");
                String clientName = console.nextLine();
                out.writeUTF(clientName);
                out.flush();

                // Receive the assigned client ID and UDP port from the server
                clientId = in.readInt();
                udpPort = in.readInt();

                System.out.println("Received Client ID: " + clientId);
                System.out.println("UDP port for updates: " + udpPort);

            } catch (IOException e) {
                e.printStackTrace();
            }

            // Send data using UDP
            try (DatagramSocket udpSocket = new DatagramSocket()) {
                while (true) {
                    System.out.print("Enter player position as 'x,y' (or type 'quit' to exit): ");
                    String input = console.nextLine();
                    if (input.equalsIgnoreCase("quit"))
                        break;

                    // Include the client ID in the message
                    String message = clientId + "," + input;

                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, udpPort);
                    udpSocket.send(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (UnknownHostException e) {
            System.out.println("Could not resolve server address.");
            e.printStackTrace();
        }

        console.close();
    }
}
