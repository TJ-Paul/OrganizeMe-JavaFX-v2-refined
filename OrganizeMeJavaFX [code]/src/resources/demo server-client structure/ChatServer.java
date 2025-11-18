import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.*;

public class ChatServer {

    private final int port;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();


    public ChatServer(int port) { this.port = port; }

    public static void main(String[] args) {
        int port = 12346;

        ChatServer server = new ChatServer(port);
        try { server.start(); } 
        catch (IOException e){ System.err.println("Server error: " + e.getMessage()); }
    }




    public void start() throws IOException {

        System.out.println("Chat server starting on port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. \nWaiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler handler = new ClientHandler(this, clientSocket);
                clients.add(handler);

                Thread t = new Thread(handler);
                t.setDaemon(true);
                t.start();
            }
        }
    }

    // Broadcast message to all connected clients
    public void broadcast(String message, ClientHandler sender) {
        // need "ClientHandler sender" because, otherwise, the sender will receive his own message
        System.out.println("Broadcasting: " + message);
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.send(message);
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("Client removed: " + handler.getName());
    }
}
