import java.sql.*;

public class Books {
    public static void main(String[] args){
        // tables created by the current program
        String dbTables[] = {
                "Authors", "Titles", "Publishers", "AuthorISBN"
        };

        try{
            // connect to database
            Connection conn = getConnection();

            // drop the database tables and recreate them below
            dropTable(conn, dbTables);

            // create the Authors table
            PreparedStatement createTable_Authors = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS Authors(" +
                            "authorID int NOT NULL AUTO_INCREMENT, " +
                            "firstName varchar(255) NOT NULL," +
                            "lastName varchar(255) NOT NULL," +
                            "PRIMARY KEY(authorID))");
            createTable_Authors.executeUpdate();
            System.out.println("Entity table Authors created.");

            // create the Publishers Table
            PreparedStatement createTable_Publishers = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS Publishers(" +
                            "publisherID int NOT NULL AUTO_INCREMENT," +
                            "publisherName varchar (255) NOT NULL," +
                            "PRIMARY KEY(publisherID))");
            createTable_Publishers.executeUpdate();
            System.out.println("Entity table Publishers created.");


            // create the Titles table
            PreparedStatement createTable_Titles = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS Titles(" +
                            "isbn int NOT NULL," +
                            "editionNUmber int NOT NULL," +
                            "year int NOT NULL," +
                            "publisherID int NOT NULL," +
                            "price double NOT NULL," +
                            "title varchar(255) NOT NULL," +
                            "PRIMARY KEY(isbn)," +
                            "FOREIGN KEY(publisherID) REFERENCES Publishers(publisherID))");
            createTable_Titles.executeUpdate();
            System.out.println("Entity table Titles created.");


            // create the AuthorISBN table
            PreparedStatement createTable_AuthorISBN = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS AuthorISBN(" +
                            "authorID int NOT NULL AUTO_INCREMENT," +
                            "isbn int NOT NULL," +
                            "PRIMARY KEY(authorID, isbn)," + // authorID and isbn completely determine relation
                            "FOREIGN KEY(authorID) REFERENCES Authors(authorID)," +
                            "FOREIGN KEY (isbn) REFERENCES Titles(isbn))");
            createTable_AuthorISBN.executeUpdate();
            System.out.println("Relation table AuthorISBN created.");

        } catch (Exception e){
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
}
