package panels;

import Database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.math.BigDecimal;

public class VehiclePanel extends JPanel {
    private JTable vehicleTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusComboBox;
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

    public VehiclePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_BASE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setOpaque(false);

        initializeComponents();
        startAnimation();
        startFadeInAnimation();
        loadVehiclesData();
    }

    private void initializeComponents() {
        // Header
        JLabel headerLabel = createGlowingLabel("Vehicle Management", new Font("Inter", Font.BOLD, 28), TEXT_COLOR, GLOW_COLOR_START);
        headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(headerLabel, BorderLayout.NORTH);

        // Top panel (search and buttons)
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Search panel
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

        JButton searchButton = createStyledButton("Search", GLOW_COLOR_START, GLOW_COLOR_END);
        searchButton.addActionListener(e -> searchVehicles());
        searchPanel.add(searchButton);

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setForeground(SECONDARY_TEXT_COLOR);
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        searchPanel.add(statusLabel);

        statusComboBox = new JComboBox<>(new String[]{"All", "Available", "Sold", "On Hold"}) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(INPUT_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        statusComboBox.setBackground(INPUT_BG);
        statusComboBox.setForeground(TEXT_COLOR);
        statusComboBox.setFont(new Font("Inter", Font.PLAIN, 14));
        statusComboBox.addActionListener(e -> filterByStatus());
        searchPanel.add(statusComboBox);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        addButton = createStyledButton("Add Vehicle", GLOW_COLOR_START, GLOW_COLOR_END);
        addButton.addActionListener(e -> showVehicleForm(null));
        buttonPanel.add(addButton);

        editButton = createStyledButton("Edit", GLOW_COLOR_START, GLOW_COLOR_END);
        editButton.addActionListener(e -> editSelectedVehicle());
        buttonPanel.add(editButton);

        deleteButton = createStyledButton("Delete", new Color(200, 50, 50), new Color(255, 100, 100));
        deleteButton.addActionListener(e -> deleteSelectedVehicle());
        buttonPanel.add(deleteButton);

        refreshButton = createStyledButton("Refresh", GLOW_COLOR_START, GLOW_COLOR_END);
        refreshButton.addActionListener(e -> loadVehiclesData());
        buttonPanel.add(refreshButton);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "VIN", "Make", "Model", "Year", "Color", "Mileage", "Condition", "Purchase Price", "List Price", "Status", "Category"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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

        // Center-align cell contents
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < vehicleTable.getColumnCount(); i++) {
            vehicleTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = vehicleTable.getTableHeader();
        header.setOpaque(false);
        header.setBackground(new Color(0, 153, 255)); // Transparent to match aesthetic
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

    private void loadVehiclesData() {
        tableModel.setRowCount(0);
        List<Vector<Object>> vehicles = DBConnection.getAllVehicles();
        for (Vector<Object> row : vehicles) {
            tableModel.addRow(row);
        }
    }

    private void searchVehicles() {
        String searchText = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Vector<Object>> vehicles = searchText.isEmpty() ? DBConnection.getAllVehicles() : DBConnection.searchVehicles(searchText);
        for (Vector<Object> row : vehicles) {
            tableModel.addRow(row);
        }
    }

    private void filterByStatus() {
        String selectedStatus = (String) statusComboBox.getSelectedItem();
        tableModel.setRowCount(0);
        List<Vector<Object>> vehicles = "All".equals(selectedStatus) ? DBConnection.getAllVehicles() : DBConnection.filterVehiclesByStatus(selectedStatus);
        for (Vector<Object> row : vehicles) {
            tableModel.addRow(row);
        }
    }

    private void loadCategories(JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        List<String> categories = DBConnection.getAllCategories();
        for (String category : categories) {
            comboBox.addItem(category);
        }
        if (comboBox.getItemCount() == 0) {
            comboBox.addItem("No Categories Available");
        }
    }

    private void showVehicleForm(Integer vehicleId) {
        JDialog formDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), vehicleId == null ? "Add Vehicle" : "Edit Vehicle", true);
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

        String[] labels = {"VIN:", "Make:", "Model:", "Year:", "Color:", "Mileage:", "Condition:", "Purchase Price:", "List Price:", "Status:", "Category:"};
        JTextField[] fields = new JTextField[9];
        JComboBox<String> statusField = new JComboBox<>(new String[]{"Available", "Sold", "On Hold"});
        JComboBox<String> categoryField = new JComboBox<>();

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setForeground(SECONDARY_TEXT_COLOR);
            label.setFont(new Font("Inter", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.EAST;
            contentPanel.add(label, gbc);

            if (i == 9) { // Status
                statusField.setBackground(INPUT_BG);
                statusField.setForeground(TEXT_COLOR);
                statusField.setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                contentPanel.add(statusField, gbc);
            } else if (i == 10) { // Category
                loadCategories(categoryField);
                categoryField.setBackground(INPUT_BG);
                categoryField.setForeground(TEXT_COLOR);
                categoryField.setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                contentPanel.add(categoryField, gbc);
            } else {
                fields[i] = new JTextField(20);
                fields[i].setBackground(INPUT_BG);
                fields[i].setForeground(TEXT_COLOR);
                fields[i].setCaretColor(TEXT_COLOR);
                fields[i].setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                fields[i].setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                contentPanel.add(fields[i], gbc);
            }
        }

        if (vehicleId != null) {
            try {
                ResultSet rs = DBConnection.getVehicleById(vehicleId);
                Connection conn = rs.getStatement().getConnection();
                if (rs.next()) {
                    fields[0].setText(rs.getString("VIN"));
                    fields[1].setText(rs.getString("Make"));
                    fields[2].setText(rs.getString("Model"));
                    fields[3].setText(String.valueOf(rs.getInt("Year")));
                    fields[4].setText(rs.getString("Color"));
                    fields[5].setText(String.valueOf(rs.getInt("Mileage")));
                    fields[6].setText(rs.getString("Condition"));
                    fields[7].setText(rs.getBigDecimal("PurchasePrice").toString());
                    fields[8].setText(rs.getBigDecimal("ListPrice").toString());
                    statusField.setSelectedItem(rs.getString("Status"));
                    int categoryId = rs.getInt("CategoryID");
                    String categoryName = DBConnection.getCategoryName(conn, categoryId);
                    categoryField.setSelectedItem(categoryName);
                }
                DBConnection.closeResources(conn, rs.getStatement(), rs);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(formDialog, "Error loading vehicle details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        JButton saveButton = createStyledButton(vehicleId == null ? "Add" : "Update", GLOW_COLOR_START, GLOW_COLOR_END);
        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            String vin = fields[0].getText().trim();
            String make = fields[1].getText().trim();
            String model = fields[2].getText().trim();
            String yearStr = fields[3].getText().trim();
            String color = fields[4].getText().trim();
            String mileageStr = fields[5].getText().trim();
            String condition = fields[6].getText().trim();
            String purchasePriceStr = fields[7].getText().trim();
            String listPriceStr = fields[8].getText().trim();
            String status = (String) statusField.getSelectedItem();
            String category = (String) categoryField.getSelectedItem();

            if (vin.isEmpty() || make.isEmpty() || model.isEmpty()) {
                JOptionPane.showMessageDialog(formDialog, "VIN, Make, and Model are required fields", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int year = Integer.parseInt(yearStr);
                int mileage = Integer.parseInt(mileageStr);
                BigDecimal purchasePrice = new BigDecimal(purchasePriceStr);
                BigDecimal listPrice = new BigDecimal(listPriceStr);

                if (year < 1900 || year > 2100) {
                    JOptionPane.showMessageDialog(formDialog, "Year must be between 1900 and 2100", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (mileage < 0) {
                    JOptionPane.showMessageDialog(formDialog, "Mileage cannot be negative", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0 || listPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(formDialog, "Prices must be positive", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean success;
                if (vehicleId == null) {
                    success = DBConnection.addVehicle(vin, make, model, year, color, mileage, condition, purchasePrice, listPrice, status, category);
                } else {
                    success = DBConnection.updateVehicle(vehicleId, vin, make, model, year, color, mileage, condition, purchasePrice, listPrice, status, category);
                }

                if (success) {
                    JOptionPane.showMessageDialog(formDialog, vehicleId == null ? "Vehicle added successfully" : "Vehicle updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadVehiclesData();
                    formDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(formDialog, "Failed to " + (vehicleId == null ? "add" : "update") + " vehicle", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(formDialog, "Invalid number format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formDialog.pack();
        formDialog.setLocationRelativeTo(this);
        formDialog.setVisible(true);
    }

    private void editSelectedVehicle() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to edit", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int vehicleId = (int) tableModel.getValueAt(selectedRow, 0);
        showVehicleForm(vehicleId);
    }

    private void deleteSelectedVehicle() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle to delete", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int vehicleId = (int) tableModel.getValueAt(selectedRow, 0);
        String make = (String) tableModel.getValueAt(selectedRow, 2);
        String model = (String) tableModel.getValueAt(selectedRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + make + " " + model + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = DBConnection.deleteVehicle(vehicleId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Vehicle deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadVehiclesData();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete vehicle with associated records", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Vehicle Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.add(new VehiclePanel());
            frame.setVisible(true);
        });
    }
}


//package panels;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableCellRenderer;
//import javax.swing.table.DefaultTableModel;
//import javax.swing.table.JTableHeader;
//import java.awt.*;
//import java.sql.*;
//import java.util.Vector;
//import java.util.List;
//import java.math.BigDecimal;
//
//// Import the DBConnection class
//import Database.DBConnection;
//
//// For modern Look and Feel
//import com.formdev.flatlaf.FlatDarkLaf;
//
///**
// * VehiclePanel - Java Swing component for vehicle management in car dealership system
// * Uses DBConnection class to access the database
// */
//public class VehiclePanel extends JPanel {
//    // UI Components
//    private JTable vehicleTable;
//    private DefaultTableModel tableModel;
//    private JTextField searchField;
//    private JComboBox<String> statusComboBox;
//    private JButton addButton, editButton, deleteButton, refreshButton;
//    private JPanel formPanel;
//
//    // Colors for styling
//    private static final Color PRIMARY_COLOR = new Color(50, 100, 200); // Blue for buttons
//    private static final Color DANGER_COLOR = new Color(200, 50, 50);   // Red for delete
//    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30); // Dark background
//    private static final Color TEXT_COLOR = new Color(220, 220, 220);   // Light text
//    private static final Color TABLE_HEADER_COLOR = new Color(40, 60, 100); // Header gradient
//
//    // Constructor
//    public VehiclePanel() {
//        // Set modern Look and Feel
//        try {
//            UIManager.setLookAndFeel(new FlatDarkLaf());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        setLayout(new BorderLayout(10, 10));
//        setBackground(BACKGROUND_COLOR);
//        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//        // Initialize components
//        initializeComponents();
//
//        // Load data from database
//        loadVehiclesData();
//    }
//
//    /**
//     * Initialize all UI components for the vehicle panel
//     */
//    private void initializeComponents() {
//        // Create table model with columns
//        String[] columns = {"ID", "VIN", "Make", "Model", "Year", "Color", "Mileage",
//                "Condition", "Purchase Price", "List Price", "Status", "Category"};
//        tableModel = new DefaultTableModel(columns, 0) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return false; // Make table non-editable
//            }
//        };
//
//        // Create table and scrollpane
//        vehicleTable = new JTable(tableModel);
//        vehicleTable.setFillsViewportHeight(true);
//        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        vehicleTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        vehicleTable.setForeground(TEXT_COLOR);
//        vehicleTable.setBackground(BACKGROUND_COLOR);
//        vehicleTable.setRowHeight(30);
//
//        // Style table header
//        JTableHeader header = vehicleTable.getTableHeader();
//        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        header.setBackground(TABLE_HEADER_COLOR);
//        header.setForeground(TEXT_COLOR);
//        header.setOpaque(false);
//        header.setBorder(BorderFactory.createLineBorder(BACKGROUND_COLOR, 1));
//
//        // Alternating row colors
//        vehicleTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
//                                                           boolean hasFocus, int row, int column) {
//                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//                if (!isSelected) {
//                    c.setBackground(row % 2 == 0 ? BACKGROUND_COLOR : new Color(40, 40, 40));
//                } else {
//                    c.setBackground(new Color(60, 80, 120));
//                }
//                c.setForeground(TEXT_COLOR);
//                return c;
//            }
//        });
//
//        JScrollPane scrollPane = new JScrollPane(vehicleTable);
//        scrollPane.setBorder(BorderFactory.createLineBorder(BACKGROUND_COLOR, 2));
//        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
//
//        // Create search and filter panel
//        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
//        topPanel.setBackground(BACKGROUND_COLOR);
//        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//
//        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
//        searchPanel.setBackground(BACKGROUND_COLOR);
//
//        // Search field
//        JLabel searchLabel = new JLabel("Search:");
//        searchLabel.setForeground(TEXT_COLOR);
//        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        searchPanel.add(searchLabel);
//
//        searchField = new JTextField(20);
//        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        searchField.setBackground(new Color(50, 50, 50));
//        searchField.setForeground(TEXT_COLOR);
//        searchField.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
//                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
//        searchPanel.add(searchField);
//
//        JButton searchButton = new JButton("Search");
//        styleButton(searchButton, PRIMARY_COLOR);
//        searchButton.addActionListener(e -> searchVehicles());
//        searchPanel.add(searchButton);
//
//        // Filter by status
//        JLabel statusLabel = new JLabel("Status:");
//        statusLabel.setForeground(TEXT_COLOR);
//        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        searchPanel.add(statusLabel);
//
//        statusComboBox = new JComboBox<>(new String[]{"All", "Available", "Sold", "On Hold"});
//        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        statusComboBox.setBackground(new Color(50, 50, 50));
//        statusComboBox.setForeground(TEXT_COLOR);
//        statusComboBox.addActionListener(e -> filterByStatus());
//        searchPanel.add(statusComboBox);
//
//        // Button panel
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
//        buttonPanel.setBackground(BACKGROUND_COLOR);
//
//        addButton = new JButton("Add Vehicle");
//        styleButton(addButton, PRIMARY_COLOR);
//        addButton.addActionListener(e -> showAddVehicleForm());
//        buttonPanel.add(addButton);
//
//        editButton = new JButton("Edit");
//        styleButton(editButton, PRIMARY_COLOR);
//        editButton.addActionListener(e -> editSelectedVehicle());
//        buttonPanel.add(editButton);
//
//        deleteButton = new JButton("Delete");
//        styleButton(deleteButton, DANGER_COLOR);
//        deleteButton.addActionListener(e -> deleteSelectedVehicle());
//        buttonPanel.add(deleteButton);
//
//        refreshButton = new JButton("Refresh");
//        styleButton(refreshButton, PRIMARY_COLOR);
//        refreshButton.addActionListener(e -> loadVehiclesData());
//        buttonPanel.add(refreshButton);
//
//        topPanel.add(searchPanel, BorderLayout.WEST);
//        topPanel.add(buttonPanel, BorderLayout.EAST);
//
//        // Form panel for adding/editing vehicles (initially hidden)
//        formPanel = new JPanel();
//        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
//        formPanel.setBackground(BACKGROUND_COLOR);
//        formPanel.setBorder(BorderFactory.createTitledBorder(
//                BorderFactory.createLineBorder(TEXT_COLOR),
//                "Vehicle Details",
//                0, 0, new Font("Segoe UI", Font.BOLD, 14), TEXT_COLOR));
//        formPanel.setVisible(false);
//
//        // Add components to main panel
//        add(topPanel, BorderLayout.NORTH);
//        add(scrollPane, BorderLayout.CENTER);
//        add(formPanel, BorderLayout.SOUTH);
//    }
//
//    /**
//     * Style a button with a modern look and hover effect
//     */
//    private void styleButton(JButton button, Color bgColor) {
//        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        button.setBackground(bgColor);
//        button.setForeground(Color.WHITE);
//        button.setFocusPainted(false);
//        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
//        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//        // Hover effect
//        Color hoverColor = bgColor.brighter();
//        button.addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mouseEntered(java.awt.event.MouseEvent evt) {
//                button.setBackground(hoverColor);
//            }
//            public void mouseExited(java.awt.event.MouseEvent evt) {
//                button.setBackground(bgColor);
//            }
//        });
//    }
//
//    /**
//     * Load vehicles data from database using DBConnection class
//     */
//    private void loadVehiclesData() {
//        // Clear existing data
//        tableModel.setRowCount(0);
//
//        // Use DBConnection to get all vehicles
//        List<Vector<Object>> vehicles = DBConnection.getAllVehicles();
//
//        // Add results to table model
//        for (Vector<Object> row : vehicles) {
//            tableModel.addRow(row);
//        }
//    }
//
//    /**
//     * Search vehicles by make, model, or VIN using DBConnection class
//     */
//    private void searchVehicles() {
//        String searchText = searchField.getText().trim();
//
//        if (searchText.isEmpty()) {
//            loadVehiclesData();
//            return;
//        }
//
//        // Clear existing data
//        tableModel.setRowCount(0);
//
//        // Use DBConnection to search vehicles
//        List<Vector<Object>> vehicles = DBConnection.searchVehicles(searchText);
//
//        // Add results to table model
//        for (Vector<Object> row : vehicles) {
//            tableModel.addRow(row);
//        }
//    }
//
//    /**
//     * Filter vehicles by status using DBConnection class
//     */
//    private void filterByStatus() {
//        String selectedStatus = (String) statusComboBox.getSelectedItem();
//
//        if ("All".equals(selectedStatus)) {
//            loadVehiclesData();
//            return;
//        }
//
//        // Clear existing data
//        tableModel.setRowCount(0);
//
//        // Use DBConnection to filter vehicles by status
//        List<Vector<Object>> vehicles = DBConnection.filterVehiclesByStatus(selectedStatus);
//
//        // Add results to table model
//        for (Vector<Object> row : vehicles) {
//            tableModel.addRow(row);
//        }
//    }
//
//    /**
//     * Show form to add new vehicle
//     */
//    private void showAddVehicleForm() {
//        // Clear the form panel
//        formPanel.removeAll();
//        formPanel.setVisible(true);
//
//        // Create form components
//        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
//        inputPanel.setBackground(BACKGROUND_COLOR);
//        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//        // Add form fields
//        JTextField vinField = new JTextField(20);
//        JTextField makeField = new JTextField(20);
//        JTextField modelField = new JTextField(20);
//        JTextField yearField = new JTextField(20);
//        JTextField colorField = new JTextField(20);
//        JTextField mileageField = new JTextField(20);
//        JTextField conditionField = new JTextField(20);
//        JTextField purchasePriceField = new JTextField(20);
//        JTextField listPriceField = new JTextField(20);
//
//        // Style form fields
//        JTextField[] fields = {vinField, makeField, modelField, yearField, colorField,
//                mileageField, conditionField, purchasePriceField, listPriceField};
//        for (JTextField field : fields) {
//            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//            field.setBackground(new Color(50, 50, 50));
//            field.setForeground(TEXT_COLOR);
//            field.setBorder(BorderFactory.createCompoundBorder(
//                    BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
//                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
//        }
//
//        // Status dropdown
//        String[] statuses = {"Available", "Sold", "On Hold"};
//        JComboBox<String> statusField = new JComboBox<>(statuses);
//        statusField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        statusField.setBackground(new Color(50, 50, 50));
//        statusField.setForeground(TEXT_COLOR);
//
//        // Category dropdown
//        JComboBox<String> categoryField = new JComboBox<>();
//        loadCategories(categoryField);
//        // Debug: Log the number of categories loaded
//        System.out.println("Categories loaded in Add Form: " + categoryField.getItemCount());
//        for (int i = 0; i < categoryField.getItemCount(); i++) {
//            System.out.println("Category " + i + ": " + categoryField.getItemAt(i));
//        }
//        categoryField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//        categoryField.setBackground(new Color(50, 50, 50));
//        categoryField.setForeground(TEXT_COLOR);
//
//        // Add fields to form with labels
//        String[] labels = {"VIN:", "Make:", "Model:", "Year:", "Color:", "Mileage:",
//                "Condition:", "Purchase Price:", "List Price:", "Status:", "Category:"};
//        Component[] components = {vinField, makeField, modelField, yearField, colorField,
//                mileageField, conditionField, purchasePriceField, listPriceField, statusField, categoryField};
//
//        for (int i = 0; i < labels.length; i++) {
//            JLabel label = new JLabel(labels[i]);
//            label.setForeground(TEXT_COLOR);
//            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//            inputPanel.add(label);
//            inputPanel.add(components[i]);
//        }
//
//        // Button panel
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
//        buttonPanel.setBackground(BACKGROUND_COLOR);
//
//        JButton saveButton = new JButton("Save");
//        styleButton(saveButton, PRIMARY_COLOR);
//        saveButton.addActionListener(e -> {
//            // Validate form
//            if (vinField.getText().trim().isEmpty() ||
//                    makeField.getText().trim().isEmpty() ||
//                    modelField.getText().trim().isEmpty()) {
//                JOptionPane.showMessageDialog(this, "VIN, Make, and Model are required fields",
//                        "Validation Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            try {
//                // Get form values
//                String vin = vinField.getText().trim();
//                String make = makeField.getText().trim();
//                String model = modelField.getText().trim();
//                int year = Integer.parseInt(yearField.getText().trim());
//                String color = colorField.getText().trim();
//                int mileage = Integer.parseInt(mileageField.getText().trim());
//                String condition = conditionField.getText().trim();
//                BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText().trim());
//                BigDecimal listPrice = new BigDecimal(listPriceField.getText().trim());
//                String status = (String) statusField.getSelectedItem();
//                String categoryName = (String) categoryField.getSelectedItem();
//                // Debug: Log the selected category
//                System.out.println("Selected category to add: " + categoryName);
//
//                // Use DBConnection to add vehicle
//                boolean success = DBConnection.addVehicle(vin, make, model, year, color, mileage,
//                        condition, purchasePrice, listPrice, status, categoryName);
//
//                if (success) {
//                    JOptionPane.showMessageDialog(this, "Vehicle added successfully",
//                            "Success", JOptionPane.INFORMATION_MESSAGE);
//                    formPanel.setVisible(false);
//                    loadVehiclesData();
//                } else {
//                    JOptionPane.showMessageDialog(this, "Failed to add vehicle",
//                            "Error", JOptionPane.ERROR_MESSAGE);
//                }
//            } catch (NumberFormatException ex) {
//                JOptionPane.showMessageDialog(this, "Invalid number format: " + ex.getMessage(),
//                        "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        });
//
//        JButton cancelButton = new JButton("Cancel");
//        styleButton(cancelButton, DANGER_COLOR);
//        cancelButton.addActionListener(e -> {
//            formPanel.setVisible(false);
//            formPanel.removeAll();
//            formPanel.revalidate();
//            formPanel.repaint();
//        });
//
//        buttonPanel.add(saveButton);
//        buttonPanel.add(cancelButton);
//
//        // Add to form panel
//        formPanel.add(inputPanel);
//        formPanel.add(buttonPanel);
//
//        // Update UI
//        formPanel.revalidate();
//        formPanel.repaint();
//    }
//
//    /**
//     * Load categories for dropdown using DBConnection class
//     */
//    private void loadCategories(JComboBox<String> comboBox) {
//        comboBox.removeAllItems();
//
//        // Use DBConnection to get all categories
//        List<String> categories = DBConnection.getAllCategories();
//
//        // Add categories to combobox
//        for (String category : categories) {
//            comboBox.addItem(category);
//        }
//
//        // If no categories are loaded, show a warning
//        if (comboBox.getItemCount() == 0) {
//            JOptionPane.showMessageDialog(this, "No categories found in the database. Please add categories first.",
//                    "Warning", JOptionPane.WARNING_MESSAGE);
//            comboBox.addItem("No Categories Available");
//        }
//    }
//
//    /**
//     * Edit selected vehicle
//     */
//    private void editSelectedVehicle() {
//        int selectedRow = vehicleTable.getSelectedRow();
//
//        if (selectedRow == -1) {
//            JOptionPane.showMessageDialog(this, "Please select a vehicle to edit",
//                    "Selection Error", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        int vehicleId = (int) tableModel.getValueAt(selectedRow, 0);
//
//        try {
//            // Get vehicle details using DBConnection
//            ResultSet rs = DBConnection.getVehicleById(vehicleId);
//            Connection conn = rs.getStatement().getConnection();
//
//            if (rs.next()) {
//                // Clear the form panel
//                formPanel.removeAll();
//                formPanel.setVisible(true);
//
//                // Create form components
//                JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
//                inputPanel.setBackground(BACKGROUND_COLOR);
//                inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//                // Add form fields
//                JTextField vinField = new JTextField(rs.getString("VIN"), 20);
//                JTextField makeField = new JTextField(rs.getString("Make"), 20);
//                JTextField modelField = new JTextField(rs.getString("Model"), 20);
//                JTextField yearField = new JTextField(String.valueOf(rs.getInt("Year")), 20);
//                JTextField colorField = new JTextField(rs.getString("Color"), 20);
//                JTextField mileageField = new JTextField(String.valueOf(rs.getInt("Mileage")), 20);
//                JTextField conditionField = new JTextField(rs.getString("Condition"), 20);
//                JTextField purchasePriceField = new JTextField(rs.getBigDecimal("PurchasePrice").toString(), 20);
//                JTextField listPriceField = new JTextField(rs.getBigDecimal("ListPrice").toString(), 20);
//
//                // Style form fields
//                JTextField[] fields = {vinField, makeField, modelField, yearField, colorField,
//                        mileageField, conditionField, purchasePriceField, listPriceField};
//                for (JTextField field : fields) {
//                    field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//                    field.setBackground(new Color(50, 50, 50));
//                    field.setForeground(TEXT_COLOR);
//                    field.setBorder(BorderFactory.createCompoundBorder(
//                            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
//                            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
//                }
//
//                // Status dropdown
//                String[] statuses = {"Available", "Sold", "On Hold"};
//                JComboBox<String> statusField = new JComboBox<>(statuses);
//                statusField.setSelectedItem(rs.getString("Status"));
//                statusField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//                statusField.setBackground(new Color(50, 50, 50));
//                statusField.setForeground(TEXT_COLOR);
//
//                // Category dropdown
//                JComboBox<String> categoryField = new JComboBox<>();
//                loadCategories(categoryField);
//                // Debug: Log the number of categories loaded
//                System.out.println("Categories loaded in Edit Form: " + categoryField.getItemCount());
//                for (int i = 0; i < categoryField.getItemCount(); i++) {
//                    System.out.println("Category " + i + ": " + categoryField.getItemAt(i));
//                }
//                int categoryId = rs.getInt("CategoryID");
//                String categoryName = DBConnection.getCategoryName(conn, categoryId);
//                // Debug: Log the category name being set
//                System.out.println("Setting category in Edit Form: " + categoryName);
//                categoryField.setSelectedItem(categoryName);
//                categoryField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//                categoryField.setBackground(new Color(50, 50, 50));
//                categoryField.setForeground(TEXT_COLOR);
//
//                // Add fields to form with labels
//                String[] labels = {"VIN:", "Make:", "Model:", "Year:", "Color:", "Mileage:",
//                        "Condition:", "Purchase Price:", "List Price:", "Status:", "Category:"};
//                Component[] components = {vinField, makeField, modelField, yearField, colorField,
//                        mileageField, conditionField, purchasePriceField, listPriceField, statusField, categoryField};
//
//                for (int i = 0; i < labels.length; i++) {
//                    JLabel label = new JLabel(labels[i]);
//                    label.setForeground(TEXT_COLOR);
//                    label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
//                    inputPanel.add(label);
//                    inputPanel.add(components[i]);
//                }
//
//                // Button panel
//                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
//                buttonPanel.setBackground(BACKGROUND_COLOR);
//
//                JButton updateButton = new JButton("Update");
//                styleButton(updateButton, PRIMARY_COLOR);
//                updateButton.addActionListener(e -> {
//                    // Validate form
//                    if (vinField.getText().trim().isEmpty() ||
//                            makeField.getText().trim().isEmpty() ||
//                            modelField.getText().trim().isEmpty()) {
//                        JOptionPane.showMessageDialog(this, "VIN, Make, and Model are required fields",
//                                "Validation Error", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//
//                    try {
//                        // Get form values
//                        String vin = vinField.getText().trim();
//                        String make = makeField.getText().trim();
//                        String model = modelField.getText().trim();
//                        int year = Integer.parseInt(yearField.getText().trim());
//                        String color = colorField.getText().trim();
//                        int mileage = Integer.parseInt(mileageField.getText().trim());
//                        String condition = conditionField.getText().trim();
//                        BigDecimal purchasePrice = new BigDecimal(purchasePriceField.getText().trim());
//                        BigDecimal listPrice = new BigDecimal(listPriceField.getText().trim());
//                        String status = (String) statusField.getSelectedItem();
//                        String category = (String) categoryField.getSelectedItem();
//                        // Debug: Log the selected category
//                        System.out.println("Selected category to update: " + category);
//
//                        // Use DBConnection to update vehicle
//                        boolean success = DBConnection.updateVehicle(vehicleId, vin, make, model, year,
//                                color, mileage, condition, purchasePrice, listPrice, status, category);
//
//                        if (success) {
//                            JOptionPane.showMessageDialog(this, "Vehicle updated successfully",
//                                    "Success", JOptionPane.INFORMATION_MESSAGE);
//                            formPanel.setVisible(false);
//                            loadVehiclesData();
//                        } else {
//                            JOptionPane.showMessageDialog(this, "Failed to update vehicle",
//                                    "Error", JOptionPane.ERROR_MESSAGE);
//                        }
//                    } catch (NumberFormatException ex) {
//                        JOptionPane.showMessageDialog(this, "Invalid number format: " + ex.getMessage(),
//                                "Error", JOptionPane.ERROR_MESSAGE);
//                    }
//                });
//
//                JButton cancelButton = new JButton("Cancel");
//                styleButton(cancelButton, DANGER_COLOR);
//                cancelButton.addActionListener(e -> {
//                    formPanel.setVisible(false);
//                    formPanel.removeAll();
//                    formPanel.revalidate();
//                    formPanel.repaint();
//                });
//
//                buttonPanel.add(updateButton);
//                buttonPanel.add(cancelButton);
//
//                // Add to form panel
//                formPanel.add(inputPanel);
//                formPanel.add(buttonPanel);
//
//                // Update UI
//                formPanel.revalidate();
//                formPanel.repaint();
//            }
//
//            // Close resources
//            DBConnection.closeResources(conn, rs.getStatement(), rs);
//
//        } catch (SQLException e) {
//            JOptionPane.showMessageDialog(this, "Error loading vehicle details: " + e.getMessage(),
//                    "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    /**
//     * Delete selected vehicle using DBConnection class
//     */
//    private void deleteSelectedVehicle() {
//        int selectedRow = vehicleTable.getSelectedRow();
//
//        if (selectedRow == -1) {
//            JOptionPane.showMessageDialog(this, "Please select a vehicle to delete",
//                    "Selection Error", JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        int vehicleId = (int) tableModel.getValueAt(selectedRow, 0);
//        String make = (String) tableModel.getValueAt(selectedRow, 2);
//        String model = (String) tableModel.getValueAt(selectedRow, 3);
//
//        // Confirm deletion
//        int confirm = JOptionPane.showConfirmDialog(this,
//                "Are you sure you want to delete " + make + " " + model + "?",
//                "Confirm Delete", JOptionPane.YES_NO_OPTION);
//
//        if (confirm == JOptionPane.YES_OPTION) {
//            // Use DBConnection to delete vehicle
//            boolean success = DBConnection.deleteVehicle(vehicleId);
//
//            if (success) {
//                JOptionPane.showMessageDialog(this, "Vehicle deleted successfully",
//                        "Success", JOptionPane.INFORMATION_MESSAGE);
//                loadVehiclesData();
//            } else {
//                JOptionPane.showMessageDialog(this,
//                        "Cannot delete vehicle with associated records",
//                        "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }
//
//    /**
//     * Main method for testing
//     */
//    public static void main(String[] args) {
//        try {
//            // Set Look and Feel
//            UIManager.setLookAndFeel(new FlatDarkLaf());
//
//            SwingUtilities.invokeLater(() -> {
//                JFrame frame = new JFrame("Vehicle Management");
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.setSize(1000, 600);
//
//                VehiclePanel panel = new VehiclePanel();
//                frame.add(panel);
//
//                frame.setVisible(true);
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(null, "Error starting application: " + e.getMessage(),
//                    "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//}