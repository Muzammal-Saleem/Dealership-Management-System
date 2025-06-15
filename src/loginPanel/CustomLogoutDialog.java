package loginPanel;

import javax.swing.*;
import java.awt.*;

public class CustomLogoutDialog extends JDialog {
    private boolean confirmed = false;

    public CustomLogoutDialog(JFrame parent) {
        super(parent, "Confirm Logout", true);
        setLayout(new BorderLayout());

        // Message
        JLabel message = new JLabel("Are you sure you want to logout?");
        message.setForeground(new Color(0, 255, 255)); // Cyan
        message.setFont(new Font("Segoe UI", Font.BOLD, 16));
        message.setHorizontalAlignment(SwingConstants.CENTER);
        message.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(message, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(30, 30, 30));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton yesButton = new JButton("Yes");
        yesButton.setBackground(new Color(0, 123, 255)); // Bright blue
        yesButton.setForeground(Color.WHITE);
        yesButton.setFocusPainted(false);
        yesButton.setPreferredSize(new Dimension(100, 35));
        yesButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yesButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JButton noButton = new JButton("No");
        noButton.setBackground(new Color(220, 53, 69)); // Red
        noButton.setForeground(Color.WHITE);
        noButton.setFocusPainted(false);
        noButton.setPreferredSize(new Dimension(100, 35));
        noButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noButton.addActionListener(e -> dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().setBackground(new Color(30, 30, 30));
        setSize(400, 160);
        setLocationRelativeTo(parent);
    }

    public static boolean showLogoutDialog(JFrame parent) {
        CustomLogoutDialog dialog = new CustomLogoutDialog(parent);
        dialog.setVisible(true);
        return dialog.confirmed;
    }
}
