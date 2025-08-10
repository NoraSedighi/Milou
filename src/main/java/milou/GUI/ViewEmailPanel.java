package milou.GUI;

import javax.swing.*;
import java.awt.*;

public class ViewEmailPanel extends JDialog {
    public ViewEmailPanel(MainFrame mainFrame) {
        super(mainFrame, "View Email", true);

        getContentPane().setBackground(new Color(200, 250, 180));
        setLayout(new BorderLayout(10, 10));
        setSize(350, 200);
        setLocationRelativeTo(mainFrame);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBackground(new Color(245, 222, 179));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton allEmailsBtn = createButton("All Emails");
        JButton unreadEmailsBtn = createButton("Unread Emails");
        JButton sentEmailsBtn = createButton("Sent Emails");
        JButton readEmailBtn = createButton("Read by Code");

        buttonPanel.add(allEmailsBtn);
        buttonPanel.add(unreadEmailsBtn);
        buttonPanel.add(sentEmailsBtn);
        buttonPanel.add(readEmailBtn);

        add(buttonPanel, BorderLayout.CENTER);

        //TODO: set buttons on action

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(150, 50));
        return btn;
    }
}
