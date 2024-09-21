package network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Peer extends JFrame {
    private JTextField emailField;
    private JTextArea messageArea;
    private JButton sendButton;
    private JTextArea receivedArea;
    private DatagramSocket socket;

    public Peer() {
        setTitle("Email Peer");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        emailField = new JTextField("Recipient Email");
        messageArea = new JTextArea("Your message here...");
        sendButton = new JButton("Send");
        receivedArea = new JTextArea();
        receivedArea.setEditable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(emailField, BorderLayout.NORTH);
        panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.SOUTH);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(receivedArea), BorderLayout.CENTER);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendEmail();
            }
        });

        startReceiving();
    }

    private void sendEmail() {
        try {
            DatagramSocket socket = new DatagramSocket();
            String email = emailField.getText();
            String message = messageArea.getText();
            String data = email + "\n" + message;

            byte[] buffer = data.getBytes();
            InetAddress address = InetAddress.getByName("localhost"); // Địa chỉ IP của peer nhận
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 9876);
            socket.send(packet);
            socket.close();

            JOptionPane.showMessageDialog(this, "Email sent!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startReceiving() {
        new Thread(() -> {
            try {
                socket = new DatagramSocket(9876);
                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String receivedData = new String(packet.getData(), 0, packet.getLength());
                    receivedArea.append(receivedData + "\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Peer peer = new Peer();
            peer.setVisible(true);
        });
    }
}