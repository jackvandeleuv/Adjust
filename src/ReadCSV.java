import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class ReadCSV {

    public ReadCSV()  {
    }

    public static void ReadLines() throws FileNotFoundException, SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        PreparedStatement preStmt = conn.prepareStatement("INSERT INTO LINES(ID, NAME, LINE, ECO) VALUES (?, ?, ?, ?)");

        Scanner sc = new Scanner(new File("C://Users//jackv//IdeaProjects//AnkiChess//src//lines.csv"));

        while (sc.hasNextLine()) {
            String csvLine = sc.nextLine();
            if (!csvLine.isEmpty()) {
                System.out.println(csvLine);
                String[] lineArr = csvLine.split(",");
                preStmt.setInt(1, Integer.valueOf(lineArr[0]));
                preStmt.setString(2, lineArr[1].strip());
                preStmt.setString(3, lineArr[2].strip());
                preStmt.setString(4, lineArr[3].strip());
                preStmt.executeUpdate();
            }

        }
        sc.close();

        conn.close();
    }

    public static void ReadMoves() throws FileNotFoundException, SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        PreparedStatement preStmt = conn.prepareStatement("INSERT INTO MOVES(ID, ORDER_IN_LINE, BEFORE_FEN, AFTER_FEN, LINES_ID) VALUES (?, ?, ?, ?, ?)");

        Scanner sc = new Scanner(new File("C://Users//jackv//IdeaProjects//AnkiChess//src//moves.csv"));

        while (sc.hasNextLine()) {
            String csvLine = sc.nextLine();
            if (!csvLine.isEmpty()) {
                System.out.println(csvLine);
                String[] lineArr = csvLine.split(",");
                preStmt.setInt(1, Integer.valueOf(lineArr[0]));
                preStmt.setInt(2, Integer.valueOf(lineArr[1]));
                preStmt.setString(3, lineArr[2].strip());
                preStmt.setString(4, lineArr[3].strip());
                preStmt.setInt(5, Integer.valueOf(lineArr[4]));
                preStmt.executeUpdate();
            }

        }
        sc.close();

        conn.close();
    }

    public static void printDB() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        String jbdcUrl = "jdbc:sqlite:database.db";
        Connection conn = DriverManager.getConnection(jbdcUrl);
        Statement stmt = conn.createStatement();

        stmt.execute("SELECT COUNT(ID) FROM LINES");
        ResultSet rs = stmt.getResultSet();
        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }

        conn.close();
    }

}
