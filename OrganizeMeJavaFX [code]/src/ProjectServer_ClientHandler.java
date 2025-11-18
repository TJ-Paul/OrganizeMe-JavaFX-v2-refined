import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Set;


class ProjectServer_ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    
    public ProjectServer_ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Handle username setup
            // out.println("SYSTEM:Enter your username:");
            String usernameInput;
            while ((usernameInput = in.readLine()) != null) {
                if (usernameInput.startsWith("USERNAME:")) {
                    String requestedUsername = usernameInput.substring(9);
                    this.username = requestedUsername;
                    if (ProjectServer.addUsername(requestedUsername, this)) {
                        out.println("SYSTEM:Welcome to the project, " + requestedUsername + "!");
                        sendUserList();
                        sendAllTasks();
                        break;
                    } else {
                        out.println("SYSTEM:Username already taken. Please choose another:");
                    }
                }
            }
            if (username == null) return;
            
            // Handle messages and commands
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("ADD_TASK:")) {
                    handleAddTask(message.substring(9));
                } else if (message.startsWith("COMPLETE_TASK:")) {
                    handleCompleteTask(message.substring(14));
                } else if (message.startsWith("DELETE_TASK:")) {
                    handleDeleteTask(message.substring(12));
                } else if (message.startsWith("MESSAGE:")) {
                    String chatMessage = message.substring(8);
                    String formattedMessage = "MESSAGE:" + username + ": " + chatMessage;
                    ProjectServer.broadcastMessage(formattedMessage, this);
                }
            }
        } catch (IOException e) {
            System.out.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void handleAddTask(String taskData) {
        String[] parts = taskData.split("\\|", 2);
        if (parts.length == 2) {
            String title = parts[0];
            String description = parts[1];
            ProjectServer.addTask(title, description, username);
            System.out.println("Task added by " + username + ": " + title);
        }
    }
    
    private void handleCompleteTask(String taskIdStr) {
        try {
            int taskId = Integer.parseInt(taskIdStr);
            boolean success = ProjectServer.completeTask(taskId, username);
            if (success) {
                System.out.println("Task " + taskId + " completed by " + username);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid task ID: " + taskIdStr);
        }
    }
    
    private void handleDeleteTask(String taskIdStr) {
        try {
            int taskId = Integer.parseInt(taskIdStr);
            boolean success = ProjectServer.deleteTask(taskId, username);
            if (success) {
                System.out.println("Task " + taskId + " deleted by " + username);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid task ID: " + taskIdStr);
        }
    }
    
    private void sendAllTasks() {
        List<ProjectTask> tasks = ProjectServer.getAllTasks();
        for (ProjectTask task : tasks) {
            String taskMessage = "TASK_ADDED:" + task.getId() + "|" + task.getTitle() + "|" + 
                                task.getDescription() + "|" + task.getAssignedBy() + "|" + 
                                task.getStatus() + "|" + 
                                (task.getCompletedBy() != null ? task.getCompletedBy() : "");
            sendMessage(taskMessage);
        }
    }
    
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    public void sendUserList() {
        Set<String> users = ProjectServer.getActiveUsers();
        sendMessage("USERS:" + String.join(",", users));
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client resources: " + e.getMessage());
        }
        ProjectServer.removeClient(this);
    }
}