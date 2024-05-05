package multicast;

import java.io.*;
import java.net.*;

public class Client {
	 @SuppressWarnings("deprecation")
		public static void main(String[] args) {
	        try {
	            InetAddress group = InetAddress.getByName("224.0.0.1"); // Adresse IP multicast
	            int port = 12345; // Port multicast

	            MulticastSocket multicastSocket = new MulticastSocket(port);
	            multicastSocket.joinGroup(group);

	            System.out.println("Client multicast en écoute sur " + group + ":" + port);

	            byte[] buffer = new byte[1024];

	            while (true) {
	                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	                multicastSocket.receive(packet);

	                String message = new String(packet.getData(), 0, packet.getLength());
	                System.out.println("Client: " + message);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}