package milou.GUI;

import milou.Entity.Email;
import milou.Entity.EmailRecipient;
import milou.Entity.User;
import milou.Service.AuthorizationService;
import milou.Util.HibernateUtil;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ViewEmailPanel extends JDialog {
    private final MainFrame mainFrame;

    public ViewEmailPanel(MainFrame mainFrame) {
        super(mainFrame, "View Email", true);
        this.mainFrame = mainFrame;

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

        // ===== Button Logic =====

        allEmailsBtn.addActionListener(e -> showAllEmails());

        unreadEmailsBtn.addActionListener(e -> showUnreadEmails());

        sentEmailsBtn.addActionListener(e -> showSentEmails());

        readEmailBtn.addActionListener(e -> showEmailByCode());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setPreferredSize(new Dimension(150, 50));
        return btn;
    }

    private void showAllEmails() {
        User user = AuthorizationService.getCurrentUser();
        if (user == null) return;

        JDialog emailDialog = new JDialog(mainFrame, "All Emails", true);
        emailDialog.setLayout(null);
        emailDialog.setSize(600, 700);
        emailDialog.setLocationRelativeTo(mainFrame);
        emailDialog.getContentPane().setBackground(new Color(200, 250, 180));

        JLabel title = new JLabel("All Emails");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(220, 30, 200, 30);
        emailDialog.add(title);

        JTextArea emailDisplay = new JTextArea();
        emailDisplay.setEditable(false);
        emailDisplay.setFont(new Font("Monospaced", Font.PLAIN, 14));
        emailDisplay.setForeground(Color.BLACK);
        emailDisplay.setBackground(Color.WHITE);
        emailDisplay.setMargin(new Insets(20, 15, 20, 15));

        JScrollPane scrollPane = new JScrollPane(emailDisplay);
        scrollPane.setBounds(50, 80, 500, 400);
        emailDialog.add(scrollPane);

        JButton okBtn = new JButton("OK");
        okBtn.setBounds(240, 550, 120, 35);
        okBtn.setFocusable(false);
        emailDialog.add(okBtn);

        okBtn.addActionListener(e -> emailDialog.dispose());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> results = session.createQuery("""
                SELECT e.senderEmail, e.subject, e.code
                FROM EmailRecipient r
                JOIN Email e ON r.emailCode = e.code
                WHERE r.recipientEmail = :email
                """, Object[].class)
                    .setParameter("email", user.getEmail())
                    .list();

            StringBuilder text = new StringBuilder();
            if (results.isEmpty()) {
                text.append("You have no emails.\n");
            } else {
                text.append(results.size()).append(" emails:\n\n");
                for (Object[] row : results) {
                    String sender = (String) row[0];
                    String subject = (String) row[1];
                    String code = (String) row[2];
                    text.append("+ ").append(sender).append(" - ").append(subject).append(" (").append(code).append(")\n");
                }
            }

            emailDisplay.setText(text.toString());
        } catch (Exception ex) {
            emailDisplay.setText("Failed to load emails: " + ex.getMessage());
        }

        emailDialog.setVisible(true);
    }

    private void showUnreadEmails() {
        User user = AuthorizationService.getCurrentUser();
        if (user == null) return;

        JDialog emailDialog = new JDialog(mainFrame, "Unread Emails", true);
        emailDialog.setLayout(null);
        emailDialog.setSize(600, 700);
        emailDialog.setLocationRelativeTo(mainFrame);
        emailDialog.getContentPane().setBackground(new Color(200, 250, 180));

        JLabel title = new JLabel("Unread Emails");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(220, 30, 200, 30);
        emailDialog.add(title);

        JTextArea emailDisplay = new JTextArea();
        emailDisplay.setEditable(false);
        emailDisplay.setFont(new Font("Monospaced", Font.PLAIN, 14));
        emailDisplay.setForeground(Color.BLACK);
        emailDisplay.setBackground(Color.WHITE);
        emailDisplay.setMargin(new Insets(20, 15, 20, 15));

        JScrollPane scrollPane = new JScrollPane(emailDisplay);
        scrollPane.setBounds(50, 80, 500, 400);
        emailDialog.add(scrollPane);

        JButton okBtn = new JButton("OK");
        okBtn.setBounds(240, 550, 120, 35);
        okBtn.setFocusable(false);
        emailDialog.add(okBtn);

        okBtn.addActionListener(e -> emailDialog.dispose());

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
        } catch (Exception ex) {
            emailDisplay.setText("Failed to load unread emails: " + ex.getMessage());
        }

        emailDialog.setVisible(true);
    }

    private void showSentEmails() {
        User user = AuthorizationService.getCurrentUser();
        if (user == null) return;

        JDialog emailDialog = new JDialog(mainFrame, "Sent Emails", true);
        emailDialog.setLayout(null);
        emailDialog.setSize(600, 700);
        emailDialog.setLocationRelativeTo(mainFrame);
        emailDialog.getContentPane().setBackground(new Color(200, 250, 180));

        JLabel title = new JLabel("Sent Emails");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(220, 30, 200, 30);
        emailDialog.add(title);

        JTextArea emailDisplay = new JTextArea();
        emailDisplay.setEditable(false);
        emailDisplay.setFont(new Font("Monospaced", Font.PLAIN, 14));
        emailDisplay.setForeground(Color.BLACK);
        emailDisplay.setBackground(Color.WHITE);
        emailDisplay.setMargin(new Insets(20, 15, 20, 15));

        JScrollPane scrollPane = new JScrollPane(emailDisplay);
        scrollPane.setBounds(50, 80, 500, 400);
        emailDialog.add(scrollPane);

        JButton okBtn = new JButton("OK");
        okBtn.setBounds(240, 550, 120, 35);
        okBtn.setFocusable(false);
        emailDialog.add(okBtn);

        okBtn.addActionListener(e -> emailDialog.dispose());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> results = session.createQuery("""
                SELECT e.senderEmail, e.subject, e.code
                FROM Email e
                WHERE e.senderEmail = :email
                """, Object[].class)
                    .setParameter("email", user.getEmail())
                    .list();

            StringBuilder text = new StringBuilder();
            if (results.isEmpty()) {
                text.append("You have no sent emails.\n");
            } else {
                text.append(results.size()).append(" sent emails:\n\n");
                for (Object[] row : results) {
                    String sender = (String) row[0];
                    String subject = (String) row[1];
                    String code = (String) row[2];
                    text.append("+ ").append(sender).append(" - ").append(subject).append(" (").append(code).append(")\n");
                }
            }

            emailDisplay.setText(text.toString());
        } catch (Exception ex) {
            emailDisplay.setText("Failed to load sent emails: " + ex.getMessage());
        }

        emailDialog.setVisible(true);
    }

    private void showEmailByCode() {
        User user = AuthorizationService.getCurrentUser();
        if (user == null) return;

        // pop-up to ask the email code
        String code = JOptionPane.showInputDialog(mainFrame, "Enter the email code to read:", "Red Email by Code", JOptionPane.PLAIN_MESSAGE);

        if (code == null || code.trim().isEmpty()) {
            dispose();
            mainFrame.showDashboardPanel();
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EmailRecipient recipient = AuthorizationService.getEmailService().getRecipientDao()
                    .findByEmailCodeAndRecipientEmail(session, code.trim(), user.getEmail());

            if (recipient == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Email not found or you are not authorized to read.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Email email = recipient.getEmail();
            if (email == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Email not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JDialog emailDialog = new JDialog(mainFrame, "Email Details", true);
            emailDialog.setLayout(null);
            emailDialog.setSize(600, 700);
            emailDialog.setLocationRelativeTo(mainFrame);
            emailDialog.getContentPane().setBackground(new Color(200, 250, 180));

            JLabel title = new JLabel("Email Details");
            title.setFont(new Font("Arial", Font.BOLD, 22));
            title.setBounds(220, 30, 200, 30);
            emailDialog.add(title);

            JLabel codeLabel = new JLabel("Code:");
            codeLabel.setBounds(80, 80, 100, 25);
            emailDialog.add(codeLabel);

            JTextField codeFieldDisplay = new JTextField(email.getCode());
            codeFieldDisplay.setEditable(false);
            codeFieldDisplay.setBounds(180, 80, 340, 25);
            emailDialog.add(codeFieldDisplay);

            JLabel recipientsLabel = new JLabel("Recipient(s):");
            recipientsLabel.setBounds(80, 120, 100, 25);
            emailDialog.add(recipientsLabel);

            JTextField recipientsField = new JTextField();
            recipientsField.setEditable(false);
            recipientsField.setBounds(180, 120, 340, 25);
            emailDialog.add(recipientsField);

            JLabel subjectLabel = new JLabel("Subject:");
            subjectLabel.setBounds(80, 160, 100, 25);
            emailDialog.add(subjectLabel);

            JTextField subjectField = new JTextField(email.getSubject());
            subjectField.setEditable(false);
            subjectField.setBounds(180, 160, 340, 25);
            emailDialog.add(subjectField);

            JLabel dateLabel = new JLabel("Date:");
            dateLabel.setBounds(80, 200, 100, 25);
            emailDialog.add(dateLabel);

            JTextField dateField = new JTextField(email.getSentAt().toLocalDate().toString());
            dateField.setEditable(false);
            dateField.setBounds(180, 200, 340, 25);
            emailDialog.add(dateField);

            JLabel bodyLabel = new JLabel("Body:");
            bodyLabel.setBounds(80, 240, 100, 25);
            emailDialog.add(bodyLabel);

            JTextArea bodyArea = new JTextArea(email.getBody());
            bodyArea.setEditable(false);
            bodyArea.setLineWrap(true);
            bodyArea.setWrapStyleWord(true);
            bodyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
            bodyArea.setForeground(Color.BLACK);
            bodyArea.setBackground(Color.WHITE);
            bodyArea.setMargin(new Insets(10, 10, 10, 10));

            JScrollPane bodyScroll = new JScrollPane(bodyArea);
            bodyScroll.setBounds(180, 240, 340, 240);
            emailDialog.add(bodyScroll);

            JButton okBtn = new JButton("OK");
            okBtn.setBounds(240, 550, 120, 35);
            okBtn.setFocusable(false);
            emailDialog.add(okBtn);

            okBtn.addActionListener(e -> emailDialog.dispose());

            try {
                List<EmailRecipient> recipients = AuthorizationService.getEmailService().getRecipientDao()
                        .findByEmailCode(session, code);
                StringBuilder recipientList = new StringBuilder();
                for (int i = 0; i < recipients.size(); i++) {
                    recipientList.append(recipients.get(i).getRecipientEmail());
                    if (i < recipients.size() - 1) {
                        recipientList.append(", ");
                    }
                }
                recipientsField.setText(recipientList.toString());
            } catch (Exception ex) {
                recipientsField.setText("Error loading recipients");
            }

            // Mark as read
            if (!recipient.isRead()) {
                session.beginTransaction();
                recipient.setRead(true);
                session.update(recipient);
                session.getTransaction().commit();
            }

            emailDialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load email: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

}
