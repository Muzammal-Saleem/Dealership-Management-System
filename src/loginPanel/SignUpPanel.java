package loginPanel;

import Database.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignUpPanel extends JPanel {
    private final Color BACKGROUND_BASE = new Color(5, 5, 6); // Near-black (#050506)
    private Color currentBackground = BACKGROUND_BASE;
    private final Color GLOW_COLOR_START = new Color(0, 163, 255); // Blue (#00A3FF)
    private final Color GLOW_COLOR_END = new Color(123, 104, 238); // Purple (#7B68EE)
    private final Color TEXT_COLOR = new Color(255, 255, 255); // Pure white (#FFFFFF)
    private final Color SECONDARY_TEXT_COLOR = new Color(160, 174, 192); // Soft gray (#A0AEC0)
    private final Color INPUT_BG = new Color(21, 21, 24, 230); // Dark gray (#151518, 90% opacity)
    private Timer animationTimer;
    private boolean animationsEnabled = true;
    private Runnable loginCallback; // Callback to switch to login panel

    public SignUpPanel() {
//        this.loginCallback = loginCallback; // For redirecting to login after signup
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_BASE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setOpaque(false);

//        initComponents();
        showSignUpForm();
        startAnimation();
    }

    private void showSignUpForm() {
        JDialog formDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sign Up", true);
        formDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (animationsEnabled) {
                    float phase = (float) (System.currentTimeMillis() % 4000) / 4000;
                    int glow = (int) (50 + 20 * Math.sin(2 * Math.PI * phase));
                    g2d.setColor(new Color(GLOW_COLOR_START.getRed(), GLOW_COLOR_START.getGreen(), GLOW_COLOR_START.getBlue(), glow));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                }
            }
        };
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formDialog.setContentPane(formPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Username:", "Password:", "First Name:", "Last Name:", "Email:", "Phone:", "Address:", "City:", "State:", "Zip Code:"};
        JTextField[] fields = new JTextField[labels.length - 1];
        JPasswordField passwordField = new JPasswordField(20);

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setForeground(SECONDARY_TEXT_COLOR);
            label.setFont(new Font("Inter", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.EAST;
            formPanel.add(label, gbc);

            if (i == 1) { // Password field
                passwordField.setBackground(INPUT_BG);
                passwordField.setForeground(TEXT_COLOR);
                passwordField.setCaretColor(TEXT_COLOR);
                passwordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                passwordField.setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                formPanel.add(passwordField, gbc);
            } else {
                fields[i - (i > 1 ? 1 : 0)] = new JTextField(20) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setColor(INPUT_BG);
                        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        if (isFocusOwner() && animationsEnabled) {
                            float phase = (float) (System.currentTimeMillis() % 2000) / 2000;
                            int glow = (int) (50 + 20 * Math.sin(2 * Math.PI * phase));
                            g2d.setColor(new Color(GLOW_COLOR_START.getRed(), GLOW_COLOR_START.getGreen(), GLOW_COLOR_START.getBlue(), glow));
                            g2d.setStroke(new BasicStroke(2));
                            g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                        }
                        super.paintComponent(g);
                    }
                };
                fields[i - (i > 1 ? 1 : 0)].setBackground(INPUT_BG);
                fields[i - (i > 1 ? 1 : 0)].setForeground(TEXT_COLOR);
                fields[i - (i > 1 ? 1 : 0)].setCaretColor(TEXT_COLOR);
                fields[i - (i > 1 ? 1 : 0)].setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                fields[i - (i > 1 ? 1 : 0)].setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                formPanel.add(fields[i - (i > 1 ? 1 : 0)], gbc);
            }
        }

        JTextField usernameField = fields[0];
        JTextField firstNameField = fields[1];
        JTextField lastNameField = fields[2];
        JTextField emailField = fields[3];
        JTextField phoneField = fields[4];
        JTextField addressField = fields[5];
        JTextField cityField = fields[6];
        JTextField stateField = fields[7];
        JTextField zipCodeField = fields[8];

        JButton submitButton = createStyledButton("Sign Up", GLOW_COLOR_START, GLOW_COLOR_END);
        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(submitButton, gbc);

        JButton backButton = createStyledButton("Back to Login", GLOW_COLOR_START, GLOW_COLOR_END);
        gbc.gridy = labels.length + 1;
        formPanel.add(backButton, gbc);

        submitButton.addActionListener(e -> handleSignUp(formDialog, usernameField, passwordField, firstNameField, lastNameField, emailField, phoneField, addressField, cityField, stateField, zipCodeField));
        backButton.addActionListener(e -> {
            formDialog.dispose();
            if (loginCallback != null) {
                loginCallback.run();
            }
        });

        formDialog.pack();
        formDialog.setLocationRelativeTo(this);
        formDialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color startColor, Color endColor) {
        JButton button = new JButton(text) {
            private float glowPhase = 0f;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                if (animationsEnabled && getMousePosition() != null) {
                    glowPhase += 0.1f;
                    float alpha = (float) (0.3 + 0.2 * Math.sin(glowPhase));
                    g2d.setColor(new Color(255, 255, 255, (int) (alpha * 255)));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                }
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setForeground(TEXT_COLOR);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (animationsEnabled) button.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (animationsEnabled) button.repaint();
            }
        });
        return button;
    }

    private JLabel createGlowingLabel(String text, Font font, Color textColor, Color glowColor) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (animationsEnabled) {
                    float phase = (float) (System.currentTimeMillis() % 4000) / 4000;
                    int glowIntensity = (int) (20 + 10 * Math.sin(2 * Math.PI * phase));
                    for (int i = 2; i >= 1; i--) {
                        g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), glowIntensity - i * 5));
                        g2d.setFont(getFont());
                        FontMetrics fm = g2d.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth(getText())) / 2;
                        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                        g2d.drawString(getText(), x + i, y + i);
                        g2d.drawString(getText(), x - i, y - i);
                        g2d.drawString(getText(), x + i, y - i);
                        g2d.drawString(getText(), x - i, y + i);
                    }
                }
                g2d.setColor(textColor);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        label.setFont(font.deriveFont(Font.PLAIN));
        label.setForeground(textColor);
        label.setOpaque(false);
        return label;
    }

    private void startAnimation() {
        if (!animationsEnabled) return;
        animationTimer = new Timer(1000 / 60, e -> {
            float phase = (float) (System.currentTimeMillis() % 5000) / 5000;
            int r = (int) (5 + 2 * Math.sin(phase));
            int g = (int) (5 + 2 * Math.sin(phase + 2));
            int b = (int) (6 + 2 * Math.sin(phase + 4));
            currentBackground = new Color(r, g, b);
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RadialGradientPaint gradient = new RadialGradientPaint(
                getWidth() / 2f, getHeight() / 2f, Math.max(getWidth(), getHeight()),
                new float[]{0f, 1f}, new Color[]{currentBackground, new Color(21, 21, 24)}
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void handleSignUp(JDialog formDialog, JTextField usernameField, JPasswordField passwordField,
                              JTextField firstNameField, JTextField lastNameField, JTextField emailField,
                              JTextField phoneField, JTextField addressField, JTextField cityField,
                              JTextField stateField, JTextField zipCodeField) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String city = cityField.getText().trim();
        String state = stateField.getText().trim();
        String zipCode = zipCodeField.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(formDialog, "Username, Password, First Name, Last Name, Email, and Phone are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(formDialog, "Please enter a valid email address (e.g., example@domain.com).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!phone.matches("^[0-9\\-\\(\\)\\s]{10,15}$")) {
            JOptionPane.showMessageDialog(formDialog, "Please enter a valid phone number (e.g., 123-456-7890).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.length() < 3 || username.length() > 20 || !username.matches("^[A-Za-z0-9_]+$")) {
            JOptionPane.showMessageDialog(formDialog, "Username must be 3-20 characters and contain only letters, numbers, or underscores.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(formDialog, "Password must be at least 8 characters long.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Optional fields
        address = address.isEmpty() ? null : address;
        city = city.isEmpty() ? null : city;
        state = state.isEmpty() ? null : state;
        zipCode = zipCode.isEmpty() ? null : zipCode;

        try {
            // Check if username already exists
            try (ResultSet rs = DBConnection.getUserByUsername(username)) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(formDialog, "Username already exists. Please choose another.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Add to Users table
            String passwordHash = password; // TODO: In production, use a secure hash (e.g., BCrypt)
            boolean userSuccess = DBConnection.addUser(username, passwordHash, "customer");
            if (!userSuccess) {
                throw new SQLException("Failed to add user to Users table.");
            }

            // Get the new UserID
            Integer userId;
            try (ResultSet rs = DBConnection.getUserByUsername(username)) {
                if (rs.next()) {
                    userId = rs.getInt("UserID");
                } else {
                    throw new SQLException("Failed to retrieve UserID after adding user.");
                }
            }

            // Add to Customers table
            boolean customerSuccess = DBConnection.addCustomer(firstName, lastName, email, phone, address, city, state, zipCode, userId);
            if (customerSuccess) {
                JOptionPane.showMessageDialog(formDialog, "Sign-up successful! Please log in with your new credentials.", "Success", JOptionPane.INFORMATION_MESSAGE);
                formDialog.dispose();
                if (loginCallback != null) {
                    loginCallback.run();
                }
            } else {
                throw new SQLException("Failed to add customer to Customers table.");
            }
        } catch (SQLException ex) {
            String errorMessage = "Sign-up failed: " + ex.getMessage();
            if (ex.getSQLState().startsWith("23")) { // SQL integrity constraint violation
                errorMessage = "Sign-up failed: Username or email may already be in use.";
            }
            JOptionPane.showMessageDialog(formDialog, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

