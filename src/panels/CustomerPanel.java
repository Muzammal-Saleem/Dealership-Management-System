package panels;

import Database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class CustomerPanel extends JPanel {
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton;
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

    public CustomerPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_BASE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setOpaque(false);

        initComponents();
        startAnimation();
        startFadeInAnimation();
    }

    private void initComponents() {
        // Header
        JLabel headerLabel = createGlowingLabel("Customer Management", new Font("Inter", Font.BOLD, 28), TEXT_COLOR, GLOW_COLOR_START);
        headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(headerLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchCustomers(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchCustomers(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchCustomers(); }
        });
        searchPanel.add(searchField);

        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Phone", "Address", "City", "State", "ZipCode", "Date Registered", "Order Count"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Object getValueAt(int row, int col) {
                Object value = super.getValueAt(row, col);
                return value != null ? value : ""; // Handle NULL values in table display
            }
        };
        customerTable = new JTable(tableModel) {
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

        // Center align all cell content
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Apply center renderer to all columns
        for (int i = 0; i < customerTable.getColumnCount(); i++) {
            customerTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Center align table header text as well
        ((DefaultTableCellRenderer)customerTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        customerTable.setOpaque(false);
        customerTable.setShowGrid(false);
        customerTable.setRowHeight(30);
        customerTable.setFont(new Font("Inter", Font.PLAIN, 14));
        customerTable.setSelectionForeground(TEXT_COLOR);
        customerTable.getTableHeader().setOpaque(false);
        customerTable.getTableHeader().setBackground(new Color(0, 153, 255));
        customerTable.getTableHeader().setForeground(TEXT_COLOR);
        customerTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 15));
        customerTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(customerTable) {
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

        addButton = createStyledButton("Add Customer", GLOW_COLOR_START, GLOW_COLOR_END);
        addButton.addActionListener(e -> showCustomerForm(null));
        buttonPanel.add(addButton);

        editButton = createStyledButton("Edit Customer", GLOW_COLOR_START, GLOW_COLOR_END);
        editButton.addActionListener(e -> {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow != -1) {
                int customerId = (int) tableModel.getValueAt(selectedRow, 0);
                showCustomerForm(customerId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a customer to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(editButton);

        deleteButton = createStyledButton("Delete Customer", new Color(200, 50, 50), new Color(255, 100, 100));
        deleteButton.addActionListener(e -> deleteCustomer());
        buttonPanel.add(deleteButton);

        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        loadCustomers();
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

    private void loadCustomers() {
        List<Vector<Object>> customers = DBConnection.getAllCustomers();
        tableModel.setRowCount(0);
        for (Vector<Object> customer : customers) {
            tableModel.addRow(customer);
        }
    }

    private void searchCustomers() {
        String searchText = searchField.getText().trim();
        List<Vector<Object>> customers = DBConnection.searchCustomers(searchText);
        tableModel.setRowCount(0);
        for (Vector<Object> customer : customers) {
            tableModel.addRow(customer);
        }
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow != -1) {
            int customerId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = DBConnection.deleteCustomer(customerId);
                    if (success) {
                        loadCustomers();
                        JOptionPane.showMessageDialog(this, "Customer deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete customer. They may have associated sales or test drives.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCustomerForm(Integer customerId) {
        JDialog formDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), customerId == null ? "Add Customer" : "Edit Customer", true);
        formDialog.setLayout(new BorderLayout());

        // Custom content pane for glassmorphism effect
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

        String[] labels = {"First Name:", "Last Name:", "Email:", "Phone:", "Address:", "City:", "State:", "Zip Code:", "Username:", "New Password (optional):"};
        JTextField[] fields = new JTextField[labels.length - 1]; // All except Password
        JPasswordField passwordField = new JPasswordField(20);
        Integer existingUserId = null;

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setForeground(SECONDARY_TEXT_COLOR);
            label.setFont(new Font("Inter", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.EAST;
            contentPanel.add(label, gbc);

            if (i == labels.length - 1) { // Password field
                passwordField.setBackground(INPUT_BG);
                passwordField.setForeground(TEXT_COLOR);
                passwordField.setCaretColor(TEXT_COLOR);
                passwordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                passwordField.setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                contentPanel.add(passwordField, gbc);
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

        // Determine the final userId to be used in the lambda
        final Integer finalUserId;
        if (customerId != null) {
            try (ResultSet rs = DBConnection.getCustomerById(customerId)) {
                if (rs.next()) {
                    fields[0].setText(rs.getString("FirstName") != null ? rs.getString("FirstName") : "");
                    fields[1].setText(rs.getString("LastName") != null ? rs.getString("LastName") : "");
                    fields[2].setText(rs.getString("Email") != null ? rs.getString("Email") : "");
                    fields[3].setText(rs.getString("Phone") != null ? rs.getString("Phone") : "");
                    fields[4].setText(rs.getString("Address") != null ? rs.getString("Address") : "");
                    fields[5].setText(rs.getString("City") != null ? rs.getString("City") : "");
                    fields[6].setText(rs.getString("State") != null ? rs.getString("State") : "");
                    fields[7].setText(rs.getString("ZipCode") != null ? rs.getString("ZipCode") : "");
                    existingUserId = rs.getObject("UserID") != null ? rs.getInt("UserID") : null;
                    fields[8].setText(rs.getString("Username") != null ? rs.getString("Username") : "");
                    fields[8].setEditable(false); // Username is read-only
                } else {
                    JOptionPane.showMessageDialog(formDialog, "Customer not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    formDialog.dispose();
                    return;
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(formDialog, "Error loading customer data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                formDialog.dispose();
                return;
            }
            finalUserId = existingUserId;
        } else {
            finalUserId = null;
        }

        JButton saveButton = createStyledButton(customerId == null ? "Add" : "Update", GLOW_COLOR_START, GLOW_COLOR_END);
        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPanel.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            String firstName = fields[0].getText().trim();
            String lastName = fields[1].getText().trim();
            String email = fields[2].getText().trim();
            String phone = fields[3].getText().trim();
            String address = fields[4].getText().trim();
            String city = fields[5].getText().trim();
            String state = fields[6].getText().trim();
            String zipCode = fields[7].getText().trim();
            String username = fields[8].getText().trim();
            String newPassword = new String(passwordField.getPassword()).trim();

            // Validation
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || (customerId == null && (username.isEmpty() || newPassword.isEmpty()))) {
                JOptionPane.showMessageDialog(formDialog, "All fields (except New Password when editing) are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                JOptionPane.showMessageDialog(formDialog, "Please enter a valid email address (e.g., example@domain.com).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Optional fields can be empty, set to NULL if so
            address = address.isEmpty() ? null : address;
            city = city.isEmpty() ? null : city;
            state = state.isEmpty() ? null : state;
            zipCode = zipCode.isEmpty() ? null : zipCode;

            try {
                boolean success;
                Integer userId = finalUserId;
                if (customerId == null) {
                    // Add to Users table first
                    String passwordHash = newPassword; // In production, use a secure hash (e.g., BCrypt)
                    success = DBConnection.addUser(username, passwordHash, "customer");
                    if (success) {
                        try (ResultSet rs = DBConnection.getUserByUsername(username)) {
                            if (rs.next()) {
                                userId = rs.getInt("UserID");
                            } else {
                                throw new SQLException("Failed to retrieve UserID after adding user.");
                            }
                        }
                    } else {
                        throw new SQLException("Failed to add user to Users table.");
                    }
                    // Then add to Customers table with the new UserID
                    success = DBConnection.addCustomer(firstName, lastName, email, phone, address, city, state, zipCode, userId);
                } else {
                    // For editing, update customer details
                    success = DBConnection.updateCustomer(customerId, firstName, lastName, email, phone, address, city, state, zipCode, userId);
                    // If a new password is provided, update the Users table
                    if (!newPassword.isEmpty() && userId != null) {
                        String newPasswordHash = newPassword; // In production, use a secure hash
                        success &= DBConnection.updateUserPassword(userId, newPasswordHash);
                    }
                }
                if (success) {
                    loadCustomers();
                    JOptionPane.showMessageDialog(formDialog, customerId == null ? "Customer and user added successfully." : "Customer updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    formDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(formDialog, "Failed to " + (customerId == null ? "add" : "update") + " customer.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(formDialog, "Failed to " + (customerId == null ? "add" : "update") + " customer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formDialog.pack();
        formDialog.setLocationRelativeTo(this);
        formDialog.setVisible(true);
    }
}