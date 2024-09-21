package network;

import javax.swing.*;
import java.net.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;

public class EmailSender extends JFrame {
    private JTextField emailField, toField, subjectField, ipField;
    private JPasswordField passwordField;
    private JTextArea messageArea;
    private JButton sendButton;

    private final String smtpHost = "smtp.gmail.com";
    private final String smtpPort = "587";

    public EmailSender() {
        setTitle("Email Sender (Gmail)");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  

        emailField = new JTextField(30);
        passwordField = new JPasswordField(30);
        toField = new JTextField(30);
        subjectField = new JTextField(30);
        ipField = new JTextField(15);
        messageArea = new JTextArea(10, 30);
        sendButton = new JButton("Send Email");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Your Gmail:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("To (Recipient's Email):"));
        panel.add(toField);
        panel.add(new JLabel("Subject:"));
        panel.add(subjectField);
        panel.add(new JLabel("Message:"));
        panel.add(new JScrollPane(messageArea));
        panel.add(new JLabel("Receiver IP (for UDP):"));
        panel.add(ipField);
        panel.add(sendButton);

        sendButton.addActionListener(e -> sendEmail());

        add(panel);
        setVisible(true);
    }

    private void sendEmail() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String to = toField.getText();
        String subject = subjectField.getText();
        String body = messageArea.getText();
        String ip = ipField.getText();

        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", smtpHost);
            properties.put("mail.smtp.port", smtpPort);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(email, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            sendUDPMessage(to, subject, body, ip);
            JOptionPane.showMessageDialog(this, "Email sent successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to send email.");
        }
    }

    private void sendUDPMessage(String to, String subject, String message, String ip) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(ip);  

            String fullMessage = "To: " + to + "\nSubject: " + subject + "\nMessage: " + message;
            byte[] buffer = fullMessage.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 9876);
            socket.send(packet);
            socket.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new EmailSender();
    }
}
