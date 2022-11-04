import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * This Main class contains the main method, which launches the program.
 *
 * superMemoAlgo method in the ReviewEngine class is an implementation of the SuperMemo2 algorithm, an open-source,
 * free-to-use algorithm developed by SuperMemo World. To learn more, please visit the following website:
 *
 * https://www.supermemo.com
 * Algorithm SM-2, (C) Copyright SuperMemo World, 1991.
 */
public class Main {
    // This static Connection is created when the program is launched, and is maintained throughout the runtime of the
    // program.
    public static Connection conn;

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");

            // The file called database.db is where all persistent data for the program is stored. It should always be
            // located in the same directory.
            String jbdcUrl = "jdbc:sqlite:database.db";
            conn = DriverManager.getConnection(jbdcUrl);

            // Turn off auto commit, so that we have to commit our changes to the database manually. Each function that
            // interacts with the database is responsible for committing its changes after it uses the connection.
            conn.setAutoCommit(false);

            // Launch the main menu, which provides further options for the user.
            new MainMenuGUI();

        } catch (ClassNotFoundException | SQLException  ex) {
            // If the connection to the database fails, print the error message and terminate the program.
            System.out.println("Could not successfully start program");
            System.out.println(ex.getMessage());
        }
    }
}
