
/**
 * Manager to reports on sales, store inventory, and restocks
 */
import java.sql.*;
import java.util.LinkedList;
import java.util.Scanner;

public class Manager {

    private static Scanner scan = new Scanner(System.in);
    //DB vars
    private Connection managerConn = null;
    private PreparedStatement prpStInventoryReport = null;
    private PreparedStatement prpStInventoryCheck = null;
    private PreparedStatement prpStRestockStore = null;
    private PreparedStatement prpStAllPhySales = null;
    private PreparedStatement prpStAllOnlSales = null;
    private PreparedStatement most_sold = null;
    private PreparedStatement mega_All = null;
    private PreparedStatement most_Sold_Phy_Item = null;
    private PreparedStatement most_Sold_Onl_Item = null;
    private PreparedStatement mega_Onl_All = null;

    //search for stores
    private int storeNum = 0;
    private String storeStreet = null;
    private String storeCity = null;
    private String storeState = null;
    private int storeZip = 0;
    private double total_Revenue = 0;
    private double total_Phy = 0;
    private double total_Onl = 0;
    //print inventory query
    private String printInventories = "";
    //check inventories of physical stores for restock
    private String inventoryCheck = "select street_number STR_NUM, street_name STR_NAME, city, state ST, zip, item_id from physicalstore where "
            + "(OLD_QUANTITY/2) > current_QUANTITY order by STREET_NUMBER, STREET_NAME, CITY, STATE, ZIP, item_id";
    //restock specific store and item
    private String restockStore = "";
    //all physical sales
    private String allPhySales = "select sale_id, physicalsale.item_id, quantity_bought, item_price, item_descr, zip from physicalsale, onlinestore "
            + "where physicalsale.item_id = onlinestore.item_id order by sale_id, item_id";
    //all online sales
    private String allOnlSales = "select sale_id, item_id, quantity_bought, item_price, item_descr from onlinesale order by sale_id";

    //most sold physical item
    private String mspi = "select item_descr, physicalsale.item_id, sum(quantity_bought) total_bought from physicalsale, onlinestore "
            + "where physicalsale.item_id = onlinestore.item_id group by item_descr, physicalsale.item_id order by total_bought desc";
    //most sold online item
    private String msoi = "select item_descr, item_id, sum(quantity_bought) total_bought from onlinesale "
            + "group by item_descr, item_id order by total_bought desc";

    //all online info
    private String onl_all = "select sale_id, item_id, sum(quantity_bought) total_bought, item_price from onlinesale "
            + "group by sale_id, item_id, item_price order by sale_id";

    public Manager(Connection con) {
        System.out.println("Hello Mr./Mrs./Ms. Manager");
        this.managerConn = con;
    }

    /**
     * fill string values in for query
     *
     * @param num
     * @param str
     * @param city
     * @param st
     * @param zip
     */
    public void setPrintInvStr(int num, String str, String city, String st, int zip) {
        this.printInventories = "select item_descr, physicalstore.item_id, current_quantity, old_quantity from physicalstore, onlinestore where physicalstore.item_id = onlinestore.item_id and "
                + "STREET_NUMBER = " + num + " and STREET_NAME like '" + str + "'" + " and CITY like '" + city + "'" + " and state like '" + st + "' and zip = " + zip + " order by item_id";
    }

    /**
     * fill string values in for query
     *
     * @param str
     * @param city
     * @param st
     */
    public void setRestockStr(int num, String str, String city, String st, int zip, int id) {
        this.restockStore = "update physicalstore set current_quantity = OLD_QUANTITY where STREET_NUMBER = " + num + " and STREET_NAME like '"
                + str + "'" + "and CITY like '" + city + "'" + "and state like '" + st + "' and zip = " + zip + " and ITEM_ID = " + id;
    }

    public void managerMenu() throws SQLException {
        String input = "";
        int choice = 0;
        boolean mngDone = false;
        do {
            System.out.println("Manager Menu Options:");
            System.out.println("1.Print Store Inventory Report\t2.Check restock needs\t3.Generate Physical and Online Sales Report\t-1.Exit Manager Interface");
            System.out.print("Enter choice->");
            input = scan.nextLine();
            if (input.equals("1") || input.equals("2") || input.equals("3") || input.equals("4") || input.equals("-1")) {
                choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        storeInventoryReport();
                        break;
                    case 2:
                        restockCheck();
                        break;
                    case 3:
                        physicalSalesReport();
                        onlineSalesReport();
                        setTotalRevenue();
                        //zero these so if successive runs of option 3 occur, they do not accumulate!
                        this.total_Onl = 0;
                        this.total_Phy = 0;
                        this.total_Revenue = 0;
                        break;
                    case -1:
                        System.out.println("Exited Manager Interface");
                        mngDone = true;
                        break;
                    default:
                        System.out.println("Something went wrong, try again");
                        break;
                }
            } else {
                System.out.println("That is not an option, try again!");
                mngDone = false;
            }
        } while (!mngDone);
    }

    /**
     * Generates inventory report for physical stores
     *
     * @throws java.sql.SQLException
     */
    public void storeInventoryReport() throws SQLException {
        boolean done = false;
        String input = "";
        int opt = 0;
        do {
            try {
                System.out.println("Choose Physical Store Location: ");
                System.out.println("1. 831 Bethlehem St. Springfield, IL 52857");
                System.out.println("2. 921 Jefferson St. Worcester, MA 16681");
                System.out.println("3. 958 Apple St. Warren, MI 81803");
                System.out.println("4. 5 President St. Duluth, MN 78820");
                System.out.println("5. 441 Kitten St. Olathe, KS 32360");
                System.out.print("->");
                input = scan.nextLine();
                System.out.println();
                if (input.matches("[12345]") || input.equals("-1")) {
                    opt = Integer.parseInt(input);
                    switch (opt) {
                        case 1:
                            this.storeNum = 831;
                            this.storeStreet = "Bethlehem";
                            this.storeCity = "Springfield";
                            this.storeState = "IL";
                            this.storeZip = 52857;
                            setPrintInvStr(this.storeNum, this.storeStreet, this.storeCity, this.storeState, this.storeZip);
                            this.prpStInventoryReport = this.managerConn.prepareStatement(printInventories);
                            //this.prpStInventoryReport.setInt(1, this.storeNum);
                            //this.prpStInventoryReport.setInt(2, this.storeZip);
                            this.prpStInventoryReport.executeQuery();
                            done = true;
                            break;
                        case 2:
                            this.storeNum = 921;
                            this.storeStreet = "Jefferson";
                            this.storeCity = "Worcester";
                            this.storeState = "MA";
                            this.storeZip = 16681;
                            setPrintInvStr(this.storeNum, this.storeStreet, this.storeCity, this.storeState, this.storeZip);
                            prpStInventoryReport = this.managerConn.prepareStatement(printInventories);
                            prpStInventoryReport.executeQuery();
                            done = true;
                            break;
                        case 3:
                            this.storeNum = 958;
                            this.storeStreet = "Apple";
                            this.storeCity = "Warren";
                            this.storeState = "MI";
                            this.storeZip = 81803;
                            setPrintInvStr(this.storeNum, this.storeStreet, this.storeCity, this.storeState, this.storeZip);
                            prpStInventoryReport = this.managerConn.prepareStatement(printInventories);
                            prpStInventoryReport.executeQuery();
                            done = true;
                            break;
                        case 4:
                            this.storeNum = 5;
                            this.storeStreet = "President";
                            this.storeCity = "Duluth";
                            this.storeState = "MN";
                            this.storeZip = 78820;
                            setPrintInvStr(this.storeNum, this.storeStreet, this.storeCity, this.storeState, this.storeZip);
                            prpStInventoryReport = this.managerConn.prepareStatement(printInventories);
                            prpStInventoryReport.executeQuery();
                            done = true;
                            break;
                        case 5:
                            this.storeNum = 441;
                            this.storeStreet = "Kitten";
                            this.storeCity = "Olathe";
                            this.storeState = "KS";
                            this.storeZip = 32360;
                            setPrintInvStr(this.storeNum, this.storeStreet, this.storeCity, this.storeState, this.storeZip);
                            prpStInventoryReport = this.managerConn.prepareStatement(printInventories);
                            prpStInventoryReport.executeQuery();
                            done = true;
                            break;
                        case -1:
                            done = true;
                            break;
                        default:
                            System.out.println("Invalid option, try again");
                            break;
                    }
                } else {
                    System.out.println("Invalid option, try again");
                }
            } catch (SQLException e) {
                System.out.println("This went wrong: " + e.getMessage());
            }
        } while (!done);
        System.out.printf("Store Address: %-4d\t%-10s\t%-10s\t%-2s\t%-5d\n", this.storeNum, this.storeStreet, this.storeCity, this.storeState, this.storeZip);
        System.out.printf("%-16s\t%-8s\t%-8s\t%-8s\n", "Item_Descr", "Item_ID", "Curr_Qty", "Orig_Qty");
        try {
            this.prpStInventoryReport.getResultSet().next();
            do {
                System.out.printf("%-16s\t%-8d\t%-8d\t%-8d\n", this.prpStInventoryReport.getResultSet().getString(1), this.prpStInventoryReport.getResultSet().getInt(2),
                        this.prpStInventoryReport.getResultSet().getInt(3), prpStInventoryReport.getResultSet().getInt(4));
            } while (this.prpStInventoryReport.getResultSet().next());
            try {
                this.prpStInventoryReport.close();
            } catch (SQLException e) {
                System.out.println("This went wrong: " + e.getMessage());
            }
        } catch (NullPointerException | SQLException e) {
            System.out.println("This went wrong: " + e.getMessage());
        }
    }

    /**
     * Checks physical store inventories to see if they need a restock from the
     * online store, if < 1/2(original_Quantity) = restock notice will proceed
     * to restock any items from returned stores that need to.
     *
     * @throws java.sql.SQLException
     */
    public void restockCheck() throws SQLException {
        try {
            this.prpStInventoryCheck = this.managerConn.prepareStatement(inventoryCheck);
            this.prpStInventoryCheck.executeQuery();
            if (this.prpStInventoryCheck.getResultSet().next()) {
                System.out.printf("%-10s\t%-10s\t%-10s\t%-10s\t%-10s\t%-10s\n", "Store_Num", "Store_Street", "Store_City", "Store_State", "Store_Zip", "Item_ID");
                do {
                    this.storeNum = this.prpStInventoryCheck.getResultSet().getInt(1);
                    this.storeStreet = this.prpStInventoryCheck.getResultSet().getString(2);
                    this.storeCity = this.prpStInventoryCheck.getResultSet().getString(3);
                    this.storeState = this.prpStInventoryCheck.getResultSet().getString(4);
                    this.storeZip = this.prpStInventoryCheck.getResultSet().getInt(5);
                    int id = this.prpStInventoryCheck.getResultSet().getInt(6);
                    System.out.printf("%-10d\t%-10s\t%-10s\t%-10s\t%-10d\t%-10d\n", this.prpStInventoryCheck.getResultSet().getInt(1), this.prpStInventoryCheck.getResultSet().getString(2), this.prpStInventoryCheck.getResultSet().getString(3),
                            this.prpStInventoryCheck.getResultSet().getString(4), this.prpStInventoryCheck.getResultSet().getInt(5), this.prpStInventoryCheck.getResultSet().getInt(6));
                    setRestockStr(this.storeNum, this.storeStreet, this.storeCity, this.storeState, this.storeZip, id);
                    this.prpStRestockStore = this.managerConn.prepareStatement(restockStore);
                    this.prpStRestockStore.executeQuery();
                } while (this.prpStInventoryCheck.getResultSet().next());
                this.prpStInventoryCheck.close();
                this.prpStRestockStore.close();
                System.out.println("Inventories Restocked");
            } else {
                System.out.println("All inventory is sufficiently stocked!");
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Restock Check Error: " + e.getMessage());
        }
    }

    /**
     * Generate a sales report based on online and physical stores will
     * highlight most sold item(s), and total revenue from online and physical
     * then compare and give total for both
     *
     * @throws java.sql.SQLException
     */
    public void physicalSalesReport() throws SQLException {
        int top_Zip = 0, top_Zip_Count = 0, max = 0, temp = 0, most_Sold_Item_id = 0, sale_ID = 0;
        double total_52857 = 0, total_16681 = 0, total_81803 = 0, total_78820 = 0, total_32360 = 0;
        int top_Zip_Set[] = new int[5];
        String mostSoldStore = "select distinct zip, count(zip) as zip_at from physicalsale group by zip order by zip_at desc";
        String all_info = "select sale_id, physicalsale.item_id, sum(quantity_bought) total_bought, item_price, zip from physicalsale, onlinestore "
                + "where physicalsale.item_id = onlinestore.item_id group by sale_id, physicalsale.item_id, item_price, zip order by sale_id, item_id";
        try {
            this.most_sold = this.managerConn.prepareStatement(mostSoldStore);
            this.mega_All = this.managerConn.prepareStatement(all_info);
            this.prpStAllPhySales = this.managerConn.prepareStatement(allPhySales);
            this.most_Sold_Phy_Item = this.managerConn.prepareStatement(mspi);
            System.out.println("\nPhysical Sales:");
            this.prpStAllPhySales.executeQuery();
            if (this.prpStAllPhySales.getResultSet().next()) {
                System.out.printf("%-10s\t%-10s\t%-10s\t%-10s\t%-10s\n", "Sale_ID", "Item_ID", "Qty_Bought", "Item_Price", "Item_Descr");
                do {
                    System.out.printf("%-10d\t%-10d\t%-10d\t%-10.2f\t%-10s\n",
                            this.prpStAllPhySales.getResultSet().getInt(1), this.prpStAllPhySales.getResultSet().getInt(2), this.prpStAllPhySales.getResultSet().getInt(3),
                            this.prpStAllPhySales.getResultSet().getDouble(4), this.prpStAllPhySales.getResultSet().getString(5));

                } while (this.prpStAllPhySales.getResultSet().next());
            } else {
                System.out.println("No Physical Sales?!");
            }
            System.out.println();
            this.most_sold.executeQuery();
            if (this.most_sold.getResultSet().next()) {
                int i = 0;
                do { //calculate the top zip code should be the first if it is unique
                    if (top_Zip_Count < this.most_sold.getResultSet().getInt(2)) {
                        top_Zip = this.most_sold.getResultSet().getInt(1);
                        top_Zip_Count = this.most_sold.getResultSet().getInt(2);
                    } else if (top_Zip_Count == this.most_sold.getResultSet().getInt(2)) { //not unique and there are multiple tied zip codes with most sales
                        top_Zip_Set[i] = this.most_sold.getResultSet().getInt(1);
                        i++;
                    }
                } while (this.most_sold.getResultSet().next());
                if (top_Zip_Set.length > 0 && top_Zip_Set[0] != 0) { //first value returned was not unique
                    System.out.println("\nBest Selling Store(s): ");
                    switch (top_Zip) { //top_Zip will initially hold first value returned
                        case 52857:
                            System.out.println("831\t Bethlehem St.\t Springfield\t IL\t 52857");
                            break;
                        case 16681:
                            System.out.println("921\t Jefferson St.\t Worcester\t MA\t 16681");
                            break;
                        case 81803:
                            System.out.println("958\t Apple St.\t Warren\t MI\t 81803");
                            break;
                        case 78820:
                            System.out.println("5\t President St.\t Duluth\t MN\t 78820");
                            break;
                        case 32360:
                            System.out.println("441\t Kitten St.\t Olathe\t KS\t 32360");
                            break;
                        default:
                            System.out.println("Something terrible happened!");
                            break;
                    }
                    for (int k = 0; k < top_Zip_Set.length; k++) { //then all non unique values are placed into array at index 0
                        switch (top_Zip_Set[k]) {
                            case 52857:
                                System.out.println("831\t Bethlehem St.\t Springfield\t IL\t 52857");
                                break;
                            case 16681:
                                System.out.println("921\t Jefferson St.\t Worcester\t MA\t 16681");
                                break;
                            case 81803:
                                System.out.println("958\t Apple St.\t Warren\t MI\t 81803");
                                break;
                            case 78820:
                                System.out.println("5\t President St.\t Duluth\t MN\t 78820");
                                break;
                            case 32360:
                                System.out.println("441\t Kitten St.\t Olathe\t KS\t 32360");
                                break;
                            default:
                                System.out.println("Something terrible happened!");
                                break;
                        }
                    }
                } else { //first value was unique
                    switch (top_Zip) {
                        case 52857:
                            System.out.println("Best Selling Store: 831\t Bethlehem St.\t Springfield\t IL\t 52857");
                            break;
                        case 16681:
                            System.out.println("Best Selling Store: 921\t Jefferson St.\t Worcester\t MA\t 16681");
                            break;
                        case 81803:
                            System.out.println("Best Selling Store: 958\t Apple St.\t Warren\t MI\t 81803");
                            break;
                        case 78820:
                            System.out.println("Best Selling Store: 5\t President St.\t Duluth\t MN\t 78820");
                            break;
                        case 32360:
                            System.out.println("Best Selling Store: 441\t Kitten St.\t Olathe\t KS\t 32360");
                            break;
                        default:
                            System.out.println("Something terrible happened!");
                            break;
                    }
                }
            }

            this.mega_All.executeQuery();
            if (this.mega_All.getResultSet().next()) {
                do {
                    switch (this.mega_All.getResultSet().getInt(5)) {
                        case 52857:
                            total_52857 += this.mega_All.getResultSet().getInt(3) * this.mega_All.getResultSet().getDouble(4);
                            break;
                        case 16681:
                            total_16681 += this.mega_All.getResultSet().getInt(3) * this.mega_All.getResultSet().getDouble(4);
                            break;
                        case 81803:
                            total_81803 += this.mega_All.getResultSet().getInt(3) * this.mega_All.getResultSet().getDouble(4);
                            break;
                        case 78820:
                            total_78820 += this.mega_All.getResultSet().getInt(3) * this.mega_All.getResultSet().getDouble(4);
                            break;
                        case 32360:
                            total_32360 += this.mega_All.getResultSet().getInt(3) * this.mega_All.getResultSet().getDouble(4);
                            break;
                    }
                } while (this.mega_All.getResultSet().next());
                System.out.println();
                System.out.println("Total Revenue per store: ");
                System.out.printf("921\t Jefferson St.\t Worcester\t MA\t 16681:\t\t $%.2f\n", total_16681);
                System.out.printf("441\t Kitten    St.\t Olathe\t\t KS\t 32360:\t\t $%.2f\n", total_32360);
                System.out.printf("831\t Bethlehem St.\t Springfiel\t IL\t 52857:\t\t $%.2f\n", total_52857);
                System.out.printf("5\t President St.\t Duluth\t\t MN\t 78820:\t\t $%.2f\n", total_78820);
                System.out.printf("958\t Apple     St.\t Warren\t\t MI\t 81803:\t\t  $%.2f\n", total_81803);

                this.total_Phy = total_16681 + total_32360 + total_52857 + total_78820 + total_81803;

                System.out.printf("\nTotal Physical Store Revenue: $%.2f\n\n", this.total_Phy);
            }
            this.most_Sold_Phy_Item.executeQuery();
            if (this.most_Sold_Phy_Item.getResultSet().next()) {
                System.out.printf("%-10s\t%-10s\t%-10s\n", "Item_Descr", "Item_ID", "Total_Bought");
                System.out.printf("Most Sold Item: %-10s\t%-10d\t%-10s\n", this.most_Sold_Phy_Item.getResultSet().getString(1), this.most_Sold_Phy_Item.getResultSet().getInt(2), this.most_Sold_Phy_Item.getResultSet().getString(3));
            }
        } catch (SQLException | NullPointerException e) {
            System.out.println("Physical Sales Report Error: " + e.getMessage());
        } finally {
            this.most_sold.close();
            this.mega_All.close();
            this.prpStAllPhySales.close();
            this.most_Sold_Phy_Item.close();
        }
    }

    public void onlineSalesReport() throws SQLException {
        try {
            this.prpStAllOnlSales = this.managerConn.prepareStatement(allOnlSales);
            this.most_Sold_Onl_Item = this.managerConn.prepareStatement(msoi);
            this.mega_Onl_All = this.managerConn.prepareStatement(this.onl_all);
            System.out.println("\nOnline Sales:");
            this.prpStAllOnlSales.executeQuery();
            if (this.prpStAllOnlSales.getResultSet().next()) {
                System.out.printf("%-10s\t%-10s\t%-10s\t%-10s\t%-10s\n", "Sales_ID", "Item_ID", "Qty_Bought", "Item_Price", "Item_Descr");
                do {
                    System.out.printf("%-10d\t%-10d\t%-10d\t%-10.2f\t%-10s\n",
                            this.prpStAllOnlSales.getResultSet().getInt(1), this.prpStAllOnlSales.getResultSet().getInt(2), this.prpStAllOnlSales.getResultSet().getInt(3),
                            this.prpStAllOnlSales.getResultSet().getDouble(4), this.prpStAllOnlSales.getResultSet().getString(5));
                } while (this.prpStAllOnlSales.getResultSet().next());
            } else {
                System.out.println("No Online Sales?!");
            }
            System.out.println();
            this.most_Sold_Onl_Item.executeQuery();
            if (this.most_Sold_Onl_Item.getResultSet().next()) {
                System.out.printf("Most Sold Item: %-10s\t%-10d\t%-10d\n", this.most_Sold_Onl_Item.getResultSet().getString(1), this.most_Sold_Onl_Item.getResultSet().getInt(2),
                        this.most_Sold_Onl_Item.getResultSet().getInt(3));
            }
            System.out.println();
            this.mega_Onl_All.executeQuery();
            if (this.mega_Onl_All.getResultSet().next()) {
                do {
                    this.total_Onl += this.mega_Onl_All.getResultSet().getInt(3) * this.mega_Onl_All.getResultSet().getDouble(4);
                } while (this.mega_Onl_All.getResultSet().next());
            }
            System.out.printf("Total Online Store Revenue: %.2f\n", this.total_Onl);
        } catch (SQLException | NullPointerException e) {
            System.out.println("Online Sales Report Error: " + e.getMessage());
        } finally {
            this.prpStAllOnlSales.close();
            this.most_Sold_Onl_Item.close();
            this.mega_Onl_All.close();
        }
    }

    public void setTotalRevenue() {
        this.total_Revenue += this.total_Phy + this.total_Onl;
        System.out.printf("Total Revenue between Physical and Online store(s): %.2f\n", this.total_Revenue);
    }
}
