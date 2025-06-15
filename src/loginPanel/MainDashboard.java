package loginPanel;

import Database.DBConnection;
import panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Arrays;

public class MainDashboard extends JFrame {
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private String currentUser;
    private String userRole;
    private Timer timeTimer;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private Timer animationTimer;
    private boolean animationsEnabled = true;
    private Color currentBackground;

    // Menu buttons
    private JButton dashboardButton;
    private JButton inventoryButton;
    private JButton employeesButton;
    private JButton customersButton;
    private JButton salesButton;
    private JButton serviceButton;
    private JButton logoutButton;
    private JButton profileButton;
    private JButton ordersButton;
    private JButton vehicleButton;

    // Colors
    private final Color BACKGROUND_BASE = new Color(5, 5, 6); // Near-black (#050506)
    private final Color TABLE_BG = new Color(21, 21, 24, 230); // Dark gray (#151518, 90% opacity)
    private final Color GLOW_COLOR_START = new Color(0, 163, 255); // Blue (#00A3FF)
    private final Color GLOW_COLOR_END = new Color(123, 104, 238); // Purple (#7B68EE)
    private final Color TEXT_COLOR = new Color(255, 255, 255); // Pure white (#FFFFFF)
    private final Color SECONDARY_TEXT_COLOR = new Color(160, 174, 192); // Soft gray (#A0AEC0)

    // Valid roles
    private static final String[] VALID_ROLES = {"admin", "employee", "customer"};

    public MainDashboard(String username, String role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (!Arrays.asList(VALID_ROLES).contains(role.toLowerCase())) {
            throw new IllegalArgumentException("Invalid role: " + role + ". Must be one of: " +
                    String.join(", ", VALID_ROLES));
        }

        this.currentUser = username;
        this.userRole = role.toLowerCase();
        this.currentBackground = BACKGROUND_BASE;

        setTitle("Car.com Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_BASE);

        // Initialize panels
        initComponents();
        setupLayout();
        startTimeUpdater();
        startAnimation();

        // Show initial panel
        showPanel("dashboard");

        // Add window listener to clean up timers
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (timeTimer != null) {
                    timeTimer.stop();
                }
                if (animationTimer != null) {
                    animationTimer.stop();
                }
            }
        });

        setVisible(true);
    }

    private void initComponents() {
        contentPanel = new JPanel(new BorderLayout()) {
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
        };
        contentPanel.setOpaque(false);

        // Initialize sidebar
        initSidebar();

        // Initialize main content area with card layout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BACKGROUND_BASE);
        mainContentPanel.setOpaque(false);

        // Create panels based on role
        DashboardPanel dashboardPanel = new DashboardPanel(currentUser, userRole);
        mainContentPanel.add(dashboardPanel, "dashboard");

        if (userRole.equals("admin") || userRole.equals("employee")) {
            createVehiclePanel();
            createProfilePanel();
            createOrdersPanel();
            if (userRole.equals("admin")) {
                createEmployeePanel();
            }
            createCustomerPanel();
            createSalesPanel();
            createServicePanel();
        }
        if (userRole.equals("customer")) {
            createProfilePanel();
            createCustomerOrdersPanel();
            createCustomerVehiclePanel();
        }
    }

    private void setupLayout() {
        // Create header panel
        JPanel headerPanel = createHeaderPanel();

        // Add components to main frame
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(sidebarPanel, BorderLayout.WEST);
        contentPanel.add(mainContentPanel, BorderLayout.CENTER);

        add(contentPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TABLE_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

        // Logo/Title on left
        JLabel titleLabel = createGlowingLabel("Car.com", new Font("Inter", Font.BOLD, 24), new Color(255, 215, 0), GLOW_COLOR_START);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Date/Time in center
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        timeLabel.setForeground(new Color(255, 215, 0));

        dateLabel = new JLabel();
        dateLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(255, 215, 0));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 0));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        timePanel.setOpaque(false);
        timePanel.add(timeLabel);
        JLabel separator = new JLabel(" | ");
        separator.setForeground(new Color(255, 215, 0));
        timePanel.add(separator);
        timePanel.add(dateLabel);

        // User info on right
        String userDisplay = userRole.equals("customer") ?
                "Customer: " + currentUser :
                "User: " + currentUser + " (" + userRole + ")";
        JLabel userLabel = new JLabel(userDisplay);
        userLabel.setFont(new Font("Inter", Font.BOLD, 14));
        userLabel.setForeground(new Color(255, 215, 0));
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Add all sections to header
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(timePanel, BorderLayout.CENTER);
        headerPanel.add(userLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private void initSidebar() {
        sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TABLE_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setOpaque(false);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebarPanel.setPreferredSize(new Dimension(200, 600));

        // Create menu buttons based on role
        dashboardButton = createMenuButton("Dashboard");
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebarPanel.add(dashboardButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        if (userRole.equals("admin") || userRole.equals("employee")) {
            profileButton = createMenuButton("Profile");
            inventoryButton = createMenuButton("Vehicles");
            ordersButton = createMenuButton("Orders");
            if (userRole.equals("admin")) {
                employeesButton = createMenuButton("Employees");
            }
            customersButton = createMenuButton("Customers");
            salesButton = createMenuButton("Sales");

            serviceButton = createMenuButton("Service");

            sidebarPanel.add(profileButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebarPanel.add(inventoryButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebarPanel.add(ordersButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            if (userRole.equals("admin")) {
                sidebarPanel.add(employeesButton);
                sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            sidebarPanel.add(customersButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebarPanel.add(salesButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebarPanel.add(serviceButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        if (userRole.equals("customer")) {
            profileButton = createMenuButton("Profile");
            vehicleButton = createMenuButton("Vehicles");
            ordersButton = createMenuButton("Orders");

            sidebarPanel.add(profileButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebarPanel.add(vehicleButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebarPanel.add(ordersButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        logoutButton = createMenuButton("Logout");
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(logoutButton);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Set initial active button
        setActiveButton(dashboardButton);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text) {
            private float glowPhase = 0f;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, isActiveButton(this) ? GLOW_COLOR_START : new Color(21, 21, 24),
                        getWidth(), getHeight(), isActiveButton(this) ? GLOW_COLOR_END : new Color(25, 25, 28));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                if (animationsEnabled && getMousePosition() != null && !isActiveButton(this)) {
                    glowPhase += 0.1f;
                    float alpha = (float) (0.3 + 0.2 * Math.sin(glowPhase));
                    g2d.setColor(new Color(255, 255, 255, (int) (alpha * 255)));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
                }
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Inter", Font.PLAIN, 16));
        button.setForeground(TEXT_COLOR);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(180, 45));
        button.setHorizontalAlignment(SwingConstants.LEFT);

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isActiveButton(button) && animationsEnabled) {
                    button.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isActiveButton(button) && animationsEnabled) {
                    button.repaint();
                }
            }
        });

        // Add action listener
        button.addActionListener(e -> {
            setActiveButton(button);
            String panelName = text.toLowerCase();
            if (panelName.equals("logout")) {
                handleLogout();
            } else {
                showPanel(panelName);
            }
        });

        return button;
    }

    private boolean isActiveButton(JButton button) {
        return button.getFont().getStyle() == Font.BOLD;
    }

    private void setActiveButton(JButton activeButton) {
        java.util.List<JButton> buttons = new java.util.ArrayList<>();
        buttons.add(dashboardButton);
        buttons.add(logoutButton);

        if (userRole.equals("admin") || userRole.equals("employee")) {
            buttons.add(profileButton);
            buttons.add(inventoryButton);
            buttons.add(ordersButton);
            if (userRole.equals("admin")) {
                buttons.add(employeesButton);
            }
            buttons.add(customersButton);
            buttons.add(salesButton);
            buttons.add(serviceButton);
        }
        if (userRole.equals("customer")) {
            buttons.add(profileButton);
            buttons.add(vehicleButton);
            buttons.add(ordersButton);
        }

        for (JButton button : buttons) {
            button.setFont(new Font("Inter", Font.PLAIN, 16));
            button.repaint();
        }

        activeButton.setFont(new Font("Inter", Font.BOLD, 16));
        activeButton.repaint();
    }

    private void createVehiclePanel() {
        VehiclePanel vehiclePanel = new VehiclePanel();
        mainContentPanel.add(vehiclePanel, "vehicles");
    }

    private void createEmployeePanel() {
        EmployeePanel employees = new EmployeePanel();
        mainContentPanel.add(employees, "employees");
    }

    private void createCustomerPanel() {
        JPanel customer = new CustomerPanel();
        mainContentPanel.add(customer, "customers");
    }

    private void createSalesPanel() {
        JPanel panel = new SalesPanel();
        mainContentPanel.add(panel, "sales");
    }

    private void createServicePanel() {
        ServicePanel panel = new ServicePanel();
        mainContentPanel.add(panel, "service");
    }

    private void createProfilePanel() {
        Profile panel = new Profile(currentUser);
        mainContentPanel.add(panel, "profile");
    }

    private void createCustomerVehiclePanel() {
        CustomerVehiclesPanel customerVehicle = new CustomerVehiclesPanel(DBConnection.getCustomerIdByUsername(currentUser));
        mainContentPanel.add(customerVehicle, "vehicles");
    }

    // Fix for the createOrdersPanel method in MainDashboard class
    private void createOrdersPanel() {
        if (userRole.equals("customer")) {
            // For customers, show their own orders using their customer ID
            int customerId = Integer.valueOf(DBConnection.getCustomerIdByUsername(currentUser));
            CustomerOrdersPanel panel = new CustomerOrdersPanel(customerId);
            mainContentPanel.add(panel, "orders");
        } else {
            // For admin/employee, show pending orders panel
            PendingOrdersPanel panel = new PendingOrdersPanel(currentUser);
            mainContentPanel.add(panel, "orders");
        }
    }
    // Also fix the createCustomerOrdersPanel method (this seems to be duplicate functionality)
    private void createCustomerOrdersPanel() {
        // Get the customer ID for the logged-in customer
        int customerId = Integer.valueOf(DBConnection.getCustomerIdByUsername(currentUser));
        CustomerOrdersPanel panel = new CustomerOrdersPanel(customerId);
        mainContentPanel.add(panel, "orders");
    }



//    private void createCustomerOrdersPanel() {
//        JPanel panel = new JPanel(new BorderLayout()) {
//            @Override
//            protected void paintComponent(Graphics g) {
//                Graphics2D g2d = (Graphics2D) g;
//                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                g2d.setColor(TABLE_BG);
//                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
//                super.paintComponent(g);
//            }
//        };
//        panel.setOpaque(false);
//        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//
//        JLabel titleLabel = createGlowingLabel("Orders", new Font("Inter", Font.BOLD, 24), TEXT_COLOR, GLOW_COLOR_START);
//        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
//
//        JLabel contentLabel = new JLabel("Your order history will be displayed here");
//        contentLabel.setFont(new Font("Inter", Font.PLAIN, 16));
//        contentLabel.setForeground(SECONDARY_TEXT_COLOR);
//        contentLabel.setHorizontalAlignment(SwingConstants.CENTER);
//
//        panel.add(titleLabel, BorderLayout.NORTH);
//        panel.add(contentLabel, BorderLayout.CENTER);
//
//        mainContentPanel.add(panel, "orders");
//    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TABLE_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = createGlowingLabel(title, new Font("Inter", Font.BOLD, 24), TEXT_COLOR, GLOW_COLOR_START);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel contentLabel = new JLabel(title + " content will be displayed here");
        contentLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        contentLabel.setForeground(SECONDARY_TEXT_COLOR);
        contentLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentLabel, BorderLayout.CENTER);

        return panel;
    }

    private void showPanel(String panelName) {
        String[] restrictedPanels = {"employees", "customers", "sales", "service", "orders"};
//        if (userRole.equals("customer") && Arrays.asList(restrictedPanels).contains(panelName)) {
//            JOptionPane.showMessageDialog(this,
//                    "Access denied: This feature is only available for admin and employee roles",
//                    "Access Denied",
//                    JOptionPane.ERROR_MESSAGE);
//            showPanel("dashboard");
//            setActiveButton(dashboardButton);
//            return;
//        }
        cardLayout.show(mainContentPanel, panelName);
    }

    private void startTimeUpdater() {
        timeTimer = new Timer(1000, e -> updateDateTime());
        timeTimer.start();
        updateDateTime();
    }

    private void updateDateTime() {
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a");
        timeLabel.setText(timeFormat.format(now));
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        dateLabel.setText(dateFormat.format(now));
    }

    private void handleLogout() {
        boolean confirm = CustomLogoutDialog.showLogoutDialog(this);

        if (confirm) {
            if (timeTimer != null) {
                timeTimer.stop();
            }
            if (animationTimer != null) {
                animationTimer.stop();
            }
            new LoginPage();
            dispose();
        }
    }

    private void startAnimation() {
        if (!animationsEnabled) return;
        animationTimer = new Timer(1000 / 60, e -> {
            float phase = (float) (System.currentTimeMillis() % 5000) / 5000;
            int r = (int) (5 + 2 * Math.sin(phase));
            int g = (int) (5 + 2 * Math.sin(phase + 2));
            int b = (int) (6 + 2 * Math.sin(phase + 4));
            currentBackground = new Color(r, g, b);
            contentPanel.repaint();
        });
        animationTimer.start();
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
        label.setFont(font);
        label.setForeground(textColor);
        label.setOpaque(false);
        return label;
    }

    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new MainDashboard("sameerahmad", "employee"));
        SwingUtilities.invokeLater(() -> new MainDashboard("sameer", "customer"));
//        SwingUtilities.invokeLater(() -> new MainDashboard("Muzammal", "Admin"));
    }
}