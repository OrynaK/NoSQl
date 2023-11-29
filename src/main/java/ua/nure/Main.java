package ua.nure;

import ua.nure.dao.EntityDAO.ClothingDAO;
import ua.nure.dao.EntityDAO.OrderDAO;
import ua.nure.dao.EntityDAO.UserDAO;
import ua.nure.dao.Factory;
import ua.nure.dao.FactoryDAO;
import ua.nure.entity.Clothing;
import ua.nure.entity.Delivery;
import ua.nure.entity.Order;
import ua.nure.entity.User;
import ua.nure.entity.enums.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Factory factory = new FactoryDAO("mongodb");
        UserDAO userDAO = factory.getUserDAO();
        ClothingDAO clothingDAO = factory.getClothingDAO();
        OrderDAO orderDAO = factory.getOrderDAO();
        Factory factory2 = new FactoryDAO("mysql");
        ClothingDAO clothingDAO2 = factory2.getClothingDAO();
        UserDAO userDAO2 = factory2.getUserDAO();
        OrderDAO orderDAO2 = factory2.getOrderDAO();

        //delete
        List<Clothing> clothesDelete = clothingDAO2.findAll();
        for (Clothing clothing : clothesDelete) {
            clothingDAO2.delete(clothing.getId());
        }
        List<User> usersDelete = userDAO2.findAll();
        for (User user : usersDelete) {
            userDAO2.delete(user.getId());
        }
        List<Order> ordersDelete = orderDAO2.findAll();
        for (Order order : ordersDelete) {
            orderDAO2.delete(order.getId());
        }

        // add
        List<Clothing> clothes = clothingDAO.findAll();
        for (Clothing clothing : clothes) {
            clothingDAO2.add(clothing);
        }
        List<User> users = userDAO.findAll();
        for (User user : users) {
            userDAO2.add(user);
        }
        List<Order> orders = orderDAO.findAll();
        for (Order order : orders) {
            orderDAO2.addMigration(order);
        }

//        System.out.println("---TEST FIND BY ID---");
//        System.out.println("Clothing with id=6550fafe473c391a243b5149: " + clothingDAO.findById("6550fafe473c391a243b5149"));
//        System.out.println("User with id=6554ce327192941e8a6f042f: " + userDAO.findById("6554ce327192941e8a6f042f"));
//        System.out.println("Order with id=6555070bd50764e3c8bb7c7b: " + orderDAO.findById("6555070bd50764e3c8bb7c7b"));
//        Clothing insertClothing = clothingDAO.findById("6550fafe473c391a243b5149");
//        User insertUser = userDAO.findById("6554ce327192941e8a6f042f");
//        Order insertOrder = orderDAO.findById("6555070bd50764e3c8bb7c7b");
//
//
//        System.out.println(clothingDAO2.findAll());
//        clothingDAO2.add(insertClothing);
//        System.out.println(clothingDAO2.findAll());
//
//        System.out.println(userDAO2.findAll());
//        userDAO2.add(insertUser);
//        System.out.println(userDAO2.findAll());
//
//        List<Order> orders = orderDAO2.findAll();
//        System.out.println("Order:");
//        for (Order order : orders) {
//            System.out.println(order);
//        }
//        orderDAO2.addMigration(insertOrder);
//        orders = orderDAO2.findAll();
//        System.out.println("Order:");
//        for (Order order : orders) {
//            System.out.println(order);
//        }
//        System.out.println("---TEST FIND BY MULTIPLY KEYS---");
//        List<User> userList = userDAO.findByMultipleKeys("oleg@example.com", "пароль789");
//        System.out.println("Users:");
//        for (User user : userList) {
//            System.out.println(user);
//        }
//        List<Clothing> clothingList = clothingDAO.findByMultipleKeys("Футболка", Size.M, "Червона");
//        System.out.println("Clothing:");
//        for (Clothing clothing : clothingList) {
//            System.out.println(clothing);
//        }
//
//
//        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2023-11-15T16:20:14.125+00:00");
//        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
//        List<Order> orders = orderDAO.findByMultipleKeys(Status.DELIVERED, localDateTime);
//        System.out.println("Order:");
//        for (Order order : orders) {
//            System.out.println(order);
//        }
//
//
//        System.out.println("---TEST ADD---");
//        User newUser = new User.Builder("тест", "тест", "тест", "тест")
//                .setPassword("тест")
//                .build();
//        System.out.println("Вставлений користувач: " + userDAO.findById(userDAO.add(newUser)));
//
//        Clothing newClothing = new Clothing.Builder("тест", Size.XS, "тест", Season.WINTER, Sex.MALE)
//                .setAmount(1)
//                .setActualPrice(new BigDecimal(17))
//                .build();
//        System.out.println("Вставлена одежа: " + clothingDAO.findById(clothingDAO.add(newClothing)));
//
//        Delivery newDelivery = new Delivery.Builder("тест", "тест", "тест")
//                .setApartmentNumber(1)
//                .setEntrance(1)
//                .build();
//        Order newOrder = new Order.Builder()
//                .addClothing(clothingDAO.findById("6550fafe473c391a243b5149"))
//                .setStatus(Status.PROCESSING)
//                .putUser(userDAO.findById("6554ce327192941e8a6f042f"))
//                .setDelivery(newDelivery)
//                .setDateTime(LocalDateTime.now())
//                .build();
//
//        System.out.println("Вставлене замовлення: " + orderDAO.findById(orderDAO.add(newOrder)));
//
//
//        System.out.println("---TEST DELETE---");
//        System.out.println("-----------------");
//        List<User> users = userDAO.findAll();
//        System.out.println("Users before delete:");
//        for (User user : users) {
//            System.out.println(user);
//        }
//        userDAO.delete("65554cb19b9177707b385d72");
//        users = userDAO.findAll();
//        System.out.println("Users after delete:");
//        for (User user : users) {
//            System.out.println(user);
//        }
//        System.out.println("-----------------");
//        List<Clothing> clothes = clothingDAO.findAll();
//        System.out.println("Clothing before delete:");
//        for (Clothing clothing : clothes) {
//            System.out.println(clothing);
//        }
//        clothingDAO.delete("6554ce4ff98bd63d777c6b65");
//        clothes = clothingDAO.findAll();
//        System.out.println("Clothing after delete:");
//        for (Clothing clothing : clothes) {
//            System.out.println(clothing);
//        }
//        List<Order> orders = orderDAO.findAll();
//        System.out.println("-----------------");
//        System.out.println("Orders before delete:");
//        for (Order order : orders) {
//            System.out.println(order);
//        }
//        orderDAO.delete("65554cd887535c73df5b9243");
//        orders = orderDAO.findAll();
//        System.out.println("-----------------");
//        System.out.println("Orders after delete:");
//        for (Order order : orders) {
//            System.out.println(order);
//        }
//        System.out.println("---TEST UPDATE---");
//        System.out.println("User before update: ");
//        System.out.println(userDAO.findById("6554ce327192941e8a6f042f"));
//        User updatedUser = new User.Builder("апдейт", "Петров", "ivan@example.com",  "+380123456789")
//                .setPassword("пароль123")
//                .setId("6554ce327192941e8a6f042f")
//                .setRole(String.valueOf(Role.USER))
//                .build();
//        userDAO.update(updatedUser);
//        System.out.println("User after update: ");
//        System.out.println(userDAO.findById("6554ce327192941e8a6f042f"));
//
//        System.out.println("Clothing before update amount: ");
//        System.out.println(clothingDAO.findById("6550fafe473c391a243b5149"));
//        clothingDAO.updateClothingAmount(clothingDAO.findById("6550fafe473c391a243b5149").getId(), 2365);
//        System.out.println("Clothing after update amount: ");
//        System.out.println(clothingDAO.findById("6550fafe473c391a243b5149"));
//
//        System.out.println("Order before update status: ");
//        System.out.println(orderDAO.findById("6555070bd50764e3c8bb7c7b"));
//        orderDAO.updateStatus(orderDAO.findById("6555070bd50764e3c8bb7c7b").getId(), Status.PROCESSING);
//        System.out.println("Order after update status: ");
//        System.out.println(orderDAO.findById("6555070bd50764e3c8bb7c7b"));
    }
}
