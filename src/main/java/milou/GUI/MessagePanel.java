package milou.GUI;

import javax.swing.*;
import java.awt.*;

public class MessagePanel extends JDialog {

    public MessagePanel(JFrame parent, String message, boolean success) {
        super(parent, true);

        String title;
        Color textColor;

        if (success) {
            title = "Success";
            textColor = Color.GREEN;
        } else {
            title = "Error";
            textColor = Color.RED;
        }

        setTitle(title);
        setSize(400, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        messageLabel.setForeground(textColor);
        add(messageLabel, BorderLayout.CENTER);

        JButton okBtn = new JButton("OK");
        okBtn.setFocusable(false);
        okBtn.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void showMessage(JFrame parent, String message, boolean success) {
        MessagePanel panel = new MessagePanel(parent, message, success);
        panel.setVisible(true);
    }
}
