package milou.GUI;

import milou.Entity.User;
import milou.Service.AuthorizationService;
import milou.DataAccessObject.UserDao;
import milou.Util.HibernateUtil;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginPanel extends JPanel {

    private final JTextField emailField;
    private final JPasswordField passwordField;

    public LoginPanel(MainFrame mainFrame) {
        setLayout(null);
        setBackground(new Color(140, 250, 200));

        JLabel title = new JLabel("Login");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBounds(240, 30, 160, 40);
        add(title);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(120, 100, 80, 30);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(200, 100, 200, 30);
        add(emailField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(120, 150, 80, 30);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(200, 150, 200, 30);
        add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(200, 210, 90, 35);
        loginBtn.setFocusable(false);
        add(loginBtn);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(310, 210, 90, 35);
        backBtn.setFocusable(false);
        add(backBtn);

        // ===== Button Logic =====

        backBtn.addActionListener(e -> mainFrame.showWelcomePanel());

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                MessagePanel.showMessage(mainFrame, "Both fields are required.", false);
                return;
            }

            if (!email.contains("@milou.com")) {
                email = email.concat("@milou.com");
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                User user = AuthorizationService.getUserDao().findByEmailAndPassword(session, email, password);

                if (user != null) {
                    AuthorizationService.setCurrentUser(user);
                    AuthorizationService.setEmailService(user);

                    String firstName = user.getName().split(" ")[0];
                    MessagePanel.showMessage(mainFrame, "Welcome back, " + firstName + "!", true);

                    mainFrame.showDashboardPanel();
                } else {
                    MessagePanel.showMessage(mainFrame, "Invalid email or password.", false);
                }
            } catch (Exception ex) {
                MessagePanel.showMessage(mainFrame, "Login failed. Please try again.", false);
            }
        });

    }
}
