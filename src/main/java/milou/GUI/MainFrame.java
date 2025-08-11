package milou.GUI;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private static final Color SKY_BLUE = new Color(135, 206, 235);
    private SignUpPanel signUpPanel;
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private SendEmailPanel sendEmailPanel;
    private ViewEmailPanel viewEmailPanel;

    public MainFrame() {
        setTitle("--Milou--");
        setSize(600, 700);
        setLocationRelativeTo(null);   // to center your window (JFrame) on the screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(SKY_BLUE);
        setLayout(null);

        ImageIcon rawIcon = new ImageIcon(getClass().getResource("/pictures/cross.png"));
        Image scaledImage = rawIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon closeIcon = new ImageIcon(scaledImage);

        JButton quitBtn = new JButton(closeIcon);
        quitBtn.setBounds(545, 0, 40, 30);
        quitBtn.setFocusable(false);
        quitBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to quit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        add(quitBtn);

        JLabel welcomeLabel = new JLabel("WELCOME!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 26));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBounds(200, 50, 200, 40);
        add(welcomeLabel);

        ImageIcon milouImg = new ImageIcon(getClass().getResource("/pictures/milou.png"));
        Image scaledMilou = milouImg.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
        ImageIcon milou = new ImageIcon(scaledMilou);

        JLabel milouLabel = new JLabel(milou);
        milouLabel.setBounds(110, 90, 350, 300);
        add(milouLabel);

        JButton signUpBtn = new JButton("Sign Up");
        signUpBtn.setBounds(200, 400, 180, 70);
        signUpBtn.setFocusable(false);
        add(signUpBtn);
        signUpBtn.addActionListener(e -> showSignUpPanel());

        signUpPanel = new SignUpPanel(this);
        signUpPanel.setBounds(0, 0, 600, 700);
        signUpPanel.setVisible(false);
        add(signUpPanel);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(200, 500, 180, 70);
        loginBtn.setFocusable(false);
        add(loginBtn);
        loginBtn.addActionListener(e -> showLoginPanel());

        loginPanel = new LoginPanel(this);
        loginPanel.setBounds(0, 0, 600, 700);
        loginPanel.setVisible(false);
        add(loginPanel);

        sendEmailPanel = new SendEmailPanel(this);
        sendEmailPanel.setBounds(0, 0, 600, 700);
        sendEmailPanel.setVisible(false);
        add(sendEmailPanel);

        setVisible(true);
    }

    public void showSignUpPanel() {
        hideAllPanels();
        signUpPanel.setVisible(true);
    }

    public void showLoginPanel() {
        hideAllPanels();
        loginPanel.setVisible(true);
    }

    public void showWelcomePanel() {
        hideAllPanels();
        for (Component comp : getContentPane().getComponents()) {
            if (!(comp instanceof JPanel)) {
                comp.setVisible(true);
            }
        }
    }

    private void hideAllPanels() {
        for (Component comp : getContentPane().getComponents()) {
            comp.setVisible(false);
        }
    }

    public void showDashboardPanel() {
        hideAllPanels();

        if (dashboardPanel == null) {
            dashboardPanel = new DashboardPanel(this);
            dashboardPanel.setBounds(0, 0, 600, 700);
            add(dashboardPanel);
        }

        dashboardPanel.setVisible(true);
        dashboardPanel.showUnreadEmails();
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public void showSendEmailPanel() {
        hideAllPanels();
        sendEmailPanel.setVisible(true);
    }

    public void showReplyEmailPanel() {
        ReplyEmailPanel replyEmailPanel = new ReplyEmailPanel(this);
        replyEmailPanel.setVisible(true);
    }

    public void showForwardEmailPanel() {
        ForwardEmailPanel forwardEmailPanel = new ForwardEmailPanel(this);
        forwardEmailPanel.setVisible(true);
    }
}
