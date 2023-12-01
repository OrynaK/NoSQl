package ua.nure.dao.EntityDAOImpl.mysql;

import org.bson.Document;
import ua.nure.dao.EntityDAO.OrderDAO;
import ua.nure.entity.*;
import ua.nure.entity.enums.*;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class MySQLOrderDAOImpl implements OrderDAO {

    private Connection con;

    public MySQLOrderDAOImpl(Connection connection) {
        con = connection;
    }

    private static final String INSERT_ORDER = "INSERT INTO `order` (datetime, status) VALUES (DEFAULT,DEFAULT)";
    private static final String UPDATE_STATUS = "UPDATE `order` SET status=? WHERE id=?";
    private static final String INSERT_CLOTHING_ORDER = "INSERT INTO `clothing_order` (clothing_id, order_id, amount, current_price) VALUES (?, ?, ?, ?)";
    private static final String INSERT_ORDER_USER = "INSERT INTO `user_order` (order_id, user_id, description, datetime) VALUES (?, ?, ?, DEFAULT)";
    private static final String GET_ORDERS = "SELECT * from `order`";
    private static final String GET_ORDER_BY_ID = "SELECT * from `order` WHERE id=?";
    private static final String GET_CLOTHING_ORDER = "SELECT * FROM `clothing_order` WHERE order_id=?";
    private static final String GET_USER_ORDER = "SELECT * FROM `user_order` WHERE order_id=?";
    private static final String GET_ORDER_FROM_USER_ORDER = "SELECT order_id FROM `user_order` WHERE user_id=? ORDER BY order_id DESC";
    private static final String GET_ROLE = "SELECT role FROM `user` WHERE id=?";
    private static final String DELETE = "DELETE FROM `order` WHERE id=?";
    private static final String DELETE_DELIVERY = "DELETE FROM delivery WHERE order_id=?";

    @Override
    public void delete(String orderId) {
        try {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM clothing_order WHERE order_id=?")) {
                ps.setLong(1, Long.parseLong(orderId));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("delete failed." +
                                "To delete order, please, firstly delete clothing_order with this order");
                    } else {
                        try (PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM user_order WHERE order_id=?")) {
                            preparedStatement.setLong(1, Long.parseLong(orderId));
                            try (ResultSet rst = ps.executeQuery()) {
                                if (rst.next()) {
                                    throw new SQLException("delete failed." +
                                            "To delete order, please, firstly delete user_order with this order");
                                } else {
                                    try (PreparedStatement statement = con.prepareStatement(DELETE)) {
                                        statement.setLong(1, Long.parseLong(orderId));
                                        if (statement.executeUpdate() > 0) {
                                            try (PreparedStatement pst = con.prepareStatement(DELETE_DELIVERY)) {
                                                pst.setLong(1, Long.parseLong(orderId));
                                                pst.executeUpdate();
                                            }
                                            con.commit();
                                        } else {
                                            con.rollback();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public String add(Order order) {
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(INSERT_ORDER, Statement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                ResultSet generatedKeys = ps.getGeneratedKeys();
                try (PreparedStatement s = con.prepareStatement(INSERT_CLOTHING_ORDER)) {
                    if (generatedKeys.next()) {
                        con.commit();
                        order.setId(String.valueOf(generatedKeys.getLong(1)));
                        for (Clothing clothingOrder : order.getClothesInOrder()) {
                            int k = 0;
                            s.setLong(++k, Long.parseLong(clothingOrder.getId()));
                            s.setLong(++k, Long.parseLong(order.getId()));
                            s.setInt(++k, clothingOrder.getAmount());
                            s.setBigDecimal(++k, clothingOrder.getActualPrice());
                            s.addBatch();
                        }
                        s.executeBatch();
                        try (PreparedStatement prs = con.prepareStatement(INSERT_ORDER_USER)) {
                            for (User user : order.getUsersInOrder()) {
                                prs.setLong(1, Long.parseLong(order.getId()));
                                prs.setLong(2, Long.parseLong(user.getId()));
                                prs.setString(3, order.getDescription());
                                prs.addBatch();
                            }
                            try (PreparedStatement pps = con.prepareStatement("INSERT INTO delivery (order_id, city, street, house_number, entrance, apartment_number) values (?, ?, ?, ?, ?, ?)")) {
                                pps.setLong(1, Long.parseLong(order.getId()));
                                pps.setString(2, order.getDelivery().getCity());
                                pps.setString(3, order.getDelivery().getStreet());
                                pps.setString(4, order.getDelivery().getHouseNumber());
                                pps.setInt(5, order.getDelivery().getEntrance());
                                pps.setInt(6, order.getDelivery().getApartmentNumber());

                                pps.executeUpdate();
                            }
                            if (prs.executeBatch().length != 0) {
                                con.commit();
                            } else {
                                con.rollback();
                            }
                        }
                    } else {
                        con.rollback();
                    }
                }
            }
            return order.getId();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String addMigration(Order order) {
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(INSERT_ORDER, Statement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                ResultSet generatedKeys = ps.getGeneratedKeys();
                try (PreparedStatement s = con.prepareStatement(INSERT_CLOTHING_ORDER)) {
                    if (generatedKeys.next()) {
                        con.commit();
                        order.setId(String.valueOf(generatedKeys.getLong(1)));
                        for (Clothing clothingOrder : order.getClothesInOrder()) {
                            int k = 0;
                            s.setLong(++k, Long.parseLong(findClothingByMultipleKeys(clothingOrder.getName(), clothingOrder.getSize(), clothingOrder.getColor(), clothingOrder.getSeason(), clothingOrder.getAmount(), clothingOrder.getActualPrice(), clothingOrder.getSex())));
                            s.setLong(++k, Long.parseLong(order.getId()));
                            s.setInt(++k, clothingOrder.getAmount());
                            s.setBigDecimal(++k, clothingOrder.getActualPrice());
                            s.addBatch();
                        }
                        s.executeBatch();
                        try (PreparedStatement prs = con.prepareStatement(INSERT_ORDER_USER)) {
                            for (User user : order.getUsersInOrder()) {
                                prs.setLong(1, Long.parseLong(order.getId()));
                                prs.setLong(2, Long.parseLong(findUserByMultipleKeys(user.getEmail())));
                                prs.setString(3, order.getDescription());
                                prs.addBatch();
                            }
                            try (PreparedStatement pps = con.prepareStatement("INSERT INTO delivery (order_id, city, street, house_number, entrance, apartment_number) values (?, ?, ?, ?, ?, ?)")) {
                                pps.setLong(1, Long.parseLong(order.getId()));
                                pps.setString(2, order.getDelivery().getCity());
                                pps.setString(3, order.getDelivery().getStreet());
                                pps.setString(4, order.getDelivery().getHouseNumber());
                                pps.setInt(5, order.getDelivery().getEntrance());
                                pps.setInt(6, order.getDelivery().getApartmentNumber());

                                pps.executeUpdate();
                            }
                            if (prs.executeBatch().length != 0) {
                                con.commit();
                            } else {
                                con.rollback();
                            }
                        }
                    } else {
                        con.rollback();
                    }
                }
            }
            return order.getId();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Document> aggregateOrderTotalByUser() {
        return null;
    }

    @Override
    public List<Document> getOrderTotalByUser() {
        return null;
    }

    @Override
    public Order findById(String orderId) {
        Order.Builder orderBuilder = new Order.Builder();
        try (PreparedStatement ps = con.prepareStatement(GET_ORDER_BY_ID)) {
            ps.setLong(1, Long.parseLong(orderId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    orderBuilder.setId(String.valueOf(rs.getInt("id")))
                            .setDateTime(rs.getTimestamp("datetime").toLocalDateTime())
                            .setStatus(Status.valueOf(rs.getString("status").toUpperCase()));
                    loadUserOrders(orderBuilder, con);
                    loadClothingOrders(orderBuilder, con);
                    loadDelivery(orderBuilder, con);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return orderBuilder.build();
    }

    private void loadDelivery(Order.Builder orderBuilder, Connection con) throws SQLException {
        try (PreparedStatement prs = con.prepareStatement("SELECT * FROM delivery WHERE order_id=?")) {
            prs.setLong(1, Long.parseLong(orderBuilder.getId()));
            try (ResultSet resultSet = prs.executeQuery()) {
                while (resultSet.next()) {
                    orderBuilder.setDelivery(mapDelivery(resultSet));
                }
            }
        }
    }

    public Delivery mapDelivery(ResultSet rs) throws SQLException {
        String orderId = String.valueOf(rs.getLong("order_id"));
        String city = rs.getString("city");
        String street = rs.getString("street");
        String houseNumber = rs.getString("house_number");
        int entrance = rs.getInt("entrance");
        int number = rs.getInt("apartment_number");
        return new Delivery.Builder(city, street, houseNumber).setEntrance(entrance).setApartmentNumber(number).setOrder_id(orderId).build();
    }

    private void loadUserOrders(Order.Builder orderBuilder, Connection con) throws SQLException {
        try (PreparedStatement prs = con.prepareStatement(GET_USER_ORDER)) {
            prs.setLong(1, Long.parseLong(orderBuilder.getId()));
            try (ResultSet resultSet = prs.executeQuery()) {
                while (resultSet.next()) {
                    long userId = resultSet.getLong("user_id");
                    User userOrder = findUserById(userId, con);
                    orderBuilder.putUser(userOrder);
                }
            }
        }
    }

    private User findUserById(long userId, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `user` WHERE id=?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }
        return null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        String id = String.valueOf(rs.getInt("id"));
        String name = rs.getString("name");
        String surname = rs.getString("surname");
        String email = rs.getString("email");
        String role = rs.getString("role").toUpperCase();
        String phone = rs.getString("phone");
        return new User.Builder(name, surname, email, phone).setId(id).setRole(role).build();
    }

    private void loadClothingOrders(Order.Builder orderBuilder, Connection con) throws SQLException {
        try (PreparedStatement prs = con.prepareStatement(GET_CLOTHING_ORDER)) {
            prs.setLong(1, Long.parseLong(orderBuilder.getId()));
            try (ResultSet resultSet = prs.executeQuery()) {
                while (resultSet.next()) {
                    String clothingId = String.valueOf(resultSet.getLong("clothing_id"));
                    int amount = resultSet.getInt("amount");
                    BigDecimal currentPrice = resultSet.getBigDecimal("current_price");
                    Clothing clothingOrder = (findClothingById(clothingId, con));
                    orderBuilder.addClothing(new Clothing.Builder(clothingOrder.getName(), clothingOrder.getSize(), clothingOrder.getColor(), clothingOrder.getSeason(), clothingOrder.getSex())
                            .setAmount(amount)
                            .setId(clothingId)
                            .setActualPrice(currentPrice)
                            .build());
                }
            }
        }
    }

    private Clothing findClothingById(String clothingId, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `clothing` WHERE id=?")) {
            ps.setLong(1, Long.parseLong(clothingId));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapClothing(rs);
                }
            }
        }
        return null;
    }

    private Clothing mapClothing(ResultSet rs) throws SQLException {
        String id = String.valueOf(rs.getLong("id"));
        String name = rs.getString("name");
        Size size = Size.valueOf(rs.getString("size").toUpperCase());
        String color = rs.getString("color");
        Season season = Season.valueOf(rs.getString("season").toUpperCase());
        int amount = rs.getInt("amount");
        BigDecimal actualPrice = rs.getBigDecimal("actual_price");
        Sex sex = Sex.valueOf(rs.getString("sex").toUpperCase());
        return new Clothing.Builder(name, size, color, season, sex)
                .setId(id)
                .setActualPrice(actualPrice)
                .setAmount(amount)
                .build();
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        try (Statement ps = con.createStatement()) {
            try (ResultSet rs = ps.executeQuery(GET_ORDERS)) {
                while (rs.next()) {
                    Order.Builder orderBuilder = new Order.Builder();
                    orderBuilder.setId(String.valueOf(rs.getInt("id")))
                            .setDateTime(rs.getTimestamp("datetime").toLocalDateTime())
                            .setStatus(Status.valueOf(rs.getString("status").toUpperCase()));
                    loadUserOrders(orderBuilder, con);
                    loadClothingOrders(orderBuilder, con);
                    loadDelivery(orderBuilder, con);
                    orders.add(orderBuilder.build());
                }
                return orders;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String findUserByMultipleKeys(String email) {
        User user = new User();

        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `user` WHERE email=?")) {
            int k = 0;
            ps.setString(++k, email);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    user = mapUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return user.getId();
    }

    public String findClothingByMultipleKeys(String name, Size size, String color, Season season, int amount, BigDecimal actual_price, Sex sex) {
        Clothing clothing = new Clothing();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `clothing` WHERE name=? AND size=? AND color = ? AND season = ? AND amount = ? AND actual_price = ? AND sex = ?")) {
            int k = 0;
            ps.setString(++k, name);
            ps.setString(++k, String.valueOf(size).toUpperCase());
            ps.setString(++k, color);
            ps.setString(++k, String.valueOf(season).toUpperCase());
            ps.setInt(++k, amount);
            ps.setBigDecimal(++k, actual_price);
            ps.setString(++k, String.valueOf(sex).toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clothing = mapClothing(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return clothing.getId();
    }

    private Role getRole(Long id) {
        Role role = null;
        try (PreparedStatement ps = con.prepareStatement(GET_ROLE)) {
            int k = 0;
            ps.setLong(++k, id);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    role = Role.valueOf(resultSet.getString("role").toUpperCase());
                }
                return role;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Order> getOrdersByUserId(String userId) {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(GET_ORDER_FROM_USER_ORDER)) {
            int k = 0;
            ps.setLong(++k, Long.parseLong(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(findById(String.valueOf(rs.getLong(1))));
                }
                return orders;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final String UPDATE_ORDER = "UPDATE `order` SET datetime=?, status=? WHERE id=?";
    private static final String DELETE_ORDER_USERS = "DELETE FROM `user_order` WHERE order_id=?";
    private static final String DELETE_ORDER_CLOTHING = "DELETE FROM `clothing_order` WHERE order_id=?";
    private static final String UPDATE_DELIVERY = "UPDATE `delivery` SET city=?, street=?, house_number=?, entrance=?, apartment_number=? WHERE order_id=?";

    @Override
    public Order update(Order entity) {
        try {
            con.setAutoCommit(false);

            // Оновити інформацію про замовлення
            updateOrderInfo(entity, con);

            // Оновити інформацію про користувачів у замовленні
            updateOrderUsers(entity, con);

            // Оновити інформацію про одяг у замовленні
            updateOrderClothing(entity, con);

            // Оновити інформацію про доставку
            updateOrderDelivery(entity, con);

            con.commit();
            return findById(entity.getId());
        } catch (SQLException ex) {
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException(rollbackEx);
            }
            throw new RuntimeException(ex);
        }
    }

    private void updateOrderInfo(Order order, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(UPDATE_ORDER)) {
            ps.setLong(3, Long.parseLong(order.getId()));
            ps.setTimestamp(1, Timestamp.valueOf(order.getDateTime()));
            ps.setString(2, order.getStatus().toString());
            ps.executeUpdate();
        }
    }

    private void updateOrderUsers(Order order, Connection con) throws SQLException {
        // Видалити всі записи про користувачів для цього замовлення
        deleteOrderUsers(Long.parseLong(order.getId()), con);

        // Вставити нові записи про користувачів
        insertOrderUsers(order, con);
    }

    private void deleteOrderUsers(long orderId, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_ORDER_USERS)) {
            ps.setLong(1, orderId);
            ps.executeUpdate();
        }
    }

    private void insertOrderUsers(Order order, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT_ORDER_USER)) {
            for (User user : order.getUsersInOrder()) {
                ps.setLong(1, Long.parseLong(order.getId()));
                ps.setLong(2, Long.parseLong(user.getId()));
                ps.setString(3, order.getDescription());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void updateOrderClothing(Order order, Connection con) throws SQLException {
        // Видалити всі записи про одяг для цього замовлення
        deleteOrderClothing(Long.parseLong(order.getId()), con);

        // Вставити нові записи про одяг
        insertOrderClothing(order, con);
    }

    private void deleteOrderClothing(long orderId, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(DELETE_ORDER_CLOTHING)) {
            ps.setLong(1, orderId);
            ps.executeUpdate();
        }
    }

    private void insertOrderClothing(Order order, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT_CLOTHING_ORDER)) {
            for (Clothing clothing : order.getClothesInOrder()) {
                ps.setLong(1, Long.parseLong(clothing.getId()));
                ps.setLong(2, Long.parseLong(order.getId()));
                ps.setInt(3, clothing.getAmount());
                ps.setBigDecimal(4, clothing.getActualPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void updateOrderDelivery(Order order, Connection con) throws SQLException {
        // Оновити інформацію про доставку
        updateDelivery(order.getDelivery(), con);
    }

    private void updateDelivery(Delivery delivery, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(UPDATE_DELIVERY)) {
            ps.setString(1, delivery.getCity());
            ps.setString(2, delivery.getStreet());
            ps.setString(3, delivery.getHouseNumber());
            ps.setInt(4, delivery.getEntrance());
            ps.setInt(5, delivery.getApartmentNumber());
            ps.setLong(6, Long.parseLong(delivery.getOrder_id()));
            ps.executeUpdate();
        }
    }


    @Override
    public void updateStatus(String orderId, Status status) {
        try (PreparedStatement ps = con.prepareStatement(UPDATE_STATUS)) {
            int k = 0;
            ps.setString(++k, String.valueOf(status));
            ps.setLong(++k, Long.parseLong(orderId));
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Order> findByMultipleKeys(Status status, LocalDateTime dateTime) {
        List<Order> orders = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `order` WHERE status = ? AND datetime=?")) {
            int k = 0;
            ps.setString(++k, String.valueOf(status).toUpperCase());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = dateFormat.format(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
            ps.setString(++k, formattedDateTime);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(findById(String.valueOf(rs.getLong("id"))));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return orders;
    }


}

