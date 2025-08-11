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

public class SendEmailPanel extends JPanel {
    public SendEmailPanel(MainFrame mainFrame) {
        setLayout(null);
        setBackground(new Color(200, 250, 180));

        JLabel title = new JLabel("Send Email");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(220, 30, 200, 30);
        add(title);

        JLabel recipientLabel = new JLabel("Recipient(s):");
        recipientLabel.setBounds(80, 80, 100, 25);
        add(recipientLabel);

        JTextField recipientField = new JTextField();
        recipientField.setBounds(180, 80, 300, 25);
        add(recipientField);

        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setBounds(80, 120, 100, 25);
        add(subjectLabel);

        JTextField subjectField = new JTextField();
        subjectField.setBounds(180, 120, 300, 25);
        add(subjectField);

        JLabel bodyLabel = new JLabel("Body:");
        bodyLabel.setBounds(80, 160, 100, 25);
        add(bodyLabel);

        JTextArea bodyArea = new JTextArea();
        bodyArea.setLineWrap(true);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setBounds(180, 160, 300, 200);
        add(bodyScroll);

        JButton sendBtn = new JButton("Send");
        sendBtn.setBounds(200, 400, 100, 35);
        sendBtn.setFocusable(false);
        add(sendBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBounds(310, 400, 100, 35);
        cancelBtn.setFocusable(false);
        add(cancelBtn);

        // ===== Button Logic =====

        cancelBtn.addActionListener(e -> mainFrame.showDashboardPanel());

        sendBtn.addActionListener(e -> {
            String recipients = recipientField.getText().trim();
            String subject = subjectField.getText().trim();
            String body = bodyArea.getText().trim();

            if (recipients.isEmpty() || subject.isEmpty() || body.isEmpty()) {
                MessagePanel.showMessage(mainFrame, "All fields are required.", false);
                return;
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                try {
                    User currentUser = AuthorizationService.getCurrentUser();
                    String code = CodeGenerator.generateCode();

                    Email email = new Email(code, currentUser.getEmail(), subject, body, LocalDateTime.now());
                    session.persist(email);

                    for (String r : recipients.split(",")) {
                        String emailAddress = r.trim();
                        if (!emailAddress.contains("@milou.com")) {
                            emailAddress = emailAddress.concat("@milou.com");
                        }

                        User recipient = session
                                .createQuery("FROM User WHERE email = :email", User.class)
                                .setParameter("email", emailAddress)
                                .uniqueResult();

                        if (recipient != null) {
                            EmailRecipient emailRecipient = new EmailRecipient(email, recipient);
                            session.persist(emailRecipient);
                        } else {
                            System.out.println("User not found: " + emailAddress);
                        }
                    }

                    tx.commit();

                    JOptionPane.showMessageDialog(
                            this,
                            "Successfully sent your email.\nCode: " + code,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    mainFrame.showDashboardPanel();
                } catch (Exception ex) {
                    tx.rollback();
                    MessagePanel.showMessage(mainFrame, "Failed to send email.", false);
                }
            }
        });
    }
}
