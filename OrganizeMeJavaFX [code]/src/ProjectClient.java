import java.io.*;
import java.net.Socket;

public class ProjectClient {

    private final String serverAddress;
    private final int port;
    private final String username;
    private final ProjectEvents events;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean connected = false;

    public ProjectClient(String serverAddress, int port, String username, ProjectEvents events) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
        this.events = events;
    }

    public void connect() throws IOException {
        socket = new Socket(serverAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        connected = true;

        // Identify to server
        out.println("USERNAME:" + username);

        // Start listener thread
        Thread listener = new Thread(this::listenForMessages);
        listener.setDaemon(true);
        listener.start();
    }

    private void listenForMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                dispatch(message);
            }
        } catch (IOException e) {
            if (connected) {
                events.onDisconnected("Connection lost: " + e.getMessage());
            }
        } finally {
            connected = false;
            closeQuietly();
        }
    }

    private void dispatch(String message) {
        if (message.startsWith("SYSTEM:")) {
            events.onSystemMessage(message.substring(7));
        } else if (message.startsWith("MESSAGE:")) {
            events.onChatMessage(message.substring(8));
        } else if (message.startsWith("USERS:")) {
            events.onUsersUpdated(message.substring(6));
        } else if (message.startsWith("TASK_ADDED:")) {
            events.onTaskAdded(message.substring(11));
        } else if (message.startsWith("TASK_COMPLETED:")) {
            events.onTaskCompleted(message.substring(15));
        } else if (message.startsWith("TASK_DELETED:")) {
            events.onTaskDeleted(message.substring(13));
        }
    }

    // ---- outgoing commands ----
    public void sendChat(String message) {
        if (connected) out.println("MESSAGE:" + message);
    }

    public void addTask(String title, String description) {
        if (connected) out.println("ADD_TASK:" + title + "|" + description);
    }

    public void completeTask(int taskId) {
        if (connected) out.println("COMPLETE_TASK:" + taskId);
    }

    public void deleteTask(int taskId) {
        if (connected) out.println("DELETE_TASK:" + taskId);
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        connected = false;
        closeQuietly();
    }

    private void closeQuietly() {
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (in  != null) in.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    }
}
