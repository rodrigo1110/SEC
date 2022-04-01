package sec.bftb.server;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

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
            connection = this.newConnection();

            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.setLogWriter(null);
            scriptRunner.runScript(new BufferedReader(new FileReader(this.dbDir)));
            this.logger.log("Database structure created successfully!");
        } catch (SQLException | FileNotFoundException e) {
            this.logger.log(e.getMessage());
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) { 
                    /* Ignored */}
            }
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

    public void openAccount(String pubKey, Float balance) throws SQLException {
        try {
            String query = "INSERT INTO account (pubKey, balance) VALUES (?, ?)";
            connection = this.newConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, pubKey);
            statement.setFloat(2, balance);
            statement.executeUpdate();
        } finally {
            closeConnection();
        }
    }

    public void addTransfer(String srcPubKey, String destPubKey, Float amount, int movementId) throws SQLException {
        try {
            String query = "INSERT INTO account (movementId, amount, sourceAccount, destinationAccount) VALUES (?, ?, ?, ?)";
            connection = this.newConnection();
            statement = connection.prepareStatement(query);
            statement.setInt(1, movementId);
            statement.setFloat(2, amount);
            statement.setString(3, srcPubKey);
            statement.setString(4, destPubKey);
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

    public void updateBalance(String pubKey, float newBalance) throws SQLException {
        try {
            String query = "UPDATE account SET balance=? WHERE pubKey=?";

            connection = this.newConnection();
            statement = connection.prepareStatement(query);
            statement.setFloat(1, newBalance);
            statement.setString(2, pubKey);
            statement.executeUpdate();
        } finally {
            closeConnection();
        }
    }

    public int getMaxTranferId() throws SQLException {
        try {
            String query = "SELECT MAX(movementId) AS maxId FROM movement";
            connection = this.newConnection();
            statement = connection.prepareStatement(query);
            resultSet = statement.executeQuery();

            if (resultSet.next())
                return resultSet.getInt("maxId");
            else
                return -1;
    
        } finally {
            closeConnection();
        }
    }
}