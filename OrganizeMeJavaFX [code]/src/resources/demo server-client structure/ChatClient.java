import java.io.*;
import java.net.Socket;

public class ChatClient {

    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader consoleIn;
    private volatile boolean running = true;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12346;
        String username = "unknown";

        ChatClient client = new ChatClient(host, port);
        client.start(username);
    }


    public void start(String username) {
        try {
            socket = new Socket(host, port);
            System.out.println("Connected to server " + host + ":" + port);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            consoleIn = new BufferedReader(new InputStreamReader(System.in));

            // Send username
            // Server first asks "Enter your name:", we read it then send our username
            String serverPrompt = in.readLine();
            System.out.println(serverPrompt);
            out.println(username);

            // Start background listener (this is your listenForMessages equivalent)
            Thread listener = new Thread(this::listenForMessages);
            listener.setDaemon(true);
            listener.start();

            // Main thread: read from console and send to server
            String input;
            while (running && (input = consoleIn.readLine()) != null) {
                out.println(input);
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            close();
        }
    }

    // 🔥 This is the terminal version of your JavaFX listenForMessages()
    private void listenForMessages() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                // In JavaFX you did: Platform.runLater(() -> handleServerMessage(msg));
                // Here we can just print directly because console isn’t single-threaded UI
                System.out.println(message);
            }
        } catch (IOException e) {
            if (running) {
                System.out.println("Connection lost to server.");
            }
        } finally {
            running = false;
        }
    }

    private void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        System.out.println("Client closed.");
    }
}
