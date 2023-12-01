package ua.nure.dao.EntityDAO;

import org.bson.Document;
import ua.nure.dao.CRUDRepository;
import ua.nure.entity.Clothing;
import ua.nure.entity.User;
import ua.nure.entity.enums.Season;
import ua.nure.entity.enums.Size;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public interface ClothingDAO extends CRUDRepository<Clothing> {
    List<Clothing> getClothingBySize(Size size);

    void updateClothingAmount(String shoeId, int amount);

    List<Clothing> findByMultipleKeys(String name, Size size, String color);

    List<Document> aggregationShowClothing();

    List<Document> aggregateGroupBySize();
    List<Document> aggregationFilterBySeason(Season targetSeason);
    List<Document> aggregationAveragePricePerSize();
    List<Document> showClothing();
    List<Document> filterBySeason(Season targetSeason);
    List<Document> groupBySize();
    List<Document> averagePricePerSize();
}