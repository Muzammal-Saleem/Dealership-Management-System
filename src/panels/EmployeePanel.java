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

public class EmployeePanel extends JPanel {
    private JTable employeeTable;
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

    public EmployeePanel() {
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
        JLabel headerLabel = createGlowingLabel("Employee Management", new Font("Inter", Font.BOLD, 28), TEXT_COLOR, GLOW_COLOR_START);
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
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchEmployees(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchEmployees(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchEmployees(); }
        });
        searchPanel.add(searchField);

        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Phone", "Position", "Hire Date", "Salary", "Username"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeeTable = new JTable(tableModel) {
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
        employeeTable.setOpaque(false);
        employeeTable.setShowGrid(false);
        employeeTable.setRowHeight(30);
        employeeTable.setFont(new Font("Inter", Font.PLAIN, 14));
        employeeTable.setSelectionForeground(TEXT_COLOR);
        employeeTable.getTableHeader().setOpaque(false);
        employeeTable.getTableHeader().setBackground(new Color (0, 153, 255));
        employeeTable.getTableHeader().setForeground(TEXT_COLOR);
        employeeTable.getTableHeader().setFont(new Font("Inter", Font.BOLD, 15));
        employeeTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Center-align cell contents
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < employeeTable.getColumnCount(); i++) {
            employeeTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(employeeTable) {
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

        addButton = createStyledButton("Add Employee", GLOW_COLOR_START, GLOW_COLOR_END);
        addButton.addActionListener(e -> showEmployeeForm(null));
        buttonPanel.add(addButton);

        editButton = createStyledButton("Edit Employee", GLOW_COLOR_START, GLOW_COLOR_END);
        editButton.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow != -1) {
                try {
                    Object idValue = tableModel.getValueAt(selectedRow, 0);
                    if (idValue == null) {
                        throw new IllegalArgumentException("EmployeeID is null");
                    }
                    int employeeId = Integer.parseInt(idValue.toString());
                    showEmployeeForm(employeeId);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid Employee ID format.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Employee ID is missing.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an employee to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(editButton);

        deleteButton = createStyledButton("Delete Employee", new Color(200, 50, 50), new Color(255, 100, 100));
        deleteButton.addActionListener(e -> deleteEmployee());
        buttonPanel.add(deleteButton);

        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        loadEmployees();
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

    private void loadEmployees() {
        List<Vector<Object>> employees = DBConnection.getAllEmployees();
        tableModel.setRowCount(0);
        for (Vector<Object> employee : employees) {
            tableModel.addRow(employee);
        }
    }

    private void searchEmployees() {
        String searchText = searchField.getText().trim();
        List<Vector<Object>> employees = DBConnection.searchEmployees(searchText);
        tableModel.setRowCount(0);
        for (Vector<Object> employee : employees) {
            tableModel.addRow(employee);
        }
    }

    private void deleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow != -1) {
            int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this employee?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = DBConnection.deleteEmployee(employeeId);
                if (success) {
                    loadEmployees();
                    JOptionPane.showMessageDialog(this, "Employee deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete employee. They may have associated sales, service records, or test drives.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEmployeeForm(Integer employeeId) {
        JDialog formDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), employeeId == null ? "Add Employee" : "Edit Employee", true);
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

        String[] labels = {"First Name:", "Last Name:", "Email:", "Phone:", "Position:", "Hire Date (YYYY-MM-DD):", "Salary:", "Username:", "Password:", "Role:"};
        JTextField[] fields = new JTextField[labels.length - 2];
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"admin", "employee"});

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setForeground(SECONDARY_TEXT_COLOR);
            label.setFont(new Font("Inter", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.EAST;
            contentPanel.add(label, gbc);

            if (i == 8) { // Password field
                passwordField.setBackground(INPUT_BG);
                passwordField.setForeground(TEXT_COLOR);
                passwordField.setCaretColor(TEXT_COLOR);
                passwordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                passwordField.setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                contentPanel.add(passwordField, gbc);
            } else if (i == 9) { // Role field
                roleComboBox.setBackground(INPUT_BG);
                roleComboBox.setForeground(TEXT_COLOR);
                roleComboBox.setFont(new Font("Inter", Font.PLAIN, 14));
                gbc.gridx = 1;
                gbc.anchor = GridBagConstraints.WEST;
                contentPanel.add(roleComboBox, gbc);
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

        if (employeeId != null) {
            DBConnection.EmployeeResult employeeResult = DBConnection.getEmployeeById(employeeId);
            ResultSet rs = null;
            try {
                if (employeeResult != null) {
                    rs = employeeResult.getResultSet();
                    if (rs != null && rs.next()) {
                        fields[0].setText(rs.getString("FirstName") != null ? rs.getString("FirstName") : "");
                        fields[1].setText(rs.getString("LastName") != null ? rs.getString("LastName") : "");
                        fields[2].setText(rs.getString("Email") != null ? rs.getString("Email") : "");
                        fields[3].setText(rs.getString("Phone") != null ? rs.getString("Phone") : "");
                        fields[4].setText(rs.getString("Position") != null ? rs.getString("Position") : "");
                        fields[5].setText(rs.getString("HireDate") != null ? rs.getString("HireDate") : "");
                        fields[6].setText(rs.getObject("Salary") != null ? rs.getString("Salary") : "");
                        fields[7].setText(rs.getString("Username") != null ? rs.getString("Username") : "");
                        roleComboBox.setSelectedItem(rs.getString("Role") != null ? rs.getString("Role") : "employee");
                    } else {
                        JOptionPane.showMessageDialog(formDialog, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        formDialog.dispose();
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(formDialog, "Error retrieving employee data.", "Error", JOptionPane.ERROR_MESSAGE);
                    formDialog.dispose();
                    return;
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(formDialog, "Error retrieving employee data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                formDialog.dispose();
                return;
            } finally {
                if (employeeResult != null) {
                    try {
                        employeeResult.close();
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(formDialog, "Error closing resources: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }

        JButton saveButton = createStyledButton(employeeId == null ? "Add" : "Update", GLOW_COLOR_START, GLOW_COLOR_END);
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
            String position = fields[4].getText().trim();
            String hireDate = fields[5].getText().trim();
            String salary = fields[6].getText().trim();
            String username = fields[7].getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleComboBox.getSelectedItem();

            // Validation
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                    position.isEmpty() || hireDate.isEmpty() || username.isEmpty()) {
                JOptionPane.showMessageDialog(formDialog, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (employeeId == null && password.isEmpty()) {
                JOptionPane.showMessageDialog(formDialog, "Password is required for new employees.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                JOptionPane.showMessageDialog(formDialog, "Please enter a valid email address (e.g., example@domain.com).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!hireDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                JOptionPane.showMessageDialog(formDialog, "Please enter a valid hire date (YYYY-MM-DD).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (!salary.isEmpty()) {
                    double salaryValue = Double.parseDouble(salary);
                    if (salaryValue <= 0) {
                        JOptionPane.showMessageDialog(formDialog, "Salary must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(formDialog, "Please enter a valid salary (numeric value).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success;
            if (employeeId == null) {
                success = DBConnection.addEmployee(firstName, lastName, email, phone, position, hireDate, salary, username, password, role);
            } else {
                success = DBConnection.updateEmployee(employeeId, firstName, lastName, email, phone, position, hireDate, salary, username, password, role);
            }

            if (success) {
                loadEmployees();
                JOptionPane.showMessageDialog(formDialog, employeeId == null ? "Employee added successfully." : "Employee updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                formDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(formDialog, "Failed to " + (employeeId == null ? "add" : "update") + " employee. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formDialog.pack();
        formDialog.setLocationRelativeTo(this);
        formDialog.setVisible(true);
    }
}

//package panels;
//
//import Database.DBConnection;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.awt.event.*;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Vector;
//
//public class EmployeePanel extends JPanel {
//    private JTable employeeTable;
//    private DefaultTableModel tableModel;
//    private JTextField searchField;
//    private JButton addButton;
//    private JButton editButton;
//    private JButton deleteButton;
//
//    private final Color PRIMARY_COLOR = new Color(50, 100, 200);
//    private final Color DANGER_COLOR = new Color(200, 50, 50);
//    private final Color BACKGROUND_COLOR = new Color(30, 30, 30);
//    private final Color TEXT_COLOR = new Color(220, 220, 220);
//    private final Color INPUT_BG_COLOR = new Color(50, 50, 50);
//    private final Color INPUT_TEXT_COLOR = new Color(200, 200, 200);
//
//    public EmployeePanel() {
//        setLayout(new BorderLayout());
//        setBackground(BACKGROUND_COLOR);
//        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//
//        initComponents();
//    }
//
//    private void initComponents() {
//        // Header
//        JLabel headerLabel = new JLabel("Employee Management");
//        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
//        headerLabel.setForeground(TEXT_COLOR);
//        add(headerLabel, BorderLayout.NORTH);
//
//        // Center Panel
//        JPanel centerPanel = new JPanel(new BorderLayout());
//        centerPanel.setBackground(BACKGROUND_COLOR);
//
//        // Search Panel
//        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        searchPanel.setBackground(BACKGROUND_COLOR);
//
//        JLabel searchLabel = new JLabel("Search:");
//        searchLabel.setForeground(TEXT_COLOR);
//        searchPanel.add(searchLabel);
//
//        searchField = new JTextField(20);
//        searchField.setBackground(INPUT_BG_COLOR);
//        searchField.setForeground(INPUT_TEXT_COLOR);
//        searchField.setCaretColor(INPUT_TEXT_COLOR);
//        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
//            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchEmployees(); }
//            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchEmployees(); }
//            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchEmployees(); }
//        });
//        searchPanel.add(searchField);
//
//        centerPanel.add(searchPanel, BorderLayout.NORTH);
//
//        // Table
//        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Phone", "Position", "Hire Date", "Salary", "Username"};
//        tableModel = new DefaultTableModel(columnNames, 0) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return false;
//            }
//        };
//        employeeTable = new JTable(tableModel);
//        employeeTable.setBackground(BACKGROUND_COLOR);
//        employeeTable.setForeground(TEXT_COLOR);
//        employeeTable.setGridColor(new Color(50, 50, 50));
//        employeeTable.setSelectionBackground(new Color(60, 80, 120));
//        employeeTable.setSelectionForeground(TEXT_COLOR);
//        employeeTable.setRowHeight(25);
//        employeeTable.getTableHeader().setBackground(new Color(40, 60, 100));
//        employeeTable.getTableHeader().setForeground(TEXT_COLOR);
//        employeeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
//
//        JScrollPane scrollPane = new JScrollPane(employeeTable);
//        scrollPane.setBackground(BACKGROUND_COLOR);
//        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
//        centerPanel.add(scrollPane, BorderLayout.CENTER);
//
//        // Button Panel
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        buttonPanel.setBackground(BACKGROUND_COLOR);
//
//        addButton = new JButton("Add Employee");
//        addButton.setBackground(PRIMARY_COLOR);
//        addButton.setForeground(TEXT_COLOR);
//        addButton.setFocusPainted(false);
//        addButton.addActionListener(e -> showEmployeeForm(null));
//        buttonPanel.add(addButton);
//
//        editButton = new JButton("Edit Employee");
//        editButton.setBackground(PRIMARY_COLOR);
//        editButton.setForeground(TEXT_COLOR);
//        editButton.setFocusPainted(false);
//        editButton.setEnabled(true);
//        editButton.addActionListener(e -> {
//            int selectedRow = employeeTable.getSelectedRow();
//            System.out.println("Selected Row: " + selectedRow);
//            if (selectedRow != -1) {
//                try {
//                    Object idValue = tableModel.getValueAt(selectedRow, 0);
//                    if (idValue == null) {
//                        throw new IllegalArgumentException("EmployeeID is null");
//                    }
//                    int employeeId = Integer.parseInt(idValue.toString());
//                    System.out.println("Employee ID: " + employeeId);
//                    showEmployeeForm(employeeId);
//                } catch (NumberFormatException ex) {
//                    System.err.println("Error parsing EmployeeID: " + ex.getMessage());
//                    JOptionPane.showMessageDialog(this, "Invalid Employee ID format.", "Error", JOptionPane.ERROR_MESSAGE);
//                } catch (IllegalArgumentException ex) {
//                    System.err.println("Error retrieving EmployeeID: " + ex.getMessage());
//                    JOptionPane.showMessageDialog(this, "Employee ID is missing.", "Error", JOptionPane.ERROR_MESSAGE);
//                }
//            } else {
//                JOptionPane.showMessageDialog(this, "Please select an employee to edit.", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        });
//        buttonPanel.add(editButton);
//
//        deleteButton = new JButton("Delete Employee");
//        deleteButton.setBackground(DANGER_COLOR);
//        deleteButton.setForeground(TEXT_COLOR);
//        deleteButton.setFocusPainted(false);
//        deleteButton.addActionListener(e -> deleteEmployee());
//        buttonPanel.add(deleteButton);
//
//        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
//
//        add(centerPanel, BorderLayout.CENTER);
//
//        loadEmployees();
//    }
//
//    private void loadEmployees() {
//        List<Vector<Object>> employees = DBConnection.getAllEmployees();
//        tableModel.setRowCount(0);
//        for (Vector<Object> employee : employees) {
//            tableModel.addRow(employee);
//        }
//    }
//
//    private void searchEmployees() {
//        String searchText = searchField.getText().trim();
//        List<Vector<Object>> employees = DBConnection.searchEmployees(searchText);
//        tableModel.setRowCount(0);
//        for (Vector<Object> employee : employees) {
//            tableModel.addRow(employee);
//        }
//    }
//
//    private void deleteEmployee() {
//        int selectedRow = employeeTable.getSelectedRow();
//        if (selectedRow != -1) {
//            int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
//            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this employee?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
//            if (confirm == JOptionPane.YES_OPTION) {
//                boolean success = DBConnection.deleteEmployee(employeeId);
//                if (success) {
//                    loadEmployees();
//                    JOptionPane.showMessageDialog(this, "Employee deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
//                } else {
//                    JOptionPane.showMessageDialog(this, "Failed to delete employee. They may have associated sales, service records, or test drives.", "Error", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        } else {
//            JOptionPane.showMessageDialog(this, "Please select an employee to delete.", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    private void showEmployeeForm(Integer employeeId) {
//        JDialog formDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), employeeId == null ? "Add Employee" : "Edit Employee", true);
//        formDialog.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(10, 10, 10, 10);
//        formDialog.getContentPane().setBackground(BACKGROUND_COLOR);
//
//        String[] labels = {"First Name:", "Last Name:", "Email:", "Phone:", "Position:", "Hire Date (YYYY-MM-DD):", "Salary:", "Username:", "Password:", "Role:"};
//        JTextField[] fields = new JTextField[labels.length - 1];
//        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"admin", "employee"});
//        JPasswordField passwordField = new JPasswordField(20);
//
//        for (int i = 0; i < labels.length; i++) {
//            JLabel label = new JLabel(labels[i]);
//            label.setForeground(TEXT_COLOR);
//            gbc.gridx = 0;
//            gbc.gridy = i;
//            formDialog.add(label, gbc);
//
//            if (i == 8) { // Password field
//                passwordField.setBackground(INPUT_BG_COLOR);
//                passwordField.setForeground(INPUT_TEXT_COLOR);
//                passwordField.setCaretColor(INPUT_TEXT_COLOR);
//                gbc.gridx = 1;
//                formDialog.add(passwordField, gbc);
//            } else if (i == 9) { // Role field
//                roleComboBox.setBackground(INPUT_BG_COLOR);
//                roleComboBox.setForeground(INPUT_TEXT_COLOR);
//                gbc.gridx = 1;
//                formDialog.add(roleComboBox, gbc);
//            } else {
//                fields[i] = new JTextField(20);
//                fields[i].setBackground(INPUT_BG_COLOR);
//                fields[i].setForeground(INPUT_TEXT_COLOR);
//                fields[i].setCaretColor(INPUT_TEXT_COLOR);
//                gbc.gridx = 1;
//                formDialog.add(fields[i], gbc);
//            }
//        }
//
//        if (employeeId != null) {
//            DBConnection.EmployeeResult employeeResult = DBConnection.getEmployeeById(employeeId);
//            ResultSet rs = null;
//            try {
//                if (employeeResult != null) {
//                    rs = employeeResult.getResultSet();
//                    if (rs != null && rs.next()) {
//                        System.out.println("Populating form for EmployeeID: " + employeeId);
//                        fields[0].setText(rs.getString("FirstName") != null ? rs.getString("FirstName") : "");
//                        fields[1].setText(rs.getString("LastName") != null ? rs.getString("LastName") : "");
//                        fields[2].setText(rs.getString("Email") != null ? rs.getString("Email") : "");
//                        fields[3].setText(rs.getString("Phone") != null ? rs.getString("Phone") : "");
//                        fields[4].setText(rs.getString("Position") != null ? rs.getString("Position") : "");
//                        fields[5].setText(rs.getString("HireDate") != null ? rs.getString("HireDate") : "");
//                        fields[6].setText(rs.getObject("Salary") != null ? rs.getString("Salary") : "");
//                        fields[7].setText(rs.getString("Username") != null ? rs.getString("Username") : "");
//                        roleComboBox.setSelectedItem(rs.getString("Role") != null ? rs.getString("Role") : "employee");
//                    } else {
//                        System.err.println("No employee found for EmployeeID: " + employeeId);
//                        JOptionPane.showMessageDialog(formDialog, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
//                        formDialog.dispose();
//                        return;
//                    }
//                } else {
//                    System.err.println("Error retrieving employee data for EmployeeID: " + employeeId);
//                    JOptionPane.showMessageDialog(formDialog, "Error retrieving employee data.", "Error", JOptionPane.ERROR_MESSAGE);
//                    formDialog.dispose();
//                    return;
//                }
//            } catch (SQLException e) {
//                System.err.println("Error retrieving employee data: " + e.getMessage());
//                JOptionPane.showMessageDialog(formDialog, "Error retrieving employee data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//                formDialog.dispose();
//                return;
//            } finally {
//                if (employeeResult != null) {
//                    try {
//                        employeeResult.close();
//                    } catch (SQLException e) {
//                        System.err.println("Error closing EmployeeResult resources: " + e.getMessage());
//                    }
//                }
//            }
//        }
//
//        JButton saveButton = new JButton(employeeId == null ? "Add" : "Update");
//        saveButton.setBackground(PRIMARY_COLOR);
//        saveButton.setForeground(TEXT_COLOR);
//        saveButton.setFocusPainted(false);
//        gbc.gridx = 0;
//        gbc.gridy = labels.length;
//        gbc.gridwidth = 2;
//        formDialog.add(saveButton, gbc);
//
//        saveButton.addActionListener(e -> {
//            String firstName = fields[0].getText().trim();
//            String lastName = fields[1].getText().trim();
//            String email = fields[2].getText().trim();
//            String phone = fields[3].getText().trim();
//            String position = fields[4].getText().trim();
//            String hireDate = fields[5].getText().trim();
//            String salary = fields[6].getText().trim();
//            String username = fields[7].getText().trim();
//            String password = new String(passwordField.getPassword()).trim();
//            String role = (String) roleComboBox.getSelectedItem();
//
//            System.out.println("Attempting to " + (employeeId == null ? "add" : "update") + " employee with ID: " + employeeId);
//            System.out.println("Data: firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", phone=" + phone +
//                    ", position=" + position + ", hireDate=" + hireDate + ", salary=" + salary + ", username=" + username +
//                    ", password=" + (password.isEmpty() ? "[unchanged]" : "[set]") + ", role=" + role);
//
//            // Validation
//            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
//                    position.isEmpty() || hireDate.isEmpty() || username.isEmpty()) {
//                JOptionPane.showMessageDialog(formDialog, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            if (employeeId == null && password.isEmpty()) {
//                JOptionPane.showMessageDialog(formDialog, "Password is required for new employees.", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
//                JOptionPane.showMessageDialog(formDialog, "Please enter a valid email address (e.g., example@domain.com).", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            if (!hireDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
//                JOptionPane.showMessageDialog(formDialog, "Please enter a valid hire date (YYYY-MM-DD).", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            try {
//                if (!salary.isEmpty()) {
//                    double salaryValue = Double.parseDouble(salary);
//                    if (salaryValue <= 0) {
//                        JOptionPane.showMessageDialog(formDialog, "Salary must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//                }
//            } catch (NumberFormatException ex) {
//                JOptionPane.showMessageDialog(formDialog, "Please enter a valid salary (numeric value).", "Error", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//
//            boolean success;
//            if (employeeId == null) {
//                success = DBConnection.addEmployee(firstName, lastName, email, phone, position, hireDate, salary, username, password, role);
//            } else {
//                success = DBConnection.updateEmployee(employeeId, firstName, lastName, email, phone, position, hireDate, salary, username, password, role);
//            }
//
//            if (success) {
//                loadEmployees();
//                JOptionPane.showMessageDialog(formDialog, employeeId == null ? "Employee added successfully." : "Employee updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
//                formDialog.dispose();
//            } else {
//                JOptionPane.showMessageDialog(formDialog, "Failed to " + (employeeId == null ? "add" : "update") + " employee. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        });
//
//        formDialog.pack();
//        formDialog.setLocationRelativeTo(this);
//        formDialog.setVisible(true);
//    }
//}