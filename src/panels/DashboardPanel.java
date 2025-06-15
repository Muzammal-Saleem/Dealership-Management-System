package panels;

import Database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;

import static Database.DBConnection.getCustomerOrderCount;

public class DashboardPanel extends JPanel {
    private String currentUser;
    private String userRole;
    private final Color BACKGROUND_BASE = new Color(5, 5, 6); // Near-black (#050506)
    private Color currentBackground = BACKGROUND_BASE;
    private final Color CARD_BACKGROUND = new Color(21, 21, 24, 230); // Dark gray (#151518, 90% opacity)
    private final Color GLOW_COLOR_START = new Color(0, 163, 255); // Blue (#00A3FF)
    private final Color GLOW_COLOR_END = new Color(123, 104, 238); // Purple (#7B68EE)
    private final Color TEXT_COLOR = new Color(255, 255, 255); // Pure white (#FFFFFF)
    private final Color TITLE_COLOR = new Color(160, 174, 192); // Soft gray (#A0AEC0)
    private Timer animationTimer;
    private boolean animationsEnabled = true; // Toggle for low-end systems
    private float[] cardOpacities;

    public DashboardPanel(String currentUser, String userRole) {
        this.currentUser = currentUser;
        this.userRole = userRole != null ? userRole.toLowerCase() : "unknown";
        System.out.println("DashboardPanel initialized for user: " + currentUser + ", role: " + this.userRole);

        setLayout(new BorderLayout());
        setBackground(BACKGROUND_BASE);
        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); // Generous padding

        // Initialize card opacities based on role
        int cardCount = this.userRole.equals("customer") ? 2 : 4;
        cardOpacities = new float[cardCount];
        for (int i = 0; i < cardOpacities.length; i++) {
            cardOpacities[i] = 0f;
        }

        // Welcome message with gradient
        String welcomeText = this.userRole.equals("customer") ?
                "Welcome, " + currentUser + "!" :
                "Welcome, " + currentUser + " (" + this.userRole + ")!";
        Font welcomeFont = getFontWithFallback("Inter", Font.BOLD, 32);
        JLabel welcomeLabel = createGlowingLabel(welcomeText, welcomeFont, TEXT_COLOR, GLOW_COLOR_START);
        welcomeLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Header underline with gradient
        JPanel underline = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, GLOW_COLOR_START, getWidth(), 0, GLOW_COLOR_END);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        underline.setPreferredSize(new Dimension(250, 4));
        underline.setMaximumSize(new Dimension(250, 4));

        // Stats panel
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 30, 30, 30); // Balanced spacing
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        if (this.userRole.equals("admin") || this.userRole.equals("employee")) {
            gbc.gridx = 0; gbc.gridy = 0;
            statsPanel.add(createStatCard("Vehicles", String.valueOf(DBConnection.getTotalCount("Vehicles")), "🚗"), gbc);
            gbc.gridx = 1;
            statsPanel.add(createStatCard("Sales", String.valueOf(DBConnection.getTotalCount("Sales")), "💰"), gbc);
            gbc.gridx = 0; gbc.gridy = 1;
            statsPanel.add(createStatCard("Customers", String.valueOf(DBConnection.getTotalCount("Customers")), "👥"), gbc);
            gbc.gridx = 1;
            statsPanel.add(createStatCard("Services", String.valueOf(DBConnection.getTotalCount("Services")), "🔧"), gbc);
        } else if (this.userRole.equals("customer")) {
            // Fetch order count with error handling
            String orderText;
            try {
                int orderCount = getCustomerOrderCount(currentUser);
                orderText = String.valueOf(orderCount);
                System.out.println("Order count for " + currentUser + ": " + orderCount);
            } catch (Exception e) {
                e.printStackTrace();
                orderText = "N/A";
            }

            // Fetch active services count (implement this method in DBConnection)
            String servicesText;
            try {
                int servicesCount = 1; // DBConnection.getCustomerActiveServices(currentUser); // Placeholder
                servicesText = String.valueOf(servicesCount);
                System.out.println("Active services for " + currentUser + ": " + servicesCount);
            } catch (Exception e) {
                e.printStackTrace();
                servicesText = "N/A";
            }

            // Use GridLayout for customer cards to ensure proper alignment
            JPanel customerStatsPanel = new JPanel(new GridLayout(1, 2, 30, 30));
            customerStatsPanel.setOpaque(false);
            customerStatsPanel.add(createStatCard("Orders", orderText, "🛒"));
            customerStatsPanel.add(createStatCard("Active Services", servicesText, "⚙️"));
            statsPanel.add(customerStatsPanel, gbc);
        }

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.add(welcomeLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        headerPanel.add(underline);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(statsPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Start animations
        startAnimation();
        startFadeInAnimation();

        setOpaque(false);
    }

    private Font getFontWithFallback(String fontName, int style, int size) {
        if (Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains(fontName)) {
            return new Font(fontName, style, size);
        }
        return new Font("SansSerif", style, size);
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

    private void startFadeInAnimation() {
        if (!animationsEnabled) return;
        Timer fadeTimer = new Timer(50, e -> {
            boolean allFaded = true;
            for (int i = 0; i < cardOpacities.length; i++) {
                if (cardOpacities[i] < 1f) {
                    cardOpacities[i] = Math.min(1f, cardOpacities[i] + 0.05f);
                    allFaded = false;
                }
            }
            repaint();
            if (allFaded) ((Timer) e.getSource()).stop();
        });
        fadeTimer.start();
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

    private JPanel createStatCard(String title, String value, String icon) {
        JPanel panel = new JPanel(new BorderLayout(25, 25)) {
            private float glowPhase = 0f;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Glassmorphism background
                g2d.setColor(CARD_BACKGROUND);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                // Gradient border
                if (animationsEnabled) {
                    glowPhase += 0.05f;
                    float alpha = (float) (0.3 + 0.2 * Math.sin(glowPhase));
                    GradientPaint borderGradient = new GradientPaint(
                            0, 0, new Color(GLOW_COLOR_START.getRed(), GLOW_COLOR_START.getGreen(), GLOW_COLOR_START.getBlue(), (int) (alpha * 255)),
                            getWidth(), getHeight(), new Color(GLOW_COLOR_END.getRed(), GLOW_COLOR_END.getGreen(), GLOW_COLOR_END.getBlue(), (int) (alpha * 255))
                    );
                    g2d.setPaint(borderGradient);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));
                }

                // Apply fade-in opacity
                int cardIndex = getCardIndex(this);
                if (cardIndex >= 0 && cardIndex < cardOpacities.length) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cardOpacities[cardIndex]));
                }
            }

            private int getCardIndex(JPanel panel) {
                Component[] components = ((JPanel) panel.getParent()).getComponents();
                for (int i = 0; i < components.length; i++) {
                    if (components[i] == panel) return i;
                }
                return -1;
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Icon label
        Font iconFont = getFontWithFallback("Inter", Font.PLAIN, 32);
        JLabel iconLabel = createGlowingLabel(icon, iconFont, TEXT_COLOR, GLOW_COLOR_START);

        // Title label
        Font titleFont = getFontWithFallback("Inter", Font.PLAIN, 20);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(TITLE_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Value label
        Font valueFont = getFontWithFallback("Inter", Font.BOLD, 48);
        JLabel valueLabel = createGlowingLabel(value, valueFont, TEXT_COLOR, GLOW_COLOR_START);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Layout for title and value
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(valueLabel);

        // Combine icon and text
        JPanel contentPanel = new JPanel(new BorderLayout(25, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel, BorderLayout.WEST);
        contentPanel.add(textPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
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
        label.setFont(font.deriveFont(Font.PLAIN)); // Ensure consistent rendering
        label.setForeground(textColor);
        label.setOpaque(false);
        return label;
    }
}