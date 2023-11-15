package ua.nure.dao;

import ua.nure.dao.EntityDAO.ClothingDAO;
import ua.nure.dao.EntityDAO.OrderDAO;
import ua.nure.dao.EntityDAO.UserDAO;

public interface Factory {
    UserDAO getUserDAO();

    ClothingDAO getClothingDAO();

    OrderDAO getOrderDAO();
}
