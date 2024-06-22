import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ServerGUI {

    // Instance variables
    private HashMap<String, String> data;
    private JSONParser parser;
    private String server_id;
    private String clients;
    private JLabel clientLabel = new JLabel();
    private JFrame serverGUI; // Declare serverGUI as an instance variable

    // Constructor
    public ServerGUI() {
        serverGUI = new JFrame();
        serverGUI.setBackground(new Color(199, 199, 199));
        serverGUI.setLayout(null);
        serverGUI.setSize(300, 300);
        serverGUI.setTitle("Java Server");
        serverGUI.setResizable(false);
        serverGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Initialize instance variables
        data = new HashMap<>();
        parser = new JSONParser();

        // Load data from JSON file
        loadDataFromJSON();

        // Display server port label
        JLabel portLabel = new JLabel(String.format("Server Port: %s", server_id));
        portLabel.setBounds(80, 27, 200, 40);
        portLabel.setForeground(Color.BLACK);
        serverGUI.add(portLabel);

        // Display client label
        clientLabel.setText(String.format("Current Online Local Clients: %s", clients));
        clientLabel.setForeground(Color.BLACK);
        clientLabel.setBounds(80, 57, 200, 40);
        serverGUI.add(clientLabel);

        // Add window listener to detect frame closure
        serverGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                updateClientsCount(Integer.valueOf(clients) - 1);
                serverGUI.dispose();
                System.exit(0);
            }
        });

        // Set GUI visible
        serverGUI.setVisible(true);

        // Start watch service to monitor JSON file changes
        startWatchService();
    }

    // Method to load data from JSON file
    private void loadDataFromJSON() {
        try {
            data = parser.parseJSONFile("public/dependencies/server.json");
            server_id = data.get("server_id");
            clients = data.get("clients");
            data.replace("clients", String.valueOf(Integer.valueOf(clients) + 1));
            clients = data.get("clients");
            updateJSONFile(); // Update JSON file with incremented clients count
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            // Handle exception appropriately (show error message, exit application, etc.)
        }
    }

    // Method to update JSON file with current values
    private void updateJSONFile() {
        String newValues = String.format("{\"server_id\": \"%s\", \"clients\": \"%s\"}", server_id, clients);
        try {
            Files.write(Paths.get("public/dependencies/server.json"), newValues.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle file writing error
        }
    }

    // Method to update clients count and write back to JSON file
    private void updateClientsCount(int newCount) {
        data.replace("clients", String.valueOf(newCount));
        clients = String.valueOf(newCount);
        updateJSONFile();
    }

    // Method to start watch service for JSON file
    private void startWatchService() {
        Path serverJSONFilePath = Paths.get("public/dependencies/server.json");

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            serverJSONFilePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            new Thread(() -> {
                while (true) {
                    WatchKey key;
                    try {
                        key = watchService.take(); // Wait for a key to be signalled
                    } catch (InterruptedException e) {
                        return; // Exit the thread if interrupted
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();

                        if (filename != null && filename.equals(serverJSONFilePath.getFileName()) && kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            // Reload and update data
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    data = parser.parseJSONFile(serverJSONFilePath.toString());
                                    server_id = data.get("server_id");
                                    clients = data.get("clients");
                                    clientLabel.setText(String.format("Current Online Local Clients: %s", clients));
                                    clientLabel.setForeground(Color.BLACK);
                                    serverGUI.revalidate();
                                    serverGUI.repaint();
                                } catch (IllegalArgumentException | IllegalStateException ex) {
                                    ex.printStackTrace();
                                    // Handle exception appropriately
                                }
                            });
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break; // Exit loop if the key is no longer valid
                    }
                }
            }).start();

        } catch (IOException | UnsupportedOperationException e) {
            e.printStackTrace();
            // Handle watch service initialization error
        }
    }
}
