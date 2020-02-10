/**
 * The sample book data is stored in a tab-separated data file called BookDataNew
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class Book {
    public static void main(String[] args) throws Exception {
        // Four tables will be created in the current program:
        // Schema of the four tables can be seen in E/R diagram in the report.
        String dbTables[] = {
                "AuthorISBN", // relation (Authors and Titles is many-to-many relationship.)
                "Authors", "Titles", "Publishers" // entities
        };

        // name of the Books data file
        String fileName = "src/BookDataNEW.txt";

        // connect to database
        Connection conn = getConnection();

        // drop the database tables and recreate them below
        dropTable(conn, dbTables);

        // create tables
        createTable(conn);

        // Initialize the different tables (at least 15 entries per table) appropriately: all
        // fields cannot be null.
        try(
                // open up data file
                BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));

                Statement stmt = conn.createStatement();

                // insert prepared statements
                PreparedStatement insertRow_Authors = conn.prepareStatement(
                        "INSERT INTO Authors(authorID, firstName, lastName) values(?,?,?)"
                );  // The question marks mean that I know the query but I don't know the values
                // The setString/ setInt methods below would fill out the values.
                // authorID, firstName, lastName are the three attributes
                PreparedStatement insertRow_Publishers = conn.prepareStatement(
                        "INSERT INTO Publishers(publisherID, publisherName) values(?,?)"
                ); // publisherID, publisherName
                PreparedStatement insertRow_Titles = conn.prepareStatement(
                        "INSERT INTO Titles(editionNumber, years, publisherID, price, title, isbn) values(?,?,?,?,?,?)"
                ); // editionNumber, years, publisherID, price, title, isbn
                PreparedStatement insertRow_AuthorISBN = conn.prepareStatement(
                        "INSERT INTO AuthorISBN(authorID, isbn) values(?,?)"
                ) // authorID, isbn
        ) {
            // clear data from tables
            for (String tbl : dbTables){
                try{
                    // delete existing records in a table
                    // delete all rows in a table without deleting the table.
                    // The table structure, attributes, and indexes will be intact
                    stmt.executeUpdate("DELETE FROM " + tbl);
                    System.out.println("Deleting existing records in table" + tbl + " succeeded.");
                } catch(SQLException e){
                    System.out.println(e.getMessage());
                    System.err.println("Deleting existing records in table" + tbl + " failed.");
                    throw new Exception(e.getMessage());
                }
            }

            String line;
            while ((line = br.readLine()) != null){
                // The test file consists of tab delimited data, split the input row.
                String[] rowArr = line.split("\t");
                // The row is only valid
                // when the attribute numbers are 10
                // i.e., authorID, firstName, lastName, publisherID, publisherName, editionNumber, years, price, title, isbn
                if (rowArr.length != 10){
                    continue;
                }
                /** get fields/values from rowArr for the Authors table */
                int authorID = Integer.parseInt(rowArr[0]);
                String firstName = rowArr[1];
                String lastName = rowArr[2];

                // add Authors tuple if does not exist
                try{
                    insertRow_Authors.setInt(1, authorID);
                    insertRow_Authors.setString(2, firstName);
                    insertRow_Authors.setString(3, lastName);
                    insertRow_Authors.executeUpdate();
                } catch(SQLException e){
                    // tuple already exists
//                     throw new Exception(e.getMessage());
                }

                /** get values from rowArr for the Publishers table */
                int publisherID = Integer.parseInt(rowArr[3]);
                String publisherName = rowArr[4];

                // add Publishers tuple if does not exist
                try{
                    insertRow_Publishers.setInt(1, publisherID);
                    insertRow_Publishers.setString(2, publisherName);
                    insertRow_Publishers.executeUpdate();
                } catch(SQLException e){
//                    System.out.println(e.getMessage());
//                    System.err.println(String.format("Table Publishers: Already inserted publisherID %d publisherName %s", publisherID, publisherName));
//                    throw new Exception(e.getMessage());
                }

                /** get values from rowArr for the Titles table */

                int editionNumber = Integer.parseInt(rowArr[5]);
                String years = rowArr[6];
                float price = Float.parseFloat(rowArr[7]);
                String title = rowArr[8];
                String isbn = rowArr[9];
                // add Titles tuple if does not exist
                try{
                    insertRow_Titles.setInt(1, editionNumber);
                    insertRow_Titles.setString(2, years);
                    insertRow_Titles.setInt(3, publisherID);
                    insertRow_Titles.setFloat(4, price);
                    insertRow_Titles.setString(5, title);
                    insertRow_Titles.setString(6, isbn);
                    insertRow_Titles.executeUpdate();
                } catch(SQLException e){
//                    System.err.println(String.format("Table Titles: Already inserted editionName %d years %s publisherID %d price %f title %s",
//                            editionNumber, years, publisherID, price, title));
//                     throw new Exception(e.getMessage());
                }


                /** get values from rowArr for the AuthorISBN table */
                // add AuthorISBN tuple if does not exist
                try{
                    insertRow_AuthorISBN.setInt(1, authorID);
                    insertRow_AuthorISBN.setString(2, isbn);
                    insertRow_AuthorISBN.executeUpdate();
                } catch (SQLException e){
//                    System.err.println(String.format("Table AuthorISBN: Already inserted authorID %d isbn %s",
//                            authorID, isbn));
//                    throw new Exception(e.getMessage());
                }
            }

            /**
             * Select all authors from the authors table. Order the information
             * alphabetically by the author’s last name and first name
             */
            orderAuthorName(stmt);

            /**
             * Select all publishers from the publishers table.
             */
            getAllPublisher(stmt);

            /**
             * Select a specific publisher and list all books published by that publisher.
             * Include the title, year and ISBN number. Order the information alphabetically
             * by title
             */
            booksByPublisher(stmt);

            /**
             * Add new Author named John Miller
             */
            insertAuthor(conn);
            insertAuthorOK(stmt); // check if new author insert succeeds by using select statement


            /**
             * Edit/Update the existing information about an author
             * change John Miller into Mary Johnson
             */
            editAuthor(conn);
            editAuthorOK(stmt);

            /**
             *  Add a new title for an author
             *  (Here I update the entire row)
             *  For the following parameterIndex:
             *  editionNumber, years, publisherID, price, title, isbn
             */
            addTitle(conn);

            /**
             * Add new publisher
             */
            addPublisher(conn);
            addPublisherOK(stmt);

            /**
             * Edit/Update the existing information about a publisher
             * update Johnson into Thompson
             */
            editPublishers(conn);
            editPublisherOK(stmt);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally{
            conn.close();
        }
    }

    public static Connection getConnection() throws Exception {
        try{
            // The connection URL for the mysql database is jdbc:mysql://localhost:3306/Books
            // where jdbc is the API,
            // mysql is the database,
            // localhost is the server name on which mysql is running,
            // we may also use IP address,
            // 3306 is the port number and
            // Books is the database name.
            // We may use any database, in such case, we need to replace the Books with our database name.
            String url = "jdbc:mysql://localhost:3306/sys";
            String username = "root"; // The default username for the mysql database is root.
            String password = "Waihanc@0";

            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password); // return instance of Connection.
            System.out.println("Connected.");
            return conn;
        } catch(Exception e){
            System.out.println(e);
            throw new Exception(e.getMessage());
        }

    }

    /**
     * drops any existing tables
     * @param conn
     * @param dbTables
     * @throws SQLException
     */
    public static void dropTable(Connection conn, String[] dbTables) throws SQLException {
        for (String tbl:dbTables){
            try{
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("drop table " + tbl);
                System.out.println("Dropped table "+ tbl);
            } catch (SQLException e){
                //System.out.println("Did not drop table " + tbl);
                System.out.println(e.getMessage());
                throw new SQLException(e.getMessage());
            }
        }
    }

    /**
     * createTable sets schema for each of the four tables
     * @param conn
     * @throws Exception
     */
    public static void createTable(Connection conn) throws Exception {
        try{
            // create the Authors table
            PreparedStatement createTable_Authors = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS Authors(" +
                            "authorID int NOT NULL AUTO_INCREMENT, " +
                            "firstName varchar(20) NOT NULL," +
                            "lastName varchar(20) NOT NULL," +
                            "PRIMARY KEY(authorID))");
            createTable_Authors.executeUpdate();
            System.out.println("Entity table Authors created.");

            // create the Publishers Table
            PreparedStatement createTable_Publishers = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS Publishers(" +
                            "publisherID int NOT NULL AUTO_INCREMENT," +
                            "publisherName CHAR (100) NOT NULL," +
                            "PRIMARY KEY(publisherID))");
            createTable_Publishers.executeUpdate();
            System.out.println("Entity table Publishers created.");


            // create the Titles table
            PreparedStatement createTable_Titles = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS Titles(" +
                            "editionNumber int NOT NULL," +
                            "years CHAR(4) NOT NULL," +
                            "publisherID int NOT NULL," +
                            "price NUMERIC (8, 2) NOT NULL," +
                            "title varchar(500) NOT NULL," +
                            "isbn CHAR(10) NOT NULL," +
                            "PRIMARY KEY(isbn)," +
                            "FOREIGN KEY(publisherID) REFERENCES Publishers(publisherID))");
            createTable_Titles.executeUpdate();
            System.out.println("Entity table Titles created.");


            // create the AuthorISBN table
            PreparedStatement createTable_AuthorISBN = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS AuthorISBN(" +
                            "authorID int NOT NULL AUTO_INCREMENT," +
                            "isbn CHAR(10) NOT NULL," +
                            "PRIMARY KEY(authorID, isbn)," + // authorID and isbn completely determine relation
                            "FOREIGN KEY(authorID) REFERENCES Authors(authorID)," +
                            "FOREIGN KEY (isbn) REFERENCES Titles(isbn))");
            createTable_AuthorISBN.executeUpdate();
            System.out.println("Relation table AuthorISBN created.");

        } catch(SQLException e){
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

    }

    /**
     * order authorname in ascending order
     * @param stmt
     * @throws Exception
     */
    public static void orderAuthorName(Statement stmt) throws Exception{
        ResultSet rs;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT * " +
                    "FROM Authors " +
                    "ORDER BY LastName, firstName ASC");
            System.out.println();
            System.out.println("========== Query 1: Order by last name and first name in ascending order =========");

            String leftAlignFormat = "| %-9s | %-12s | %-12s|%n";

            System.out.format("+-----------+--------------+-------------+%n");
            System.out.format("| authorID  | first name   | last name   |%n");
            System.out.format("+-----------+--------------+-------------+%n");
            while (rs.next()){
                int authorID = rs.getInt(1);
                String firstName = rs.getString(2);
                String lastName = rs.getString(3);
                System.out.format(leftAlignFormat,authorID, firstName, lastName);
            }
            System.out.format("+-----------+--------------+-------------+%n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * find all publishers from the publisher table
     */
    public static void getAllPublisher(Statement stmt) throws Exception{
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT publisherName " +
                    "FROM Publishers ");
            System.out.println();
            System.out.println("========== Query 2: find all publishers from the publisher table =========");

            String leftAlignFormat = "| %-25s |%n";

            System.out.format("+---------------------------+%n");
            System.out.format("| publisherName             |%n");
            System.out.format("+---------------------------+%n");
            while (rs.next()){
                String publisherName = rs.getString(1);
                System.out.format(leftAlignFormat,publisherName);
            }
            System.out.format("+---------------------------+%n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Select a specific publisher and list all books published by that publisher.
     * Include the title, year and ISBN number. Order the information alphabetically
     * by title
     * @param stmt
     * @throws Exception
     */


    public static void booksByPublisher(Statement stmt) throws Exception{
        ResultSet rs;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT Titles.title, Titles.years, Titles.isbn " +
                    "FROM Titles, Publishers " +
                    "WHERE Titles.publisherID = Publishers.publisherID AND Publishers.publisherName = 'IEEE' " +
                    "ORDER BY Titles.title ASC");
            System.out.println();
            System.out.println("========== Query 3: Order by last name and first name in ascending order =========");

            String leftAlignFormat = "| %-30s | %-12s | %-12s|%n";

            System.out.format("+--------------------------------+--------------+-------------+%n");
            System.out.format("| title                          | year         | isbn        |%n");
            System.out.format("+--------------------------------+--------------+-------------+%n");
            while (rs.next()){
                String title = rs.getString(1);
                int year = rs.getInt(2);
                int isbn = rs.getInt(3);
                System.out.format(leftAlignFormat,title, year, isbn);
            }
            System.out.format("+--------------------------------+--------------+-------------+%n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add new Author name John miller
     * @param conn
     */
    public static void insertAuthor(Connection conn){
        final String first = "John";
        final String last = "Miller";
        try{
            PreparedStatement posted = conn.prepareStatement("INSERT INTO Authors(firstName, lastName) " +
                    "values ('" + first + "','" + last + "')"); // single quote for string
            posted.executeUpdate();
        } catch(Exception e){
            System.out.println(e);
        } finally{
            System.out.println();
        }
    }

    /**
     * check if adding author John Miller is successful.
     * @param stmt
     * @throws Exception
     */
    public static void insertAuthorOK(Statement stmt) throws Exception{
        ResultSet rs;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT * " +
                    "FROM Authors " +
                    "WHERE authorID = 37 AND firstName = 'John' AND lastName = 'Miller' ;");
            System.out.println();
            while (rs.next()){
                System.out.println("========== Query 4: New Author inserted: [Author ID: +" + rs.getInt(1) +
                        ", first name: " + rs.getString(2) +
                        ", last name: " + rs.getString(3) +
                        "] =========");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Edit/Update the existing information about an author
     */
    public static void editAuthor(Connection conn){
        try{
            PreparedStatement posted1 = conn.prepareStatement("UPDATE Authors, AuthorISBN " +
                    "SET Authors.firstName = 'Mary', Authors.lastName = 'Johnson' " +
                    "WHERE Authors.authorID = 37 AND Authors.firstName = 'John' AND lastName = 'Miller' ;"); // single quote for string
            posted1.executeUpdate();

        } catch(Exception e){
            System.out.println(e);
        } finally{
        }
    }

    /**
     * check if changing John Miller into Mary Johnson is successful.
     * @param stmt
     * @throws Exception
     */
    public static void editAuthorOK(Statement stmt) throws Exception{
        ResultSet rs;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT * " +
                    "FROM Authors " +
                    "WHERE authorID = 37 ;");
            System.out.println();
            if (rs.next()){
                System.out.println("========== Query 5: Edit author name: Change John Miller into Mary Johnson. \n" +
                        "Result after edits: [Author ID: +" + rs.getInt(1) +
                        ", first name is Mary: " + rs.getString(2).equals("Mary") +
                        ", last name is Johnson: " + rs.getString(3).equals("Johnson") +
                        "] ========= \n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * add an title to the Titles table
     * @param conn
     */
    public static void addTitle(Connection conn){
        try{
            PreparedStatement insertRow_Titles = conn.prepareStatement(
                    "INSERT INTO Titles(editionNumber, years, publisherID, price, title, isbn) values(?,?,?,?,?,?)"
            );

            insertRow_Titles.setInt(1, 1);
            insertRow_Titles.setString(2, "1993");
            insertRow_Titles.setInt(3, 5);
            insertRow_Titles.setFloat(4, 55);
            insertRow_Titles.setString(5, "Marching Band");
            insertRow_Titles.setString(6, "12345678");
            insertRow_Titles.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * add a publisher into Publishers table
     * @param conn
     */
    public static void addPublisher(Connection conn){
        String publisherName = "Johnson";
        try{

            PreparedStatement post = conn.prepareStatement(
                    "INSERT INTO Publishers(publisherName) values('" + publisherName + "')"
            );
            post.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * check if adding a publisher is successful.
     * @param stmt
     * @throws Exception
     */
    public static void addPublisherOK(Statement stmt) throws Exception{
        ResultSet rs;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT * " +
                    "FROM Publishers " +
                    "WHERE publisherID = 17 AND publisherName = 'Johnson' ;");
            System.out.println();
            while (rs.next()){
                System.out.println("========== Query 6: New publisher added: [publisher ID: +" + rs.getInt(1) +
                        ", publisher name: " + rs.getString(2) +
                        "] =========");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * edit publisher name.
     * @param conn
     */
    public static void editPublishers(Connection conn){
        try{
            System.out.println("Update the existing information about a publisher: Change Johnson into Thompson");
            PreparedStatement posted = conn.prepareStatement("UPDATE Publishers " +
                    "SET publisherName = 'Thompson' " +
                    "WHERE publisherID = 17 ;");
            posted.executeUpdate();
        } catch(Exception e){
            System.out.println(e);
        } finally{
            System.out.println();
        }
    }

    /**
     * check if changing publisher name is successful.
     * @param stmt
     * @throws Exception
     */
    public static void editPublisherOK(Statement stmt) throws Exception{
        ResultSet rs;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT * " +
                    "FROM Publishers " +
                    "WHERE publisherID = 17 AND publisherName = 'Thompson' ;");
            System.out.println();
            if (rs.next()){
                System.out.println("========== Query 7: Edit publisher: Change Johnson into Thompson. \n" +
                        "Result after edits: [publisher ID: +" + rs.getInt(1) +
                        ", publisher name is Thompson: " + rs.getString(2).equals("Thompson") +
                        "] =========");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
