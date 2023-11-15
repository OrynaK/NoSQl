package ua.nure.dao.EntityDAOImpl.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import ua.nure.dao.EntityDAO.OrderDAO;
import ua.nure.entity.*;
import ua.nure.entity.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import ua.nure.entity.enums.Status;


public class MongoOrderDAOImpl implements OrderDAO {
    private final MongoDatabase connection;
    private final MongoCollection<Document> collection;
    private static final String COLLECTION_NAME = "order";

    public MongoOrderDAOImpl(MongoDatabase connection) {
        this.connection = connection;
        this.collection = connection.getCollection(COLLECTION_NAME);
    }

    @Override
    public String add(Order order) {
        Document orderDocument = new Document();
        orderDocument.append("users", mapUsers(order.getUsersInOrder()))
                .append("status", order.getStatus().toString())
                .append("clothing", mapClothing(order.getClothesInOrder()))
                .append("delivery", mapDelivery(order.getDelivery()))
                .append("datetime", order.getDateTime());

        collection.insertOne(orderDocument);
        ObjectId id = orderDocument.getObjectId("_id");
        return id != null ? id.toString() : null;
    }

    @Override
    public Order update(Order order) {
        Document filter = new Document("_id", new ObjectId(order.getId()));
        Document updateDoc = new Document("$set", new Document()
                .append("datetime", Date.from(order.getDateTime().atZone(ZoneId.systemDefault()).toInstant()))
                .append("status", order.getStatus().toString())
                .append("clothing", mapClothing(order.getClothesInOrder()))
                .append("users", mapUsers(order.getUsersInOrder()))
                .append("delivery", mapDelivery(order.getDelivery())));

        collection.updateOne(filter, updateDoc);

        return findById(order.getId());
    }


    @Override
    public void delete(String id) {
        Document filter = new Document("_id", new ObjectId(id));
        collection.deleteOne(filter);
    }

    @Override
    public void updateStatus(String orderId, Status status) {
        Document filter = new Document("_id", new ObjectId(orderId));
        Document updateDoc = new Document("$set", new Document("status", status));
        collection.updateOne(filter, updateDoc);
    }

    @Override
    public List<Order> findByMultipleKeys(Status status, LocalDateTime dateTime) {
        BasicDBObject query = new BasicDBObject();
        query.put("status", status);
        query.put("datetime", dateTime);

        FindIterable<Document> documents = collection.find(query);
        List<Order> orderList = new ArrayList<>();

        for (Document document : documents) {
            Order order = mapOrder(document);
            orderList.add(order);
        }

        return orderList;
    }

    @Override
    public Order findById(String id) {
        Document filter = new Document("_id", new ObjectId(id));
        Document result = collection.find(filter).first();
        return (result != null) ? mapOrder(result) : null;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                Document orderDocument = cursor.next();
                Order order = mapOrder(orderDocument);
                orders.add(order);
            }
        }

        return orders;
    }

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        List<Order> orders = new ArrayList<>();
        Document filter = new Document("users._id", new ObjectId(userId));

        FindIterable<Document> orderDocuments = collection.find(filter);

        for (Document orderDocument : orderDocuments) {
            Order order = mapOrder(orderDocument);
            orders.add(order);
        }

        return orders;
    }

    private List<Document> mapClothing(List<Clothing> clothes) {
        List<Document> clothingDocuments = new ArrayList<>();

        for (Clothing clothing : clothes) {
            Document clothingDocument = new Document();
            clothingDocument.append("name", clothing.getName());
            clothingDocument.append("size", clothing.getSize().toString());
            clothingDocument.append("color", clothing.getColor());
            clothingDocument.append("season", clothing.getSeason().toString());
            clothingDocument.append("amount", clothing.getAmount());
            clothingDocument.append("actual_price", clothing.getActualPrice());
            clothingDocument.append("sex", clothing.getSex().toString());
            clothingDocument.append("_id", new ObjectId(clothing.getId()));
            clothingDocuments.add(clothingDocument);
        }
        return clothingDocuments;
    }

    private List<Document> mapUsers(List<User> users) {
        List<Document> userDocuments = new ArrayList<>();

        for (User user : users) {
            Document userDocument = new Document();
            userDocument.append("name", user.getName());
            userDocument.append("surname", user.getSurname());
            userDocument.append("email", user.getEmail());
            userDocument.append("phone", user.getPhone());
            userDocument.append("_id", new ObjectId(user.getId()));
            userDocuments.add(userDocument);
        }

        return userDocuments;
    }

    private Order mapOrder(Document orderDocument) {
        Order order = new Order();
        order.setId(orderDocument.getObjectId("_id").toString());
        order.setDateTime(orderDocument.getDate("datetime").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        order.setStatus(Status.valueOf(orderDocument.getString("status")));
        order.setClothesInOrder(mapClothingDocuments(orderDocument.getList("clothing", Document.class)));
        order.setUsersInOrder(mapUserDocuments(orderDocument.getList("users", Document.class)));
        order.setDelivery(mapDelivery(orderDocument));

        return order;
    }

    private Document mapOrder(Order order) {
        Document orderDocument = new Document();
        orderDocument.append("datetime", LocalDateTime.from(order.getDateTime().atZone(ZoneId.systemDefault()).toInstant()));
        orderDocument.append("status", order.getStatus().toString());
        orderDocument.append("clothing", mapClothing(order.getClothesInOrder()));
        orderDocument.append("users", mapUsers(order.getUsersInOrder()));
        orderDocument.append("delivery", mapDelivery(order.getDelivery()));

        return orderDocument;
    }


    private List<Clothing> mapClothingDocuments(List<Document> clothingDocuments) {
        List<Clothing> clothes = new ArrayList<>();

        for (Document clothingDocument : clothingDocuments) {
            String id = clothingDocument.getObjectId("_id").toString();
            String name = clothingDocument.getString("name");
            Size size = Size.valueOf(clothingDocument.getString("size").toUpperCase());
            String color = clothingDocument.getString("color");
            Season season = Season.valueOf(clothingDocument.getString("season").toUpperCase());
            int amount = clothingDocument.getInteger("amount");
            BigDecimal actualPrice = convertDecimal128ToBigDecimal(clothingDocument.get("actual_price", Decimal128.class));
            Sex sex = Sex.valueOf(clothingDocument.getString("sex").toUpperCase());

            clothes.add(new Clothing.Builder(name, size, color, season, sex)
                    .setId(id)
                    .setAmount(amount)
                    .setActualPrice(actualPrice)
                    .build());
        }

        return clothes;
    }

    private BigDecimal convertDecimal128ToBigDecimal(Decimal128 decimal128) {
        return (decimal128 != null) ? decimal128.bigDecimalValue() : null;
    }

    private List<User> mapUserDocuments(List<Document> userDocuments) {
        List<User> users = new ArrayList<>();
        for (Document userDocument : userDocuments) {
            User user = new User();
            user.setId(userDocument.getObjectId("_id").toString());
            user.setName(userDocument.getString("name"));
            user.setSurname(userDocument.getString("surname"));
            user.setEmail(userDocument.getString("email"));
            users.add(user);
        }

        return users;
    }

    private Delivery mapDelivery(Document deliveryDocument) {
        Delivery delivery = new Delivery();
        Document deliveryObject = deliveryDocument.get("delivery", Document.class);

        if (deliveryObject != null) {
            delivery.setCity(deliveryObject.getString("city"));
            delivery.setStreet(deliveryObject.getString("street"));
            delivery.setHouseNumber(deliveryObject.getString("house_number"));
            Integer entrance = deliveryObject.getInteger("entrance");
            delivery.setEntrance(entrance != null ? entrance.intValue() : 0);
            Integer apartmentNumber = deliveryObject.getInteger("apartment_number");
            delivery.setApartmentNumber(apartmentNumber != null ? apartmentNumber.intValue() : 0);
        }

        return delivery;
    }

    private Document mapDelivery(Delivery delivery) {
        Document deliveryDocument = new Document();
        deliveryDocument.append("city", delivery.getCity());
        deliveryDocument.append("street", delivery.getStreet());
        deliveryDocument.append("house_number", delivery.getHouseNumber());
        deliveryDocument.append("entrance", delivery.getEntrance());
        deliveryDocument.append("apartment_number", delivery.getApartmentNumber());
        return deliveryDocument;
    }

}
