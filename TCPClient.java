import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient {

    private static final int TCP_PORT = 54321;
    private static int udpPort = -1;
    private static InetAddress serverAddress;

    public static void main(String[] args) {
        // First, connect to the server using TCP to obtain the UDP port and server
        // address
        try (Socket socket = new Socket("127.0.0.1", TCP_PORT);
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            udpPort = in.readInt();
            serverAddress = socket.getInetAddress();
            System.out.println("Received UDP port from server: " + udpPort);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Now switch to sending updates over UDP
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter player position as 'x,y' (or type 'quit' to exit): ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("quit"))
                    break;

                // Send the Vec2 position over UDP
                byte[] buffer = input.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, udpPort);
                udpSocket.send(packet);
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}