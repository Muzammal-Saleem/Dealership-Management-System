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
import java.util.List;
import java.util.Vector;

import static Database.DBConnection.getEmployeeIdByUsername;

public class PendingOrdersPanel extends JPanel {
    private JTable ordersTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton searchButton, refreshButton;
    private final String employeeId; // Logged-in employee's ID

    private final Color BACKGROUND_BASE = new Color(5, 5, 6);
    private Color currentBackground = BACKGROUND_BASE;
    private final Color TABLE_BG = new Color(21, 21, 24, 230);
    private final Color GLOW_COLOR_START = new Color(0, 163, 255);
    private final Color GLOW_COLOR_END = new Color(123, 104, 238);
    private final Color TEXT_COLOR = new Color(255, 255, 255);
    private final Color SECONDARY_TEXT_COLOR = new Color(160, 174, 192);
    private final Color INPUT_BG = new Color(21, 21, 24, 230);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private Timer animationTimer;
    private boolean animationsEnabled = true;
    private float tableOpacity = 0f;

    public PendingOrdersPanel(String username) {
        String employeeId=getEmployeeIdByUsername(username);
        System.out.println("employeeId "+employeeId);
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty. Please ensure a valid employee ID is provided.");
        }
        this.employeeId = employeeId;
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_BASE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setOpaque(false);

        initializeComponents();
        startAnimation();
        startFadeInAnimation();
        loadPendingOrders();
        System.out.println("OrdersPanel initialized with employeeId: " + this.employeeId);
    }

    private void initializeComponents() {
        JLabel headerLabel = createGlowingLabel("Pending Customer Orders", new Font("Inter", Font.BOLD, 28), TEXT_COLOR, GLOW_COLOR_START);
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
        searchButton.addActionListener(e -> searchOrders());
        searchPanel.add(searchButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        refreshButton = createStyledButton("Refresh", GLOW_COLOR_START, GLOW_COLOR_END);
        refreshButton.addActionListener(e -> {
            loadPendingOrders();
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No pending orders found.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        buttonPanel.add(refreshButton);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        String[] columns = {"SaleID", "Invoice#", "Customer", "Vehicle", "Sale Date", "Sale Price", "Tax", "Total", "Status", "Confirm"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9; // Only the "Confirm" button column is editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // SaleID
                if (columnIndex == 5 || columnIndex == 6 || columnIndex == 7) return BigDecimal.class; // Prices
                return String.class;
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
        ordersTable.setRowHeight(35);
        ordersTable.setFont(new Font("Inter", Font.PLAIN, 14));
        ordersTable.setSelectionForeground(TEXT_COLOR);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide SaleID column
        ordersTable.getColumnModel().getColumn(0).setMinWidth(0);
        ordersTable.getColumnModel().getColumn(0).setMaxWidth(0);
        ordersTable.getColumnModel().getColumn(0).setWidth(0);

        // Set column widths
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Invoice#
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Customer
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Vehicle
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Sale Date
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Sale Price
        ordersTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Tax
        ordersTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Total
        ordersTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Status
        ordersTable.getColumnModel().getColumn(9).setPreferredWidth(100); // Confirm

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < ordersTable.getColumnCount() - 1; i++) {
            ordersTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        ordersTable.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer());
        ordersTable.getColumnModel().getColumn(9).setCellEditor(new ButtonEditor(new JCheckBox()));

        JTableHeader header = ordersTable.getTableHeader();
        header.setOpaque(false);
        header.setBackground(new Color(0, 153, 255));
        header.setForeground(TEXT_COLOR);
        header.setFont(new Font("Inter", Font.BOLD, 15));
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

    private void loadPendingOrders() {
        tableModel.setRowCount(0);
        List<Vector<Object>> orders = DBConnection.getPendingOrders();
        for (Vector<Object> row : orders) {
            tableModel.addRow(new Object[]{
                    row.get(0), // SaleID
                    row.get(1), // InvoiceNumber
                    row.get(2), // Customer Name
                    row.get(3), // Vehicle Info
                    row.get(4), // SaleDate
                    row.get(5), // SalePrice
                    row.get(6), // TaxAmount
                    row.get(7), // TotalPrice
                    row.get(8), // SaleStatus
                    "Confirm" // Confirm button
            });
        }
    }

    private void searchOrders() {
        String searchText = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Vector<Object>> orders = searchText.isEmpty() ?
                DBConnection.getPendingOrders() :
                DBConnection.searchPendingOrders(searchText);
        for (Vector<Object> row : orders) {
            tableModel.addRow(new Object[]{
                    row.get(0), // SaleID
                    row.get(1), // InvoiceNumber
                    row.get(2), // Customer Name
                    row.get(3), // Vehicle Info
                    row.get(4), // SaleDate
                    row.get(5), // SalePrice
                    row.get(6), // TaxAmount
                    row.get(7), // TotalPrice
                    row.get(8), // SaleStatus
                    "Confirm" // Confirm button
            });
        }
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No matching pending orders found.",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Inter", Font.BOLD, 14));
            setForeground(TEXT_COLOR);
            setBackground(SUCCESS_COLOR);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Confirm" : value.toString());
            if (isSelected) {
                setBackground(new Color(60, 80, 120, 200));
            } else {
                setBackground(SUCCESS_COLOR);
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int clickedRow;
        private boolean shouldRemoveRow = false; // Flag to indicate if row should be removed

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Inter", Font.BOLD, 14));
            button.setForeground(TEXT_COLOR);
            button.setBackground(SUCCESS_COLOR);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "Confirm" : value.toString();
            button.setText(label);
            isPushed = true;
            clickedRow = row;
            shouldRemoveRow = false; // Reset flag
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int saleId = (Integer) tableModel.getValueAt(clickedRow, 0);
                String invoiceNumber = (String) tableModel.getValueAt(clickedRow, 1);
                String customerName = (String) tableModel.getValueAt(clickedRow, 2);
                String vehicleInfo = (String) tableModel.getValueAt(clickedRow, 3);
                BigDecimal totalPrice = (BigDecimal) tableModel.getValueAt(clickedRow, 7);

                int confirm = JOptionPane.showConfirmDialog(
                        PendingOrdersPanel.this,
                        String.format("Confirm this order?\n\n" +
                                        "Invoice: %s\n" +
                                        "Customer: %s\n" +
                                        "Vehicle: %s\n" +
                                        "Total: RS %.2f\n\n" +
                                        "This will:\n" +
                                        "- Mark the sale as 'Completed'\n" +
                                        "- Update vehicle status to 'Sold'\n" +
                                        "- Assign you as the sales representative",
                                invoiceNumber, customerName, vehicleInfo, totalPrice),
                        "Confirm Order",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    System.out.println("salesid: " +saleId+"EMployee ID: "+Integer.parseInt(employeeId));
                    boolean success = DBConnection.confirmOrder(saleId, Integer.parseInt(employeeId));
                    System.out.println("salesid: " +saleId+"EMployee ID: "+Integer.parseInt(employeeId));
                    System.out.println(saleId + " employee id" + Integer.parseInt(employeeId));

                    if (success) {
                        JOptionPane.showMessageDialog(
                                PendingOrdersPanel.this,
                                String.format("Order confirmed successfully!\n\n" +
                                                "Invoice: %s\n" +
                                                "Customer: %s\n" +
                                                "Vehicle: %s\n" +
                                                "Status: Completed",
                                        invoiceNumber, customerName, vehicleInfo),
                                "Order Confirmed",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        shouldRemoveRow = true; // Set flag to remove the row after editing
                    } else {
                        JOptionPane.showMessageDialog(
                                PendingOrdersPanel.this,
                                "Failed to confirm the order. Please try again or contact system administrator.",
                                "Confirmation Failed",
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
            boolean result = super.stopCellEditing();
            // Remove the row after editing is fully stopped, if needed
            if (shouldRemoveRow && clickedRow < tableModel.getRowCount()) {
                tableModel.removeRow(clickedRow);
                // Show remaining orders count
                if (tableModel.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(
                            PendingOrdersPanel.this,
                            "All pending orders have been processed!",
                            "No Pending Orders",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
            return result;
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Orders Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.add(new PendingOrdersPanel("umair")); // Example EmployeeID
            frame.setVisible(true);
        });
    }
}