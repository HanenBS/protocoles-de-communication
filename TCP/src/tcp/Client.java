package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

            BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            System.out.print("Entrez votre nom d'utilisateur : ");
            String username = userIn.readLine();
            out.println(username);

            Thread readThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverIn.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    // Afficher un message d'information lorsque l'utilisateur quitte la conversation
                    System.out.println("Vous avez quitt  la conversation.");
                }
            });
            readThread.start();

            System.out.println("Bienvenue dans le chat, " + username + "!");
            
            boolean canSendMessage = true;

            String userInput;
            while (canSendMessage) {
                userInput = userIn.readLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    canSendMessage = false;
                }
                if (canSendMessage) {
                    out.println(userInput);
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}