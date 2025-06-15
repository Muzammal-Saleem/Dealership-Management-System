package panels;

import Database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CustomerVehiclesPanel extends JPanel {
    private JTable vehicleTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryComboBox;
    private JButton searchButton, refreshButton;
    private final String customerId; // Logged-in customer's ID

    private final Color BACKGROUND_BASE = new Color(5, 5, 6);
    private Color currentBackground = BACKGROUND_BASE;
    private final Color TABLE_BG = new Color(21, 21, 24, 230);
    private final Color GLOW_COLOR_START = new Color(0, 163, 255);
    private final Color GLOW_COLOR_END = new Color(123, 104, 238);
    private final Color TEXT_COLOR = new Color(255, 255, 255);
    private final Color SECONDARY_TEXT_COLOR = new Color(160, 174, 192);
    private final Color INPUT_BG = new Color(21, 21, 24, 230);
    private Timer animationTimer;
    private boolean animationsEnabled = true;
    private float tableOpacity = 0f;

    public CustomerVehiclesPanel(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty. Please ensure a valid customer ID is provided.");
        }
        this.customerId = customerId;
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_BASE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setOpaque(false);

        initializeComponents();
        startAnimation();
        startFadeInAnimation();
        loadAvailableVehicles();
        System.out.println("CustomerVehiclesPanel initialized with customerId: " + this.customerId); // Debug statement
    }

    private void initializeComponents() {
        JLabel headerLabel = createGlowingLabel("Available Vehicles", new Font("Inter", Font.BOLD, 28), TEXT_COLOR, GLOW_COLOR_START);
        headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(headerLabel, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(SECONDARY_TEXT_COLOR);
        searchLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        searchPanel.add(searchLabel);

        searchField = new JTextField(20) {
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
        searchField.setBackground(INPUT_BG);
        searchField.setForeground(TEXT_COLOR);
        searchField.setCaretColor(TEXT_COLOR);
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        searchField.setFont(new Font("Inter", Font.PLAIN, 14));
        searchPanel.add(searchField);

        searchButton = createStyledButton("Search", GLOW_COLOR_START, GLOW_COLOR_END);
        searchButton.addActionListener(e -> searchVehicles());
        searchPanel.add(searchButton);

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setForeground(SECONDARY_TEXT_COLOR);
        categoryLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        searchPanel.add(categoryLabel);

        categoryComboBox = new JComboBox<>() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        loadCategories(categoryComboBox);
        categoryComboBox.setBackground(INPUT_BG);
        categoryComboBox.setForeground(TEXT_COLOR);
        categoryComboBox.setFont(new Font("Inter", Font.PLAIN, 14));
        categoryComboBox.addActionListener(e -> filterByCategory());
        searchPanel.add(categoryComboBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        refreshButton = createStyledButton("Refresh", GLOW_COLOR_START, GLOW_COLOR_END);
        refreshButton.addActionListener(e -> loadAvailableVehicles());
        buttonPanel.add(refreshButton);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        String[] columns = {"VehicleID", "Make", "Model", "Year", "Color", "Mileage", "Condition", "List Price", "Category", "Buy"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9; // Only the "Buy" button column is editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // VehicleID
                if (columnIndex == 3) return Integer.class; // Year
                if (columnIndex == 5) return Integer.class; // Mileage
                if (columnIndex == 7) return BigDecimal.class; // List Price
                return String.class;
            }
        };
        vehicleTable = new JTable(tableModel) {
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
        vehicleTable.setOpaque(false);
        vehicleTable.setShowGrid(false);
        vehicleTable.setRowHeight(30);
        vehicleTable.setFont(new Font("Inter", Font.PLAIN, 14));
        vehicleTable.setSelectionForeground(TEXT_COLOR);
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        vehicleTable.getColumnModel().getColumn(0).setMinWidth(0);
        vehicleTable.getColumnModel().getColumn(0).setMaxWidth(0);
        vehicleTable.getColumnModel().getColumn(0).setWidth(0);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < vehicleTable.getColumnCount() - 1; i++) {
            vehicleTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        vehicleTable.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer());
        vehicleTable.getColumnModel().getColumn(9).setCellEditor(new ButtonEditor(new JCheckBox()));

        JTableHeader header = vehicleTable.getTableHeader();
        header.setOpaque(false);
        header.setBackground(new Color(0, 153, 255));
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Inter", Font.BOLD, 15));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(vehicleTable) {
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

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
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

    private void loadAvailableVehicles() {
        tableModel.setRowCount(0);
        List<Vector<Object>> vehicles = DBConnection.filterVehiclesByStatus("Available");
        for (Vector<Object> row : vehicles) {
            tableModel.addRow(new Object[]{
                    row.get(0), // VehicleID
                    row.get(2), // Make
                    row.get(3), // Model
                    row.get(4), // Year
                    row.get(5), // Color
                    row.get(6), // Mileage
                    row.get(7), // Condition
                    row.get(9), // ListPrice
                    row.get(11), // CategoryName
                    "Buy" // Buy button
            });
        }
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No available vehicles found.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void searchVehicles() {
        String searchText = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Vector<Object>> vehicles = searchText.isEmpty() ?
                DBConnection.filterVehiclesByStatus("Available") :
                DBConnection.searchVehicles(searchText).stream()
                        .filter(row -> "Available".equals(row.get(10))) // Filter for Status = 'Available'
                        .collect(Collectors.toList());
        for (Vector<Object> row : vehicles) {
            tableModel.addRow(new Object[]{
                    row.get(0), // VehicleID
                    row.get(2), // Make
                    row.get(3), // Model
                    row.get(4), // Year
                    row.get(5), // Color
                    row.get(6), // Mileage
                    row.get(7), // Condition
                    row.get(9), // ListPrice
                    row.get(11), // CategoryName
                    "Buy" // Buy button
            });
        }
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No matching available vehicles found.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void filterByCategory() {
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        tableModel.setRowCount(0);
        List<Vector<Object>> vehicles = DBConnection.filterVehiclesByStatus("Available");
        if (!"All".equals(selectedCategory)) {
            vehicles = vehicles.stream()
                    .filter(row -> selectedCategory.equals(row.get(11))) // Filter for CategoryName
                    .collect(Collectors.toList());
        }
        for (Vector<Object> row : vehicles) {
            tableModel.addRow(new Object[]{
                    row.get(0), // VehicleID
                    row.get(2), // Make
                    row.get(3), // Model
                    row.get(4), // Year
                    row.get(5), // Color
                    row.get(6), // Mileage
                    row.get(7), // Condition
                    row.get(9), // ListPrice
                    row.get(11), // CategoryName
                    "Buy" // Buy button
            });
        }
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No available vehicles found in this category.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadCategories(JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        comboBox.addItem("All");
        List<String> categories = DBConnection.getAllCategories();
        for (String category : categories) {
            comboBox.addItem(category);
        }
        if (comboBox.getItemCount() == 1) {
            comboBox.addItem("No Categories Available");
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Inter", Font.BOLD, 14));
            setForeground(TEXT_COLOR);
            setBackground(GLOW_COLOR_START);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Buy" : value.toString());
            if (isSelected) {
                setBackground(new Color(60, 80, 120, 200));
            } else {
                setBackground(GLOW_COLOR_START);
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int clickedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Inter", Font.BOLD, 14));
            button.setForeground(TEXT_COLOR);
            button.setBackground(GLOW_COLOR_START);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "Buy" : value.toString();
            button.setText(label);
            isPushed = true;
            clickedRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                String vehicleId = String.valueOf(tableModel.getValueAt(clickedRow, 0)); // VehicleID
                String make = (String) tableModel.getValueAt(clickedRow, 1);
                String model = (String) tableModel.getValueAt(clickedRow, 2);
                int year = (Integer) tableModel.getValueAt(clickedRow, 3);
                BigDecimal listPrice = (BigDecimal) tableModel.getValueAt(clickedRow, 7);

                // Verify vehicle is still available
                List<Vector<Object>> vehicleCheck = DBConnection.filterVehiclesByStatus("Available").stream()
                        .filter(row -> String.valueOf(row.get(0)).equals(vehicleId))
                        .collect(Collectors.toList());
                if (vehicleCheck.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            CustomerVehiclesPanel.this,
                            "This vehicle is no longer available for purchase. Please refresh the list.",
                            "Vehicle Unavailable",
                            JOptionPane.ERROR_MESSAGE
                    );
                    loadAvailableVehicles(); // Refresh the table
                    return label;
                }

                // Show confirmation dialog with purchase details
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String saleDate = dateFormat.format(new java.util.Date()); // Current date: 01:22 AM PKT, May 24, 2025
                double salePrice = listPrice.doubleValue();
                double taxRate = 0.08; // 8% tax rate
                double taxAmount = salePrice * taxRate;
                double totalPrice = salePrice + taxAmount;
                // Shortened InvoiceNumber to fit within 15 characters (e.g., "INV250524-1")
                String invoiceNumber = "INV" + new SimpleDateFormat("yyMMdd").format(new java.util.Date()) + "-" + vehicleId;

                int confirm = JOptionPane.showConfirmDialog(
                        CustomerVehiclesPanel.this,
                        String.format("Please confirm your purchase request:\n\n" +
                                        "Vehicle: %s %s (%d)\n" +
                                        "Price: rs %.2f\n" +
                                        "Tax (8%%): rs %.2f\n" +
                                        "Total: rs %.2f\n" +
                                        "Customer ID: %s\n" +
                                        "Request Date: %s\n\n" +
                                        "This will place the vehicle on hold and notify a sales representative. Proceed?",
                                make, model, year, salePrice, taxAmount, totalPrice, customerId, saleDate),
                        "Confirm Purchase Request",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    // Update vehicle status to "On Hold" using the existing method
                    boolean statusUpdated = DBConnection.updateVehicleStatus(vehicleId, "On Hold");
                    System.out.println("Status update result for vehicle " + vehicleId + ": " + statusUpdated);

                    // Record the sale with "Pending" status and no EmployeeID
                    int custId;
                    try {
                        custId = Integer.parseInt(customerId);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(
                                CustomerVehiclesPanel.this,
                                "Invalid Customer ID format: " + customerId + ". Please log in again.",
                                "Invalid Customer ID",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return label;
                    }
                    boolean saleRecorded = DBConnection.addSale(
                            Integer.parseInt(vehicleId),
                            custId,
                            null, // No employee involved
                            saleDate,
                            salePrice,
                            taxAmount,
                            invoiceNumber,
                            "Pending"
                    );
                    System.out.println("Sale record result for vehicle " + vehicleId + ": " + saleRecorded);

                    if (statusUpdated && saleRecorded) {
                        JOptionPane.showMessageDialog(
                                CustomerVehiclesPanel.this,
                                String.format("Purchase request submitted!\n\n" +
                                                "Vehicle: %s %s (%d)\n" +
                                                "Price: rs %.2f\n" +
                                                "Tax: rs %.2f\n" +
                                                "Total: rs %.2f\n" +
                                                "Invoice: %s\n" +
                                                "The vehicle is now on hold. A sales representative will contact you to finalize the purchase.",
                                        make, model, year, salePrice, taxAmount, totalPrice, invoiceNumber),
                                "Request Submitted",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        tableModel.removeRow(clickedRow);
                    } else {
                        // Rollback status if sale fails
                        if (statusUpdated) {
                            DBConnection.updateVehicleStatus(vehicleId, "Available");
                            System.out.println("Rolled back status for vehicle " + vehicleId + " to Available");
                        }
                        JOptionPane.showMessageDialog(
                                CustomerVehiclesPanel.this,
                                "Failed to submit the purchase request. Please try again or refresh the list. (Debug: Status=" + statusUpdated + ", Sale=" + saleRecorded + ")",
                                "Request Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Available Vehicles");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.add(new CustomerVehiclesPanel("13")); // Example CustomerID
            frame.setVisible(true);
        });
    }
}