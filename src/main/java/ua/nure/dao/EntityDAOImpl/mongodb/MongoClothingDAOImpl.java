package ua.nure.dao.EntityDAOImpl.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import ua.nure.dao.EntityDAO.ClothingDAO;
import ua.nure.entity.Clothing;
import ua.nure.entity.enums.Season;
import ua.nure.entity.enums.Sex;
import ua.nure.entity.enums.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;


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
//                collection.withWriteConcern(WriteConcern.JOURNALED);
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
    public List<Document> aggregationShowClothing() {
        List<Document> results = new ArrayList<>();

        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document ("_id", 0).append("name", 1)
                        .append("size", 1)
                        .append("color", 1)
                        .append("season", 1)
                        .append("sex", 1))
        );

        AggregateIterable<Document> resultIterable = collection.aggregate(pipeline);

        for (Document document : resultIterable) {
            results.add(document);
        }

        return results;
    }
    public List<Document> showClothing() {
        List<Document> results = new ArrayList<>();

        FindIterable<Document> clothingIterable = collection.find();

        for (Document clothing : clothingIterable) {
            Document resultClothing = new Document("_id", 0)
                    .append("name", clothing.getString("name"))
                    .append("size", clothing.getString("size"))
                    .append("color", clothing.getString("color"))
                    .append("season", clothing.getString("season"))
                    .append("sex", clothing.getString("sex"));

            results.add(resultClothing);
        }

        return results;
    }

    public List<Document> aggregationFilterBySeason(Season targetSeason) {
        List<Document> results = new ArrayList<>();

        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("season", targetSeason.toString())),  // Фільтр за умовою
                new Document("$project", new Document("_id", 0))  // Виключити поле "_id" з результатів
        );

        AggregateIterable<Document> resultIterable = collection.aggregate(pipeline);

        for (Document document : resultIterable) {
            results.add(document);
        }

        return results;
    }
    public List<Document> filterBySeason(Season targetSeason) {
        List<Document> results = new ArrayList<>();

        FindIterable<Document> clothingIterable = collection.find(eq("season", targetSeason.toString()));

        for (Document clothing : clothingIterable) {
            clothing.remove("_id"); // Видалити поле "_id" з результатів
            results.add(clothing);
        }

        return results;
    }

    public List<Document> aggregateGroupBySize() {
        List<Document> results = new ArrayList<>();

        List<Document> pipeline = Arrays.asList(
                new Document("$group", new Document("_id", "$size").append("totalAmount", new Document("$sum", "$amount")))
        );

        AggregateIterable<Document> resultIterable = collection.aggregate(pipeline);

        for (Document document : resultIterable) {
            results.add(document);
        }

        return results;
    }
    public List<Document> groupBySize() {
        List<Document> results = new ArrayList<>();

        DistinctIterable<String> distinctSizes = collection.distinct("size", String.class);

        for (String size : distinctSizes) {
            int totalAmount = (int) collection.countDocuments(eq("size", size));
            Document resultDocument = new Document("_id", size).append("totalAmount", totalAmount);
            results.add(resultDocument);
        }

        return results;
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

    public List<Document> aggregationAveragePricePerSize() {
        List<Document> results = new ArrayList<>();

        List<Document> pipeline = Arrays.asList(
                new Document("$group", new Document("_id", "$size")
                        .append("averagePrice", new Document("$avg", "$actual_price"))
                )
        );

        AggregateIterable<Document> resultIterable = collection.aggregate(pipeline);

        for (Document document : resultIterable) {
            results.add(document);
        }

        return results;
    }
    public List<Document> averagePricePerSize() {
        List<Document> results = new ArrayList<>();

        DistinctIterable<String> distinctSizes = collection.distinct("size", String.class);

        for (String size : distinctSizes) {
            List<Document> sizeDocuments = collection.find(eq("size", size)).into(new ArrayList<>());

            double totalActualPrice = 0.0;
            int documentCount = sizeDocuments.size();

            for (Document document : sizeDocuments) {
                totalActualPrice += ((Number) document.get("actual_price")).doubleValue();
            }

            double averagePrice = documentCount > 0 ? totalActualPrice / documentCount : 0.0;

            Document resultDocument = new Document("_id", size).append("averagePrice", averagePrice);
            results.add(resultDocument);
        }

        return results;
    }
}
