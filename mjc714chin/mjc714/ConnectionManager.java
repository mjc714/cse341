
/**
 * Connection Manager to pass collections to necessary classes.
 */
import java.sql.*;
import java.util.Scanner;

public class ConnectionManager {

    private static Connection conn;
    private static String conn_info = "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241";
    private static String userName = null;
    private static String passWord = null;
    Exception exception = null;

    public ConnectionManager() {

    }

    /**
     * get a user name and password, pass to instance vars then we pass these to
     * getConnection when called in another class
     */
    public void getLogin() {
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter your Oracle user id ->");
        if (scan.hasNextLine()) {
            this.userName = scan.nextLine();
            //get password
            System.out.print("Enter Oracle password for " + userName + "->");
            if (scan.hasNextLine()) {
                this.passWord = scan.nextLine();
            }
        }
    }

    /**
     * takes entered id and pw and attempts to open connection to DB it is the
     * job of the calling class to catch the exceptions
     */
    public Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        conn = DriverManager.getConnection(conn_info, this.userName, this.passWord);
        return conn;
    }
}
