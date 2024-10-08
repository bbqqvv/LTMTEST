package network;

import javax.swing.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

public class EmailReceiver extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JList<String> emailList;
    private JTextArea messageArea;
    private DefaultListModel<String> listModel;
    private Map<String, Message> messagesMap;
    private Folder folder;
    private DatagramSocket udpSocket;

    private final String imapHost = "imap.gmail.com";
    private final String imapPort = "993";
    private String userEmail;   
    private String userPassword;

    public EmailReceiver() {
        setTitle("Email Receiver (Gmail)");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  

        if (showLoginDialog()) {
            initializeUI();
            initializeUDPListener(); 
            receiveEmail(); 
        } else {
            JOptionPane.showMessageDialog(this, "Login failed. Exiting.");
            System.exit(0);
        }
    }

    private void initializeUI() {
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        listModel = new DefaultListModel<>();
        emailList = new JList<>(listModel);
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        emailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedEmail = emailList.getSelectedValue();
                if (selectedEmail != null && messagesMap.containsKey(selectedEmail)) {
                    displayEmailContent(messagesMap.get(selectedEmail));
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(emailList), new JScrollPane(messageArea));
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> receiveEmail());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);

        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }

    private boolean showLoginDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLabel = new JLabel("Your Gmail:");
        emailField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(emailLabel, constraints);
        constraints.gridx = 1;
        panel.add(emailField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(passwordLabel, constraints);
        constraints.gridx = 1;
        panel.add(passwordField, constraints);

        int option = JOptionPane.showConfirmDialog(this, panel, "Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            userEmail = emailField.getText();
            userPassword = new String(passwordField.getPassword());
            return !userEmail.isEmpty() && !userPassword.isEmpty();
        }
        return false;
    }

    private void receiveEmail() {
        new Thread(() -> {
            try {
                Properties properties = new Properties();
                properties.put("mail.store.protocol", "imap");
                properties.put("mail.imap.host", imapHost);
                properties.put("mail.imap.port", imapPort);
                properties.put("mail.imap.ssl.enable", "true");

                Session session = Session.getDefaultInstance(properties);
                Store store = session.getStore("imap");
                store.connect(userEmail, userPassword);

                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);

                Message[] messages = folder.getMessages(Math.max(1, folder.getMessageCount() - 10), folder.getMessageCount());
                listModel.clear();
                messagesMap = new HashMap<>();

                for (Message message : messages) {
                    String subject = message.getSubject();
                    listModel.addElement(subject);
                    messagesMap.put(subject, message);
                }

                folder.close(false);
                store.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to receive email: " + ex.getMessage());
            }
        }).start();
    }

    private void displayEmailContent(Message message) {
        try {
            if (folder == null || !folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }

            StringBuilder content = new StringBuilder();
            content.append("From: ").append(message.getFrom()[0]).append("\n");
            content.append("Subject: ").append(message.getSubject()).append("\n\n");

            if (message.isMimeType("text/plain")) {
                content.append("Message: ").append(message.getContent().toString());
            } else if (message.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (bodyPart.isMimeType("text/plain")) {
                        content.append("Message: ").append(bodyPart.getContent().toString());
                    }
                }
            }

            messageArea.setText(content.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            messageArea.setText("Failed to display email content: " + ex.getMessage());
        }
    }

    private void initializeUDPListener() {
        try {
            udpSocket = new DatagramSocket(9876);
            new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        udpSocket.receive(packet);
                        String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                        updateEmailList(receivedMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to initialize UDP listener: " + e.getMessage());
        }
    }

    private void updateEmailList(String receivedMessage) {
        // Phân tích và cập nhật danh sách email từ thông điệp nhận được
        // Ví dụ: Nếu thông điệp chứa subject mới, bạn có thể thêm vào danh sách
        JOptionPane.showMessageDialog(this, "Received via UDP:\n" + receivedMessage);
        // Cập nhật danh sách email nếu có thông tin mới
        listModel.addElement(receivedMessage);
    }

    @Override
    public void dispose() {
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmailReceiver::new);
    }
}
