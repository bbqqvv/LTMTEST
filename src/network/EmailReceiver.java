package network;

import javax.swing.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class EmailReceiver extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JList<String> emailList;
    private JTextArea messageArea;
    private DefaultListModel<String> listModel;
    private Map<String, Message> messagesMap; // Lưu trữ email để truy cập dễ dàng
    private Folder folder; // Thêm biến folder để sử dụng sau này

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
            receiveEmail(); // Gọi phương thức để nhận email
        } else {
            JOptionPane.showMessageDialog(this, "Login failed. Exiting.");
            System.exit(0);
        }
    }

    private void initializeUI() {
        // Tạo các thành phần UI
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        listModel = new DefaultListModel<>();
        emailList = new JList<>(listModel);
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Listener để hiển thị nội dung email khi nhấp vào
        emailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedEmail = emailList.getSelectedValue();
                if (selectedEmail != null && messagesMap.containsKey(selectedEmail)) {
                    displayEmailContent(messagesMap.get(selectedEmail));
                }
            }
        });

        // Bố cục
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(emailList), new JScrollPane(messageArea));
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.3);

        // Thêm nút Refresh
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> receiveEmail());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);

        // Thêm vào khung chính
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
                listModel.clear(); // Xóa danh sách trước khi thêm mới
                messagesMap = new HashMap<>(); // Khởi tạo lại bản đồ lưu trữ email

                String filterEmail = "vanbui0966467356@gmail.com"; // Địa chỉ email cần lọc

                for (Message message : messages) {
                    if (message.getFrom()[0].toString().equalsIgnoreCase(filterEmail)) {
                        String subject = message.getSubject();
                        listModel.addElement(subject); // Thêm subject vào danh sách
                        messagesMap.put(subject, message); // Lưu trữ message theo subject
                    }
                }

                // Không đóng folder ở đây, sẽ đóng sau khi người dùng không còn cần nữa.
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to receive email.");
            }
        }).start();
    }

    private void displayEmailContent(Message message) {
        try {
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

            messageArea.setText(content.toString()); // Hiển thị nội dung email
        } catch (Exception ex) {
            ex.printStackTrace();
            messageArea.setText("Failed to display email content.");
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmailReceiver::new);
    }
}
