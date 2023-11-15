package ua.nure.dao.EntityDAO;

import ua.nure.dao.CRUDRepository;
import ua.nure.entity.Clothing;
import ua.nure.entity.User;
import ua.nure.entity.enums.Size;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public interface ClothingDAO extends CRUDRepository<Clothing> {
    List<Clothing> getClothingBySize(Size size);
    void updateClothingAmount(String shoeId, int amount);
    List<Clothing> findByMultipleKeys(String name, Size size, String color);

}