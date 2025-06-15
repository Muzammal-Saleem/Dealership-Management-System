package panels;

import Database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class Profile extends JPanel {
    private static final Color BACKGROUND_BASE = new Color(0, 0, 0);
    private static final Color CARD_BG = new Color(20, 20, 20, 240);
    private static final Color ACCENT_COLOR = new Color(66, 133, 244);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color SECONDARY_TEXT_COLOR = new Color(150, 150, 150);
    private static final Color BORDER_COLOR = new Color(50, 50, 50);
    private static final Color SELECTED_TAB_COLOR = new Color(239, 68, 68);
    private static final Color UNSELECTED_TAB_COLOR = new Color(0,0,0);

    private final String currentUser;
    private JPanel contentPanel, accountPanel;
    private JLabel nameLabel, emailLabel, phoneLabel, usernameLabel;
    private JTextField nameField, emailField, phoneField, usernameField;
    private JPasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    private JButton editButton, changeUsernameButton, changePasswordButton;
    private boolean isEditMode = false;
    private JTabbedPane tabbedPane;
    private String[] originalProfileData; // Store original data for revert

    public Profile(String currentUser) {
        this.currentUser = currentUser;
        this.originalProfileData = new String[]{"N/A", "N/A", "N/A"};
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);
        setBackground(BACKGROUND_BASE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BACKGROUND_BASE);
        tabbedPane.setForeground(TEXT_COLOR);
        tabbedPane.setFont(new Font("Inter", Font.BOLD, 14));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(isSelected ? SELECTED_TAB_COLOR : UNSELECTED_TAB_COLOR);
                g2d.fillRoundRect(x, y, w, h + 4, 10, 10);
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(BORDER_COLOR);
                g2d.drawRoundRect(x, y, w - 1, h + 3, 10, 10);
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
        });

        JPanel profileTab = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            }
        };
        profileTab.setOpaque(false);
        profileTab.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab("Profile Info", profileTab);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        profileTab.add(headerPanel, BorderLayout.NORTH);

        JLabel titleLabel = createStyledLabel("My Profile", new Font("Inter", Font.BOLD, 28), TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        editButton = createStyledButton("Edit Profile");
        headerPanel.add(editButton, BorderLayout.EAST);

        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        profileTab.add(contentPanel, BorderLayout.CENTER);

        JPanel accountTab = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            }
        };
        accountTab.setOpaque(false);
        accountTab.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tabbedPane.addTab("Account Settings", accountTab);

        accountPanel = new JPanel(new GridBagLayout());
        accountPanel.setOpaque(false);
        accountTab.add(accountPanel, BorderLayout.CENTER);

        add(tabbedPane, BorderLayout.CENTER);

        loadProfileData();
        loadAccountSettings();

        editButton.addActionListener(e -> toggleEditMode());
    }

    private void loadProfileData() {
        contentPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 12, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] profileData = new String[3];
        try {
            DBConnection.getProfileData(currentUser, profileData);
            // Store original data for revert
            System.arraycopy(profileData, 0, originalProfileData, 0, profileData.length);
        } catch (SQLException e) {
            System.err.println("Database error while loading profile: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading profile: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            // Use original data or "N/A" as fallback
            profileData[0] = originalProfileData[0] != null ? originalProfileData[0] : "N/A";
            profileData[1] = originalProfileData[1] != null ? originalProfileData[1] : "N/A";
            profileData[2] = originalProfileData[2] != null ? originalProfileData[2] : "N/A";
        }

        String name = profileData[0];
        String email = profileData[1];
        String phone = profileData[2];

        if (!isEditMode) {
            gbc.gridy = 0;
            addFieldRow(contentPanel, gbc, "Name", nameLabel = createStyledLabel(name, new Font("Inter", Font.PLAIN, 16), TEXT_COLOR));
            gbc.gridy = 1;
            addFieldRow(contentPanel, gbc, "Email", emailLabel = createStyledLabel(email, new Font("Inter", Font.PLAIN, 16), TEXT_COLOR));
            gbc.gridy = 2;
            addFieldRow(contentPanel, gbc, "Phone", phoneLabel = createStyledLabel(phone, new Font("Inter", Font.PLAIN, 16), TEXT_COLOR));
        } else {
            gbc.gridy = 0;
            addFieldRow(contentPanel, gbc, "Name", nameField = createStyledTextField(name));
            gbc.gridy = 1;
            addFieldRow(contentPanel, gbc, "Email", emailField = createStyledTextField(email));
            gbc.gridy = 2;
            addFieldRow(contentPanel, gbc, "Phone", phoneField = createStyledTextField(phone));
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void loadAccountSettings() {
        accountPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 12, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        addFieldRow(accountPanel, gbc, "Username", usernameLabel = createStyledLabel(currentUser, new Font("Inter", Font.PLAIN, 16), TEXT_COLOR));
        gbc.gridy = 1;
        addFieldRow(accountPanel, gbc, "New Username", usernameField = createStyledTextField(""));
        gbc.gridy = 2;
        gbc.gridx = 1;
        changeUsernameButton = createStyledButton("Change Username");
        accountPanel.add(changeUsernameButton, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        addFieldRow(accountPanel, gbc, "Current Password", currentPasswordField = createStyledPasswordField(""));
        gbc.gridy = 4;
        addFieldRow(accountPanel, gbc, "New Password", newPasswordField = createStyledPasswordField(""));
        gbc.gridy = 5;
        addFieldRow(accountPanel, gbc, "Confirm New Password", confirmPasswordField = createStyledPasswordField(""));
        gbc.gridy = 6;
        gbc.gridx = 1;
        changePasswordButton = createStyledButton("Change Password");
        accountPanel.add(changePasswordButton, gbc);

        changeUsernameButton.addActionListener(e -> changeUsername());
        changePasswordButton.addActionListener(e -> changePassword());

        accountPanel.revalidate();
        accountPanel.repaint();
    }

    private void addFieldRow(JPanel panel, GridBagConstraints gbc, String labelText, JComponent valueComponent) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel label = createStyledLabel(labelText + ":", new Font("Inter", Font.PLAIN, 14), SECONDARY_TEXT_COLOR);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(valueComponent, gbc);
    }

    private JLabel createStyledLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text, 20);
        field.setFont(new Font("Inter", Font.PLAIN, 16));
        field.setForeground(TEXT_COLOR);
        field.setBackground(BACKGROUND_BASE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        field.setCaretColor(ACCENT_COLOR);
        return field;
    }

    private JPasswordField createStyledPasswordField(String text) {
        JPasswordField field = new JPasswordField(text, 20);
        field.setFont(new Font("Inter", Font.PLAIN, 16));
        field.setForeground(TEXT_COLOR);
        field.setBackground(BACKGROUND_BASE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        field.setCaretColor(ACCENT_COLOR);
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACCENT_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_COLOR.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
        });

        return button;
    }

    private void toggleEditMode() {
        if (isEditMode) {
            // Validate inputs before saving
            String newName = nameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();

            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Invalid email format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPhone.isEmpty() && !newPhone.matches("\\d{4}-\\d{7}")) {
                JOptionPane.showMessageDialog(this, "Phone must be in format XXX-XXXX or empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean success = DBConnection.updateProfileData(currentUser, newName, newEmail, newPhone);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Update original data
                    originalProfileData[0] = newName;
                    originalProfileData[1] = newEmail;
                    originalProfileData[2] = newPhone;
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update profile. User not found or data unchanged.", "Update Error", JOptionPane.ERROR_MESSAGE);
                    // Revert to original data
                    loadProfileData();
                }
            } catch (SQLException e) {
                String message = e.getMessage();
                if (message.contains("UNIQUE KEY constraint") && message.contains("Email")) {
                    message = "Email already exists.";
                } else {
                    message = "Database error: " + message;
                }
                System.err.println("Database error while saving profile: " + message);
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE);
                // Revert to original data
                loadProfileData();
            }
        }

        isEditMode = !isEditMode;
        editButton.setText(isEditMode ? "Save" : "Edit Profile");
        loadProfileData();
    }

    private void changeUsername() {
        String newUsername = usernameField.getText().trim();
        if (newUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "New username cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newUsername.equals(currentUser)) {
            JOptionPane.showMessageDialog(this, "New username must be different from the current one.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean success = DBConnection.changeUsername(currentUser, newUsername);
            if (success) {
                JOptionPane.showMessageDialog(this, "Username updated successfully! Please re-login with your new username.", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Update currentUser and reload
                // Note: You may need to update the application state to reflect new username
                usernameLabel.setText(newUsername);
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists or update failed.", "Username Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.err.println("Database error while updating username: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating username: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        usernameField.setText("");
        loadAccountSettings();
    }

    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All password fields are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPassword.length() < 8) {
            JOptionPane.showMessageDialog(this, "New password must be at least 8 characters long.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean success = DBConnection.changePassword(currentUser, currentPassword, newPassword);
            if (success) {
                JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Current password is incorrect or update failed.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.err.println("Database error while updating password: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating password: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }
}