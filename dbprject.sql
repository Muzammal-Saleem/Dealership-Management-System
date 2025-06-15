-- Create Database
CREATE DATABASE CarDealership;
USE CarDealership;


-- alteratiuons
ALTER TABLE Vehicles
ADD SupplierID INT;


ALTER TABLE Vehicles
ADD CONSTRAINT FK_Vehicles_Suppliers
FOREIGN KEY (SupplierID) REFERENCES Suppliers(SupplierID);



-- Create Tables

-- USERS table for login/authentication
CREATE TABLE Users (
    UserID INT PRIMARY KEY IDENTITY(1,1),
    Username VARCHAR(50) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    Role VARCHAR(20) CHECK (Role IN ('admin', 'employee', 'customer')),
    CreatedAt DATETIME DEFAULT GETDATE(),
    LastLogin DATETIME
);


-- VEHICLE CATEGORIES
CREATE TABLE VehicleCategories (
    CategoryID INT PRIMARY KEY IDENTITY(1,1),
    CategoryName VARCHAR(50) NOT NULL,
    Description NVARCHAR(MAX)
);

-- VEHICLES
CREATE TABLE Vehicles (
    VehicleID INT PRIMARY KEY IDENTITY(1,1),
    VIN VARCHAR(17) UNIQUE NOT NULL,
    Make VARCHAR(50) NOT NULL,
    Model VARCHAR(50) NOT NULL,
    Year INT NOT NULL,
    Color VARCHAR(30) NOT NULL,
    Mileage INT NOT NULL,
    Condition VARCHAR(20) NOT NULL,
    PurchasePrice DECIMAL(10, 2) NOT NULL,
    ListPrice DECIMAL(10, 2) NOT NULL,
    Status VARCHAR(20) CHECK (Status IN ('Available', 'Sold', 'On Hold')) DEFAULT 'Available',
    CategoryID INT NOT NULL,
    DateAcquired DATE NOT NULL,
    Description NVARCHAR(MAX),
    ImageURL VARCHAR(255),
    FeaturedVehicle BIT DEFAULT 0,
    FOREIGN KEY (CategoryID) REFERENCES VehicleCategories(CategoryID)
);

-- CUSTOMERS
CREATE TABLE Customers (
    CustomerID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT UNIQUE,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    Phone VARCHAR(20) NOT NULL,
    Address VARCHAR(100),
    City VARCHAR(50),
    State VARCHAR(20),
    ZipCode VARCHAR(10),
    DateRegistered DATE NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- EMPLOYEES
CREATE TABLE Employees (
    EmployeeID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT UNIQUE,
    FirstName VARCHAR(50) NOT NULL,
    LastName VARCHAR(50) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    Phone VARCHAR(20) NOT NULL,
    Position VARCHAR(50) NOT NULL,
    HireDate DATE NOT NULL,
    Salary DECIMAL(10, 2),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- SALES
CREATE TABLE Sales (
    SaleID INT PRIMARY KEY IDENTITY(1,1),
    VehicleID INT NOT NULL,
    CustomerID INT NOT NULL,
    EmployeeID INT NOT NULL,
    SaleDate DATETIME NOT NULL,
    SalePrice DECIMAL(10, 2) NOT NULL,
    TaxAmount DECIMAL(10, 2) NOT NULL,
    TotalPrice DECIMAL(10, 2) NOT NULL,
    SaleStatus VARCHAR(10) CHECK (SaleStatus IN ('Pending', 'Completed')) DEFAULT 'Pending',
    InvoiceNumber VARCHAR(20) UNIQUE NOT NULL,
    FOREIGN KEY (VehicleID) REFERENCES Vehicles(VehicleID),
    FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID),
    FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID)
);

-- SERVICE DEPARTMENT
CREATE TABLE ServiceDepartment (
    ServiceID INT PRIMARY KEY IDENTITY(1,1),
    CustomerID INT,
    VehicleID INT,
    ServiceType VARCHAR(20) NOT NULL,
    ServiceDate DATETIME NOT NULL,
    TechnicianID INT,
    Description NVARCHAR(MAX) NOT NULL,
    Cost DECIMAL(10, 2),
    Status VARCHAR(20) CHECK (Status IN ('Scheduled', 'In Progress', 'Completed')) DEFAULT 'Scheduled',
    FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID),
    FOREIGN KEY (VehicleID) REFERENCES Vehicles(VehicleID),
    FOREIGN KEY (TechnicianID) REFERENCES Employees(EmployeeID)
);

-- SUPPLIERS
CREATE TABLE Suppliers (
    SupplierID INT PRIMARY KEY IDENTITY(1,1),
    SupplierName VARCHAR(100) NOT NULL,
    ContactPerson VARCHAR(100),
    Email VARCHAR(100),
    Phone VARCHAR(20) NOT NULL,
    Address VARCHAR(100)
);

-- TEST DRIVES

CREATE TABLE TestDrives (
    TestDriveID INT PRIMARY KEY IDENTITY(1,1),
    CustomerID INT NOT NULL,
    VehicleID INT NOT NULL,
    EmployeeID INT NOT NULL,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NOT NULL,
    FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID),
    FOREIGN KEY (VehicleID) REFERENCES Vehicles(VehicleID),
    FOREIGN KEY (EmployeeID) REFERENCES Employees(EmployeeID)
);

-- Create Indexes
CREATE INDEX idx_vehicles_status ON Vehicles(Status);
CREATE INDEX idx_vehicles_make_model ON Vehicles(Make, Model);
CREATE INDEX idx_sales_date ON Sales(SaleDate);
CREATE INDEX idx_customers_name ON Customers(LastName, FirstName);
CREATE INDEX idx_service_dates ON ServiceDepartment(ServiceDate);


-- Add new column to Customers table
ALTER TABLE Customers
ADD OrderCount INT NOT NULL DEFAULT 0;

-- Initialize OrderCount to 0 for existing customers
UPDATE Customers
SET OrderCount = 0;

-- Create trigger to update OrderCount when a new sale is inserted
CREATE TRIGGER trg_UpdateCustomerOrderCount
ON Sales
AFTER INSERT
AS
BEGIN
    -- Update OrderCount for the customer in the inserted sale
    UPDATE c
    SET OrderCount = OrderCount + 1
    FROM Customers c
    INNER JOIN inserted i ON c.CustomerID = i.CustomerID
    WHERE i.SaleStatus = 'Completed'; -- Only count completed sales
END;