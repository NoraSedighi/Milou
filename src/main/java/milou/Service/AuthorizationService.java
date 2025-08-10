package milou.Service;

import milou.DataAccessObject.UserDao;
import milou.Entity.User;
import milou.Util.HibernateUtil;
import org.hibernate.Session;

import javax.sound.midi.SysexMessage;
import java.util.List;
import java.util.Scanner;

public class AuthorizationService {
    private static final UserDao userDao = new UserDao();
    private static EmailService emailService;
    private static User currentUser;
    private static AccountManagementService accountService;

    public static void signUp(Scanner scanner) {
        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        if (!email.contains("@milou.com")) {
            email = email.concat("@milou.com");
        }

        String password;
        while (true) {
            System.out.print("Password: ");
            password = scanner.nextLine();

            if (password.length() >= 8) {
                break;
            } else {
                System.out.println("Password must at least contain 8 characters");
                System.out.println("Please try again");
            }
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            if (userDao.findByEmail(session, email) != null) {
                System.out.println("An account with this email already exists");
                session.getTransaction().rollback();
                return;
            }

            User newUser = new User(name, email, password);
            userDao.save(session, newUser);
            session.getTransaction().commit();
            System.out.println("\nYour new account is created.\nGo ahead and login!");
        } catch (Exception e) {
            System.out.println("Error while saving user: " + e.getMessage());
        }

        // Menu after sign-up
        while (true) {
            System.out.print("[L]ogin, [S]ign up: ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "s", "sign up" -> signUp(scanner);
                case "l", "login" -> login(scanner);
                default -> System.out.println("Invalid option! Please try again.");
            }
        }
    }


    public static void login(Scanner scanner) {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        if (!email.contains("@milou.com")) {
            email = email.concat("@milou.com");
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = userDao.findByEmailAndPassword(session, email, password);

            if (user != null) {
                currentUser = user;
                emailService = new EmailService(currentUser);
                accountService = new AccountManagementService(currentUser);
                String namePart = currentUser.getName().split(" ")[0];
                System.out.println("\nWelcome back, " + namePart + "!\n");

                showUnreadEmails(session);
                handleEmailOperations(scanner, session);
            } else {
                System.out.println("Invalid email or password");
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

    private static void showUnreadEmails(Session session) {
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

    private static void handleEmailOperations(Scanner scanner, Session session) {
        System.out.print("[S]end, [V]iew, [R]eply, [F]orward, [D]elete Account, [X]Delete Email, [L]ogout: ");
        String input = scanner.nextLine().trim().toLowerCase();

        switch (input) {
            case "s", "send" -> emailService.sendEmail(session, scanner);
            case "v", "view" -> emailService.viewEmails(session, scanner);
            case "r", "reply" -> emailService.replyEmails(session, scanner);
            case "f", "forward" -> emailService.forwardEmails(session, scanner);
            case "d", "delete account" -> accountService.deleteAccount(scanner, session);
            case "x", "delete email" -> accountService.deleteReceivedEmails(scanner, session);
            case "l", "logout" -> {
                System.out.println("Logging out...");
                currentUser = null;
                emailService = null;
            }
            default -> System.out.println("Invalid option! Please try again");
        }
    }

    public static boolean register(String name, String email, String password) {
        if (!email.contains("@milou.com")) {
            email = email.concat("@milou.com");
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            // Email already exists
            if (userDao.findByEmail(session, email) != null) {
                session.getTransaction().rollback();
                return false;
            }

            User newUser = new User(name, email, password);
            userDao.save(session, newUser);
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            System.out.println("An error occurred during registration.");
            return false;
        }
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void setEmailService(User user) {
        emailService = new EmailService(user);
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static EmailService getEmailService() {
        return emailService;
    }

    public static UserDao getUserDao() {
        return userDao;
    }

}
