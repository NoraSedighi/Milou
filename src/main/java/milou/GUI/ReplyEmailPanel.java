package milou.GUI;

import milou.Entity.Email;
import milou.Entity.EmailRecipient;
import milou.Entity.User;
import milou.Service.AuthorizationService;
import milou.Util.CodeGenerator;
import milou.Util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class ReplyEmailPanel extends JDialog {
    private final MainFrame mainFrame;

    public ReplyEmailPanel(MainFrame mainFrame) {
        super(mainFrame, "Reply to Email", true);
        this.mainFrame = mainFrame;
        setLayout(null);
        setSize(600, 700);
        setLocationRelativeTo(mainFrame);
        getContentPane().setBackground(new Color(200, 250, 180));

        String code = JOptionPane.showInputDialog(mainFrame, "Enter the email code to reply:", "Reply Code", JOptionPane.PLAIN_MESSAGE);

        if (code == null || code.trim().isEmpty()) {
            dispose();
            mainFrame.showDashboardPanel();
            return;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            EmailRecipient recipient = AuthorizationService.getEmailService().getRecipientDao()
                    .findByEmailCodeAndRecipientEmail(session, code.trim(), AuthorizationService.getCurrentUser().getEmail());

            if (recipient == null) {
                JOptionPane.showMessageDialog(mainFrame, "Email not found or you are not authorized to reply.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                mainFrame.showDashboardPanel();
                return;
            }

            Email originalEmail = recipient.getEmail();
            if (originalEmail == null) {
                JOptionPane.showMessageDialog(mainFrame, "Original email not found.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                mainFrame.showDashboardPanel();
                return;
            }

            buildReplyPanel(originalEmail, code.trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "Error checking email: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            mainFrame.showDashboardPanel();
        }
    }

    private void buildReplyPanel(Email originalEmail, String code) {
        JLabel title = new JLabel("Reply to Email");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(220, 30, 200, 30);
        add(title);

        JLabel codeLabel = new JLabel("Code:");
        codeLabel.setBounds(80, 80, 100, 25);
        add(codeLabel);

        JTextField codeField = new JTextField(code);
        codeField.setEditable(false);
        codeField.setBounds(180, 80, 340, 25);
        add(codeField);

        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setBounds(80, 120, 100, 25);
        add(subjectLabel);

        JTextField subjectField = new JTextField("[Re] " + originalEmail.getSubject());
        subjectField.setEditable(false);
        subjectField.setBounds(180, 120, 340, 25);
        add(subjectField);

        JLabel recipientsLabel = new JLabel("Recipient(s):");
        recipientsLabel.setBounds(80, 160, 100, 25);
        add(recipientsLabel);

        JTextField recipientsField = new JTextField(getRecipientsString(originalEmail.getCode()));
        recipientsField.setEditable(false);
        recipientsField.setBounds(180, 160, 340, 25);
        add(recipientsField);

        JLabel bodyLabel = new JLabel("Body:");
        bodyLabel.setBounds(80, 200, 100, 25);
        add(bodyLabel);

        JTextArea bodyArea = new JTextArea();
        bodyArea.setLineWrap(true);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setBounds(180, 200, 340, 300);
        add(bodyScroll);

        JButton sendBtn = new JButton("Send Reply");
        sendBtn.setBounds(180, 550, 120, 35);
        sendBtn.setFocusable(false);
        add(sendBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBounds(310, 550, 120, 35);
        cancelBtn.setFocusable(false);
        add(cancelBtn);

        // ===== Button Logic =====

        cancelBtn.addActionListener(e -> {
            dispose();
            mainFrame.showDashboardPanel();
        });

        sendBtn.addActionListener(e -> {
            String replyBody = bodyArea.getText().trim();
            if (replyBody.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Reply body cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    User currentUser = AuthorizationService.getCurrentUser();
                    String replyCode = CodeGenerator.generateCode();
                    String replySubject = "[Re] " + originalEmail.getSubject();

                    Email replyEmail = new Email(replyCode, currentUser.getEmail(), replySubject, replyBody, LocalDateTime.now());
                    session.persist(replyEmail);

                    List<EmailRecipient> originalRecipients = AuthorizationService.getEmailService().getRecipientDao().findByEmailCode(session, code);

                    for (EmailRecipient r : originalRecipients) {
                        String recipientEmail = r.getRecipientEmail();
                        if (!recipientEmail.equals(currentUser.getEmail())) {
                            User recipient = session
                                    .createQuery("FROM User WHERE email = :email", User.class)
                                    .setParameter("email", recipientEmail)
                                    .uniqueResult();
                            if (recipient != null) {
                                EmailRecipient replyRecipient = new EmailRecipient(replyEmail, recipient);
                                session.persist(replyRecipient);
                            }
                        }
                    }

                    String originalSender = originalEmail.getSenderEmail();
                    if (!originalSender.equals(currentUser.getEmail())) {
                        User senderUser = session
                                .createQuery("FROM User WHERE email = :email", User.class)
                                .setParameter("email", originalSender)
                                .uniqueResult();
                        if (senderUser != null) {
                            EmailRecipient replyToSender = new EmailRecipient(replyEmail, senderUser);
                            session.persist(replyToSender);
                        }
                    }

                    tx.commit();

                    JOptionPane.showMessageDialog(
                            this,
                            "Successfully sent your reply to email " + code +
                                    ".\nCode: " + replyCode,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    dispose();
                    mainFrame.showDashboardPanel();
                } catch (Exception ex) {
                    tx.rollback();
                    JOptionPane.showMessageDialog(this, "Failed to send reply: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private String getRecipientsString(String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<EmailRecipient> recipients = AuthorizationService.getEmailService().getRecipientDao().findByEmailCode(session, code);
            StringBuilder recipientList = new StringBuilder();
            for (int i = 0; i < recipients.size(); i++) {
                recipientList.append(recipients.get(i).getRecipientEmail());
                if (i < recipients.size() - 1) {
                    recipientList.append(", ");
                }
            }
            return recipientList.toString();
        } catch (Exception ex) {
            return "Error loading recipients";
        }
    }
}