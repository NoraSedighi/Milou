package milou.GUI;

import milou.Service.AuthorizationService;
import milou.Entity.User;
import milou.Util.HibernateUtil;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {

    private JTextArea emailDisplay;
    private JLabel titleLabel;

    public DashboardPanel(MainFrame mainFrame) {
        setLayout(null);
        setBackground(new Color(255, 253, 200));

        JLabel title = new JLabel("Unread Emails");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(200, 20, 300, 30);
        add(title);

        emailDisplay = new JTextArea();
        emailDisplay.setEditable(false);
        emailDisplay.setFont(new Font("Monospaced", Font.PLAIN, 14));
        emailDisplay.setForeground(Color.BLACK);
        emailDisplay.setBackground(Color.WHITE);
        emailDisplay.setMargin(new Insets(20, 15, 20, 15));

        JScrollPane scrollPane = new JScrollPane(emailDisplay);
        scrollPane.setBounds(50, 60, 500, 200);
        add(scrollPane);

        JButton sendBtn = new JButton("Send");
        sendBtn.setBounds(200, 280, 200, 40);
        sendBtn.setFocusable(false);
        add(sendBtn);

        JButton viewBtn = new JButton("View");
        viewBtn.setBounds(200, 325, 200, 40);
        viewBtn.setFocusable(false);
        add(viewBtn);

        JButton replyBtn = new JButton("Reply");
        replyBtn.setBounds(200, 370, 200, 35);
        replyBtn.setFocusable(false);
        add(replyBtn);

        JButton forwardBtn = new JButton("Forward");
        forwardBtn.setBounds(200, 415, 200, 35);
        forwardBtn.setFocusable(false);
        add(forwardBtn);

        JButton deleteEmailBtn = new JButton("Delete Email");
        deleteEmailBtn.setBounds(200, 460, 200, 40);
        deleteEmailBtn.setFocusable(false);
        add(deleteEmailBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(200, 505, 200, 40);
        logoutBtn.setFocusable(false);
        add(logoutBtn);

        // ===== Button Logic =====

        logoutBtn.addActionListener(e -> mainFrame.showWelcomePanel());

        sendBtn.addActionListener(e -> mainFrame.showSendEmailPanel());

        viewBtn.addActionListener(e -> {
            ViewEmailPanel dialog = new ViewEmailPanel(mainFrame);
            dialog.setVisible(true);
        });

        replyBtn.addActionListener(e -> mainFrame.showReplyEmailPanel());

        forwardBtn.addActionListener(e -> mainFrame.showForwardEmailPanel());

        // TODO : set the rest of the buttons on action

        showUnreadEmails();
    }

    public void showUnreadEmails() {
        User user = AuthorizationService.getCurrentUser();
        if (user == null) return;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> results = session.createQuery("""
                SELECT e.senderEmail, e.subject, e.code
                FROM EmailRecipient r
                JOIN Email e ON r.emailCode = e.code
                WHERE r.recipientEmail = :email AND r.isRead = false
                """, Object[].class)
                    .setParameter("email", user.getEmail())
                    .list();

            StringBuilder text = new StringBuilder();
            if (results.isEmpty()) {
                text.append("You have no unread emails.\n");
            } else {
                text.append(results.size()).append(" unread emails:\n\n");
                for (Object[] row : results) {
                    String sender = (String) row[0];
                    String subject = (String) row[1];
                    String code = (String) row[2];
                    text.append("+ ").append(sender).append(" - ").append(subject).append(" (").append(code).append(")\n");
                }
            }

            emailDisplay.setText(text.toString());

        } catch (Exception e) {
            emailDisplay.setText("Failed to load unread emails.");
        }
    }
}
