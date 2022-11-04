import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

/**
 * This class reads two external CSV files and inserts the data into LINES and MOVES respectively. In the current
 * version of Adjust, this feature is for development purposes only and has not yet been fully integrated
 * into the application.
 * @author Jack Vandeleuv
 */
public final class ReadCSV {

    /**
     * This static method reads a lines.csv file and inserts them into the database.
     * @throws FileNotFoundException If the csv file cannot be found, an exception is thrown.
     * @throws SQLException If the database transaction cannot be processed, an exception is thrown.
     */
    public static void ReadLines() throws FileNotFoundException, SQLException {
        // Insert new tuples into LINES.
        PreparedStatement preStmt = Main.conn.prepareStatement("INSERT INTO LINES(ID, NAME, LINE, ECO) " +
                "VALUES (?, ?, ?, ?)");

        // Instantiate scanner and pass a new File object (and pass an absolute path to that File object).
        Scanner sc = new Scanner(new File("C://Users//jackv//IdeaProjects//AnkiChess//src//lines.csv"));

        while (sc.hasNextLine()) {
            // Each iteration returns a line for the CSV.
            String csvLine = sc.nextLine();

            // Validate the line to ensure it is not empty.
            if (!csvLine.isEmpty()) {

                // Split each csv by comma and insert each value into a string array.
                String[] lineArr = ReadCSV.parseCSVLine(csvLine, 4);

                // The ID field is an int, so we call parseInt and pass the String.
                preStmt.setInt(1, Integer.parseInt(lineArr[0]));
                preStmt.setString(2, lineArr[1]);
                preStmt.setString(3, lineArr[2]);
                preStmt.setString(4, lineArr[3]);

                // Execute the insert operation
                preStmt.executeUpdate();
            }
        }

        // Commit the transaction.
        System.out.println("makeLines finished");
        Main.conn.commit();
    }

    /**
     * Private method that splits a CSV with quotes around each value into a String[].
     * @param csvLine String representing line of the CSV.
     * @param ePerRow Elements for this line of the CSV.
     * @return A String[] with each String representing a value in the CSV.
     */
    private static String[] parseCSVLine(String csvLine, int ePerRow) {
        String[] lineArr = new String[ePerRow];
        int count = 0;
        boolean isData = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < csvLine.length(); i++) {
            if (csvLine.charAt(i) == '"') {
                if (!isData) {
                    isData = true;
                } else {
                    lineArr[count] = sb.toString();
                    count = count + 1;
                    sb = new StringBuilder();
                    isData = false;
                }
            } else if (isData) {
                sb.append(csvLine.charAt(i));
            }
        }
        return lineArr;
    }

    /**
     * This static method reads a moves.csv file and inserts them into the database.
     * @throws FileNotFoundException If the csv file cannot be found, an exception is thrown.
     * @throws SQLException If the database transaction cannot be processed, an exception is thrown.
     */
    public static void ReadMoves() throws FileNotFoundException, SQLException {
        // Insert new tuples into MOVES.
        PreparedStatement preStmt = Main.conn.prepareStatement("INSERT INTO MOVES(ID, ORDER_IN_LINE, BEFORE_FEN, " +
                "AFTER_FEN, LINES_ID) VALUES (?, ?, ?, ?, ?)");

        // Instantiate scanner and pass a new File object (and pass an absolute path to that File object).
        Scanner sc = new Scanner(new File("C://Users//jackv//IdeaProjects//AnkiChess//src//moves.csv"));

        while (sc.hasNextLine()) {
            // Each iteration returns a line for the CSV.
            String csvLine = sc.nextLine();

            // Validate the line to ensure it is not empty.
            if (!csvLine.isEmpty()) {

                // Split the input by comma.
                String[] lineArr = ReadCSV.parseCSVLine(csvLine, 5);

                // If the relevant field is an int, so we call parseInt and pass the String.
                preStmt.setInt(1, Integer.parseInt(lineArr[0]));
                preStmt.setInt(2, Integer.parseInt(lineArr[1]));
                preStmt.setString(3, lineArr[2]);
                preStmt.setString(4, lineArr[3]);
                preStmt.setInt(5, Integer.parseInt(lineArr[4]));

                // Execute the insertion operation.
                preStmt.executeUpdate();
            }
        }

        // Commit the transaction.
        System.out.println("makeMoves finished");
        Main.conn.commit();
    }

    /**
     * This method is for debugging. It prints out a summary of the database.
     * @throws SQLException If database operation cannot be performed, throw an exception.
     */
    public static void printDB() throws SQLException {
        Statement stmt = Main.conn.createStatement();

        stmt.execute("SELECT COUNT(ID) FROM LINES");
        ResultSet rs = stmt.getResultSet();
        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }
    }
}
