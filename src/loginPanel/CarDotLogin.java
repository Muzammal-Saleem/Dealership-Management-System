package loginPanel;
import Database.DBConnection;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class CarDotLogin {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LandingPage());
    }
}

class LandingPage extends JFrame {
    private JButton letsGoButton;

    public LandingPage() {
        setTitle("Car.com by Muzammal");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new BackgroundPanel("/images/image6.jpg");
        mainPanel.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(10, 12, 20, 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("Car.com by Muzammal");
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 56));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Your Premium Auto Dealership Experience");
        subtitleLabel.setForeground(new Color(200, 200, 200));
        subtitleLabel.setFont(new Font("Montserrat", Font.PLAIN, 28));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        letsGoButton = new JButton("LET'S GO");
        letsGoButton.setFont(new Font("Montserrat", Font.BOLD, 18));
        letsGoButton.setBackground(new Color(0, 102, 204));
        letsGoButton.setForeground(Color.WHITE);
        letsGoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        letsGoButton.setFocusPainted(false);
        letsGoButton.setBorder(new RoundedBorder(15));
        letsGoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        letsGoButton.setPreferredSize(new Dimension(220, 60));

        letsGoButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                letsGoButton.setBackground(new Color(0, 122, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                letsGoButton.setBackground(new Color(0, 102, 204));
            }
        });

        letsGoButton.addActionListener(e -> {
            new LoginPage();
            dispose();
        });

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        contentPanel.add(letsGoButton);
        contentPanel.add(Box.createVerticalGlue());

        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.setOpaque(false);
        centeringPanel.add(contentPanel);

        mainPanel.add(centeringPanel, BorderLayout.CENTER);

        JLabel versionLabel = new JLabel("© 2025 Car.com by Muzammal | All Rights Reserved");
        versionLabel.setForeground(new Color(150, 150, 150));
        versionLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(versionLabel);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        try {
            backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        } catch (Exception e) {
            System.out.println("Background image not found. Using gradient instead.");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(10, 12, 20),
                    getWidth(), getHeight(), new Color(20, 24, 40)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        g2d.dispose();
    }
}

class LoginPage extends JFrame {
    private ModernTextField usernameField;
    private ModernPasswordField passwordField;
    private ModernComboBox roleComboBox;
    private ModernButton loginButton;
    private ModernButton exitButton;
    private JLabel forgotPasswordLabel;
    private JLabel signupLabel;
    private Timer animationTimer;

    public LoginPage() {
        setTitle("Login - Car.com by Muzammal");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(10, 12, 20),
                        getWidth(), getHeight(), new Color(20, 24, 40)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        JPanel brandingPanel = createBrandingPanel();
        JPanel formPanel = createFormPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(50, 50, 50, 25);
        mainPanel.add(brandingPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        gbc.insets = new Insets(50, 25, 50, 50);
        mainPanel.add(formPanel, gbc);

        add(mainPanel);
        setVisible(true);

        startEntranceAnimation();
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.setColor(new Color(255, 215, 0, 80));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 20, 0);

        JLabel logoLabel = new JLabel("Car.com");
        logoLabel.setFont(new Font("Montserrat", Font.BOLD, 60));
        logoLabel.setForeground(new Color(255, 215, 0));
        panel.add(logoLabel, gbc);

        JLabel byLabel = new JLabel("by Muzammal");
        byLabel.setFont(new Font("Montserrat", Font.PLAIN, 20));
        byLabel.setForeground(new Color(180, 180, 180));
        gbc.insets = new Insets(0, 0, 40, 0);
        panel.add(byLabel, gbc);

        JLabel taglineLabel = new JLabel("Premium Auto Dealership");
        taglineLabel.setFont(new Font("Montserrat", Font.PLAIN, 18));
        taglineLabel.setForeground(new Color(200, 200, 200));
        gbc.insets = new Insets(0, 0, 40, 0);
        panel.add(taglineLabel, gbc);

        String[] features = {
                "Enterprise Security",
                "Advanced Analytics",
                "Seamless Experience",
                "Professional Support"
        };

        for (String feature : features) {
            JPanel featurePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            featurePanel.setOpaque(false);

            JLabel bullet = new JLabel("•");
            bullet.setFont(new Font("Montserrat", Font.PLAIN, 18));
            bullet.setForeground(new Color(255, 215, 0));

            JLabel featureLabel = new JLabel(" " + feature);
            featureLabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
            featureLabel.setForeground(new Color(160, 160, 160));

            featurePanel.add(bullet);
            featurePanel.add(featureLabel);

            gbc.insets = new Insets(5, 0, 5, 0);
            panel.add(featurePanel, gbc);
        }

        gbc.insets = new Insets(40, 0, 15, 0);
        JLabel credentialsLabel = new JLabel("Industry Leading Platform");
        credentialsLabel.setFont(new Font("Montserrat", Font.PLAIN, 14));
        credentialsLabel.setForeground(new Color(140, 140, 140));
        panel.add(credentialsLabel, gbc);

        gbc.insets = new Insets(20, 0, 0, 0);
        JLabel versionLabel = new JLabel("Version 2.0 Enterprise");
        versionLabel.setFont(new Font("Montserrat", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(120, 120, 120));
        panel.add(versionLabel, gbc);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(20, 22, 30, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.setColor(new Color(255, 215, 0, 80));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 30, 0);

        JLabel titleLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Montserrat", Font.BOLD, 36));
        panel.add(titleLabel, gbc);

        gbc.gridy++;
        JLabel subtitleLabel = new JLabel("Sign in to your account", SwingConstants.CENTER);
        subtitleLabel.setForeground(new Color(180, 180, 180));
        subtitleLabel.setFont(new Font("Montserrat", Font.PLAIN, 18));
        gbc.insets = new Insets(0, 0, 50, 0);
        panel.add(subtitleLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 25, 0);
        usernameField = new ModernTextField("Username");
        usernameField.setPreferredSize(new Dimension(400, 50));
        panel.add(usernameField, gbc);

        gbc.gridy++;
        passwordField = new ModernPasswordField("Password");
        passwordField.setPreferredSize(new Dimension(400, 50));
        panel.add(passwordField, gbc);

        gbc.gridy++;
        String[] roles = {"Admin", "Customer", "Employee"};
        roleComboBox = new ModernComboBox(roles);
        roleComboBox.setPreferredSize(new Dimension(400, 50));
        panel.add(roleComboBox, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(40, 0, 25, 0);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        exitButton = new ModernButton("Exit", new Color(100, 100, 100));
        exitButton.setPreferredSize(new Dimension(140, 50));
        exitButton.addActionListener(e -> {
            new LandingPage();
            dispose();
        });

        loginButton = new ModernButton("Login", new Color(0, 102, 204));
        loginButton.setPreferredSize(new Dimension(140, 50));
        loginButton.addActionListener(this::handleLogin);

        buttonPanel.add(exitButton);
        buttonPanel.add(loginButton);
        panel.add(buttonPanel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(25, 0, 0, 0);
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        linkPanel.setOpaque(false);

        forgotPasswordLabel = createLinkLabel("Forgot Password?");
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCustomDialog("Info", "Forgot Password functionality coming soon!", "OK", true);
            }
        });

        signupLabel = createLinkLabel("Sign Up");
        signupLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new SignUpPanel();
            }
        });

        linkPanel.add(forgotPasswordLabel);
        linkPanel.add(new JLabel("|") {{ setForeground(new Color(100, 100, 100)); }});
        linkPanel.add(signupLabel);
        panel.add(linkPanel, gbc);

        return panel;
    }

    private JLabel createLinkLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(255, 215, 0));
        label.setFont(new Font("Montserrat", Font.PLAIN, 16));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(new Color(255, 235, 100));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(new Color(255, 215, 0));
            }
        });

        return label;
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = roleComboBox.getSelectedItem().toString().toLowerCase();

        if (username.isEmpty() || password.isEmpty()) {
            showCustomDialog("Login Failed", "Please enter both username and password.", "OK", false);
            return;
        }

        if (validateLogin(username, password, role)) {
            showCustomDialog("Login Successful", "Welcome to Car.com by Muzammal", "CONTINUE", true);
            new MainDashboard(username, role);
            dispose();
        } else {
            showCustomDialog("Login Failed", "Invalid credentials. Please try again.", "TRY AGAIN", false);
        }
    }

    private boolean validateLogin(String username, String password, String role) {
        return DBConnection.validateUser(username, password, role);
    }

    private void startEntranceAnimation() {
        setOpacity(0.0f);
        Timer fadeTimer = new Timer(50, null);
        fadeTimer.addActionListener(new ActionListener() {
            float opacity = 0.0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.05f;
                if (opacity >= 1.0f) {
                    opacity = 1.0f;
                    fadeTimer.stop();
                }
                setOpacity(opacity);
            }
        });
        fadeTimer.start();
    }

    private void showCustomDialog(String title, String message, String buttonText, boolean isSuccess) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(450, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, 450, 220, 20, 20));

        JPanel contentPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(20, 22, 30));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.setColor(isSuccess ? new Color(0, 150, 80) : new Color(200, 50, 50));
                g2d.fillRoundRect(0, 0, getWidth(), 5, 20, 20);
                g2d.dispose();
            }
        };

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>");
        messageLabel.setFont(new Font("Montserrat", Font.PLAIN, 18));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        ModernButton button = new ModernButton(buttonText,
                isSuccess ? new Color(0, 150, 80) : new Color(200, 50, 50));
        button.setPreferredSize(new Dimension(140, 50));
        button.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(button);

        contentPanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
}

class ModernTextField extends JTextField {
    private String placeholder;
    private boolean focused = false;

    public ModernTextField(String placeholder) {
        this.placeholder = placeholder;
        setupTextField();
    }

    private void setupTextField() {
        setFont(new Font("Montserrat", Font.PLAIN, 16));
        setForeground(Color.WHITE);
        setBackground(new Color(30, 32, 40));
        setBorder(new RoundedBorder(12, new Color(80, 80, 80)));
        setCaretColor(new Color(255, 215, 0));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                setBorder(new RoundedBorder(12, new Color(255, 215, 0))); // Gold border on focus
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                setBorder(new RoundedBorder(12, new Color(80, 80, 80))); // Default gray border
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !focused) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(120, 120, 120));
            g2d.setFont(getFont());
            FontMetrics fm = g2d.getFontMetrics();
            int x = getInsets().left;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(placeholder, x, y);
            g2d.dispose();
        }
    }
}

class ModernPasswordField extends JPasswordField {
    private String placeholder;
    private boolean focused = false;

    public ModernPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setupPasswordField();
    }

    private void setupPasswordField() {
        setFont(new Font("Montserrat", Font.PLAIN, 16));
        setForeground(Color.WHITE);
        setBackground(new Color(30, 32, 40));
        setBorder(new RoundedBorder(12, new Color(80, 80, 80)));
        setCaretColor(new Color(255, 215, 0));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                setBorder(new RoundedBorder(12, new Color(255, 215, 0))); // Gold border on focus
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                setBorder(new RoundedBorder(12, new Color(80, 80, 80))); // Default gray border
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getPassword().length == 0 && !focused) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(120, 120, 120));
            g2d.setFont(getFont());
            FontMetrics fm = g2d.getFontMetrics();
            int x = getInsets().left;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(placeholder, x, y);
            g2d.dispose();
        }
    }
}

class ModernComboBox extends JComboBox<String> {
    public ModernComboBox(String[] items) {
        super(items);
        setupComboBox();
    }

    private void setupComboBox() {
        setFont(new Font("Montserrat", Font.PLAIN, 16));
        setForeground(Color.WHITE);
        setBackground(new Color(30, 32, 40));
        setBorder(new RoundedBorder(12, new Color(80, 80, 80)));
        setFocusable(false);

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(new Font("Montserrat", Font.PLAIN, 16));
                if (isSelected) {
                    setBackground(new Color(255, 215, 0));
                    setForeground(new Color(20, 22, 30));
                } else {
                    setBackground(new Color(30, 32, 40));
                    setForeground(Color.WHITE);
                }
                setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                return c;
            }
        });
    }
}

class ModernButton extends JButton {
    private Color baseColor;
    private Color hoverColor;
    private boolean isHovered = false;

    public ModernButton(String text, Color baseColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = baseColor.brighter();
        setupButton();
    }

    private void setupButton() {
        setFont(new Font("Montserrat", Font.BOLD, 16));
        setForeground(Color.WHITE);
        setBackground(baseColor);
        setBorder(new RoundedBorder(12));
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bgColor = isHovered ? hoverColor : baseColor;
        g2d.setColor(bgColor);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        g2d.dispose();
        super.paintComponent(g);
    }
}

class RoundedBorder extends AbstractBorder {
    private int radius;
    private Color borderColor;

    public RoundedBorder(int radius) {
        this(radius, null);
    }

    public RoundedBorder(int radius, Color borderColor) {
        this.radius = radius;
        this.borderColor = borderColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (borderColor != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(borderColor);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius/2, radius, radius/2, radius);
    }
}