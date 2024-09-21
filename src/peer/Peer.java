package peer;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;

public class Peer {
    private DatagramSocket socket;

    public Peer(int port) throws IOException {
        socket = new DatagramSocket(port);
        startListening();
    }

    public void sendMessage(String message, String ipAddress, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(ipAddress), port);
        socket.send(packet);
    }

    public void sendFile(File file, String ipAddress, int port) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        DatagramPacket packet = new DatagramPacket(fileBytes, fileBytes.length, InetAddress.getByName(ipAddress), port);
        socket.send(packet);
    }

    private void startListening() {
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[65536];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    PeerApp.appendToChat("Received: " + received);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
