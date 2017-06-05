
/**
 * Main menu driver to begin use of Jog interfaces.
 */
import java.util.Scanner;
import java.sql.*;

public class Menu {

    public static void main(String args[]) throws SQLException, ClassNotFoundException {
        Exception exception = null;
        ConnectionManager connM = new ConnectionManager();
        Scanner scan = new Scanner(System.in);

        //variable declarations
        Connection conn = null;
        String input = "";
        int choice = 0;
        boolean done = false;
        boolean done2 = false;

        System.out.println("Welcome to Jog Wireless Systems.");
        do {
            try {
                connM.getLogin(); //prompt for oracle id and password, pass to connection Manager
                conn = connM.getConnection(); //open connection if valid id and pw entered, else throw sqlexception and restart
                do {
                    System.out.print("Select your role:\n1.Customer\t 2.Manager\t " +/*3.Sales Representative\t*/ "-1.Exit System ->");
                    input = scan.nextLine();
                    if (/*input.matches("[123]") ||*/input.equals("1") || input.equals("2") || /*input.equals("3") ||*/ input.equals("-1")) { //check for correct input
                        choice = Integer.parseInt(input);
                        switch (choice) {
                            case 1:
                                //run customer routine
                                Customer cust = new Customer(conn);
                                cust.customerMenu();
                                break;
                            case 2:
                                //run manager routine
                                Manager mng = new Manager(conn);
                                mng.managerMenu();
                                break;
//                            case 3:
//                                //run sales rep routine
//                                SalesRep sr = new SalesRep(conn);
//                                sr.closeSalesRepConnection();
//                                break;
                            case -1:
                                System.out.println("Exit");
                                done = true;
                                done2 = true;
                                break;
                            default:
                                System.out.println("Input not recognized, try again");
                        }
                    } else {
                        System.out.println("That is not an option, Try Again!");
                        done2 = false;
                    }
                } while (!done2);
            } catch (SQLException e) {
                System.out.println("This went wrong: " + e.getMessage());
                System.out.println("Restarting...");
                exception = e;
            } catch (ClassNotFoundException e) {
                System.out.println("Cannot find class " + e.getMessage());
                exception = e;
            } catch (NullPointerException e) {
                System.out.println("Null Value is: " + e.getMessage());
                exception = e;
            } catch (NumberFormatException e) {
                System.out.println("Bad number " + e.getMessage());
                System.out.println("Restarting...");
                exception = e;
            } finally { //connections closed here, restart
                if (conn != null) { //check if any exceptions are thrown upon closing connection
                    try {
                        conn.close();
                        System.out.println("Connection closed!");
                    } catch (SQLException e) {
                        System.out.println("This went wrong!: " + e.getMessage());
                        System.out.println("Restarting...");
                        conn.close();
                        exception = e;
                    } finally {
                        if (exception != null) {
                            exception = null;
                            continue;
                        }
                    }
                } else if (conn == null && exception != null) {
                    exception = null;
                    continue;
                }
                done = true;
            }
        } while (!done);
    }
}
