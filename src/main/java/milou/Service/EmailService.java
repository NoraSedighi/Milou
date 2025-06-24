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
        System.out.println("Enter the code of the email you want to reply to:");
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

        System.out.println("Replying to: " + originalEmail.getSenderEmail());
        System.out.println("Original Email Subject: " + originalEmail.getSubject());
        System.out.println("Body: ");
        String body = scanner.nextLine();

        String replySubject = "RE: " + originalEmail.getSubject();
        String codeReply = CodeGenerator.generateCode();

        Transaction tx = session.beginTransaction();
        try {
            Email replyEmail = new Email(codeReply, currentUser.getEmail(), replySubject, body, LocalDateTime.now());
            emailDao.save(session, replyEmail);

            User recipientUser = userDao.findByEmail(session, originalEmail.getSenderEmail());
            if (recipientUser != null) {
                EmailRecipient replyRecipient = new EmailRecipient(replyEmail, recipientUser);
                recipientDao.save(session, replyRecipient);
            }

            tx.commit();
            System.out.println("Reply sent successfully with code: " + codeReply);
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
                ORDER BY e.sentAt DESC
            """, Object[].class).setParameter("email", currentUser.getEmail()).list();

        System.out.println("All emails: ");
        for (Object[] row : emails) {
            System.out.println("+" + row[0] + "-" + row[1] + "(" + row[2] + ")");
        }
    }

    private void showUnreadEmails(Session session) {
        List<Object[]> results = session.createQuery("""
                SELECT e.senderEmail, e.subject, e.code
                FROM EmailRecipient r
                JOIN Email e ON r.emailCode = e.code
                WHERE r.recipientEmail = :email AND r.isRead = false
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
        List<Email> sentEmails = session.createQuery("FROM Email WHERE senderEmail = :email ORDER BY sentAt DESC", Email.class)
                .setParameter("email", currentUser.getEmail())
                .list();

        if (sentEmails.isEmpty()) {
            System.out.println("You have no sent emails.");
        } else {
            System.out.println("Sent Emails:");
            for (Email email : sentEmails) {
                System.out.println("+" + email.getSenderEmail() + " - " + email.getSubject() + " (" + email.getCode() + ")");
            }
        }
    }

    public void readEmailByCode(Session session, Scanner scanner) {
        System.out.println("Enter email code: ");
        String code = scanner.nextLine().trim();
        String currentEmail = currentUser.getEmail();

        EmailRecipient recipient = recipientDao.findByEmailCodeAndRecipientEmail(session, code, currentEmail);

        if (recipient != null) {
            Email email = recipient.getEmail();

            System.out.println("From: " + email.getSenderEmail());
            System.out.println("Subject: " + email.getSubject());
            System.out.println("Body:\n" + email.getBody());

            if (!recipient.isRead()) {
                Transaction tx = session.beginTransaction();
                recipient.setRead(true);
                session.update(recipient);
                tx.commit();
            }
        } else {
            System.out.println("Email not found or you are not authorized to view it.");
        }
    }

}
