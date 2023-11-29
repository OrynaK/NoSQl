package ua.nure.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConnectionProperties {
    private String type;
    private String url;
    private String user;
    private String password;
    private String mongoUri;
    private String mongoDatabaseName;

    public ConnectionProperties(String dbType) {
        loadProperties(dbType);
    }

    private void loadProperties(String dbType) {
        Properties config = new Properties();
        this.type = dbType;
        try {
            config.load(new FileInputStream("application.properties"));
            url = config.getProperty("database.url");
            user = config.getProperty("database.user");
            password = config.getProperty("database.password");
            mongoUri = config.getProperty("database.mongoUri");
            mongoDatabaseName = config.getProperty("database.mongoDatabaseName");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getMongoDatabaseName() {
        return mongoDatabaseName;
    }

    public String getType() {
        return type;
    }

    public String getMongoUri() {
        return mongoUri;
    }

    public ConnectionProperties setType(String type) {
        this.type = type;
        return this;
    }
    @Override
    public String toString() {
        return "ConnectionProperties{" +
                "url='" + url + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
