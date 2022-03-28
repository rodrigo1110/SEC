package pt.tecnico.grpc.server;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.awt.*;
import java.io.*;
import java.security.PublicKey;
import java.sql.*;
import java.util.Properties;


public class ServerRepo {

    private final Logger logger;
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final String dbDir;
    private Connection connection = null;
    private PreparedStatement statement = null;
    private ResultSet resultSet = null;

    public ServerRepo() {
        this.logger = new Logger("Server", "DB");
        this.dbUrl = System.getenv("DB_URL");
        this.dbUsername = System.getenv("DB_USERNAME");
        this.dbPassword = System.getenv("DB_PASSWORD");
        this.dbDir = System.getenv("DB_DIR");

        try {
            if (this.dbUrl == null || this.dbDir == null || this.dbUsername == null || this.dbPassword == null) {
                this.logger.log("You must set the environment variables!");
                System.exit(-1);
            }
            connection = this.newConnection();
            this.logger.log("Connected to database successfully!");

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
        Properties properties = new Properties();
        properties.setProperty("user", this.dbUsername);
        properties.setProperty("password", this.dbPassword);
        properties.setProperty("ssl", "true");
        properties.setProperty("sslmode", "require");
        return DriverManager.getConnection(this.dbUrl, properties);
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

    public int getAccount(String pubKey) throws SQLException {
        try {
            String query = "SELECT * FROM account WHERE pubKey=?";
            connection = this.newConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, pubKey);

            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("balance");             
            }
            return -1;
        } finally{
            closeConnection();
        }
    }

    public void createAccount(String pubKey, Integer balance) throws SQLException {
        try {
            String query = "INSERT INTO account (pubKey, balance) VALUES (?, ?)";
            connection = this.newConnection();
            statement = connection.prepareStatement(query);
            statement.setString(1, pubKey);
            statement.setString(2, balance);
            statement.executeUpdate();
        } finally {
            closeConnection();
        }
    }
}
