package ua.nure.dao.EntityDAOImpl.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.ReadConcern;
import com.mongodb.WriteConcern;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import ua.nure.dao.EntityDAO.ClothingDAO;
import ua.nure.entity.Clothing;
import ua.nure.entity.User;
import ua.nure.entity.enums.Season;
import ua.nure.entity.enums.Sex;
import ua.nure.entity.enums.Size;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class MongoClothingDAOImpl implements ClothingDAO {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private final MongoDatabase connection;
    private final MongoCollection<Document> collection;
    private static final String COLLECTION_NAME = "clothing";

    public MongoClothingDAOImpl(MongoDatabase connection) {
        this.connection = connection;
        this.collection = connection.getCollection(COLLECTION_NAME);
    }

    @Override
    public String add(Clothing clothing) {
        Document clothingDoc = new Document()
                .append("name", clothing.getName())
                .append("size", clothing.getSize().toString().toUpperCase())
                .append("color", clothing.getColor())
                .append("season", clothing.getSeason().toString().toUpperCase())
                .append("amount", clothing.getAmount())
                .append("actual_price", clothing.getActualPrice())
                .append("sex", clothing.getSex().toString().toUpperCase());

        int retries = 0;
        boolean success = false;
        ObjectId id = null;

        while (retries < MAX_RETRIES && !success) {
            try {
                collection.withWriteConcern(WriteConcern.JOURNALED);
                collection.insertOne(clothingDoc);

                id = clothingDoc.getObjectId("_id");
                success = true;
            } catch (Exception e) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
                retries++;
                System.err.println("Error writing to database. Retrying... Attempt " + retries);
            }
        }

        if (!success) {
            System.err.println("Failed to write to database after " + MAX_RETRIES + " attempts. Aborting.");
            return null;
        }

        return id != null ? id.toString() : null;
    }



    @Override
    public Clothing update(Clothing clothing) {
        Document filter = new Document("_id", new ObjectId(clothing.getId()));
        Document updateDoc = new Document("$set", new Document()
                .append("name", clothing.getName())
                .append("size", clothing.getSize().toString().toUpperCase())
                .append("color", clothing.getColor())
                .append("season", clothing.getSeason().toString().toUpperCase())
                .append("amount", clothing.getAmount())
                .append("actual_price", clothing.getActualPrice())
                .append("sex", clothing.getSex().toString().toUpperCase()));

        collection.updateOne(filter, updateDoc);

        return findById(clothing.getId());
    }

    @Override
    public void delete(String id) {
        Document filter = new Document("_id", new ObjectId(id));
        collection.deleteOne(filter);
    }
    @Override
    public Clothing findById(String id) {
        Document filter = new Document("_id", new ObjectId(id));
        Document result = collection.find(filter).first();
        return (result != null) ? mapClothing(result) : null;
    }

    @Override
    public List<Clothing> findAll() {
        List<Clothing> clothingList = new ArrayList<>();
        collection.find().iterator().forEachRemaining(document -> clothingList.add(mapClothing(document)));
        return clothingList;
    }


    @Override
    public List<Clothing> findByMultipleKeys(String name, Size size, String color) {
        BasicDBObject query = new BasicDBObject();
        query.put("name", name);
        query.put("size", size.toString().toUpperCase());
        query.put("color", color);

        FindIterable<Document> documents = collection.find(query);
        List<Clothing> clothingList = new ArrayList<>();

        for (Document document : documents) {
            Clothing clothing = mapClothing(document);
            clothingList.add(clothing);
        }

        return clothingList;
    }
    @Override
    public List<Clothing> getClothingBySize(Size size) {
        Document filter = new Document("size", size.toString().toUpperCase());
        List<Clothing> clothingList = new ArrayList<>();
        collection.find(filter).iterator().forEachRemaining(document -> clothingList.add(mapClothing(document)));
        return clothingList;
    }
    @Override
    public void updateClothingAmount(String clothingId, int amount) {
        Document filter = new Document("_id", new ObjectId(clothingId));
        Document updateDoc = new Document("$set", new Document("amount", amount));

        collection.updateOne(filter, updateDoc);
    }

    private Clothing mapClothing(Document document) {
        String id = document.getObjectId("_id").toString();
        String name = document.getString("name");
        Size size = Size.valueOf(document.getString("size").toUpperCase());
        String color = document.getString("color");
        Season season = Season.valueOf(document.getString("season").toUpperCase());
        int amount = document.getInteger("amount");
        BigDecimal actualPrice = convertDecimal128ToBigDecimal(document.get("actual_price", Decimal128.class));
        Sex sex = Sex.valueOf(document.getString("sex").toUpperCase());

        return new Clothing.Builder(name, size, color, season, sex)
                .setId(id)
                .setAmount(amount)
                .setActualPrice(actualPrice)
                .build();
    }
    private BigDecimal convertDecimal128ToBigDecimal(Decimal128 decimal128) {
        return (decimal128 != null) ? decimal128.bigDecimalValue() : null;
    }
}
