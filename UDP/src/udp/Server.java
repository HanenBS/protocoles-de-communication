package udp;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class Server extends Thread {
    private DatagramSocket socket;
    private Set<ClientInfo> clients = new HashSet<>();

    public Server(int port) throws IOException {
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        try {
            System.out.println("Server is running...");

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                String clientName = getClientName(clientAddress, clientPort);
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                if (message.trim().equalsIgnoreCase("quit")) {
                    handleClientExit(clientAddress, clientPort);
                } else {
                    System.out.println("Received from " + clientName + " - " + message);

                    // Add the client to the set if not already present
                    ClientInfo clientInfo = new ClientInfo(clientAddress, clientPort, clientName);
                    clients.add(clientInfo);

                    // Broadcast the message to all clients
                    broadcast(message, clientInfo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private String getClientName(InetAddress address, int port) {
        for (ClientInfo client : clients) {
            if (client.getAddress().equals(address) && client.getPort() == port) {
                return client.getName();
            }
        }
        return address.toString() + ":" + port; // Default to address and port if name not found
    }

    private void handleClientExit(InetAddress address, int port) {
        ClientInfo exitingClient = new ClientInfo(address, port, getClientName(address, port));
        clients.remove(exitingClient);
        System.out.println("Client " + exitingClient.getName() + " has quit.");
    }

    private void broadcast(String message, ClientInfo sender) throws IOException {
        for (ClientInfo client : clients) {
            if (!client.equals(sender)) {
                byte[] responseBytes = (sender.getName() + ": " + message).getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, client.getAddress(), client.getPort());
                socket.send(responsePacket);
            }
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(49001);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientInfo {
    private InetAddress address;
    private int port;
    private String name;

    public ClientInfo(InetAddress address, int port, String name) {
        this.address = address;
        this.port = port;
        this.name = name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClientInfo that = (ClientInfo) obj;
        return port == that.port && address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return address.hashCode() + 31 * port;
    }

    @Override
    public String toString() {
        return name + "@" + address.toString() + ":" + port;
    }
}
