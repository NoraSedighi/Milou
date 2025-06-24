package milou;

import java.util.Scanner;

import java.util.logging.LogManager;

import milou.Util.HibernateUtil;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static milou.Service.AuthorizationService.login;
import static milou.Service.AuthorizationService.signUp;

public class Main {
    public static void main(String[] args) {
        //to clean output
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("[L]ogin, [S]ign up, [Q]uit: ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("s") || input.equals("sign up")) {
                signUp(scanner);
            } else if (input.equals("l") || input.equals("login")) {
                login(scanner);
            } else if (input.equals("q") || input.equals("quit")) {
                System.out.println("Goodbye...");
                break;
            } else {
                System.out.println("Invalid option");
            }
        }

        scanner.close();
        HibernateUtil.getSessionFactory().close();
    }
}