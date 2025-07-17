package milou.Service;

import milou.DataAccessObject.EmailDao;
import milou.DataAccessObject.EmailRecipientDao;
import milou.DataAccessObject.UserDao;
import milou.Entity.Email;
import milou.Entity.EmailRecipient;
import milou.Entity.User;
import milou.Util.CodeGenerator;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class EmailService {

    private final EmailDao emailDao = new EmailDao();
    private final EmailRecipientDao recipientDao = new EmailRecipientDao();
    private final UserDao userDao = new UserDao();
    private final User currentUser;

    public EmailService(User currentUser) {
        this.currentUser = currentUser;
    }

    public void sendEmail(Session session, Scanner scanner) {
        System.out.println("Recipient(s): ");
        String[] recipients = scanner.nextLine().trim().split(",");

        System.out.println("Subject: ");
        String subject = scanner.nextLine();

        System.out.println("Body: ");
        String body = scanner.nextLine();

        String code = CodeGenerator.generateCode();

        Transaction tx = session.beginTransaction();
        try {
            Email email = new Email(code, currentUser.getEmail(), subject, body, LocalDateTime.now());
            emailDao.save(session, email);

            for (String r : recipients) {
                String emailAddrress = r.trim();
                if (!emailAddrress.contains("@milou.com")) {
                    emailAddrress = emailAddrress.concat("@milou.com");
                }

                User recipientUser = userDao.findByEmail(session, emailAddrress);

                if (recipientUser != null) {
                    EmailRecipient recipient = new EmailRecipient(email, recipientUser);
                    recipientDao.save(session, recipient);
                } else {
                    System.out.println("User not found: " + emailAddrress);
                }
            }

            tx.commit();
            System.out.println("Email sent successfully with code: " + code);
        } catch (Exception e) {
            tx.rollback();
            System.out.println("Error while sending email: " + e.getMessage());
        }
    }

    public void viewEmails(Session session, Scanner scanner) {
        System.out.println("[A]ll emails, [U]nread emails, [S]ent emails, Read by [C]ode: ");
        String input = scanner.nextLine().trim().toLowerCase();

        switch (input) {
            case "a", "all emails" -> showAllEmails(session);
            case "u", "unread emails" -> showUnreadEmails(session);
            case "s", "send emails" -> showSentEmails(session);
            case "c", "read by code" -> readEmailByCode(session, scanner);
        }
    }

    public void replyEmails(Session session, Scanner scanner) {
        System.out.println("Code: ");
        String code = scanner.nextLine().trim();

        EmailRecipient recipient = recipientDao.findByEmailCodeAndRecipientEmail(session, code, currentUser.getEmail());
        if (recipient == null) {
            System.out.println("Email not found or you are not authorized to reply.");
            return;
        }

        Email originalEmail = recipient.getEmail();
        if (originalEmail == null) {
            System.out.println("Original email not found.");
            return;
        }

        System.out.println("Body: ");
        String replyBody = scanner.nextLine();

        String replySubject = "[Re] " + originalEmail.getSubject();
        String replyCode = CodeGenerator.generateCode();

        Transaction tx = session.beginTransaction();
        try {
            Email replyEmail = new Email(replyCode, currentUser.getEmail(), replySubject, replyBody, LocalDateTime.now());
            emailDao.save(session, replyEmail);

            List<EmailRecipient> originalRecipients = recipientDao.findByEmailCode(session, code);

            for (EmailRecipient r : originalRecipients) {
                String recipientEmail = r.getRecipientEmail();
                if (!recipientEmail.equals(currentUser.getEmail())) {
                    User user = userDao.findByEmail(session, recipientEmail);
                    if (user != null) {
                        EmailRecipient replyRecipient = new EmailRecipient(replyEmail, user);
                        recipientDao.save(session, replyRecipient);
                    }
                }
            }

            String originalSender = originalEmail.getSenderEmail();
            if (!originalSender.equals(currentUser.getEmail())) {
                User senderUser = userDao.findByEmail(session, originalSender);
                if (senderUser != null) {
                    EmailRecipient replyToSender = new EmailRecipient(replyEmail, senderUser);
                    recipientDao.save(session, replyToSender);
                }
            }

            tx.commit();
            System.out.println("Successfully sent your reply to email " + code + ".");
            System.out.println("Code: " + replyCode);
        } catch (Exception e) {
            tx.rollback();
            System.out.println("Error while sending reply: " + e.getMessage());
        }
    }

    public void forwardEmails(Session session, Scanner scanner) {
        System.out.println("Enter the code of the email you want to forward: ");
        String code = scanner.nextLine().trim();

        EmailRecipient recipient = recipientDao.findByEmailCodeAndRecipientEmail(session, code, currentUser.getEmail());

        if (recipient == null) {
            System.out.println("Email not found or you don't have access.");
            return;
        }

        Email originalEmail = recipient.getEmail();

        if (originalEmail == null) {
            System.out.println("Original email not found");
            return;
        }

        System.out.println("Enter recipient(s) to forward to: ");
        String input = scanner.nextLine().trim();
        String[] forwardRecipients = input.split(",");

        System.out.println("Enter additional message (optional): ");
        String additionalBody = scanner.nextLine();
        String forwardBody;
        if (additionalBody.isEmpty()) {
            forwardBody = originalEmail.getBody();
        } else {
            forwardBody = additionalBody + "\n\n--- Forwarded message ---\n" + originalEmail.getBody();
        }

        String forwardSubject = "FWD: " + originalEmail.getSubject();
        String forwardCode = CodeGenerator.generateCode();

        Transaction tx = session.beginTransaction();
        try {
            Email forwardEmail = new Email(forwardCode, currentUser.getEmail(), forwardSubject, forwardBody, LocalDateTime.now());
            emailDao.save(session, forwardEmail);

            for (String r : forwardRecipients) {
                String emailAddrress = r.trim();
                if (!emailAddrress.contains("@milou.com")) {
                    emailAddrress = emailAddrress.concat("@milou.com");
                }

                User recipientUser = userDao.findByEmail(session, emailAddrress);
                if (recipientUser != null) {
                    EmailRecipient forwardRecipient = new EmailRecipient(forwardEmail, recipientUser);
                    recipientDao.save(session, forwardRecipient);
                } else {
                    System.out.println("User not found: " + emailAddrress);
                }
            }

            tx.commit();
            System.out.println("Email forwarded successfully with code: " + forwardCode);
        } catch (Exception e) {
            tx.rollback();
            System.out.println("Error while forwarding email: " + e.getMessage());
        }
    }

    private void showAllEmails(Session session) {
        var emails = session.createQuery("""
            SELECT e.senderEmail, e.subject, e.code
            FROM EmailRecipient r
            JOIN Email e ON r.emailCode = e.code
            WHERE r.recipientEmail = :email
            ORDER BY e.sentAt ASC
        """, Object[].class)
                .setParameter("email", currentUser.getEmail()).list();

        System.out.println("All emails: ");
        for (Object[] row : emails) {
            System.out.println("+ " + row[0] + " - " + row[1] + " (" + row[2] + ")");
        }
    }

    private void showUnreadEmails(Session session) {
        List<Object[]> results = session.createQuery("""
                SELECT e.senderEmail, e.subject, e.code
                FROM EmailRecipient r
                JOIN Email e ON r.emailCode = e.code
                WHERE r.recipientEmail = :email AND r.isRead = false
                ORDER BY e.sentAT ASC
                """, Object[].class)
                .setParameter("email", currentUser.getEmail())
                .list();

        if (results.isEmpty()) {
            System.out.println("You have no unread emails.\n");
        } else {
            System.out.println("Unread Emails:\n");
            System.out.println(results.size() + " unread emails:");
            for (Object[] row : results) {
                String sender = (String) row[0];
                String subject = (String) row[1];
                String code = (String) row[2];
                System.out.println("+ " + sender + " - " + subject + " (" + code + ")");
            }
            System.out.println();
        }
    }

    private void showSentEmails(Session session) {
        List<Email> sentEmails = session
                .createQuery("FROM Email WHERE senderEmail = :email ORDER BY sentAt ASC", Email.class)
                .setParameter("email", currentUser.getEmail())
                .list();

        if (sentEmails.isEmpty()) {
            System.out.println("You have no sent emails.");
        } else {
            System.out.println("Sent Emails:");
            for (Email email : sentEmails) {
                System.out.println("+ " + email.getSenderEmail() + " - " + email.getSubject() + " (" + email.getCode() + ")");
            }
        }
    }

    private void readEmailByCode(Session session, Scanner scanner) {
        System.out.println("Enter email code: ");
        String code = scanner.nextLine().trim();
        String currentEmail = currentUser.getEmail();

        Email email = emailDao.findByCode(session, code);
        if (email == null) {
            System.out.println("Email not found");
            return;
        }

        List<EmailRecipient> recipients = recipientDao.findByEmailCode(session, code);

        boolean isRecipient = false;
        for (EmailRecipient r : recipients) {
            if (r.getRecipientEmail().equals(currentEmail)) {
                isRecipient = true;
                break;
            }
        }

        boolean isSender = email.getSenderEmail().equals(currentEmail);

        if (!isSender && !isRecipient) {
            System.out.println("You cannot read this email.");
            return;
        }

        System.out.println("Code: " + email.getCode());

        StringBuilder recipientList = new StringBuilder();
        for (int i = 0; i < recipients.size(); i++) {
            recipientList.append(recipients.get(i).getRecipientEmail());
            if (i < recipients.size() - 1) {
                recipientList.append(", ");
            }
        }

        System.out.println("Recipient(s): " + recipientList);
        System.out.println("Subject: " + email.getSubject());
        System.out.println("Date: " + email.getSentAt().toLocalDate());
        System.out.println();
        System.out.println(email.getBody());

        for (EmailRecipient r : recipients) {
            if (r.getRecipientEmail().equals(currentEmail) && !r.isRead()) {
                Transaction tx = session.beginTransaction();
                r.setRead(true);
                session.update(r);
                tx.commit();
                break;
            }
        }
    }
}
