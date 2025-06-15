package Database;

import java.sql.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Handles database connections and common database operations
 * for the car dealership management system.
 */
public class DBConnection {
    private static final String DB_URL = "";
    private static final String USER = "";
    private static final String PASS = "";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("SQL Server JDBC Driver not found. Include it in your library path.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static int getTotalCount(String tableName) {
        int count = 0;
        List<String> allowedTables = Arrays.asList("Vehicles", "Customers", "Sales", "ServiceDepartment", "VehicleCategories", "Users");
        if (!allowedTables.contains(tableName)) {
            System.out.println("Invalid table name: " + tableName);
            return 0;
        }

        String query = "SELECT COUNT(*) AS Total FROM " + tableName;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt("Total");
                System.out.println("Total rows in " + tableName + ": " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static int getCountByCondition(String tableName, String columnName, String value) {
        int count = 0;
        List<String> allowedTables = Arrays.asList("Vehicles", "Customers", "Sales", "ServiceDepartment", "VehicleCategories");
        if (!allowedTables.contains(tableName)) {
            System.out.println("Invalid table name: " + tableName);
            return 0;
        }

        String query = "SELECT COUNT(*) AS Total FROM " + tableName + " WHERE " + columnName + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static boolean validateUser(String username, String password, String role) {
        String query = "SELECT 1 FROM Users WHERE Username = ? AND PasswordHash = ? AND Role = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // Note: Should use hashed password comparison in production
            stmt.setString(3, role);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet executeQuery(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int executeUpdate(String query) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(query);
        }
    }

    public static int getLastInsertedId(String tableName, String idColumnName) {
        int lastId = -1;
        String query = "SELECT MAX(" + idColumnName + ") AS LastID FROM " + tableName;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                lastId = rs.getInt("LastID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastId;
    }

    public static List<Vector<Object>> getAllVehicles() {
        List<Vector<Object>> vehicles = new ArrayList<>();
        String sql = "SELECT v.VehicleID, v.VIN, v.Make, v.Model, v.Year, v.Color, v.Mileage, v.Condition, v.PurchasePrice, v.ListPrice, v.Status, c.CategoryName FROM Vehicles v JOIN VehicleCategories c ON v.CategoryID = c.CategoryID";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("VehicleID"));
                row.add(rs.getString("VIN"));
                row.add(rs.getString("Make"));
                row.add(rs.getString("Model"));
                row.add(rs.getInt("Year"));
                row.add(rs.getString("Color"));
                row.add(rs.getInt("Mileage"));
                row.add(rs.getString("Condition"));
                row.add(rs.getBigDecimal("PurchasePrice"));
                row.add(rs.getBigDecimal("ListPrice"));
                row.add(rs.getString("Status"));
                row.add(rs.getString("CategoryName"));
                vehicles.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public static List<Vector<Object>> searchVehicles(String searchText) {
        List<Vector<Object>> vehicles = new ArrayList<>();
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllVehicles();
        }
        String sql = "SELECT v.VehicleID, v.VIN, v.Make, v.Model, v.Year, v.Color, v.Mileage, v.Condition, v.PurchasePrice, v.ListPrice, v.Status, c.CategoryName FROM Vehicles v JOIN VehicleCategories c ON v.CategoryID = c.CategoryID WHERE v.Make LIKE ? OR v.Model LIKE ? OR v.VIN LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchText.trim() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("VehicleID"));
                    row.add(rs.getString("VIN"));
                    row.add(rs.getString("Make"));
                    row.add(rs.getString("Model"));
                    row.add(rs.getInt("Year"));
                    row.add(rs.getString("Color"));
                    row.add(rs.getInt("Mileage"));
                    row.add(rs.getString("Condition"));
                    row.add(rs.getBigDecimal("PurchasePrice"));
                    row.add(rs.getBigDecimal("ListPrice"));
                    row.add(rs.getString("Status"));
                    row.add(rs.getString("CategoryName"));
                    vehicles.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public static List<Vector<Object>> filterVehiclesByStatus(String status) {
        List<Vector<Object>> vehicles = new ArrayList<>();
        if (status == null || status.equals("All")) {
            return getAllVehicles();
        }
        String sql = "SELECT v.VehicleID, v.VIN, v.Make, v.Model, v.Year, v.Color, v.Mileage, v.Condition, v.PurchasePrice, v.ListPrice, v.Status, c.CategoryName FROM Vehicles v JOIN VehicleCategories c ON v.CategoryID = c.CategoryID WHERE v.Status = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("VehicleID"));
                    row.add(rs.getString("VIN"));
                    row.add(rs.getString("Make"));
                    row.add(rs.getString("Model"));
                    row.add(rs.getInt("Year"));
                    row.add(rs.getString("Color"));
                    row.add(rs.getInt("Mileage"));
                    row.add(rs.getString("Condition"));
                    row.add(rs.getBigDecimal("PurchasePrice"));
                    row.add(rs.getBigDecimal("ListPrice"));
                    row.add(rs.getString("Status"));
                    row.add(rs.getString("CategoryName"));
                    vehicles.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }


    public static boolean addVehicle(String vin, String make, String model, int year, String color, int mileage, String condition, BigDecimal purchasePrice, BigDecimal listPrice, String status, String categoryName) {
        try (Connection conn = getConnection()) {
            int categoryId = getCategoryId(conn, categoryName);
            String sql = "INSERT INTO Vehicles (VIN, Make, Model, Year, Color, Mileage, Condition, PurchasePrice, ListPrice, Status, CategoryID, DateAcquired) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, vin);
                stmt.setString(2, make);
                stmt.setString(3, model);
                stmt.setInt(4, year);
                stmt.setString(5, color);
                stmt.setInt(6, mileage);
                stmt.setString(7, condition);
                stmt.setBigDecimal(8, purchasePrice);
                stmt.setBigDecimal(9, listPrice);
                stmt.setString(10, status);
                stmt.setInt(11, categoryId);
                int result = stmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error adding vehicle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateVehicle(int vehicleId, String vin, String make, String model, int year, String color, int mileage, String condition, BigDecimal purchasePrice, BigDecimal listPrice, String status, String categoryName) {
        try (Connection conn = getConnection()) {
            int categoryId = getCategoryId(conn, categoryName);
            String sql = "UPDATE Vehicles SET VIN = ?, Make = ?, Model = ?, Year = ?, Color = ?, Mileage = ?, Condition = ?, PurchasePrice = ?, ListPrice = ?, Status = ?, CategoryID = ? WHERE VehicleID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, vin);
                stmt.setString(2, make);
                stmt.setString(3, model);
                stmt.setInt(4, year);
                stmt.setString(5, color);
                stmt.setInt(6, mileage);
                stmt.setString(7, condition);
                stmt.setBigDecimal(8, purchasePrice);
                stmt.setBigDecimal(9, listPrice);
                stmt.setString(10, status);
                stmt.setInt(11, categoryId);
                stmt.setInt(12, vehicleId);
                int result = stmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating vehicle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteVehicle(int vehicleId) {
        try (Connection conn = getConnection()) {
            if (hasAssociatedRecords(conn, vehicleId)) {
                return false;
            }
            String sql = "DELETE FROM Vehicles WHERE VehicleID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vehicleId);
                int result = stmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting vehicle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean hasAssociatedRecords(Connection conn, int vehicleId) throws SQLException {
        String checkSql = "SELECT COUNT(*) AS count FROM Sales WHERE VehicleID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, vehicleId);
            try (ResultSet checkRs = checkStmt.executeQuery()) {
                if (checkRs.next() && checkRs.getInt("count") > 0) return true;
            }
        }
        checkSql = "SELECT COUNT(*) AS count FROM TestDrives WHERE VehicleID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, vehicleId);
            try (ResultSet checkRs = checkStmt.executeQuery()) {
                return checkRs.next() && checkRs.getInt("count") > 0;
            }
        }
    }

    public static ResultSet getVehicleById(int vehicleId) throws SQLException {
        Connection conn = getConnection();
        String sql = "SELECT * FROM Vehicles WHERE VehicleID = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, vehicleId);
        return stmt.executeQuery();
    }

    public static List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT CategoryName FROM VehicleCategories";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("CategoryName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    private static int getCategoryId(Connection conn, String categoryName) throws SQLException {
        String sql = "SELECT CategoryID FROM VehicleCategories WHERE CategoryName = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("CategoryID");
            }
        }
        throw new SQLException("Category not found: " + categoryName);
    }

    public static String getCategoryName(Connection conn, int categoryId) throws SQLException {
        String sql = "SELECT CategoryName FROM VehicleCategories WHERE CategoryID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("CategoryName");
            }
        }
        throw new SQLException("Category ID not found: " + categoryId);
    }

    public static List<Vector<Object>> getAllCustomers() {
        List<Vector<Object>> customers = new ArrayList<>();
        String sql = "SELECT CustomerID, FirstName, LastName, Email, Phone, Address, City, State, ZipCode, DateRegistered FROM Customers";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("CustomerID"));
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("LastName"));
                row.add(rs.getString("Email"));
                row.add(rs.getString("Phone"));
                row.add(rs.getString("Address"));
                row.add(rs.getString("City"));
                row.add(rs.getString("State"));
                row.add(rs.getString("ZipCode"));
                row.add(rs.getDate("DateRegistered"));
                customers.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    public static List<Vector<Object>> searchCustomers(String searchText) {
        List<Vector<Object>> customers = new ArrayList<>();
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllCustomers();
        }
        String sql = "SELECT CustomerID, FirstName, LastName, Email, Phone, Address, City, State, ZipCode, DateRegistered FROM Customers WHERE FirstName LIKE ? OR LastName LIKE ? OR Email LIKE ? OR Phone LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchText.trim() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("CustomerID"));
                    row.add(rs.getString("FirstName"));
                    row.add(rs.getString("LastName"));
                    row.add(rs.getString("Email"));
                    row.add(rs.getString("Phone"));
                    row.add(rs.getString("Address"));
                    row.add(rs.getString("City"));
                    row.add(rs.getString("State"));
                    row.add(rs.getString("ZipCode"));
                    row.add(rs.getDate("DateRegistered"));
                    customers.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    public static List<Vector<Object>> filterCustomersByStatus(String status) {
        List<Vector<Object>> customers = new ArrayList<>();
        if (status == null || status.equals("All")) {
            return getAllCustomers();
        }
        String sql = "SELECT CustomerID, FirstName, LastName, Email, Phone, Address, City, State, ZipCode, DateRegistered FROM Customers WHERE Status = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("CustomerID"));
                    row.add(rs.getString("FirstName"));
                    row.add(rs.getString("LastName"));
                    row.add(rs.getString("Email"));
                    row.add(rs.getString("Phone"));
                    row.add(rs.getString("Address"));
                    row.add(rs.getString("City"));
                    row.add(rs.getString("State"));
                    row.add(rs.getString("ZipCode"));
                    row.add(rs.getDate("DateRegistered"));
                    customers.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    public static boolean addCustomer(String firstName, String lastName, String email, String phone, String address, String city, String state, String zipCode, Integer userId) throws SQLException {
        try (Connection conn = getConnection()) {
            // Validate inputs
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new SQLException("Invalid email format");
            }
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                throw new SQLException("First name, last name, email, and phone are required");
            }

            // Validate UserID if provided
            if (userId != null) {
                try (PreparedStatement stmtCheck = conn.prepareStatement("SELECT COUNT(*) FROM Users WHERE UserID = ?")) {
                    stmtCheck.setInt(1, userId);
                    try (ResultSet rs = stmtCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            throw new SQLException("Provided UserID " + userId + " does not exist in the Users table");
                        }
                    }
                }
            }

            // Check for duplicate email
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT CustomerID FROM Customers WHERE Email = ?")) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("Email already exists: " + email);
                    }
                }
            }

            // Insert into Customers table
            String sql = "INSERT INTO Customers (FirstName, LastName, Email, Phone, Address, City, State, ZipCode, DateRegistered, UserID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, email);
                stmt.setString(4, phone);
                stmt.setString(5, address.isEmpty() ? null : address);
                stmt.setString(6, city.isEmpty() ? null : city);
                stmt.setString(7, state.isEmpty() ? null : state);
                stmt.setString(8, zipCode.isEmpty() ? null : zipCode);
                stmt.setObject(9, userId);
                int result = stmt.executeUpdate();

                if (result == 0) {
                    throw new SQLException("Failed to create customer record");
                }

                System.out.println("Add customer result: " + result + " rows affected");
                return true;
            }
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message.contains("Duplicate entry") && message.contains("Email")) {
                message = "Email already exists";
            }
            System.err.println("Error adding customer: " + message);
            throw new SQLException(message, e);
        }
    }

    public static boolean updateCustomer(int customerId, String firstName, String lastName, String email, String phone, String address, String city, String state, String zipCode, Integer userId) throws SQLException {
        try (Connection conn = getConnection()) {
            // Validate inputs
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new SQLException("Invalid email format");
            }
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                throw new SQLException("First name, last name, email, and phone are required");
            }

            // Validate UserID if provided
            if (userId != null) {
                try (PreparedStatement stmtCheck = conn.prepareStatement("SELECT COUNT(*) FROM Users WHERE UserID = ?")) {
                    stmtCheck.setInt(1, userId);
                    try (ResultSet rs = stmtCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            throw new SQLException("Provided UserID " + userId + " does not exist in the Users table");
                        }
                    }
                }
            }

            // Check for duplicate email (excluding current customer)
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT CustomerID FROM Customers WHERE Email = ? AND CustomerID != ?")) {
                checkStmt.setString(1, email);
                checkStmt.setInt(2, customerId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("Email already exists: " + email);
                    }
                }
            }

            // Update Customers table
            String sql = "UPDATE Customers SET FirstName = ?, LastName = ?, Email = ?, Phone = ?, Address = ?, City = ?, State = ?, ZipCode = ?, UserID = ? WHERE CustomerID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, email);
                stmt.setString(4, phone);
                stmt.setString(5, address.isEmpty() ? null : address);
                stmt.setString(6, city.isEmpty() ? null : city);
                stmt.setString(7, state.isEmpty() ? null : state);
                stmt.setString(8, zipCode.isEmpty() ? null : zipCode);
                stmt.setObject(9, userId);
                stmt.setInt(10, customerId);
                int result = stmt.executeUpdate();

                if (result == 0) {
                    throw new SQLException("Failed to update customer record or customer not found");
                }

                System.out.println("Update customer result: " + result + " rows affected");
                return true;
            }
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message.contains("Duplicate entry") && message.contains("Email")) {
                message = "Email already exists";
            }
            System.err.println("Error updating customer: " + message);
            throw new SQLException(message, e);
        }
    }

    public static boolean deleteCustomer(int customerId) {
        try (Connection conn = getConnection()) {
            // Check for associated records
            String associatedRecordsMessage = hasAssociatedCustomerRecords(conn, customerId);
            if (associatedRecordsMessage != null) {
                System.err.println("Cannot delete customer: " + associatedRecordsMessage);
                return false;
            }

            // Delete from Customers
            String sql = "DELETE FROM Customers WHERE CustomerID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, customerId);
                int result = stmt.executeUpdate();

                if (result == 0) {
                    System.err.println("Failed to delete customer record or customer not found");
                    return false;
                }

                System.out.println("Delete customer result: " + result + " rows affected");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String hasAssociatedCustomerRecords(Connection conn, int customerId) throws SQLException {
        String[] tables = {"Sales", "TestDrives"};
        String[] columns = {"CustomerID", "CustomerID"};

        for (int i = 0; i < tables.length; i++) {
            String checkSql = "SELECT COUNT(*) AS count FROM " + tables[i] + " WHERE " + columns[i] + " = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, customerId);
                try (ResultSet checkRs = checkStmt.executeQuery()) {
                    if (checkRs.next() && checkRs.getInt("count") > 0) {
                        return "Cannot delete customer: Associated records exist in " + tables[i];
                    }
                }
            }
        }
        return null;
    }

    public static ResultSet getCustomerById(int customerId) throws SQLException {
        Connection conn = getConnection();
        String sql = "SELECT c.CustomerID, c.UserID, c.FirstName, c.LastName, c.Email, c.Phone, c.Address, c.City, c.State, c.ZipCode, u.Username " +
                "FROM Customers c " +
                "LEFT JOIN Users u ON c.UserID = u.UserID " +
                "WHERE c.CustomerID = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, customerId);
        return stmt.executeQuery();
    }
    public static boolean addUser(String username, String passwordHash, String role) throws SQLException {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO Users (Username, PasswordHash, Role) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, passwordHash); // Use a secure hash in production
                stmt.setString(3, role);
                int result = stmt.executeUpdate();
                return result > 0;
            }
        }
    }
    public static boolean updateUserPassword(int userId, String passwordHash) throws SQLException {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE Users SET PasswordHash = ? WHERE UserID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, passwordHash);
                stmt.setInt(2, userId);
                int result = stmt.executeUpdate();
                return result > 0;
            }
        }
    }

    public static ResultSet getUserByUsername(String username) throws SQLException {
        Connection conn = getConnection();
        String sql = "SELECT UserID FROM Users WHERE Username = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        return stmt.executeQuery();
    }

//    public static boolean deleteCustomer(int customerId) {
//        try (Connection conn = getConnection()) {
//            if (hasAssociatedCustomerRecords(conn, customerId)) {
//                return false;
//            }
//            String sql = "DELETE FROM Customers WHERE CustomerID = ?";
//            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//                stmt.setInt(1, customerId);
//                int result = stmt.executeUpdate();
//                System.out.println("Delete customer result: " + result + " rows affected");
//                return result > 0;
//            }
//        } catch (SQLException e) {
//            System.err.println("Error deleting customer: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
//    }




    public static List<Vector<Object>> getAllSales() {
        List<Vector<Object>> sales = new ArrayList<>();
        String sql = "SELECT s.SaleID, s.InvoiceNumber, CONCAT(v.Make, ' ', v.Model, ' (', v.Year, ')') AS Vehicle, " +
                "CONCAT(c.FirstName, ' ', c.LastName) AS Customer, " +
                "CONCAT(e.FirstName, ' ', e.LastName) AS Employee, " +
                "s.SaleDate, s.SalePrice, s.TaxAmount, s.TotalPrice, s.SaleStatus " +
                "FROM Sales s " +
                "JOIN Vehicles v ON s.VehicleID = v.VehicleID " +
                "JOIN Customers c ON s.CustomerID = c.CustomerID " +
                "JOIN Employees e ON s.EmployeeID = e.EmployeeID";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("SaleID"));
                row.add(rs.getString("InvoiceNumber"));
                row.add(rs.getString("Vehicle"));
                row.add(rs.getString("Customer"));
                row.add(rs.getString("Employee"));
                row.add(rs.getTimestamp("SaleDate"));
                row.add(rs.getBigDecimal("SalePrice"));
                row.add(rs.getBigDecimal("TaxAmount"));
                row.add(rs.getBigDecimal("TotalPrice"));
                row.add(rs.getString("SaleStatus"));
                sales.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sales: " + e.getMessage());
            e.printStackTrace();
        }
        return sales;
    }

    public static List<Vector<Object>> searchSales(String searchText, String status) {
        List<Vector<Object>> sales = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT s.SaleID, s.InvoiceNumber, CONCAT(v.Make, ' ', v.Model, ' (', v.Year, ')') AS Vehicle, " +
                        "CONCAT(c.FirstName, ' ', c.LastName) AS Customer, " +
                        "CONCAT(e.FirstName, ' ', e.LastName) AS Employee, " +
                        "s.SaleDate, s.SalePrice, s.TaxAmount, s.TotalPrice, s.SaleStatus " +
                        "FROM Sales s " +
                        "JOIN Vehicles v ON s.VehicleID = v.VehicleID " +
                        "JOIN Customers c ON s.CustomerID = c.CustomerID " +
                        "JOIN Employees e ON s.EmployeeID = e.EmployeeID " +
                        "WHERE 1=1"
        );
        List<String> params = new ArrayList<>();

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND s.InvoiceNumber LIKE ?");
            params.add("%" + searchText.trim() + "%");
        }
        if (status != null && !status.equals("ALL")) {
            sql.append(" AND s.SaleStatus = ?");
            params.add(status);
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("SaleID"));
                    row.add(rs.getString("InvoiceNumber"));
                    row.add(rs.getString("Vehicle"));
                    row.add(rs.getString("Customer"));
                    row.add(rs.getString("Employee"));
                    row.add(rs.getTimestamp("SaleDate"));
                    row.add(rs.getBigDecimal("SalePrice"));
                    row.add(rs.getBigDecimal("TaxAmount"));
                    row.add(rs.getBigDecimal("TotalPrice"));
                    row.add(rs.getString("SaleStatus"));
                    sales.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching sales: " + e.getMessage());
            e.printStackTrace();
        }
        return sales;
    }

    public static boolean addSale(int vehicleId, int customerId, int employeeId, String saleDate, double salePrice, double taxAmount, String invoiceNumber, String status) {
        try (Connection conn = getConnection()) {
            if (!checkExists(conn, "Vehicles", "VehicleID", vehicleId)) {
                System.err.println("Cannot add sale: VehicleID " + vehicleId + " does not exist.");
                return false;
            }
            if (!checkExists(conn, "Customers", "CustomerID", customerId)) {
                System.err.println("Cannot add sale: CustomerID " + customerId + " does not exist.");
                return false;
            }
            if (!checkExists(conn, "Employees", "EmployeeID", employeeId)) {
                System.err.println("Cannot add sale: EmployeeID " + employeeId + " does not exist.");
                return false;
            }

            double totalPrice = salePrice + taxAmount;
            String sql = "INSERT INTO Sales (VehicleID, CustomerID, EmployeeID, SaleDate, SalePrice, TaxAmount, TotalPrice, SaleStatus, InvoiceNumber) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vehicleId);
                stmt.setInt(2, customerId);
                stmt.setInt(3, employeeId);
                stmt.setString(4, saleDate);
                stmt.setBigDecimal(5, BigDecimal.valueOf(salePrice));
                stmt.setBigDecimal(6, BigDecimal.valueOf(taxAmount));
                stmt.setBigDecimal(7, BigDecimal.valueOf(totalPrice));
                stmt.setString(8, status);
                stmt.setString(9, invoiceNumber);
                int result = stmt.executeUpdate();
                System.out.println("Add sale result: " + result + " rows affected");
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error adding sale: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateSale(int saleId, int vehicleId, int customerId, int employeeId, String saleDate, double salePrice, double taxAmount, String invoiceNumber, String status) {
        try (Connection conn = getConnection()) {
            if (!checkExists(conn, "Vehicles", "VehicleID", vehicleId)) {
                System.err.println("Cannot update sale: VehicleID " + vehicleId + " does not exist.");
                return false;
            }
            if (!checkExists(conn, "Customers", "CustomerID", customerId)) {
                System.err.println("Cannot update sale: CustomerID " + customerId + " does not exist.");
                return false;
            }
            if (!checkExists(conn, "Employees", "EmployeeID", employeeId)) {
                System.err.println("Cannot update sale: EmployeeID " + employeeId + " does not exist.");
                return false;
            }

            double totalPrice = salePrice + taxAmount;
            String sql = "UPDATE Sales SET VehicleID = ?, CustomerID = ?, EmployeeID = ?, SaleDate = ?, SalePrice = ?, TaxAmount = ?, TotalPrice = ?, SaleStatus = ?, InvoiceNumber = ? WHERE SaleID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, vehicleId);
                stmt.setInt(2, customerId);
                stmt.setInt(3, employeeId);
                stmt.setString(4, saleDate);
                stmt.setBigDecimal(5, BigDecimal.valueOf(salePrice));
                stmt.setBigDecimal(6, BigDecimal.valueOf(taxAmount));
                stmt.setBigDecimal(7, BigDecimal.valueOf(totalPrice));
                stmt.setString(8, status);
                stmt.setString(9, invoiceNumber);
                stmt.setInt(10, saleId);
                int result = stmt.executeUpdate();
                System.out.println("Update sale result: " + result + " rows affected");
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating sale: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSale(int saleId) {
        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM Sales WHERE SaleID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, saleId);
                int result = stmt.executeUpdate();
                System.out.println("Delete sale result: " + result + " rows affected");
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting sale: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet getSaleById(int saleId) throws SQLException {
        Connection conn = getConnection();
        String sql = "SELECT * FROM Sales WHERE SaleID = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, saleId);
        return stmt.executeQuery();
    }

    private static boolean checkExists(Connection conn, String tableName, String columnName, int value) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public static class EmployeeResult {
        private final ResultSet resultSet;
        private final PreparedStatement statement;
        private final Connection connection;

        public EmployeeResult(ResultSet resultSet, PreparedStatement statement, Connection connection) {
            this.resultSet = resultSet;
            this.statement = statement;
            this.connection = connection;
        }

        public ResultSet getResultSet() {
            return resultSet;
        }

        public void close() throws SQLException {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }

    public static List<Vector<Object>> getAllEmployees() {
        List<Vector<Object>> employees = new ArrayList<>();
        String sql = "SELECT e.EmployeeID, e.FirstName, e.LastName, e.Email, e.Phone, e.Position, e.HireDate, e.Salary, u.Username " +
                "FROM Employees e JOIN Users u ON e.UserID = u.UserID";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("EmployeeID"));
                row.add(rs.getString("FirstName"));
                row.add(rs.getString("LastName"));
                row.add(rs.getString("Email"));
                row.add(rs.getString("Phone"));
                row.add(rs.getString("Position"));
                row.add(rs.getDate("HireDate"));
                row.add(rs.getObject("Salary") != null ? rs.getBigDecimal("Salary") : "");
                row.add(rs.getString("Username"));
                employees.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employees: " + e.getMessage());
            e.printStackTrace();
        }
        return employees;
    }

    public static List<Vector<Object>> searchEmployees(String searchText) {
        List<Vector<Object>> employees = new ArrayList<>();
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllEmployees();
        }
        String sql = "SELECT e.EmployeeID, e.FirstName, e.LastName, e.Email, e.Phone, e.Position, e.HireDate, e.Salary, u.Username " +
                "FROM Employees e JOIN Users u ON e.UserID = u.UserID " +
                "WHERE e.FirstName LIKE ? OR e.LastName LIKE ? OR e.Email LIKE ? OR u.Username LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchText.trim() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("EmployeeID"));
                    row.add(rs.getString("FirstName"));
                    row.add(rs.getString("LastName"));
                    row.add(rs.getString("Email"));
                    row.add(rs.getString("Phone"));
                    row.add(rs.getString("Position"));
                    row.add(rs.getDate("HireDate"));
                    row.add(rs.getObject("Salary") != null ? rs.getBigDecimal("Salary") : "");
                    row.add(rs.getString("Username"));
                    employees.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching employees: " + e.getMessage());
            e.printStackTrace();
        }
        return employees;
    }

    public static EmployeeResult getEmployeeById(int employeeId) {
        String sql = "SELECT e.EmployeeID, e.FirstName, e.LastName, e.Email, e.Phone, e.Position, e.HireDate, e.Salary, u.Username, u.Role, u.PasswordHash " +
                "FROM Employees e JOIN Users u ON e.UserID = u.UserID WHERE e.EmployeeID = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, employeeId);
            System.out.println("Executing getEmployeeById for EmployeeID: " + employeeId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.isBeforeFirst()) {
                System.out.println("No employee found in database for EmployeeID: " + employeeId);
            }
            return new EmployeeResult(rs, stmt, conn);
        } catch (SQLException e) {
            System.err.println("Error fetching employee by ID " + employeeId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean addEmployee(String firstName, String lastName, String email, String phone, String position,
                                      String hireDate, String salary, String username, String passwordHash, String role) {
        Connection conn = null;
        PreparedStatement userStmt = null;
        PreparedStatement empStmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.err.println("Invalid email format");
                return false;
            }
            if (!Arrays.asList("admin", "employee").contains(role)) {
                System.err.println("Invalid role: must be 'admin' or 'employee'");
                return false;
            }
            BigDecimal salaryValue = null;
            if (!salary.isEmpty()) {
                try {
                    salaryValue = new BigDecimal(salary);
                    if (salaryValue.compareTo(BigDecimal.ZERO) <= 0) {
                        System.err.println("Salary must be positive");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid salary format");
                    return false;
                }
            }

            String userQuery = "INSERT INTO Users (Username, PasswordHash, Role, CreatedAt) VALUES (?, ?, ?, GETDATE())";
            userStmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, passwordHash); // Note: Should use hashed password in production
            userStmt.setString(3, role);
            int userResult = userStmt.executeUpdate();

            if (userResult == 0) {
                conn.rollback();
                System.err.println("Failed to create user account");
                return false;
            }

            ResultSet rs = userStmt.getGeneratedKeys();
            if (!rs.next()) {
                conn.rollback();
                System.err.println("Failed to retrieve generated UserID");
                return false;
            }
            int userId = rs.getInt(1);

            String empQuery = "INSERT INTO Employees (UserID, FirstName, LastName, Email, Phone, Position, HireDate, Salary) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            empStmt = conn.prepareStatement(empQuery);
            empStmt.setInt(1, userId);
            empStmt.setString(2, firstName);
            empStmt.setString(3, lastName);
            empStmt.setString(4, email);
            empStmt.setString(5, phone);
            empStmt.setString(6, position);
            empStmt.setString(7, hireDate);
            empStmt.setObject(8, salaryValue);
            int empResult = empStmt.executeUpdate();

            conn.commit();
            System.out.println("Add employee result: " + empResult + " rows affected");
            return empResult > 0;
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message.contains("UNIQUE KEY constraint") && message.contains("Email")) {
                message = "Email already exists";
            } else if (message.contains("UNIQUE KEY constraint") && message.contains("Username")) {
                message = "Username already exists";
            } else {
                message = "Database error: " + message;
            }
            System.err.println("Error adding employee: " + message);
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (userStmt != null) userStmt.close();
                if (empStmt != null) empStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static boolean updateEmployee(int employeeId, String firstName, String lastName, String email, String phone,
                                         String position, String hireDate, String salary, String username, String password, String role) {
        Connection conn = null;
        PreparedStatement empStmt = null;
        PreparedStatement userStmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.err.println("Invalid email format");
                return false;
            }
            if (!Arrays.asList("admin", "employee").contains(role)) {
                System.err.println("Invalid role: must be 'admin' or 'employee'");
                return false;
            }
            BigDecimal salaryValue = null;
            if (!salary.isEmpty()) {
                try {
                    salaryValue = new BigDecimal(salary);
                    if (salaryValue.compareTo(BigDecimal.ZERO) <= 0) {
                        System.err.println("Salary must be positive");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid salary format");
                    return false;
                }
            }

            String getUserIdQuery = "SELECT UserID FROM Employees WHERE EmployeeID = ?";
            try (PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdQuery)) {
                getUserIdStmt.setInt(1, employeeId);
                ResultSet rs = getUserIdStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    System.err.println("EmployeeID " + employeeId + " not found");
                    return false;
                }
                int userId = rs.getInt("UserID");

                String userQuery;
                if (password != null && !password.isEmpty()) {
                    userQuery = "UPDATE Users SET Username = ?, PasswordHash = ?, Role = ? WHERE UserID = ?";
                    userStmt = conn.prepareStatement(userQuery);
                    userStmt.setString(1, username);
                    userStmt.setString(2, password); // Note: Should use hashed password in production
                    userStmt.setString(3, role);
                    userStmt.setInt(4, userId);
                } else {
                    userQuery = "UPDATE Users SET Username = ?, Role = ? WHERE UserID = ?";
                    userStmt = conn.prepareStatement(userQuery);
                    userStmt.setString(1, username);
                    userStmt.setString(2, role);
                    userStmt.setInt(3, userId);
                }
                userStmt.executeUpdate();

                String empQuery = "UPDATE Employees SET FirstName = ?, LastName = ?, Email = ?, Phone = ?, Position = ?, HireDate = ?, Salary = ? WHERE EmployeeID = ?";
                empStmt = conn.prepareStatement(empQuery);
                empStmt.setString(1, firstName);
                empStmt.setString(2, lastName);
                empStmt.setString(3, email);
                empStmt.setString(4, phone);
                empStmt.setString(5, position);
                empStmt.setString(6, hireDate);
                empStmt.setObject(7, salaryValue);
                empStmt.setInt(8, employeeId);
                int empResult = empStmt.executeUpdate();

                conn.commit();
                System.out.println("Update employee result: " + empResult + " rows affected");
                return empResult > 0;
            }
        } catch (SQLException e) {
            String message = e.getMessage();
            if (message.contains("UNIQUE KEY constraint") && message.contains("Email")) {
                message = "Email already exists";
            } else if (message.contains("UNIQUE KEY constraint") && message.contains("Username")) {
                message = "Username already exists";
            } else {
                message = "Database error: " + message;
            }
            System.err.println("Error updating employee: " + message);
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (userStmt != null) userStmt.close();
                if (empStmt != null) empStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteEmployee(int employeeId) {
        Connection conn = null;
        PreparedStatement empStmt = null;
        PreparedStatement userStmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String associatedRecordsMessage = hasAssociatedEmployeeRecords(conn, employeeId);
            if (associatedRecordsMessage != null) {
                System.err.println(associatedRecordsMessage);
                return false;
            }

            String getUserIdQuery = "SELECT UserID FROM Employees WHERE EmployeeID = ?";
            try (PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdQuery)) {
                getUserIdStmt.setInt(1, employeeId);
                ResultSet rs = getUserIdStmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    System.err.println("EmployeeID " + employeeId + " not found");
                    return false;
                }
                int userId = rs.getInt("UserID");

                String empQuery = "DELETE FROM Employees WHERE EmployeeID = ?";
                empStmt = conn.prepareStatement(empQuery);
                empStmt.setInt(1, employeeId);
                int empResult = empStmt.executeUpdate();

                String userQuery = "DELETE FROM Users WHERE UserID = ?";
                userStmt = conn.prepareStatement(userQuery);
                userStmt.setInt(1, userId);
                int userResult = userStmt.executeUpdate();

                if (empResult == 0 || userResult == 0) {
                    conn.rollback();
                    System.err.println("Failed to delete employee: No rows affected (Employees: " + empResult + ", Users: " + userResult + ")");
                    return false;
                }

                conn.commit();
                System.out.println("Delete successful: Deleted " + empResult + " employee row(s) and " + userResult + " user row(s) for EmployeeID " + employeeId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (empStmt != null) empStmt.close();
                if (userStmt != null) userStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static String hasAssociatedEmployeeRecords(Connection conn, int employeeId) throws SQLException {
        String[] tables = {"Sales", "TestDrives"};
        String[] employeeIdColumns = {"EmployeeID", "EmployeeID"};

        for (int i = 0; i < tables.length; i++) {
            String table = tables[i];
            String column = employeeIdColumns[i];
            String checkSql = "SELECT COUNT(*) AS count FROM " + table + " WHERE " + column + " = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, employeeId);
                try (ResultSet checkRs = checkStmt.executeQuery()) {
                    if (checkRs.next() && checkRs.getInt("count") > 0) {
                        return "Cannot delete employee: Associated records exist in " + table;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error checking associated records in " + table + ": " + e.getMessage());
                return "Cannot delete employee: Error checking associated records in " + table;
            }
        }
        return null;
    }

    public static void getProfileData(String username, String[] profileData) throws SQLException {
        // First, determine the user's role
        String roleQuery = "SELECT Role FROM Users WHERE Username = ?";
        String role = null;
        try (Connection conn = getConnection();
             PreparedStatement roleStmt = conn.prepareStatement(roleQuery)) {
            roleStmt.setString(1, username);
            try (ResultSet roleRs = roleStmt.executeQuery()) {
                if (roleRs.next()) {
                    role = roleRs.getString("Role");
                } else {
                    throw new SQLException("User not found: " + username);
                }
            }
        }

        // Fetch profile data based on the role
        String sql;
        if ("customer".equals(role)) {
            sql = "SELECT c.FirstName, c.LastName, c.Email, c.Phone " +
                    "FROM Users u JOIN Customers c ON u.UserID = c.UserID " +
                    "WHERE u.Username = ?";
        } else if ("admin".equals(role) || "employee".equals(role)) {
            sql = "SELECT e.FirstName, e.LastName, e.Email, e.Phone " +
                    "FROM Users u JOIN Employees e ON u.UserID = e.UserID " +
                    "WHERE u.Username = ?";
        } else {
            throw new SQLException("Unsupported role: " + role);
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    profileData[0] = rs.getString("FirstName") + " " + rs.getString("LastName");
                    profileData[1] = rs.getString("Email") != null ? rs.getString("Email") : "N/A";
                    profileData[2] = rs.getString("Phone") != null ? rs.getString("Phone") : "N/A";
                } else {
                    Arrays.fill(profileData, "N/A");
                }
            }
        }
    }

    public static boolean updateProfileData(String username, String fullName, String email, String phone) throws SQLException {
        // First, determine the user's role
        String roleQuery = "SELECT Role FROM Users WHERE Username = ?";
        String role = null;
        try (Connection conn = getConnection();
             PreparedStatement roleStmt = conn.prepareStatement(roleQuery)) {
            roleStmt.setString(1, username);
            try (ResultSet roleRs = roleStmt.executeQuery()) {
                if (roleRs.next()) {
                    role = roleRs.getString("Role");
                } else {
                    throw new SQLException("User not found: " + username);
                }
            }
        }

        // Split the full name into first and last names
        String[] nameParts = fullName.trim().split("\\s+", 2);
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Update based on the role
        String sql;
        if ("customer".equals(role)) {
            sql = "UPDATE c SET c.FirstName = ?, c.LastName = ?, c.Email = ?, c.Phone = ? " +
                    "FROM Customers c INNER JOIN Users u ON u.UserID = c.UserID " +
                    "WHERE u.Username = ?";
        } else if ("admin".equals(role) || "employee".equals(role)) {
            sql = "UPDATE e SET e.FirstName = ?, e.LastName = ?, e.Email = ?, e.Phone = ? " +
                    "FROM Employees e INNER JOIN Users u ON u.UserID = e.UserID " +
                    "WHERE u.Username = ?";
        } else {
            throw new SQLException("Unsupported role: " + role);
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, username);
            int result = stmt.executeUpdate();
            return result > 0;
        }
    }

    public static boolean changeUsername(String currentUsername, String newUsername) throws SQLException {
        if (newUsername.equals(currentUsername)) {
            return false;
        }

        String checkSql = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, newUsername);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
        }

        String updateSql = "UPDATE Users SET Username = ? WHERE Username = ?";
        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setString(1, newUsername);
            updateStmt.setString(2, currentUsername);
            int result = updateStmt.executeUpdate();
            return result > 0;
        }
    }

    public static int getCustomerOrderCount(String username) {
        int orderCount = 0;
        String sql = "SELECT COUNT(s.SaleID) AS OrderCount " +
                "FROM Users u " +
                "JOIN Customers c ON u.UserID = c.UserID " +
                "LEFT JOIN Sales s ON c.CustomerID = s.CustomerID " +
                "WHERE u.Username = ? AND u.Role = 'customer'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    orderCount = rs.getInt("OrderCount");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customer order count: " + e.getMessage());
            e.printStackTrace();
        }
        return orderCount;
    }

    public static boolean changePassword(String username, String currentPassword, String newPassword) throws SQLException {
        String verifySql = "SELECT PasswordHash FROM Users WHERE Username = ?";
        String storedPassword = null;
        try (Connection conn = getConnection();
             PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {
            verifyStmt.setString(1, username);
            try (ResultSet rs = verifyStmt.executeQuery()) {
                if (rs.next()) {
                    storedPassword = rs.getString("PasswordHash");
                }
            }
        }

        if (!currentPassword.equals(storedPassword)) { // Note: Should use hashed password comparison in production
            return false;
        }

        String updateSql = "UPDATE Users SET PasswordHash = ? WHERE Username = ?";
        try (Connection conn = getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setString(1, newPassword); // Note: Should hash newPassword before storing
            updateStmt.setString(2, username);
            int result = updateStmt.executeUpdate();
            return result > 0;
        }
    }

    public static boolean updateVehicleStatus(String vehicleId, String status) {
        String query = "UPDATE Vehicles SET Status = ? WHERE VehicleID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, Integer.parseInt(vehicleId));
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Update vehicle status result: " + rowsAffected + " rows affected for VehicleID " + vehicleId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating vehicle status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addSale(int vehicleId, int customerId, Integer employeeId, String saleDate, double salePrice, double taxAmount, String invoiceNumber, String saleStatus) {
        String query = "INSERT INTO Sales (VehicleID, CustomerID, EmployeeID, SaleDate, SalePrice, TaxAmount, TotalPrice, InvoiceNumber, SaleStatus) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, vehicleId);
            stmt.setInt(2, customerId);

            // Fix: Set NULL instead of -1 when employeeId is null
            if (employeeId != null) {
                stmt.setInt(3, employeeId);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER); // Set NULL instead of -1
            }

            stmt.setString(4, saleDate);
            stmt.setDouble(5, salePrice);
            stmt.setDouble(6, taxAmount);
            stmt.setDouble(7, salePrice + taxAmount); // Calculate TotalPrice
            stmt.setString(8, invoiceNumber);
            stmt.setString(9, saleStatus);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Add sale result: " + rowsAffected + " rows affected for VehicleID " + vehicleId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding sale: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public static boolean placeOrder(String vehicleId, String customerId, String employeeId, double salePrice) {
        String query = "INSERT INTO Sales (VehicleID, CustomerID, EmployeeID, SaleDate, SalePrice, TaxAmount, TotalPrice, SaleStatus, InvoiceNumber) " +
                "VALUES (?, ?, ?, GETDATE(), ?, ?, ?, 'Pending', ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            double taxRate = 0.08; // 8% tax rate; adjust as needed
            double taxAmount = salePrice * taxRate;
            double totalPrice = salePrice + taxAmount;
            String invoiceNumber = "INV-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());

            stmt.setInt(1, Integer.parseInt(vehicleId));
            stmt.setInt(2, Integer.parseInt(customerId));

            // Fix: Handle null employeeId properly
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                stmt.setInt(3, Integer.parseInt(employeeId));
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER); // Set NULL instead of trying to parse null/empty string
            }

            stmt.setBigDecimal(4, BigDecimal.valueOf(salePrice));
            stmt.setBigDecimal(5, BigDecimal.valueOf(taxAmount));
            stmt.setBigDecimal(6, BigDecimal.valueOf(totalPrice));
            stmt.setString(7, invoiceNumber);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Place order result: " + rowsAffected + " rows affected for VehicleID " + vehicleId);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error placing order: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String getCustomerIdByUsername(String username) {
        String query = "SELECT c.CustomerID FROM Customers c JOIN Users u ON c.UserID = u.UserID WHERE u.Username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getInt("CustomerID"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching CustomerID for username " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
//    -----------------------------------order requests-------------------------------
// Complete database functions for your DBConnection class

    // Complete database functions for your DBConnection class

// Fixed database functions for your DBConnection class

    // Fixed database functions for your DBConnection class

    /**
     * Get all pending orders (sales with null EmployeeID and status 'Pending')
     * @return List of Vector objects containing order details
     */
    public static List<Vector<Object>> getPendingOrders() {
        List<Vector<Object>> orders = new ArrayList<>();
        String query = """
        SELECT 
            s.SaleID,
            s.InvoiceNumber,
            CONCAT(c.FirstName, ' ', c.LastName) AS CustomerName,
            CONCAT(v.Make, ' ', v.Model, ' (', v.Year, ')') AS VehicleInfo,
            FORMAT(s.SaleDate, 'yyyy-MM-dd HH:mm') AS SaleDate,
            s.SalePrice,
            s.TaxAmount,
            s.TotalPrice,
            s.SaleStatus,
            v.VehicleID
        FROM Sales s
        JOIN Customers c ON s.CustomerID = c.CustomerID
        JOIN Vehicles v ON s.VehicleID = v.VehicleID
        WHERE s.EmployeeID IS NULL 
        AND s.SaleStatus = 'Pending'
        ORDER BY s.SaleDate DESC
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("SaleID"));                    // 0
                row.add(rs.getString("InvoiceNumber"));          // 1
                row.add(rs.getString("CustomerName"));           // 2
                row.add(rs.getString("VehicleInfo"));            // 3
                row.add(rs.getString("SaleDate"));               // 4
                row.add(rs.getBigDecimal("SalePrice"));          // 5
                row.add(rs.getBigDecimal("TaxAmount"));          // 6
                row.add(rs.getBigDecimal("TotalPrice"));         // 7
                row.add(rs.getString("SaleStatus"));             // 8
                row.add(rs.getInt("VehicleID"));                 // 9 (for internal use)
                orders.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending orders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    /**
     * Search pending orders by customer name, invoice number, or vehicle info
     * @param searchText Search criteria
     * @return List of Vector objects containing matching order details
     */
    public static List<Vector<Object>> searchPendingOrders(String searchText) {
        List<Vector<Object>> orders = new ArrayList<>();
        String query = """
        SELECT 
            s.SaleID,
            s.InvoiceNumber,
            CONCAT(c.FirstName, ' ', c.LastName) AS CustomerName,
            CONCAT(v.Make, ' ', v.Model, ' (', v.Year, ')') AS VehicleInfo,
            FORMAT(s.SaleDate, 'yyyy-MM-dd HH:mm') AS SaleDate,
            s.SalePrice,
            s.TaxAmount,
            s.TotalPrice,
            s.SaleStatus,
            v.VehicleID
        FROM Sales s
        JOIN Customers c ON s.CustomerID = c.CustomerID
        JOIN Vehicles v ON s.VehicleID = v.VehicleID
        WHERE s.EmployeeID IS NULL 
        AND s.SaleStatus = 'Pending'
        AND (
            CONCAT(c.FirstName, ' ', c.LastName) LIKE ? OR
            s.InvoiceNumber LIKE ? OR
            CONCAT(v.Make, ' ', v.Model) LIKE ? OR
            v.VIN LIKE ?
        )
        ORDER BY s.SaleDate DESC
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchText + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("SaleID"));                    // 0
                    row.add(rs.getString("InvoiceNumber"));          // 1
                    row.add(rs.getString("CustomerName"));           // 2
                    row.add(rs.getString("VehicleInfo"));            // 3
                    row.add(rs.getString("SaleDate"));               // 4
                    row.add(rs.getBigDecimal("SalePrice"));          // 5
                    row.add(rs.getBigDecimal("TaxAmount"));          // 6
                    row.add(rs.getBigDecimal("TotalPrice"));         // 7
                    row.add(rs.getString("SaleStatus"));             // 8
                    row.add(rs.getInt("VehicleID"));                 // 9 (for internal use)
                    orders.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching pending orders: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    /**
     * Confirm an order by updating the sale status and vehicle status
     * @param saleId The sale ID to confirm
     * @param employeeId The employee confirming the order
     * @return true if successful, false otherwise
     */
    public static boolean confirmOrder(int saleId, int employeeId) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            // First, validate that the employee exists
            String validateEmployeeQuery = "SELECT COUNT(*) FROM Employees WHERE EmployeeID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(validateEmployeeQuery)) {
                stmt.setInt(1, employeeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.err.println("Employee not found with ID: " + employeeId);
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Get the vehicle ID from the sale and validate sale exists
            int vehicleId = -1;
            String getVehicleQuery = "SELECT VehicleID FROM Sales WHERE SaleID = ? AND SaleStatus = 'Pending' AND EmployeeID IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(getVehicleQuery)) {
                stmt.setInt(1, saleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        vehicleId = rs.getInt("VehicleID");
                    } else {
                        System.err.println("Pending sale not found with ID: " + saleId + " or sale already processed");
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Update the sales record
            String updateSaleQuery = """
            UPDATE Sales 
            SET SaleStatus = 'Completed', 
                EmployeeID = ?
            WHERE SaleID = ? AND SaleStatus = 'Pending' AND EmployeeID IS NULL
        """;

            int salesUpdated = 0;
            try (PreparedStatement stmt = conn.prepareStatement(updateSaleQuery)) {
                stmt.setInt(1, employeeId);
                stmt.setInt(2, saleId);
                salesUpdated = stmt.executeUpdate();
            }

            if (salesUpdated == 0) {
                System.err.println("Failed to update sale. SaleID: " + saleId + " may have been processed already");
                conn.rollback();
                return false;
            }

            // Update the vehicle status to 'Sold'
            String updateVehicleQuery = """
            UPDATE Vehicles 
            SET Status = 'Sold'
            WHERE VehicleID = ? AND Status = 'On Hold'
        """;

            int vehiclesUpdated = 0;
            try (PreparedStatement stmt = conn.prepareStatement(updateVehicleQuery)) {
                stmt.setInt(1, vehicleId);
                vehiclesUpdated = stmt.executeUpdate();
            }

            if (vehiclesUpdated == 0) {
                System.err.println("Vehicle not available for sale or already sold. VehicleID: " + vehicleId);
                conn.rollback();
                return false;
            }

            // Commit the transaction
            conn.commit();
            System.out.println("Order confirmed successfully. SaleID: " + saleId + ", EmployeeID: " + employeeId);
            return true;

        } catch (SQLException e) {
            System.err.println("Error confirming order: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("Transaction rolled back due to error");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset to default
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Get order details by Sale ID
     * @param saleId The sale ID to retrieve
     * @return Vector containing order details or null if not found
     */
    public static Vector<Object> getOrderDetails(int saleId) {
        String query = """
        SELECT 
            s.SaleID,
            s.InvoiceNumber,
            CONCAT(c.FirstName, ' ', c.LastName) AS CustomerName,
            c.Email AS CustomerEmail,
            c.Phone AS CustomerPhone,
            CONCAT(v.Make, ' ', v.Model, ' (', v.Year, ')') AS VehicleInfo,
            v.VIN,
            v.Color,
            v.Mileage,
            FORMAT(s.SaleDate, 'yyyy-MM-dd HH:mm') AS SaleDate,
            s.SalePrice,
            s.TaxAmount,
            s.TotalPrice,
            s.SaleStatus,
            s.EmployeeID,
            CONCAT(e.FirstName, ' ', e.LastName) AS EmployeeName
        FROM Sales s
        JOIN Customers c ON s.CustomerID = c.CustomerID
        JOIN Vehicles v ON s.VehicleID = v.VehicleID
        LEFT JOIN Employees e ON s.EmployeeID = e.EmployeeID
        WHERE s.SaleID = ?
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, saleId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Vector<Object> orderDetails = new Vector<>();
                    orderDetails.add(rs.getInt("SaleID"));                   // 0
                    orderDetails.add(rs.getString("InvoiceNumber"));         // 1
                    orderDetails.add(rs.getString("CustomerName"));          // 2
                    orderDetails.add(rs.getString("CustomerEmail"));         // 3
                    orderDetails.add(rs.getString("CustomerPhone"));         // 4
                    orderDetails.add(rs.getString("VehicleInfo"));           // 5
                    orderDetails.add(rs.getString("VIN"));                   // 6
                    orderDetails.add(rs.getString("Color"));                 // 7
                    orderDetails.add(rs.getInt("Mileage"));                  // 8
                    orderDetails.add(rs.getString("SaleDate"));              // 9
                    orderDetails.add(rs.getBigDecimal("SalePrice"));         // 10
                    orderDetails.add(rs.getBigDecimal("TaxAmount"));         // 11
                    orderDetails.add(rs.getBigDecimal("TotalPrice"));        // 12
                    orderDetails.add(rs.getString("SaleStatus"));            // 13
                    orderDetails.add(rs.getObject("EmployeeID"));            // 14 (can be null)
                    orderDetails.add(rs.getString("EmployeeName"));          // 15 (can be null)
                    return orderDetails;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order details: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get pending orders count
     * @return Number of pending orders
     */
    public static int getPendingOrdersCount() {
        String query = """
        SELECT COUNT(*) as PendingCount
        FROM Sales s
        WHERE s.EmployeeID IS NULL 
        AND s.SaleStatus = 'Pending'
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("PendingCount");
            }
        } catch (SQLException e) {
            System.err.println("Error getting pending orders count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get orders confirmed by a specific employee
     * @param employeeId The employee ID
     * @param limit Maximum number of records to return (0 for no limit)
     * @return List of Vector objects containing order details
     */
    public static List<Vector<Object>> getOrdersByEmployee(int employeeId, int limit) {
        List<Vector<Object>> orders = new ArrayList<>();

        // Build the query with proper SQL Server syntax for limiting results
        String query = """
        SELECT 
            s.SaleID,
            s.InvoiceNumber,
            CONCAT(c.FirstName, ' ', c.LastName) AS CustomerName,
            CONCAT(v.Make, ' ', v.Model, ' (', v.Year, ')') AS VehicleInfo,
            FORMAT(s.SaleDate, 'yyyy-MM-dd HH:mm') AS SaleDate,
            s.SalePrice,
            s.TaxAmount,
            s.TotalPrice,
            s.SaleStatus
        FROM Sales s
        JOIN Customers c ON s.CustomerID = c.CustomerID
        JOIN Vehicles v ON s.VehicleID = v.VehicleID
        WHERE s.EmployeeID = ?
        ORDER BY s.SaleDate DESC
    """;

        // Add TOP clause for SQL Server if limit is specified
        if (limit > 0) {
            query = query.replace("SELECT ", "SELECT TOP " + limit + " ");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("SaleID"));                    // 0
                    row.add(rs.getString("InvoiceNumber"));          // 1
                    row.add(rs.getString("CustomerName"));           // 2
                    row.add(rs.getString("VehicleInfo"));            // 3
                    row.add(rs.getString("SaleDate"));               // 4
                    row.add(rs.getBigDecimal("SalePrice"));          // 5
                    row.add(rs.getBigDecimal("TaxAmount"));          // 6
                    row.add(rs.getBigDecimal("TotalPrice"));         // 7
                    row.add(rs.getString("SaleStatus"));             // 8
                    orders.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders by employee: " + e.getMessage());
            e.printStackTrace();
        }

        return orders;
    }

    /**
     * Cancel a pending order
     * @param saleId The sale ID to cancel
     * @param reason Reason for cancellation
     * @return true if successful, false otherwise
     */
    public static boolean cancelOrder(int saleId, String reason) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Get vehicle ID before cancelling
            int vehicleId = -1;
            String getVehicleQuery = "SELECT VehicleID FROM Sales WHERE SaleID = ? AND SaleStatus = 'Pending'";
            try (PreparedStatement stmt = conn.prepareStatement(getVehicleQuery)) {
                stmt.setInt(1, saleId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        vehicleId = rs.getInt("VehicleID");
                    } else {
                        System.err.println("Pending sale not found with ID: " + saleId);
                        conn.rollback();
                        return false;
                    }
                }
            }

            // For cancellation, we'll just update the status since we don't have cancellation columns
            // You may want to add these columns to your Sales table if needed
            String updateSaleQuery = """
            UPDATE Sales 
            SET SaleStatus = 'Cancelled'
            WHERE SaleID = ? AND SaleStatus = 'Pending'
        """;

            int salesUpdated = 0;
            try (PreparedStatement stmt = conn.prepareStatement(updateSaleQuery)) {
                stmt.setInt(1, saleId);
                salesUpdated = stmt.executeUpdate();
            }

            if (salesUpdated == 0) {
                System.err.println("No pending sale found to cancel with ID: " + saleId);
                conn.rollback();
                return false;
            }

            // Make vehicle available again
            String updateVehicleQuery = """
            UPDATE Vehicles 
            SET Status = 'Available'
            WHERE VehicleID = ?
        """;

            try (PreparedStatement stmt = conn.prepareStatement(updateVehicleQuery)) {
                stmt.setInt(1, vehicleId);
                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Order cancelled successfully. SaleID: " + saleId + ". Reason: " + reason);
            return true;

        } catch (SQLException e) {
            System.err.println("Error cancelling order: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

    }
//    ----------------------------------customer orders------------------------------------------------------------
    /**
     * Retrieves all orders for a specific customer from the database.
     * @param customerID The ID of the customer whose orders are to be retrieved.
     * @return A list of Object arrays containing order details.
     * @throws SQLException If a database error occurs.
     */
    public static List<Object[]> getOrdersFromDatabase(int customerID) throws SQLException {
        List<Object[]> orders = new ArrayList<>();
        String query = """
        SELECT 
            s.SaleID, 
            s.InvoiceNumber, 
            v.VIN, 
            v.Make, 
            v.Model, 
            v.Year, 
            FORMAT(s.SaleDate, 'yyyy-MM-dd HH:mm') AS SaleDate, 
            s.SalePrice, 
            s.TaxAmount, 
            s.TotalPrice, 
            s.SaleStatus, 
            CONCAT(e.FirstName, ' ', e.LastName) AS SalesPerson 
        FROM Sales s 
        INNER JOIN Vehicles v ON s.VehicleID = v.VehicleID 
        LEFT JOIN Employees e ON s.EmployeeID = e.EmployeeID 
        WHERE s.CustomerID = ? 
        ORDER BY 
            CASE 
                WHEN s.SaleStatus = 'Pending' THEN 1 
                WHEN s.SaleStatus = 'Completed' THEN 2 
                ELSE 3 
            END, 
            s.SaleDate DESC
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[12];
                    row[0] = rs.getInt("SaleID");
                    row[1] = rs.getString("InvoiceNumber");
                    row[2] = rs.getString("VIN");
                    row[3] = rs.getString("Make");
                    row[4] = rs.getString("Model");
                    row[5] = rs.getInt("Year");
                    row[6] = rs.getString("SaleDate"); // Formatted as string
                    row[7] = rs.getBigDecimal("SalePrice");
                    row[8] = rs.getBigDecimal("TaxAmount");
                    row[9] = rs.getBigDecimal("TotalPrice");
                    row[10] = rs.getString("SaleStatus");
                    row[11] = rs.getString("SalesPerson") != null ? rs.getString("SalesPerson") : "Unassigned"; // Handle NULL SalesPerson
                    orders.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders for customerID " + customerID + ": " + e.getMessage());
            e.printStackTrace();
            throw e; // Rethrow to let the caller handle the exception
        }
        return orders;
    }
    public static String getEmployeeIdByUsername(String username) {
        String query = "SELECT E.EmployeeID FROM Employees E JOIN Users u ON E.UserID = u.UserID WHERE u.Username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return String.valueOf(rs.getInt("EmployeeID"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching CustomerID for username " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}