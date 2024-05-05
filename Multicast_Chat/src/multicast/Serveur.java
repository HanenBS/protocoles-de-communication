package multicast;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Serveur {
	public static void main(String[] args) {
        try {
            InetAddress group = InetAddress.getByName("224.0.0.1"); // Adresse IP multicast
            int port = 12345; // Port multicast

            MulticastSocket multicastSocket = new MulticastSocket(port);

            multicastSocket.joinGroup(group);

            System.out.println("Serveur multicast en écoute sur " + group + ":" + port);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Serveur: ");
                String message = scanner.nextLine();
                byte[] buffer = message.getBytes();

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                multicastSocket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}