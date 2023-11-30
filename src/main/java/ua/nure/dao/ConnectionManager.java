package ua.nure.dao;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ConnectionString;
import com.mongodb.connection.ClusterDescription;
import com.mongodb.connection.ServerDescription;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConnectionManager {
    private static Map<String, ConnectionManager> instances = new HashMap<>();

    private Connection connection;
    private MongoDatabase mongoDatabase;
    private ConnectionProperties properties;

    private ConnectionManager(ConnectionProperties properties) {
        this.properties = properties;
    }

    public static ConnectionManager getInstance(ConnectionProperties properties) {
        String key = properties.getType() + "_" + properties.getUrl();

        if (!instances.containsKey(key)) {
            ConnectionManager instance = new ConnectionManager(properties);
            instance.initializeConnection();
            instances.put(key, instance);
        }

        return instances.get(key);
    }

    private void initializeConnection() {
        if ("mysql".equalsIgnoreCase(properties.getType())) {
            try {
                connection = DriverManager.getConnection(properties.getUrl(), properties.getUser(), properties.getPassword());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if ("mongodb".equalsIgnoreCase(properties.getType())) {
            if (properties.getReplicaSetHosts().length > 0) {
                initializeMongoReplicaSetConnection();
            } else {
                initializeMongoSingleConnection();
            }
        }
    }

    private void initializeMongoSingleConnection() {
        MongoClient mongoClient = MongoClients.create(properties.getMongoUri());
        mongoDatabase = mongoClient.getDatabase(properties.getMongoDatabaseName());
    }

    private void initializeMongoReplicaSetConnection() {
        String connectionString = buildReplicaSetConnectionString(properties.getReplicaSetHosts(), properties.getMongoDatabaseName());
        MongoClient mongoClient = MongoClients.create(connectionString);
        mongoDatabase = mongoClient.getDatabase(properties.getMongoDatabaseName());
    }

    private String buildReplicaSetConnectionString(String[] hosts, String databaseName) {
        StringBuilder connectionString = new StringBuilder("mongodb://");
        for (String host : hosts) {
            connectionString.append(host).append(",");
        }
        connectionString.deleteCharAt(connectionString.length() - 1); // Remove the trailing comma
        connectionString.append("/").append(databaseName);
        return connectionString.toString();
    }

    public Connection getMySQLConnection() {
        return connection;
    }

    public MongoDatabase getMongoConnection() {
        return mongoDatabase;
    }

    public static void rollback(Connection connection) {
        try {
            Objects.requireNonNull(connection).rollback();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void close(AutoCloseable... closeable) {
        for (AutoCloseable c : closeable) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
