/*
 * Author: Matěj Šťastný
 * Date created: 10/28/2024
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Client side of the app.
 * 
 */
public class Client {

    private static final int TCP_PORT = 54321;
    private static int udpPort;
    private static int clientId;
    private static InetAddress serverAddress;

    public static void main(String[] args) {
        // Create a single Scanner instance at the start
        Scanner console = new Scanner(System.in);

        try (Socket socket = new Socket("127.0.0.1", TCP_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send client name to the server
            System.out.print("Enter name: ");
            String clientName = console.nextLine(); // Reusing the same scanner
            out.writeUTF(clientName); // Send name as UTF string
            out.flush();

            // Now receive the client ID and UDP port
            clientId = in.readInt();
            udpPort = in.readInt();
            serverAddress = socket.getInetAddress();

            System.out.println("Received Client ID: " + clientId);
            System.out.println("UDP port for updates: " + udpPort);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now switch to sending updates over UDP, including the assigned client ID
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            while (true) {
                System.out.print("Enter player position as 'x,y' (or type 'quit' to exit): ");
                String input = console.nextLine(); // Reusing the same scanner
                if (input.equalsIgnoreCase("quit"))
                    break;

                // Include the client ID in the message
                String message = clientId + "," + input; // Format: "ID,x,y"

                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, udpPort);
                udpSocket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            console.close(); // Close the scanner here
        }
    }
}
