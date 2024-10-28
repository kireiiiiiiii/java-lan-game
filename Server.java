import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

	private static final int PORT = 54321;
	private static final int BROADCAST_PORT = 54322;
	private static List<Integer> receivedNumbers = new ArrayList<>();

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Server started on port '" + PORT + "'. Waiting for clients...");

			// Start broadcasting the server IP address
			Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(Server::broadcastServerAddress, 0, 5,
					TimeUnit.SECONDS);

			// Handle client connections
			while (true) {
				try (
						Socket clientSocket = serverSocket.accept();
						ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
						ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
					// Read integer from client
					int receivedNumber = in.readInt();
					System.out.println("Received number: " + receivedNumber);
					receivedNumbers.add(receivedNumber);

					// Send back the full list of numbers
					out.writeObject(new ArrayList<>(receivedNumbers));
					out.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void broadcastServerAddress() {
		try (DatagramSocket broadcastSocket = new DatagramSocket()) {
			broadcastSocket.setBroadcast(true);
			String message = InetAddress.getLocalHost().getHostAddress();
			byte[] buffer = message.getBytes();

			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"),
					BROADCAST_PORT);
			broadcastSocket.send(packet);

			System.out.println("Broadcasted server IP address: " + message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}