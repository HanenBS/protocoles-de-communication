package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName("localhost");
            int serverPort = 49001;

            Scanner sc = new Scanner(System.in);
            System.out.println("Enter your name: ");
            String clientName = sc.nextLine();

            final String[] choice = {""};  // Utilisation d'un tableau pour simuler une variable "effectively final"

            // Start a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    while (!choice[0].trim().equalsIgnoreCase("quit") && !clientSocket.isClosed()) {
                        clientSocket.receive(receivePacket);

                        String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println("Message received from server: " + receivedMessage);
                    }
                } catch (IOException e) {
                    if (!choice[0].trim().equalsIgnoreCase("quit") && !clientSocket.isClosed()) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Allow the user to send messages
            while (!choice[0].trim().equalsIgnoreCase("quit")) {
                System.out.println("Enter a message ('quit' to quit): ");
                choice[0] = sc.nextLine();

                byte[] sendData = (clientName + ": " + choice[0]).getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
                clientSocket.send(sendPacket);
            }

            System.out.println("You have left the conversation.");
            sc.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
