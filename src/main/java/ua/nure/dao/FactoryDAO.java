package ua.nure.dao;

import com.mongodb.client.MongoDatabase;
import ua.nure.dao.EntityDAO.ClothingDAO;
import ua.nure.dao.EntityDAO.OrderDAO;
import ua.nure.dao.EntityDAO.UserDAO;
import ua.nure.dao.EntityDAOImpl.mongodb.MongoClothingDAOImpl;
import ua.nure.dao.EntityDAOImpl.mongodb.MongoOrderDAOImpl;
import ua.nure.dao.EntityDAOImpl.mongodb.MongoUserDAOImpl;
import ua.nure.dao.EntityDAOImpl.mysql.MySQLClothingDAOImpl;
import ua.nure.dao.EntityDAOImpl.mysql.MySQLOrderDAOImpl;
import ua.nure.dao.EntityDAOImpl.mysql.MySQLUserDAOImpl;

import java.sql.Connection;

public class FactoryDAO implements Factory {
    private ConnectionManager connectionManager;
    private ConnectionProperties connectionProperties;

    public FactoryDAO(String dbType) {
        connectionProperties = new ConnectionProperties(dbType);
        connectionManager = ConnectionManager.getInstance(connectionProperties);
    }

    @Override
    public UserDAO getUserDAO() {
        if ("mysql".equalsIgnoreCase(connectionProperties.getType())) {
            Connection connection = connectionManager.getMySQLConnection();
            return new MySQLUserDAOImpl(connection);
        } else if ("mongodb".equalsIgnoreCase(connectionProperties.getType())) {
            MongoDatabase connection = connectionManager.getMongoConnection();
            return new MongoUserDAOImpl(connection);
        } else {
            throw new UnsupportedOperationException("Unsupported database type");
        }
    }

    @Override
    public ClothingDAO getClothingDAO() {
        if ("mysql".equalsIgnoreCase(connectionProperties.getType())) {
            Connection connection = connectionManager.getMySQLConnection();
            return new MySQLClothingDAOImpl(connection);
        } else if ("mongodb".equalsIgnoreCase(connectionProperties.getType())) {
            MongoDatabase connection = connectionManager.getMongoConnection();
            return new MongoClothingDAOImpl(connection);
        } else {
            throw new UnsupportedOperationException("Unsupported database type");
        }
    }

    @Override
    public OrderDAO getOrderDAO() {
        if ("mysql".equalsIgnoreCase(connectionProperties.getType())) {
            Connection connection = connectionManager.getMySQLConnection();
            return new MySQLOrderDAOImpl(connection);
        } else if ("mongodb".equalsIgnoreCase(connectionProperties.getType())) {
            MongoDatabase connection = connectionManager.getMongoConnection();
            return new MongoOrderDAOImpl(connection);
        } else {
            throw new UnsupportedOperationException("Unsupported database type");
        }
    }
}
