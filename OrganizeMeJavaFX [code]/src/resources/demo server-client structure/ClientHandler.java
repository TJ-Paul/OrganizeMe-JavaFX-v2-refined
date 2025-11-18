import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private ChatServer server;
    private Socket socket;

    private PrintWriter out;
    private BufferedReader in;

    private String name = "Unknown";

    private static int userid = 0;

    public ClientHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // First line from client is the username
            name = "user " + (++userid);
            out.println("Welcome " + name);


            server.broadcast("[SYSTEM]: " + name + " joined the chat", this);

            String line;
            while ((line = in.readLine()) != null) {
                String msg = name + ": " + line;
                server.broadcast(msg, this);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {}

            String announcement = "[SYSTEM] " + name + " left the chat";
            server.broadcast(announcement, this);
            server.removeClient(this);
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
