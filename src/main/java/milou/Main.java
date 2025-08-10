package milou;

import javax.swing.SwingUtilities;
import milou.GUI.MainFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}