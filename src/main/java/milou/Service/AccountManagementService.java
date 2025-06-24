package milou.Service;

import milou.Entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Scanner;

public class AccountManagementService {
    private final User currentUser;

    public AccountManagementService(User currentUser) {
        this.currentUser = currentUser;
    }

    public void deleteAccount(Scanner scanner, Session session) {
        System.out.print("Are you sure you want to delete your account? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("no")) {
            System.out.println("Account deletion cancelled.");
            return;
        }

        Transaction tx = session.beginTransaction();

        try {
            session.createQuery("DELETE FROM EmailRecipient r WHERE r.recipientEmail = :email")
                    .setParameter("email", currentUser.getEmail())
                    .executeUpdate();

            session.createQuery("DELETE FROM Email e WHERE e.senderEmail = :email")
                    .setParameter("email", currentUser.getEmail())
                    .executeUpdate();

            session.delete(currentUser);

            tx.commit();
            System.out.println("Your account and all associated data have been deleted.");
        } catch (Exception e) {
            tx.rollback();
            System.out.println("Error while deleting account: " + e.getMessage());
        }
    }

    public void deleteReceivedEmails(Scanner scanner, Session session) {
        System.out.print("Enter the code of the email(s) you want to delete (comma-separated): ");
        String input = scanner.nextLine().trim().toUpperCase();
        String[] codes = input.split(",");

        Transaction tx = session.beginTransaction();

        try {
            for (String code : codes) {
                int deleted = session.createQuery("""
                            DELETE FROM EmailRecipient r
                            WHERE r.emailCode = :code AND r.recipientEmail = :email
                        """)
                        .setParameter("code", code.trim())
                        .setParameter("email", currentUser.getEmail())
                        .executeUpdate();

                if (deleted > 0) {
                    System.out.println("Email '" + code + "' deleted from your inbox.");
                } else {
                    System.out.println("Email '" + code + "' not found or you do not have access.");
                }
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println("Error while deleting emails: " + e.getMessage());
        }
    }
}
