package tcp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Serveur {

	private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<PrintWriter, String> clientUsernameMap = new HashMap<>();
    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("Serveur de chat démarré...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur de communication: " + e);
        } finally {
            executorService.shutdown();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                username = in.readLine();
                System.out.println("Nouveau client connecté: " + username);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                synchronized (clientUsernameMap) {
                    clientUsernameMap.put(out, username);
                }

                broadcast("Serveur: " + username);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(username + ": " + message);
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }
                    if (message.startsWith("@")) {
                        // Message privé
                        sendPrivateMessage(username, message);
                    } else {
                        // Message public
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur de communication avec un client: " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Erreur lors de la fermeture du socket: " + e);
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                synchronized (clientUsernameMap) {
                    clientUsernameMap.remove(out);
                }
                broadcast("Serveur: " + username + " a quitté le chat.");

                // Ajoutez le message ci-dessous pour informer que le client a quitté
                System.out.println("Client " + username + " a quitté la conversation.");
            }
        }

        private void broadcast(String message) {
            executorService.execute(() -> {
                synchronized (clientWriters) {
                    for (PrintWriter writer : clientWriters) {
                        writer.println(message);
                    }
                }
            });
        }

        private void sendPrivateMessage(String sender, String message) {
            String[] parts = message.split(" ", 2);
            if (parts.length == 2) {
                String recipient = parts[0].substring(1); // Remove the '@' symbol
                String privateMessage = sender + " (privé): " + parts[1];

                synchronized (clientUsernameMap) {
                    for (Map.Entry<PrintWriter, String> entry : clientUsernameMap.entrySet()) {
                        if (entry.getValue().equals(recipient)) {
                            entry.getKey().println(privateMessage);
                            break; // Assume there's only one recipient with the given username
                        }
                    }
                }
            }
        }
    }
}