package panels;

import Database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class SalesPanel extends JPanel {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusCombo;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private final Color BACKGROUND_BASE = new Color(5, 5, 6); // Near-black (#050506)
    private Color currentBackground = BACKGROUND_BASE;
    private final Color TABLE_BG = new Color(21, 21, 24, 230); // Dark gray (#151518, 90% opacity)
    private final Color GLOW_COLOR_START = new Color(0, 163, 255); // Blue (#00A3FF)
    private final Color GLOW_COLOR_END = new Color(123, 104, 238); // Purple (#7B68EE)
    private final Color TEXT_COLOR = new Color(255, 255, 255); // Pure white (#FFFFFF)
    private final Color SECONDARY_TEXT_COLOR = new Color(160, 174, 192); // Soft gray (#A0AEC0)
    private final Color INPUT_BG = new Color(21, 21, 24, 230); // Matches table
    private Timer animationTimer;
    private boolean animationsEnabled = true;
    private float tableOpacity = 0f;

    public SalesPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_BASE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setOpaque(false);

        initComponents();
        startAnimation();
        startFadeInAnimation();
        try {
            loadSales();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load sales data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        // Header
        JLabel headerLabel = createGlowingLabel("Sales Management", new Font("Inter", Font.BOLD, 28), TEXT_COLOR, GLOW_COLOR_START);
        headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(headerLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(SECONDARY_TEXT_COLOR);
        searchLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        controlPanel.add(searchLabel);

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
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchSales(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchSales(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchSales(); }
        });
        controlPanel.add(searchField);

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setForeground(SECONDARY_TEXT_COLOR);
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        controlPanel.add(statusLabel);

        statusCombo = new JComboBox<>(new String[]{"All", "Pending", "Completed"}) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        statusCombo.setBackground(INPUT_BG);
        statusCombo.setForeground(TEXT_COLOR);
        statusCombo.setFont(new Font("Inter", Font.PLAIN, 14));
        statusCombo.addActionListener(e -> searchSales());
        controlPanel.add(statusCombo);

        centerPanel.add(controlPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Invoice Number", "Vehicle", "Customer", "Employee", "Sale Date", "Sale Price", "Tax Amount", "Total Price", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesTable = new JTable(tableModel) {
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
        salesTable.setOpaque(false);
        salesTable.setShowGrid(false);
        salesTable.setRowHeight(30);
        salesTable.setFont(new Font("Inter", Font.PLAIN, 14));
        salesTable.setSelectionForeground(TEXT_COLOR);
        salesTable.getTableHeader().setOpaque(false);
        salesTable.getTableHeader().setBackground(new Color(0, 153, 255));
        salesTable.getTableHeader().setForeground(TEXT_COLOR);
        salesTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 15));
        salesTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Center-align cell contents
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < salesTable.getColumnCount(); i++) {
            salesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(salesTable) {
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
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        addButton = createStyledButton("Add Sale", GLOW_COLOR_START, GLOW_COLOR_END);
        addButton.addActionListener(e -> showSaleForm(null));
        buttonPanel.add(addButton);

        editButton = createStyledButton("Edit Sale", GLOW_COLOR_START, GLOW_COLOR_END);
        editButton.addActionListener(e -> {
            int selectedRow = salesTable.getSelectedRow();
            if (selectedRow != -1) {
                int saleId = (int) tableModel.getValueAt(selectedRow, 0);
                showSaleForm(saleId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a sale to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(editButton);

        deleteButton = createStyledButton("Delete Sale", new Color(200, 50, 50), new Color(255, 100, 100));
        deleteButton.addActionListener(e -> deleteSale());
        buttonPanel.add(deleteButton);

        refreshButton = createStyledButton("Refresh", GLOW_COLOR_START, GLOW_COLOR_END);
        refreshButton.addActionListener(e -> {
            try {
                loadSales();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to refresh sales: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(refreshButton);

        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
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

    private void loadSales() {
        try {
            List<Vector<Object>> sales = DBConnection.getAllSales();
            tableModel.setRowCount(0);
            for (Vector<Object> sale : sales) {
                tableModel.addRow(sale);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading sales: " + e.getMessage(), e);
        }
    }

    private void searchSales() {
        try {
            String searchText = searchField.getText().trim();
            String status = (String) statusCombo.getSelectedItem();
            List<Vector<Object>> sales = DBConnection.searchSales(searchText, status);
            tableModel.setRowCount(0);
            for (Vector<Object> sale : sales) {
                tableModel.addRow(sale);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to search sales: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSale() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow != -1) {
            int saleId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this sale?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = DBConnection.deleteSale(saleId);
                    if (success) {
                        loadSales();
                        JOptionPane.showMessageDialog(this, "Sale deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete sale.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Failed to delete sale: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a sale to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSaleForm(Integer saleId) {
        JDialog formDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), saleId == null ? "Add Sale" : "Edit Sale", true);
        formDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(21, 21, 24, 230));
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
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formDialog.setContentPane(contentPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        String[] labels = {"Vehicle ID:", "Customer ID:", "Employee ID:", "Sale Date (YYYY-MM-DD):", "Sale Price:", "Tax Amount:", "Invoice Number:", "Status:"};
        JTextField[] fields = new JTextField[labels.length - 1];
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"Pending", "Completed"});

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setForeground(SECONDARY_TEXT_COLOR);
            label.setFont(new Font("Inter", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.EAST;
            contentPanel.add(label, gbc);

            if (i < labels.length - 1) {
                fields[i] = new JTextField(20);
                fields[i].setBackground(INPUT_BG);
                fields[i].setForeground(TEXT_COLOR);
                fields[i].setCaretColor(TEXT_COLOR);
                fields[i].setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                fields[i].setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                contentPanel.add(fields[i], gbc);
            } else {
                statusComboBox.setBackground(INPUT_BG);
                statusComboBox.setForeground(TEXT_COLOR);
                statusComboBox.setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                contentPanel.add(statusComboBox, gbc);
            }
        }

        if (saleId != null) {
            try {
                ResultSet rs = DBConnection.getSaleById(saleId);
                if (rs.next()) {
                    fields[0].setText(String.valueOf(rs.getInt("VehicleID")));
                    fields[1].setText(String.valueOf(rs.getInt("CustomerID")));
                    fields[2].setText(String.valueOf(rs.getInt("EmployeeID")));
                    fields[3].setText(rs.getString("SaleDate"));
                    fields[4].setText(String.valueOf(rs.getDouble("SalePrice")));
                    fields[5].setText(String.valueOf(rs.getDouble("TaxAmount")));
                    fields[6].setText(rs.getString("InvoiceNumber"));
                    statusComboBox.setSelectedItem(rs.getString("SaleStatus"));
                } else {
                    JOptionPane.showMessageDialog(formDialog, "Sale record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    formDialog.dispose();
                    return;
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(formDialog, "Failed to fetch sale: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                formDialog.dispose();
                return;
            }
        }

        JButton saveButton = createStyledButton(saleId == null ? "Add" : "Update", GLOW_COLOR_START, GLOW_COLOR_END);
        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            try {
                String vehicleIdStr = fields[0].getText().trim();
                String customerIdStr = fields[1].getText().trim();
                String employeeIdStr = fields[2].getText().trim();
                String saleDate = fields[3].getText().trim();
                String salePriceStr = fields[4].getText().trim();
                String taxAmountStr = fields[5].getText().trim();
                String invoiceNumber = fields[6].getText().trim();
                String status = (String) statusComboBox.getSelectedItem();

                if (vehicleIdStr.isEmpty() || customerIdStr.isEmpty() || employeeIdStr.isEmpty() || saleDate.isEmpty() || salePriceStr.isEmpty() || taxAmountStr.isEmpty() || invoiceNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(formDialog, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!saleDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(formDialog, "Please enter a valid date in YYYY-MM-DD format.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int vehicleId = Integer.parseInt(vehicleIdStr);
                int customerId = Integer.parseInt(customerIdStr);
                int employeeId = Integer.parseInt(employeeIdStr);
                double salePrice = Double.parseDouble(salePriceStr);
                double taxAmount = Double.parseDouble(taxAmountStr);

                if (salePrice <= 0 || taxAmount < 0) {
                    JOptionPane.showMessageDialog(formDialog, "Sale Price must be positive and Tax Amount cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success;
                if (saleId == null) {
                    success = DBConnection.addSale(vehicleId, customerId, employeeId, saleDate, salePrice, taxAmount, invoiceNumber, status);
                } else {
                    success = DBConnection.updateSale(saleId, vehicleId, customerId, employeeId, saleDate, salePrice, taxAmount, invoiceNumber, status);
                }

                if (success) {
                    loadSales();
                    JOptionPane.showMessageDialog(formDialog, saleId == null ? "Sale added successfully." : "Sale updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    formDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(formDialog, "Failed to " + (saleId == null ? "add" : "update") + " sale. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(formDialog, "Please enter valid numeric values for IDs, Sale Price, and Tax Amount.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(formDialog, "Error saving sale: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formDialog.pack();
        formDialog.setLocationRelativeTo(this);
        formDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Sales Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.add(new SalesPanel());
            frame.setVisible(true);
        });
    }
}