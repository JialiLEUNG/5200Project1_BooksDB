//import java.sql.*;
//
//
///**
// * 1. import --> java.sql
// * 2. load and register the driver --> com.mysql.jdbc.Driver
// * 3. create connection --> Connection
// * 4. create a statement --> Statement
// * 5. execute the query
// * 6. process the result
// * 7. close
// */
//
//public class DemoClass {
//    public static void main(String[] args) throws Exception{
//        Connection conn = createTable();
//        post(conn);
//    }
//
//
//    public static Connection getConnection(){
//        try{
//            String url = "jdbc:mysql://localhost:3306/sys";
//            String username = "root";
//            String password = "Waihanc@0";
//
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            Connection conn = DriverManager.getConnection(url, username, password); // return instance of Connection.
//            System.out.println("Connected.");
//            return conn;
//        } catch(Exception e){
//            System.out.println(e);
//        }
//        return null;
//    }
//
//    /**
//     * create table.
//     */
//    public static Connection createTable(){
//        try{
//            Connection conn = getConnection();
//            PreparedStatement create = conn.prepareStatement(
//                    "CREATE TABLE IF NOT EXISTS testID(id int NOT NULL AUTO_INCREMENT, " +
//                            "first varchar(255)," +
//                            "last varchar(255)," +
//                            "PRIMARY KEY(id))");
//            create.executeUpdate();
//            System.out.println("table created.");
//            create.close();
//            conn.close();
//            return conn;
//        }catch(Exception e){
//            System.out.println(e);
//        }
//        finally {
//            System.out.println("Function complete");
//        }
//        return null;
//    }
//
//    public static void post(Connection conn){
//        final String var1 = "john";
//        final String var2 = "Miller";
//        try{
//            PreparedStatement posted = conn.prepareStatement("INSERT INTO testIDtable(first, last) " +
//                                                                  "values ('" + var1 + "','" + var2 + "')");
//            posted.executeUpdate();
//        } catch(Exception e){
//            System.out.println(e);
//        } finally{
//            System.out.println("insert completed");
//        }
//    }
//
//    public static void dropTable(Connection conn, String[] dbTables){
//        for (String tbl:dbTables){
//            try{
//                Statement stmt = conn.createStatement();
//                stmt.executeUpdate("drop table " + tbl);
//                System.out.println("Dropped table "+ tbl);
//            } catch (SQLException e){
//                System.out.println("Did not drop table " + tbl);
//            }
//        }
//    }
//
//    public static void queryName(){
//        // ===================== DQL ==================================== //
//        // see https://www.geeksforgeeks.org/sql-ddl-dql-dml-dcl-tcl-commands/
//        try{
//            Connection conn = getConnection();
//            Statement st = conn.createStatement();
//            // executeQuery: receiving information
//            /// executeUpdate: manipulate information or add in.
//            ResultSet rs = st.executeQuery("select userName " +
//                    "from student " +
//                    "where userID = 3"); // DDL, DML, DQL
//            while(rs.next()){
//                String name = rs.getString("userName");
//                System.out.println("userName is: " + name);
//            }
//            conn.close();
//            st.close();
//
//
//        } catch(Exception e){
//            System.out.println(e);
//        }
//    }
//
//    public static void insertRow(){
//        // ======================== DML ========================================== //
//        // insert
//        try{
//            Connection conn = getConnection();
//            int userID = 5;
//            String userName = "Ali";
//            PreparedStatement pt = conn.prepareStatement("insert into student " +
//                    "                                         values (" + userID + ",'" + userName + "')");
//            int count = pt.executeUpdate("insert into student " +
//                    "                          values (" + userID + ",'" + userName + "')"); // DDL, DML, DQL
//            System.out.println(count + "row/s affected");
//            conn.close();
//
//        } catch (Exception e){
//            System.err.println(e);
//        }
//    }
//
//    public static void ddl(){
//        // ======================== DDL ========================================== //
//    }
//}
