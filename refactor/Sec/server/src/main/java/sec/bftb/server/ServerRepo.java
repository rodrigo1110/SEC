package sec.bftb.server;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerRepo {

    private final Logger logger;
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final String dbDir;
    private Connection connection = null;
    private PreparedStatement statement = null;
    private ResultSet resultSet = null;

    public ServerRepo(int port) {
        this.logger = new Logger("Server", "DB");
        this.dbUrl = System.getenv("DB_URL");
        this.dbUsername = System.getenv("DB_USERNAME");
        this.dbPassword = System.getenv("DB_PASSWORD");
        this.dbDir = System.getenv("DB_DIR");

        try {
            Connection connection = this.newConnection();
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE DATABASE IF NOT EXISTS remotedocs_db");
            this.logger.log("Database created successfully!");

            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setLogWriter(null);
            scriptRunner.runScript(new BufferedReader(new FileReader(this.dbDir)));
            this.logger.log("Database structure created successfully!");
        } catch (SQLException | FileNotFoundException e) {
            this.logger.log(e.getMessage());
        }
    }

    private Connection newConnection() throws SQLException {
        return DriverManager.getConnection(this.dbUrl, this.dbUsername, this.dbPassword);
    }

    private void closeConnection(){
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) { 
                this.logger.log(e.getMessage());
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                this.logger.log(e.getMessage());
            }
        }

        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                this.logger.log(e.getMessage());
            }
        }
    }

    public void createAccount(String pubKey, Integer balance) throws SQLException {
        try {
            String query = "INSERT INTO account (pubKey, balance) VALUES (?, ?)";
            connection = this.newConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, pubKey);
            statement.setInt(2, balance);
            statement.executeUpdate();
        } finally {
            closeConnection();
        }
    }

    public float getBalance(String pubKey) throws SQLException {
        try {
            String query = "SELECT balance FROM account WHERE pubKey=?";
            connection = this.newConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, pubKey);

            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getFloat("balance");             
            }
            else{
                return -1; //serve para verificar se a conta ja existe
            }
        } finally{
            closeConnection();
        }
    }
}