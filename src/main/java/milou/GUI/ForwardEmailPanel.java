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

public class ForwardEmailPanel extends JDialog {
    private final MainFrame mainFrame;

    public ForwardEmailPanel(MainFrame mainFrame) {
        super(mainFrame, "Forward Email", true);
        this.mainFrame = mainFrame;
        setLayout(null);
        setSize(600, 700);
        setLocationRelativeTo(mainFrame);
        getContentPane().setBackground(new Color(200, 250, 180));

        buildForwardPanel();
    }

    private void buildForwardPanel() {
        JLabel title = new JLabel("Forward Email");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(220, 30, 200, 30);
        add(title);

        JLabel codeLabel = new JLabel("Code:");
        codeLabel.setBounds(80, 80, 100, 25);
        add(codeLabel);

        JTextField codeField = new JTextField();
        codeField.setBounds(180, 80, 340, 25);
        add(codeField);

        JLabel recipientsLabel = new JLabel("Recipient(s):");
        recipientsLabel.setBounds(80, 120, 100, 25);
        add(recipientsLabel);

        JTextField recipientsField = new JTextField();
        recipientsField.setBounds(180, 120, 340, 25);
        add(recipientsField);

        JButton sendBtn = new JButton("Send");
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
            String code = codeField.getText().trim();
            String recipients = recipientsField.getText().trim();

            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email code cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (recipients.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Recipient(s) cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                EmailRecipient recipient = AuthorizationService.getEmailService().getRecipientDao()
                        .findByEmailCodeAndRecipientEmail(session, code, AuthorizationService.getCurrentUser().getEmail());

                if (recipient == null) {
                    JOptionPane.showMessageDialog(this, "Email not found or you are not authorized to forward.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Email originalEmail = recipient.getEmail();
                if (originalEmail == null) {
                    JOptionPane.showMessageDialog(this, "Original email not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Transaction tx = session.beginTransaction();
                try {
                    User currentUser = AuthorizationService.getCurrentUser();
                    String forwardCode = CodeGenerator.generateCode();
                    String forwardSubject = "[Fw] " + originalEmail.getSubject();
                    String forwardBody = originalEmail.getBody();

                    Email forwardEmail = new Email(forwardCode, currentUser.getEmail(), forwardSubject, forwardBody, LocalDateTime.now());
                    session.persist(forwardEmail);

                    for (String r : recipients.split(",")) {
                        String emailAddress = r.trim();
                        if (!emailAddress.contains("@milou.com")) {
                            emailAddress = emailAddress.concat("@milou.com");
                        }

                        User recipientUser = session
                                .createQuery("FROM User WHERE email = :email", User.class)
                                .setParameter("email", emailAddress)
                                .uniqueResult();

                        if (recipientUser != null) {
                            EmailRecipient forwardRecipient = new EmailRecipient(forwardEmail, recipientUser);
                            session.persist(forwardRecipient);
                        } else {
                            System.out.println("User not found: " + emailAddress);
                        }
                    }

                    tx.commit();

                    JOptionPane.showMessageDialog(
                            this,
                            "Successfully forwarded your email.\nCode: " + forwardCode,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    dispose();
                    mainFrame.showDashboardPanel();
                } catch (Exception ex) {
                    tx.rollback();
                    JOptionPane.showMessageDialog(this, "Failed to forward email: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error checking email: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}