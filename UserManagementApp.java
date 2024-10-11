package com.oct10;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Scanner;

public class UserManagementApp {

	static final String DB_URL = "jdbc:mysql://localhost:3307/";
	static final String USER = "root";
	static final String PASS = "root";

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		try {
			createDatabaseAndTable();

			while (true) {
				System.out.println("\n--- Menu ---");
				System.out.println("1. Show Users");
				System.out.println("2. Sign Up");
				System.out.println("3. Login");
				System.out.println("4. Exit");
				System.out.print("Choose an option: ");
				int choice = scanner.nextInt();
				scanner.nextLine(); // Consume leftover newline after nextInt()

				switch (choice) {
				case 1:
					showUsers();
					break;
				case 2:
					signUp(scanner);
					break;
				case 3:
					loginUser(scanner);
					break;
				case 4:
					System.out.println("Exiting application.");
					return;
				default:
					System.out.println("Invalid choice, please try again.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
	}

	public static void createDatabaseAndTable() throws SQLException {
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement()) {

			String sql = "CREATE DATABASE IF NOT EXISTS UserDB";
			stmt.executeUpdate(sql);
			System.out.println("Database created successfully...");

			String dbUrl = DB_URL + "UserDB";
			try (Connection dbConn = DriverManager.getConnection(dbUrl, USER, PASS)) {

				String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" + "id INT PRIMARY KEY AUTO_INCREMENT, "
						+ "full_name VARCHAR(100), " + "username VARCHAR(50) UNIQUE, " + "password VARCHAR(50), "
						+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + "aadhar LONGBLOB)";
				try (Statement tableStmt = dbConn.createStatement()) {
					tableStmt.execute(createTableSQL);
					System.out.println("Table created successfully...");
				}
			}
		}
	}

	public static void showUsers() throws SQLException {
		String dbUrl = DB_URL + "UserDB";
		try (Connection conn = DriverManager.getConnection(dbUrl, USER, PASS);
				Statement stmt = conn.createStatement()) {

			String query = "SELECT id, full_name, createdAt FROM users";
			try (ResultSet rs = stmt.executeQuery(query)) {
				System.out.println("\n--- Users ---");
				while (rs.next()) {
					int id = rs.getInt("id");
					String fullName = rs.getString("full_name");
					Timestamp createdAt = rs.getTimestamp("createdAt");

					System.out.printf("ID: %d, Name: %s, Created At: %s%n", id, fullName, createdAt);
				}
			}
		}
	}

 	public static void signUp(Scanner scanner) throws SQLException, FileNotFoundException {
		String dbUrl = DB_URL + "UserDB";
		try (Connection conn = DriverManager.getConnection(dbUrl, USER, PASS);
				PreparedStatement pstmt = conn.prepareStatement(
						"INSERT INTO users (full_name, username, password, aadhar) VALUES (?, ?, ?, ?)")) {

			System.out.print("Enter Full Name: ");
			String fullName = scanner.nextLine();
			System.out.print("Enter Username: ");
			String username = scanner.nextLine();
			System.out.print("Enter Password: ");
			String password = scanner.nextLine();

 			System.out.print("Enter the path of the Aadhar file: ");
			String aadharFilePath = scanner.nextLine();

			// Convert the file into a byte array
			File aadharFile = new File(aadharFilePath);
			FileInputStream fis = new FileInputStream(aadharFile);

			pstmt.setString(1, fullName);
			pstmt.setString(2, username);
			pstmt.setString(3, password);
			pstmt.setBinaryStream(4, fis, (int) aadharFile.length()); 

			try {
				int rowsInserted = pstmt.executeUpdate();
				if (rowsInserted > 0) {
					System.out.println("User registered successfully!");
				}
			} catch (SQLIntegrityConstraintViolationException e) {
				System.out.println("Username already exists. Please choose a different username.");
			}
		}
	}

	public static void loginUser(Scanner scanner) throws SQLException {
		String dbUrl = DB_URL + "UserDB";
		try (Connection conn = DriverManager.getConnection(dbUrl, USER, PASS);
				PreparedStatement pstmt = conn
						.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {

			System.out.print("Enter Username: ");
			String username = scanner.nextLine();
			System.out.print("Enter Password: ");
			String password = scanner.nextLine();

			pstmt.setString(1, username);
			pstmt.setString(2, password);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					System.out.println("Login successful! Welcome, " + rs.getString("full_name"));
				} else {
					System.out.println("Invalid username or password.");
				}
			}
		}
	}
}
