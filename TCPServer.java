import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class TCPServer {

    private static final int TCP_PORT = 54321;
    private static final int UDP_PORT = 54322;
    private static List<Integer> receivedNumbers = new ArrayList<>();

    public static void main(String[] args) {
        Executors.newSingleThreadExecutor().submit(TCPServer::startUDPListener);
        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println("Server started on port '" + TCP_PORT + "'. Waiting for clients...");

            // Handle client connections over TCP
            while (true) {
                try (
                        Socket clientSocket = serverSocket.accept();
                        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
                    System.out.println("Client connected, sending UDP port: " + UDP_PORT);

                    // Send the UDP port to the client for real-time updates
                    out.writeInt(UDP_PORT);
                    out.flush();

                    // Handle any additional initial setup if needed
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startUDPListener() {
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
            byte[] buffer = new byte[1024];
            System.out.println("UDP listener started on port " + UDP_PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                // Decode the packet data (e.g., Vec2 position)
                String data = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received data over UDP: " + data);

                // Process the data (e.g., update player position)
                // Assuming data format is "x,y"
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}