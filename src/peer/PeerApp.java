package peer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class PeerApp {
    private static JFrame frame;
    private static JTextArea textArea;
    private static JTextField textField;
    private static JTextField ipField;
    private static JTextField portField;
    private static Peer peer;

    public PeerApp(int port, String ipAddress) throws IOException {
        peer = new Peer(port);
        frame = new JFrame("P2P Email Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));

        textArea = new JTextArea(15, 30);
        textField = new JTextField(30);
        ipField = new JTextField(ipAddress, 15);
        portField = new JTextField(String.valueOf(port), 5);

        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);

        JButton sendButton = new JButton("Send");
        JButton fileButton = new JButton("Send File");

        sendButton.addActionListener(e -> sendMessage());
        fileButton.addActionListener(e -> sendFile());

        textField.addActionListener(e -> sendMessage());

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(240, 240, 240));
        topPanel.add(new JLabel("IP:"));
        topPanel.add(ipField);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(portField);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        mainPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(240, 240, 240));
        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.add(fileButton, BorderLayout.WEST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void sendMessage() {
        String message = textField.getText();
        if (!message.trim().isEmpty()) {
            String ipAddress = ipField.getText();
            int port;
            try {
                port = Integer.parseInt(portField.getText());
                peer.sendMessage(message, ipAddress, port);
                appendToChat("Sent: " + message);
                textField.setText("");
            } catch (NumberFormatException e) {
                appendToChat("Invalid port number!");
            } catch (Exception e) {
                appendToChat("Error sending message: " + e.getMessage());
            }
        } else {
            appendToChat("Message cannot be empty!");
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String ipAddress = ipField.getText();
            int port;
            try {
                port = Integer.parseInt(portField.getText());
                peer.sendFile(file, ipAddress, port);
                appendToChat("Sent file: " + file.getName());
            } catch (NumberFormatException e) {
                appendToChat("Invalid port number!");
            } catch (Exception e) {
                appendToChat("Error sending file: " + e.getMessage());
            }
        } else {
            appendToChat("File selection cancelled.");
        }
    }

    public static void appendToChat(String message) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(message + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Show a dialog to get IP and Port before creating PeerApp
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new GridLayout(2, 2));

            JTextField ipField = new JTextField("localhost");
            JTextField portField = new JTextField("8000");

            inputPanel.add(new JLabel("IP Address:"));
            inputPanel.add(ipField);
            inputPanel.add(new JLabel("Port:"));
            inputPanel.add(portField);

            int option = JOptionPane.showConfirmDialog(null, inputPanel, 
                "Enter IP Address and Port", JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                String ipAddress = ipField.getText();
                int port;
                try {
                    port = Integer.parseInt(portField.getText());
                    new PeerApp(port, ipAddress);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Invalid port number. Please enter a valid number.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.exit(0); // Exit if the user cancels
            }
        });
    }
}
