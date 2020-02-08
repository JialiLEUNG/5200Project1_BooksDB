import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class BooksPopulate {
    public static void main(String[] args) throws IOException {
        // Four tables will be created in the current program:
        // Schema of the four tables can be seen in E/R diagram in the report.
        String dbTables[] = {
                "Authors", "Titles", "Publishers", // entities
                "AuthorISBN" // relation (Authors and Titles is many-to-many relationship.)
        };

        // name of the Books data file
        String fileName = "src/BookData.txt";

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
                    "INSERT INTO Titles(isbn, years, publisherID, price, title) values(?,?,?,?,?,?)"
            ); // isbn, years, publisherID, price, title
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
                    System.err.println("Deleting existing records in table" + tbl + " failed.");
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
                    System.err.println(String.format("Table Authors: Already inserted authorID %d firstName %s lastName %s",
                            authorID, firstName, lastName));
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
                    System.err.println(String.format("Table Publishers: Already inserted publisherID %d publisherName %s", publisherID, publisherName));
                }

                /** get values from rowArr for the Titles table */
                int editionName = Integer.parseInt(rowArr[5]);
                String years = rowArr[6];
                float price = Float.parseFloat(rowArr[7]);
                String title = rowArr[8];
                // add Titles tuple if does not exist
                try{
                    insertRow_Titles.setInt(1, editionName);
                    insertRow_Titles.setString(2, years);
                    insertRow_Titles.setInt(3, publisherID);
                    insertRow_Titles.setFloat(4, price);
                    insertRow_Titles.setString(5, title);
                    insertRow_Titles.executeUpdate();
                } catch(SQLException e){
                    System.err.println(String.format("Table Titles: Already inserted editionName %d years %s publisherID %d price %f title %s",
                            editionName, years, publisherID, price, title));
                }

                /** get values from rowArr for the AuthorISBN table */
                String isbn = rowArr[9];
                // add AuthorISBN tuple if does not exist
                try{
                    insertRow_AuthorISBN.setInt(1, authorID);
                    insertRow_AuthorISBN.setString(2, isbn);
                    insertRow_AuthorISBN.executeUpdate();
                } catch (SQLException e){
                    System.err.println(String.format("Table AuthorISBN: Already inserted authorID %d isbn %s",
                            authorID, isbn));
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection(){
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
        }
        return null;
    }

    public static void dropTable(Connection conn, String[] dbTables){
        for (String tbl:dbTables){
            try{
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("drop table " + tbl);
                System.out.println("Dropped table "+ tbl);
            } catch (SQLException e){
                System.out.println("Did not drop table " + tbl);
            }
        }
    }

    public static void createTable(Connection conn){
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
                            "isbn CHAR(10) NOT NULL," +
                            "editionNumber int NOT NULL," +
                            "years CHAR(4) NOT NULL," +
                            "publisherID int NOT NULL," +
                            "price NUMERIC (8, 2) NOT NULL," +
                            "title varchar(500) NOT NULL," +
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
}
