package ua.nure.dao.EntityDAOImpl.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import ua.nure.dao.EntityDAO.UserDAO;
import ua.nure.entity.Clothing;
import ua.nure.entity.User;
import ua.nure.entity.enums.Role;

import java.util.ArrayList;
import java.util.List;

public class MongoUserDAOImpl implements UserDAO {
    private final MongoDatabase connection;
    private final MongoCollection<Document> collection;
    private static final String COLLECTION_NAME = "user";

    public MongoUserDAOImpl(MongoDatabase connection) {
        this.connection = connection;
        this.collection = connection.getCollection(COLLECTION_NAME);
    }

    @Override
    public String add(User user) {
        Document document = new Document()
                .append("name", user.getName())
                .append("surname", user.getSurname())
                .append("password", user.getPassword())
                .append("email", user.getEmail())
                .append("phone", user.getPhone())
                .append("role", user.getRole());

        collection.insertOne(document);
        ObjectId id = document.getObjectId("_id");
        return id != null ? id.toString() : null;
    }

    @Override
    public User update(User entity) {
        Bson filter = Filters.eq("_id", new ObjectId(entity.getId()));
        Document updateDocument = new Document("$set", new Document("name", entity.getName())
                .append("surname", entity.getSurname())
                .append("email", entity.getEmail())
                .append("password", entity.getPassword())
                .append("phone", entity.getPhone()));

        collection.updateOne(filter, updateDocument);
        return findById(entity.getId());
    }

    @Override
    public void delete(String id) {
        Bson filter = Filters.eq("_id", new ObjectId(id));
        collection.deleteOne(filter);
    }

    @Override
    public User findById(String id) {
        try {
            Bson filter = Filters.eq("_id", new ObjectId(id));
            Document result = collection.find(filter).first();
            return result != null ? mapUser(result) : null;
        } catch (MongoException e) {
            throw new RuntimeException("Failed to find user by ID", e);
        }
    }

    @Override
    public List<User> findAll() {
        try {
            List<User> users = new ArrayList<>();
            collection.find().forEach(document -> users.add(mapUser(document)));
            return users;
        } catch (MongoException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    private User mapUser(Document document) {
        String id = document.getObjectId("_id").toString();
        String name = document.getString("name");
        String surname = document.getString("surname");
        String password = document.getString("password");
        String email = document.getString("email");
        String phone = document.getString("phone");
        Role role = Role.valueOf(document.getString("role"));
        return new User.Builder(name, surname, email, phone)
                .setId(id)
                .setPassword(password)
                .build();
    }

    @Override
    public List<User> findByMultipleKeys(String email, String password) {
        BasicDBObject query = new BasicDBObject();
        query.put("email", email);
        query.put("password", password);

        FindIterable<Document> documents = collection.find(query);
        List<User> userList = new ArrayList<>();

        for (Document document : documents) {
            User user = mapUser(document);
            userList.add(user);
        }

        return userList;
    }
}
