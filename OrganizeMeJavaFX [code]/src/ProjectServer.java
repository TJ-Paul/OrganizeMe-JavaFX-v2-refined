import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProjectServer {
    private static final int PORT = 12345;
    private static Set<ProjectServer_ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static Map<String, ProjectServer_ClientHandler> usernames = new ConcurrentHashMap<>();
    private static Map<Integer, ProjectTask> tasks = new ConcurrentHashMap<>();
    private static AtomicInteger taskIdCounter = new AtomicInteger(1);
    


    public static void main(String[] args) {
        System.out.println("Project Management Server starting on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started successfully!");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                ProjectServer_ClientHandler clientHandler = new ProjectServer_ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }



    
    public static synchronized void broadcastMessage(String message, ProjectServer_ClientHandler sender) {
        for (ProjectServer_ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    
    public static synchronized void removeClient(ProjectServer_ClientHandler client) {
        clients.remove(client);
        if (client.getUsername() != null) {
            usernames.remove(client.getUsername());
            broadcastMessage("SYSTEM:" + client.getUsername() + " left the project", client);
        }
        System.out.println("Client disconnected. Active clients: " + clients.size());
    }
    
    public static synchronized boolean addUsername(String username, ProjectServer_ClientHandler client) {
        if (usernames.containsKey(username)) {
            return false;
        }
        usernames.put(username, client);
        client.setUsername(username);
        broadcastMessage("SYSTEM:" + username + " joined the project", client);
        return true;
    }
    
    public static synchronized Set<String> getActiveUsers() {
        return new HashSet<>(usernames.keySet());
    }
    
    public static synchronized int addTask(String title, String description, String assignedBy) {
        int taskId = taskIdCounter.getAndIncrement();
        ProjectTask task = new ProjectTask(taskId, title, description, assignedBy);
        tasks.put(taskId, task);
        
        String taskMessage = "TASK_ADDED:" + taskId + "|" + title + "|" + description + "|" + assignedBy + "|PENDING|";
        broadcastMessage(taskMessage, null);
        
        return taskId;
    }
    
    public static synchronized boolean completeTask(int taskId, String completedBy) {
        ProjectTask task = tasks.get(taskId);
        if (task != null && task.getStatus() == TaskStatus.PENDING) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedBy(completedBy);
            task.setCompletedDate(new Date());
            
            String taskMessage = "TASK_COMPLETED:" + taskId + "|" + task.getTitle() + "|" + completedBy;
            broadcastMessage(taskMessage, null);
            return true;
        }
        return false;
    }
    
    public static synchronized boolean deleteTask(int taskId, String deletedBy) {
        ProjectTask task = tasks.get(taskId);
        if (task != null) {
            tasks.remove(taskId);
            
            String taskMessage = "TASK_DELETED:" + taskId + "|" + task.getTitle() + "|" + deletedBy;
            broadcastMessage(taskMessage, null);
            return true;
        }
        return false;
    }
    
    public static synchronized List<ProjectTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }
}

enum TaskStatus {
    PENDING, COMPLETED
}

class ProjectTask {
    private int id;
    private String title;
    private String description;
    private String assignedBy;
    private String completedBy;
    private TaskStatus status;
    private Date createdDate;
    private Date completedDate;
    
    public ProjectTask(int id, String title, String description, String assignedBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.assignedBy = assignedBy;
        this.status = TaskStatus.PENDING;
        this.createdDate = new Date();
    }
    
    // Getters and setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAssignedBy() { return assignedBy; }
    public String getCompletedBy() { return completedBy; }
    public TaskStatus getStatus() { return status; }
    public Date getCreatedDate() { return createdDate; }
    public Date getCompletedDate() { return completedDate; }
    
    public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setCompletedDate(Date completedDate) { this.completedDate = completedDate; }
}
