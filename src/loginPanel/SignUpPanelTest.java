package loginPanel;

import javax.swing.*;
import java.awt.*;

public class SignUpPanelTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Customer Sign-Up");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(800, 600);
//            frame.setLocationRelativeTo(null);
//            frame.setLayout(new BorderLayout());
//            frame.getContentPane().setBackground(new Color(5, 5, 6));

            // Example callback to simulate switching to login panel
            Runnable loginCallback = ()-> new LoginPage();
//            SignUpPanel signUpPanel = new SignUpPanel(loginCallback);
//            frame.add(signUpPanel, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}
