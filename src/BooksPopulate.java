import com.mysql.cj.protocol.Resultset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class BooksPopulate {
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

        // result set for queries
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
                // i.e., authorID, firstName, lastName, publisherID,publisherName, editionNumber, years, price, title, isbn
                if (rowArr.length != 10){
                    continue;
                }
                // authorID	firstName	lastName	publisherID	publisherName	editionNO	year	price	title	isbn
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
//                    System.out.println(e.getMessage());
//                    System.err.println(String.format("Table Authors: Already inserted authorID %d firstName %s lastName %s",
//                            authorID, firstName, lastName));
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

            // Print the articles that are published after 2000 in the Titles table
            System.out.println("=============== articles published after 2000 in table Titles================");
            getArticleAfter2000(stmt);
            // Print the articles that are published by the same publisher.
//            getArticleByPublisher(stmt);
            // order book by price in table Titles
            System.out.println("=============== order book by price in table Titles ================");
            orderBookByPrice(stmt);

            // count the number of books by publisherID in table Titles
            System.out.println("=============== count the number of books by publisher in table Titles ================");
            countISBNbyPublisher(stmt);

            // books start with 'Harry Potter'
            System.out.println("=============== books whose title starts with 'Harry Potter' in table Titles ================");
            bookStartByHarryPotter(stmt);


            // find authors of 'Bitcoin's Academic Pedigree'
            System.out.println("=============== authors of 'Bitcoin's Academic Pedigree' ================");
            findAuthorByBookTitle(stmt);

            // find the book written by Neil Savage
            System.out.println("=============== find Neil Savage's book ================");
            findNeilSavageBooks(stmt);

            // average price by each publisher
            System.out.println("=============== calculate average price by each publisher ==================");
            avgPriceByPublisher(stmt);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
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

    // post information
    public static void post(Connection conn){
        final String var1 = "John";
        final String var2 = "Miller";
        try{
            PreparedStatement posted = conn.prepareStatement("INSERT INTO testIDtable(first, last) " +
                    "values ('" + var1 + "','" + var2 + "')"); // single quote for string
            posted.executeUpdate();
        } catch(Exception e){
            System.out.println(e);
        } finally{
            System.out.println("insert completed");
        }
    }

    // get information
    // getArticleAfter2000 prints the articles that are published after 2000 in the Titles table
    public static void getArticleAfter2000(Statement stmt) throws SQLException {
        // result set for queries
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT title, years " +
                    "FROM Titles " +
                    "Where years > 2000");
            while (rs.next()){
                String articleName = rs.getString(1);
                int year = rs.getInt(2);
                System.out.printf(String.format("Table Title: article after Year 2000 is %s (year: %d).\n", articleName, year));
            }
            System.out.println("Table Title: No more article after Year 2000");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void getArticleByPublisher(Statement stmt) throws SQLException {
        // result set for queries
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT Titles.title, Publishers.publisherName " +
                    "FROM Titles" +
                    "NATURAL JOIN Publishers on Titles.publisherID = Publishers.publisherID" +
                    "GROUP BY Titles.publisherID");
            while (rs.next()){
                String articleName = rs.getString(1);
                String publisherName = rs.getString(2);
                System.out.printf(String.format("Table Title: article by %s is %s.\n", publisherName, articleName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * order the book by price in ascending order
     * @param stmt
     * @throws Exception
     */
    public static void orderBookByPrice(Statement stmt) throws Exception{
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT title, price " +
                    "FROM Titles " +
                    "ORDER BY price ASC");
            while (rs.next()){
                String articleName = rs.getString(1);
                Float price = rs.getFloat(2);
                System.out.printf(String.format("Table Title: price of article %s is %f.\n", articleName, price));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * count the number of books by publisher
     * @param stmt
     * @throws Exception
     */
    public static void countISBNbyPublisher(Statement stmt) throws Exception{
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT publisherID, count(isbn) " +
                    "FROM Titles " +
                    "GROUP BY publisherID");
            while (rs.next()){
                int publisherID = rs.getInt(1);
                int bookCount = rs.getInt(2);
                System.out.printf(String.format("Table Title: Number of books by Publisher %d is %d.\n", publisherID, bookCount));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * books with title starting with 'Harry Potter';
     * @param stmt
     * @throws Exception
     */
    public static void bookStartByHarryPotter(Statement stmt) throws Exception{
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT title " +
                    "FROM Titles " +
                    "WHERE title like 'Harry Potter%'");
            while (rs.next()){
                String harryBook = rs.getString(1);
                System.out.printf(String.format("Table Title: Book start with 'Harry Potter': %s.\n", harryBook));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void findAuthorByBookTitle(Statement stmt) throws Exception{
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT firstName, lastName " +
                    "FROM Titles, Authors, AuthorISBN " +
                    "WHERE Authors.authorID = AuthorISBN.authorID " +
                    "      AND Titles.isbn = AuthorISBN.isbn" +
                    "      AND Titles.title = 'Bitcoin''s Academic Pedigree'");
            while (rs.next()){
                String firstName = rs.getString(1);
                String lastName = rs.getString(2);
                System.out.printf(String.format("The author(s) of Bitcoin''s Academic Pedigree: %s %s.\n", firstName, lastName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * find books written by Nail Savage
     * @param stmt
     * @throws Exception
     */
    public static void findNeilSavageBooks(Statement stmt) throws Exception{
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT Titles.title " +
                    "FROM Titles, Authors, AuthorISBN " +
                    "WHERE Authors.authorID = AuthorISBN.authorID " +
                    "      AND Titles.isbn = AuthorISBN.isbn" +
                    "      AND Authors.firstName = 'Neil'" +
                    "      AND Authors.lastName = 'Savage'");
            while (rs.next()){
                String title = rs.getString(1);
                System.out.printf(String.format("Neil Savage's book: %s.\n", title));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * calculate the average price for books by each publisher
     * @param stmt
     * @throws Exception
     */
    public static void avgPriceByPublisher(Statement stmt) throws Exception{
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("" +
                    "SELECT Titles.publisherID, Publishers.publisherName, round(avg(Titles.price),2) " +
                    "FROM Titles, Publishers " +
                    "WHERE Titles.publisherID = Publishers.publisherID " +
                    "GROUP BY Titles.publisherID");
            while (rs.next()){
                int publisherID = rs.getInt(1);
                String publisherName = rs.getString(2);
                Float price = rs.getFloat(3);
                System.out.printf(String.format("Average price by Publisher %d (%s): %f.\n", publisherID, publisherName, price));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
