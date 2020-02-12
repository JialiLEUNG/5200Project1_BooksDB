 * To run the Book.java, please include the classpath of the mysql connector jar before typing Book.java,
 * and then add the following four arguments: <File pathname> <The connection URL for the mysql database> <mysql username> <mysql password>
 * Specifically,
 * The 1st argument is to put the file path of BookDataNEW.txt;
 * The 2nd argument is the connection URL for the mysql database;
 * The 3rd argument is your username of the mysql database in order to connect to the MySQL server;
 * The 4th argument is your password of the mysql database
 * For example, in your console, type the following:
 * java -classpath ~/Downloads/mysql-connector-java-8.0.19.jar Book.java /Users/hcloud/Desktop/Jiali/NEU/courses/5200_Database_Management_System/2020Spring/project/project1/src/BookDataNEW.txt jdbc:mysql://localhost:3306/sys root abc123
 * I have included the connector jar in the same directory of Book.java.
 * Please specify the pathname of the jar.