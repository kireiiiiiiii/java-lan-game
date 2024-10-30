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
import java.util.ArrayList;
import java.util.List;

/**
 * Server side of the application.
 * 
 */
public class Server {

    /////////////////
    // Constants
    ////////////////

    private static final int TCP_PORT = 54321;
    private static final int UDP_PORT = 54322;

    /////////////////
    // Variables
    ////////////////

    private static final List<Client> clients = new ArrayList<>();
    private static int clientNum = 0;

    /////////////////
    // Main
    ////////////////

    public static void main(String[] args) {
        // Start UDP listener for real-time updates in a separate thread
        new Thread(Server::startUDPListener).start();

        // Get new clients using TCP
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("Server started on port '" + TCP_PORT + "'. Waiting for clients...");

            while (true) {
                try (
                        Socket clientSocket = serverSocket.accept();
                        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                    // Receive the client's name
                    String clientName = in.readUTF();
                    int clientId = clientNum;
                    clientNum++;

                    // Store client info (name and address) in the map
                    clients.add(new Client(clientId, clientName, clientSocket.getInetAddress()));
                    System.out.println("Assigned Client ID " + clientId + " to " + clientName);

                    // Send assigned client ID and UDP port back to the client
                    out.writeInt(clientId);
                    out.writeInt(UDP_PORT);
                    out.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts listening for client data on the UDP port.
     * 
     */
    private static void startUDPListener() {
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
            byte[] buffer = new byte[1024];
            System.out.println("UDP listener started on port " + UDP_PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                // Parse client ID and data from received packet
                String message = new String(packet.getData(), 0, packet.getLength());
                String[] parts = message.split(",");

                if (parts.length >= 3) {
                    int clientId = Integer.parseInt(parts[0]);
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    Client c = getClient(clientId);

                    System.out.println(
                            "Received position from Client " + c.getDisplayName() + ": (" + x + ", " + y + ")");

                    // Process position data (e.g., update game state)
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /////////////////
    // Helper methods
    ////////////////

    private static Client getClient(int id) {
        for (Client c : clients) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    /////////////////
    // Client class
    ////////////////

    @SuppressWarnings("unused")
    private static class Client {

        public Client(Integer id, String displayName, InetAddress inetAddress) {
            this.id = id;
            this.displayName = displayName;
            this.inetAddress = inetAddress;
        }

        private Integer id;
        private String displayName;
        private InetAddress inetAddress;

        // Getter for id
        public Integer getId() {
            return id;
        }

        // Getter for displayName
        public String getDisplayName() {
            return displayName;
        }

        // Getter for inetAddress
        public InetAddress getInetAddress() {
            return inetAddress;
        }
    }
}
