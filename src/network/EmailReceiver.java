package network;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailReceiver extends JFrame {
    private JTextField emailField, passwordField;
    private JTextArea messageArea;
    private JButton receiveButton;

    // Cố định thông tin IMAP cho Gmail
    private final String imapHost = "imap.gmail.com";
    private final String imapPort = "993"; // Cổng IMAP cho SSL

    public EmailReceiver() {
        setTitle("Email Receiver (Gmail)");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        emailField = new JTextField(30);
        passwordField = new JPasswordField(30);
        messageArea = new JTextArea(10, 30);
        receiveButton = new JButton("Receive Email");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Your Gmail:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JScrollPane(messageArea));
        panel.add(receiveButton);

        receiveButton.addActionListener(e -> receiveEmail());

        add(panel);
        setVisible(true);
    }

    private void receiveEmail() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            Properties properties = new Properties();
            properties.put("mail.store.protocol", "imap");
            properties.put("mail.imap.host", imapHost);
            properties.put("mail.imap.port", imapPort);
            properties.put("mail.imap.ssl.enable", "true"); // Sử dụng SSL

            Session session = Session.getDefaultInstance(properties);
            Store store = session.getStore("imap");

            store.connect(email, password);

            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            Message[] messages = folder.getMessages();

            for (Message message : messages) {
                messageArea.append("From: " + message.getFrom()[0] + "\n");
                messageArea.append("Subject: " + message.getSubject() + "\n");
                messageArea.append("Message: " + message.getContent().toString() + "\n\n");
            }

            folder.close(false);
            store.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to receive email.");
        }
    }

    // Nhận thư qua UDP
    private void receiveUDPMessage() {
        try {
            DatagramSocket socket = new DatagramSocket(9876);
            byte[] buffer = new byte[1024];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String receivedMessage = new String(packet.getData(), 0, packet.getLength());
            messageArea.append("Received via UDP: \n" + receivedMessage + "\n");
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EmailReceiver receiver = new EmailReceiver();
        new Thread(() -> receiver.receiveUDPMessage()).start();
    }
}