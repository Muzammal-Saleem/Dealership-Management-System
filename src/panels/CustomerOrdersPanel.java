package panels;

import Database.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrdersPanel extends JPanel {
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private final int customerID;
    private JComboBox<String> statusFilter;
    private JLabel statusLabel;
    private JLabel summaryLabel;
    private Timer animationTimer;
    private float tableOpacity = 0f;
    private boolean animationsEnabled = true;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("RS:#,##0.00");

    // Colors matching VehiclePanel theme
    private final Color BACKGROUND_BASE = new Color(5, 5, 6);
    private Color currentBackground = BACKGROUND_BASE;
    private final Color TABLE_BG = new Color(21, 21, 24, 230);
    private final Color GLOW_COLOR_START = new Color(0, 163, 255);
    private final Color GLOW_COLOR_END = new Color(123, 104, 238);
    private final Color TEXT_COLOR = new Color(255, 255, 255);
    private final Color SECONDARY_TEXT_COLOR = new Color(160, 174, 192);
    private final Color INPUT_BG = new Color(21, 21, 24, 230);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private final Color WARNING_COLOR = new Color(251, 191, 36);
    private final Color DANGER_COLOR = new Color(239, 68, 68);

    private boolean isInitialLoad = true; // Flag to track initial load vs. refresh

    public CustomerOrdersPanel(int customerID) {
        this.customerID = customerID;

        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_BASE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        initializeComponents();
        startAnimation();
        startFadeInAnimation();
        loadOrdersAsync(); // Initial load
    }

    private void initializeComponents() {
        // Header
        JLabel headerLabel = createGlowingLabel("Your Orders", new Font("Inter", Font.BOLD, 24), TEXT_COLOR, GLOW_COLOR_START);
        headerLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.add(headerLabel, BorderLayout.WEST);

        // Button panel for refresh
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton refreshButton = createStyledButton("Refresh Orders", GLOW_COLOR_START, GLOW_COLOR_END);
        refreshButton.addActionListener(e -> {
            isInitialLoad = false; // Set flag to false for refresh
            loadOrdersAsync();
        });
        buttonPanel.add(refreshButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Filter panel
        JPanel filterPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TABLE_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        filterPanel.setOpaque(false);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Filter controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.setOpaque(false);
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel filterLabel = new JLabel("Filter by Status:");
        filterLabel.setFont(new Font("Inter", Font.BOLD, 14));
        filterLabel.setForeground(SECONDARY_TEXT_COLOR);
        controlsPanel.add(filterLabel);

        statusFilter = new JComboBox<>(new String[]{"All Orders", "Pending", "Completed", "Cancelled"}) {
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
        statusFilter.setFont(new Font("Inter", Font.PLAIN, 14));
        statusFilter.setBackground(INPUT_BG);
        statusFilter.setForeground(TEXT_COLOR);
        statusFilter.addActionListener(e -> applyFilter());
        controlsPanel.add(statusFilter);

        filterPanel.add(controlsPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"Sale ID", "Invoice", "VIN", "Make", "Model", "Year",
                "Sale Date", "Sale Price", "Tax", "Total Price", "Status", "Sales Person"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ordersTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(60, 80, 120, 200));
                } else {
                    c.setBackground(row % 2 == 0 ? TABLE_BG : new Color(25, 25, 28, 230));
                }
                c.setForeground(TEXT_COLOR);
                return c;
            }
        };
        ordersTable.setOpaque(false);
        ordersTable.setShowGrid(false);
        ordersTable.setRowHeight(30);
        ordersTable.setFont(new Font("Inter", Font.PLAIN, 14));
        ordersTable.setSelectionForeground(TEXT_COLOR);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        ordersTable.setRowSorter(sorter);

        // Center-align cell contents and apply custom rendering
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);

                if (column == 6) {
                    setText(value != null ? value.toString() : ""); // SaleDate is already formatted as string
                } else if (value instanceof java.math.BigDecimal && (column == 7 || column == 8 || column == 9)) {
                    setText(CURRENCY_FORMAT.format(value));
                } else if (column == 10 && value != null) {
                    String status = value.toString();
                    if (!isSelected) {
                        switch (status.toLowerCase()) {
                            case "completed": setForeground(SUCCESS_COLOR); break;
                            case "pending": setForeground(WARNING_COLOR); break;
                            case "cancelled": setForeground(DANGER_COLOR); break;
                            default: setForeground(TEXT_COLOR); break;
                        }
                    }
                    setText("● " + status);
                } else {
                    setText(value != null ? value.toString() : "");
                }

                return c;
            }
        };
        for (int i = 0; i < ordersTable.getColumnCount(); i++) {
            ordersTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = ordersTable.getTableHeader();
        header.setOpaque(false);
        header.setBackground(new Color(0, 153, 255));
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Inter", Font.BOLD, 14));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(ordersTable) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(TABLE_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        filterPanel.add(scrollPane, BorderLayout.CENTER);
        add(filterPanel, BorderLayout.CENTER);

        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        statusLabel = new JLabel("Loading orders...");
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        statusLabel.setForeground(SECONDARY_TEXT_COLOR);
        summaryPanel.add(statusLabel, BorderLayout.WEST);

        summaryLabel = new JLabel("");
        summaryLabel.setFont(new Font("Inter", Font.BOLD, 12));
        summaryLabel.setForeground(TEXT_COLOR);
        summaryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        summaryPanel.add(summaryLabel, BorderLayout.EAST);

        add(summaryPanel, BorderLayout.SOUTH);
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
            if (tableOpacity < 1f) {
                tableOpacity = Math.min(1f, tableOpacity + 0.05f);
                repaint();
            } else {
                ((Timer) e.getSource()).stop();
            }
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
        if (tableOpacity < 1f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tableOpacity));
        }
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
                        int x = 0;
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
                int x = 0;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        label.setFont(font);
        label.setForeground(textColor);
        label.setOpaque(false);
        return label;
    }

    private void applyFilter() {
        String selectedFilter = (String) statusFilter.getSelectedItem();
        if ("All Orders".equals(selectedFilter)) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + selectedFilter, 10));
        }
        updateSummaryInfo();
    }

    private void updateSummaryInfo() {
        int totalRows = tableModel.getRowCount();
        int visibleRows = ordersTable.getRowCount();

        if (totalRows == 0) {
            summaryLabel.setText("");
            return;
        }

        double totalAmount = 0;
        int completedOrders = 0;
        int pendingOrders = 0;
        int cancelledOrders = 0;

        for (int i = 0; i < totalRows; i++) {
            Object priceObj = tableModel.getValueAt(i, 9);
            if (priceObj instanceof java.math.BigDecimal) {
                totalAmount += ((java.math.BigDecimal) priceObj).doubleValue();
            }

            String status = tableModel.getValueAt(i, 10).toString().toLowerCase();
            switch (status) {
                case "completed": completedOrders++; break;
                case "pending": pendingOrders++; break;
                case "cancelled": cancelledOrders++; break;
            }
        }

        String summaryText = String.format("Total: %d orders | Completed: %d | Pending: %d | Cancelled: %d | Total Value: %s",
                totalRows, completedOrders, pendingOrders, cancelledOrders, CURRENCY_FORMAT.format(totalAmount));

        if (visibleRows != totalRows) {
            summaryText = String.format("Showing: %d of %d orders | %s",
                    visibleRows, totalRows, summaryText.substring(summaryText.indexOf("|") + 2));
        }

        summaryLabel.setText(summaryText);
    }

    private void loadOrdersAsync() {
        SwingUtilities.invokeLater(() -> {
            Component[] components = findRefreshButton(this);
            for (Component comp : components) {
                if (comp instanceof JButton && ((JButton) comp).getText().equals("Refresh Orders")) {
                    comp.setEnabled(false);
                    break;
                }
            }
            statusLabel.setText("Loading orders...");
            summaryLabel.setText("");
        });

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                loadOrders();
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    Component[] components = findRefreshButton(CustomerOrdersPanel.this);
                    for (Component comp : components) {
                        if (comp instanceof JButton && ((JButton) comp).getText().equals("Refresh Orders")) {
                            comp.setEnabled(true);
                            break;
                        }
                    }
                    updateSummaryInfo();
                });
            }
        };
        worker.execute();
    }

    private Component[] findRefreshButton(Container container) {
        List<Component> components = new ArrayList<>();
        findComponents(container, components);
        return components.toArray(new Component[0]);
    }

    private void findComponents(Container container, List<Component> components) {
        for (Component comp : container.getComponents()) {
            components.add(comp);
            if (comp instanceof Container) {
                findComponents((Container) comp, components);
            }
        }
    }

    private void loadOrders() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            statusLabel.setText("Loading orders...");
        });

        List<Object[]> orders;
        try {
            orders = DBConnection.getOrdersFromDatabase(customerID);
            System.out.println("Orders retrieved: " + (orders != null ? orders.size() : "null")); // Debug output
        } catch (SQLException e) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Error loading orders.");
                JOptionPane.showMessageDialog(this,
                        "Failed to load orders: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            });
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (!isInitialLoad && (orders == null || orders.isEmpty())) {
                statusLabel.setText("No orders found for this customer.");
                JOptionPane.showMessageDialog(this,
                        "No orders found for this customer.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (orders != null && !orders.isEmpty()) {
                for (Object[] row : orders) {
                    tableModel.addRow(row);
                }
                int rowCount = orders.size();
                statusLabel.setText(rowCount + " order(s) loaded successfully.");
            } else if (isInitialLoad) {
                statusLabel.setText("No orders loaded yet. Try refreshing.");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Customer Orders");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            frame.getContentPane().setBackground(new Color(5, 5, 6));

            CustomerOrdersPanel panel = new CustomerOrdersPanel(13); // Test with customerID 13
            frame.add(panel);

            frame.setVisible(true);
        });
    }
}