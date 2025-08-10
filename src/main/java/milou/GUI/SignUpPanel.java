package milou.GUI;

import milou.Service.AuthorizationService;

import javax.swing.*;
import java.awt.*;

public class SignUpPanel extends JPanel {

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;

    public SignUpPanel(MainFrame mainFrame) {
        setLayout(null);
        setBackground(new Color(140, 250, 200));

        JLabel title = new JLabel("Sign Up");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBounds(220, 30, 160, 40);
        add(title);

        JLabel nameLabel = new JLabel("Name: ");
        nameLabel.setBounds(120, 100, 80, 30);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(200, 100, 200, 30);
        add(nameField);

        JLabel emailLabel = new JLabel("Email: ");
        emailLabel.setBounds(120, 150, 80, 30);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(200, 150, 200, 30);
        add(emailField);

        JLabel passwordLabel = new JLabel("Password: ");
        passwordLabel.setBounds(120, 200, 80, 30);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(200, 200, 200, 30);
        add(passwordField);

        JButton submitBtn = new JButton("Submit");
        submitBtn.setBounds(200, 260, 90, 35);
        submitBtn.setFocusable(false);
        add(submitBtn);

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(310, 260, 90, 35);
        backBtn.setFocusable(false);
        add(backBtn);

        // ===== Button Logic =====

        backBtn.addActionListener(e -> {
            mainFrame.showWelcomePanel();
        });

        submitBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());


            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                MessagePanel.showMessage(mainFrame, "All fields are required.", false);
                return;
            }

            if (password.length() < 8) {
                MessagePanel.showMessage(mainFrame, "Password must be at least 8 characters long.", false);
                return;
            }

            boolean registered = AuthorizationService.register(name, email, password);

            if (registered) {
                MessagePanel.showMessage(mainFrame, "Your new account is created. \nGo ahead and login!", true);
                mainFrame.showWelcomePanel();
            } else {
                MessagePanel.showMessage(mainFrame, "An account with this email already exists or error occurred", false);
            }

            mainFrame.showWelcomePanel();
            });
        }
    }

