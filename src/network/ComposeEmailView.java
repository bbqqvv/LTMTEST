package network;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ComposeEmailView extends JFrame {
    private JTextField recipientField;
    private JTextField subjectField;
    private JTextArea bodyArea;
    private JButton sendButton;

    public ComposeEmailView() {
        setTitle("Compose Email");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel recipientLabel = new JLabel("Recipient:");
        recipientLabel.setBounds(20, 20, 80, 25);
        add(recipientLabel);

        recipientField = new JTextField();
        recipientField.setBounds(100, 20, 250, 25);
        add(recipientField);

        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setBounds(20, 60, 80, 25);
        add(subjectLabel);

        subjectField = new JTextField();
        subjectField.setBounds(100, 60, 250, 25);
        add(subjectField);

        JLabel bodyLabel = new JLabel("Body:");
        bodyLabel.setBounds(20, 100, 80, 25);
        add(bodyLabel);

        bodyArea = new JTextArea();
        bodyArea.setBounds(100, 100, 250, 100);
        add(bodyArea);

        sendButton = new JButton("Send");
        sendButton.setBounds(150, 220, 100, 25);
        add(sendButton);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String recipient = recipientField.getText();
                String subject = subjectField.getText();
                String body = bodyArea.getText();

                SmtpEmailSender.sendEmail(recipient, subject, body);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ComposeEmailView view = new ComposeEmailView();
            view.setVisible(true);
        });
    }
}
