import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    private static final int PORT = 54321;
    private static final int BROADCAST_PORT = 54322;

    public static void main(String[] args) {
        try {
            String serverAddress = receiveServerAddress();
            if (serverAddress == null) {
                System.out.println("Failed to find the server. Exiting.");
                return;
            }
            System.out.println("Connecting to server at " + serverAddress);

            try (
                    Socket socket = new Socket(serverAddress, PORT);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    Scanner console = new Scanner(System.in)) {
                System.out.print("Enter an integer to send to the server: ");
                int number = console.nextInt();

                // Send the number to the server
                out.writeInt(number);
                out.flush();

                // Retrieve the list of all numbers & print them
                List<Integer> numbers = readList(in, true);
                System.out.println(printNumbers(numbers));
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String receiveServerAddress() {
        try (DatagramSocket socket = new DatagramSocket(BROADCAST_PORT)) {
            socket.setSoTimeout(10000); // 10-second timeout
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Listening for server broadcast...");
            socket.receive(packet);

            String serverAddress = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received server IP address: " + serverAddress);
            return serverAddress;

        } catch (IOException e) {
            System.out.println("Failed to receive server IP address.");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> readList(ObjectInputStream in, boolean log)
            throws IOException, ClassNotFoundException {
        Object receivedObject = in.readObject();
        List<Integer> numbers = new ArrayList<>();

        if (receivedObject instanceof List<?>) {
            List<?> tempList = (List<?>) receivedObject;
            boolean allIntegers = tempList.stream().allMatch(item -> item instanceof Integer);

            if (allIntegers) {
                numbers = (List<Integer>) tempList;
            } else {
                if (log)
                    System.out.println("Error: The received list does not contain only Integers.");
                return null;
            }
        } else {
            if (log)
                System.out.println("Error: Received object is not a List.");
            return null;
        }

        return numbers;
    }

    private static String printNumbers(List<Integer> list) {
        StringBuilder output = new StringBuilder("\nAll server numbers:");
        for (Integer i : list) {
            output.append("\n - ").append(i);
        }
        return output.toString();
    }
}