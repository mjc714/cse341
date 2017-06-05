
/**
 * Customer class to buy items online, create new service, pay bill view bill,
 * check usage
 */
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Customer {

    private Connection custConn = null;
    static final Scanner scan = new Scanner(System.in);
    private int mrsid = 0;

    //prepared statements        
    private PreparedStatement onl_Purch_List = null;
    private PreparedStatement onl_Purch_Ins = null;
    private PreparedStatement sale_Ins = null;
    private PreparedStatement mr_Sale_ID = null;

    /*
     query strings
     */
    //produce items for customer to buy, listing
    private String onl_Purch_List_q = "select * from onlinestore order by item_id";
    //insert into sale
    private String sale_Ins_q = "";
    //after successful purchase by customer, insert sale into onlineSale
    private String onl_Purch_Ins_q = "";
    //get most recent sale_id
    private String mr_Sale_ID_q = "select sale_id from sale order by sale_id desc";

    public Customer(Connection con) {
        System.out.println("Hello Mr./Mrs./Ms. Customer");
        this.custConn = con;
    }

    /**
     * generate the query string to insert a sale into DB
     *
     * @param sid
     */
    public void setSale_Ins_q(int sid) {
        LocalDateTime ldt = LocalDateTime.now();
        String year = Integer.toString(ldt.getYear());
        String month = Integer.toString(ldt.getMonthValue());
        String day = Integer.toString(ldt.getDayOfMonth());
        String hr = Integer.toString(ldt.getHour());
        String min = Integer.toString(ldt.getMinute());
        String sec = Integer.toString(ldt.getSecond());
        this.sale_Ins_q = "insert into sale(sale_id, sale_time) values (" + sid + ", timestamp '" + year + "-" + month + "-" + day + " " + hr + ":" + min + ":" + sec + "')";
    }

    /**
     * generate the query string to insert an online sale into DB
     *
     * @param sid
     * @param iid
     * @param ipr
     * @param idescr
     * @param qtyb
     */
    public void setOnl_Purch_Ins_q(int sid, int iid, double ipr, String idescr, int qtyb) {
        this.onl_Purch_Ins_q = "insert into onlinesale(sale_id, item_id, item_price, item_descr, quantity_bought) values "
                + "(" + sid + ", " + iid + ", " + ipr + ", '" + idescr + "', " + qtyb + ")";
    }

    /**
     * Main Customer Menu for Selecting Options
     *
     * @throws java.sql.SQLException
     */
    public void customerMenu() throws SQLException {
        String input = "";
        int choice = 0;
        boolean custDone = false;
        do {
            System.out.println("Customer Menu Options:");
            //System.out.println("1.Generate Bill and Usage from File(sample_XYZ.txt)\t2.Make Online Purchase\t\t-1.Exit Customer Interface");
            System.out.println("1.Make Online Purchase\t-1.Exit Customer Interface");
            System.out.print("Enter choice->");
            input = scan.nextLine();
            if (input.equals("1") || /* input.equals("2") || input.equals("3") ||*/ input.equals("-1")) {
                choice = Integer.parseInt(input);
                switch (choice) {
                    //case 1:
                    //generateBill();
                    // break;
                    case 1:
                        onlinePurchase();
                        break;
                    /*
                     case 3:
                     //customerthingy3
                     break;
                     */
                    case -1:
                        System.out.println("Exited Customer Interface");
                        custDone = true;
                        break;
                    default:
                        System.out.println("Something went wrong, try again");
                        break;
                }
            } else {
                System.out.println("That is not an option, try again!");
                custDone = false;
            }
        } while (!custDone);
    }

    /**
     * generate the online inventory listing for customer to choose items to buy
     *
     * @throws SQLException
     */
    public void viewOnlineInventory() throws SQLException {
        try {
            this.onl_Purch_List = this.custConn.prepareStatement("select item_id, item_price, item_descr from onlinestore order by item_id");
            this.onl_Purch_List.executeQuery();
            System.out.printf("%-8s\t%-8s\t%-16s\n", "Item_ID", "Item_Price", "Item_Descr");
            if (this.onl_Purch_List.getResultSet().next()) {
                do {
                    System.out.printf("%-8d\t%.2f\t\t%-16s\n", this.onl_Purch_List.getResultSet().getInt(1),
                            this.onl_Purch_List.getResultSet().getDouble(2), this.onl_Purch_List.getResultSet().getString(3));
                } while (this.onl_Purch_List.getResultSet().next());
            }
        } catch (SQLException e) {
            System.out.println("Online Inventory Display Error: " + e.getMessage());
        } finally {
            this.onl_Purch_List.close();
        }
    }

    /**
     * operates on cart passed by onlinePurchase routine updates database of
     * sales that occurred
     *
     * @param hmap
     * @throws java.sql.SQLException
     */
    public void cartOps(HashMap<Integer, Integer> hmap) throws SQLException {
//        Iterator it2 = hmap.entrySet().iterator();
//        while (it2.hasNext()) {
//            Map.Entry pp = (Map.Entry<Integer, Integer>) it2.next();
//            System.out.println(pp.getKey() + "\t" + pp.getValue());
//        }
        //calculate cart total
        double total = 0;
        int s_id = 0;
        try {
            this.onl_Purch_List = this.custConn.prepareStatement(this.onl_Purch_List_q);
            this.mr_Sale_ID = this.custConn.prepareStatement(this.mr_Sale_ID_q);
            this.mr_Sale_ID.executeQuery();
            this.onl_Purch_List.executeQuery();
            if (this.mr_Sale_ID.getResultSet().next()) { // this gets the most recent sale_id...
                this.mrsid = this.mr_Sale_ID.getResultSet().getInt(1);
            }
            this.mr_Sale_ID.close();
            this.mrsid += 1; //...but we want to add a new sale, +1 to most recent one;
            s_id = this.mrsid;
            setSale_Ins_q(this.mrsid);
            if (hmap.size() > 0) { //nonempty cart
                Iterator it = hmap.entrySet().iterator(); //iterator to traverse hashmap set that will be populated by user input below
                System.out.println("Cart:");
                System.out.println("---------------------");
                while (it.hasNext()) {
                    Map.Entry p2 = (Map.Entry<Integer, Integer>) it.next();
                    int id1 = (Integer) p2.getKey();
                    int qty1 = (Integer) p2.getValue();

                    System.out.printf("Item_ID: %d", id1);
                    System.out.printf("\tQty: %d\n", qty1);
                    while (this.onl_Purch_List.getResultSet().next()) {
                        if (this.onl_Purch_List.getResultSet().getInt(1) == id1) {
                            total += qty1 * this.onl_Purch_List.getResultSet().getDouble(2);
                        }
                    }
                }
                while (it.hasNext()) { //only insert a sale if the cart is NOT empty
                    Map.Entry p = (Map.Entry<Integer, Integer>) it.next();
                    int id = (Integer) p.getKey();
                    int qty = (Integer) p.getValue();
                    //insert into sale table
                    this.sale_Ins = this.custConn.prepareStatement(this.sale_Ins_q);
                    this.sale_Ins.executeQuery();

                    if (this.onl_Purch_List.getResultSet().next()) {
                        do {
                            if (id == (this.onl_Purch_List.getResultSet().getInt(1))) { //match up the item_ids...
                                total += qty * this.onl_Purch_List.getResultSet().getDouble(2); //...match up prices
                                setOnl_Purch_Ins_q(s_id, id, this.onl_Purch_List.getResultSet().getDouble(2),
                                        this.onl_Purch_List.getResultSet().getString(3), qty);
                                this.onl_Purch_Ins = this.custConn.prepareStatement(this.onl_Purch_Ins_q);
                                this.onl_Purch_Ins.executeQuery();
                            }
                        } while (this.onl_Purch_List.getResultSet().next());
                    }
                }
                System.out.println("-------------------\n");
                System.out.printf("Cart Total: %.2f\n", total);
            } else {
                System.out.println("Empty Cart!");
            }
        } catch (SQLException | NullPointerException | NoSuchElementException e) {
        } finally {
            this.sale_Ins.close();
            this.onl_Purch_Ins.close();
            this.onl_Purch_List.close();
        }
    }

    /**
     * online purchase routine takes item_id and quantity from user after
     * purchase inputs are done, we can then insert into the DB
     *
     * @throws SQLException
     */
    public void onlinePurchase() throws SQLException {
        String input = "";
        String input2 = "";
        String qty = "";
        boolean done = false;
        int id = 0;
        int q = 0;
        //this will store item_id and quantity in online purchase such that (K,V) k = item_id, and v = quantity
        HashMap<Integer, Integer> hm = new HashMap<>();
        try {
            System.out.println("Here is the Online Store Inventory: ");
            do {
                viewOnlineInventory();
                System.out.println("Enter '-11-' to continue to checkout!");
                System.out.print("Please select an ITEM_ID that you wish to purchase->");
                input = scan.nextLine();
                if (input.matches("[1-9][0-9]?") && Integer.parseInt(input) >= 1 && Integer.parseInt(input) <= 23) {
                    id = Integer.parseInt(input);
                    System.out.printf("\nPlease select quantity of Item %d->", id);
                    qty = scan.nextLine();
                    if (qty.matches("[1-9]+[0-9]*")) {
                        q = Integer.parseInt(qty);
                        System.out.printf("You have selected Item:%d\tQty:%-4d\n", id, q);
                        System.out.print("\nConfirm addition to cart (Y/N)->");
                        input2 = scan.nextLine();
                        if (input2.equalsIgnoreCase("y")) {
                            if (hm.containsKey(id) && hm.get(id) != null) { //if cart already has the entered id, we need to add quantity purchased
                                hm.replace(id, hm.get(id), (hm.get(id) + q)); //replace key(id) with old value at key(id) with old value at key(id) + q entered;
                                continue;
                            }
                            hm.put(id, q);
                            System.out.printf("\n%d of Item: %d was added to your cart!\n\n", q, id);
                        } else if (input2.equalsIgnoreCase("n")) {
                            System.out.println("Item(s) will not be added to your cart!");
                        } else {
                            System.out.println("Improper Input...Item(s) will not be added to your cart!");
                        }
                    } else {
                        System.out.println("Please check your input!\nResetting...\n");
                    }
                } else if (input.equals("-11-")) {
                    cartOps(hm);
                    done = true;
                } else {
                    System.out.println("Please check your input!\nResetting...\n");
                }
            } while (!done);
        } catch (SQLException | NullPointerException e) {
            System.out.println("Online Purchase Error: " + e.getMessage());
        }
    }
}
