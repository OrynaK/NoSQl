package ua.nure.dao.EntityDAOImpl.mysql;

import org.bson.Document;
import ua.nure.dao.EntityDAO.ClothingDAO;
import ua.nure.entity.Clothing;
import ua.nure.entity.User;
import ua.nure.entity.enums.Season;
import ua.nure.entity.enums.Sex;
import ua.nure.entity.enums.Size;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLClothingDAOImpl implements ClothingDAO {
    private static final String GET_ALL_CLOTHES = "SELECT * FROM clothing";
    private static final String UPDATE = "UPDATE clothing SET name=?, size=?, color=?, season=?, amount=?, actual_price=?, sex=? WHERE id=?";
    private static final String DELETE = "DELETE FROM clothing WHERE id=?";
    private static final String ADD_CLOTHING = "INSERT INTO clothing (name, size, color, season, amount, actual_price, sex) VALUES(?,?,?,?,?,?,?)";
    private static final String FIND_CLOTHING_IN_ORDER = "SELECT * FROM clothing_order WHERE clothing_id=?";
    private static final String FIND_BY_ID = "SELECT * FROM clothing WHERE id=?";
    private static final String GET_CLOTHING_BY_SIZE = "SELECT * FROM clothing WHERE size=?";
    private static final String UPDATE_AMOUNT = "UPDATE clothing SET amount=? WHERE id=?";
    private final Connection con;

    public MySQLClothingDAOImpl(Connection connection) {
        this.con = connection;
    }

    @Override
    public String add(Clothing clothing) {
        try (PreparedStatement ps = con.prepareStatement(ADD_CLOTHING, Statement.RETURN_GENERATED_KEYS)) {
            int k = 0;
            ps.setString(++k, clothing.getName());
            ps.setString(++k, clothing.getSize().toString().toUpperCase());
            ps.setString(++k, clothing.getColor());
            ps.setString(++k, clothing.getSeason().toString().toUpperCase());
            ps.setInt(++k, clothing.getAmount());
            ps.setBigDecimal(++k, clothing.getActualPrice());
            ps.setString(++k, clothing.getSex().toString().toUpperCase());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    clothing.setId(String.valueOf(keys.getLong(1)));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return clothing.getId();
    }

    @Override
    public Clothing update(Clothing clothing) {
        try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
            int k = 0;
            ps.setString(++k, clothing.getName());
            ps.setString(++k, clothing.getSize().toString().toUpperCase());
            ps.setString(++k, clothing.getColor());
            ps.setString(++k, clothing.getSeason().toString().toUpperCase());
            ps.setInt(++k, clothing.getAmount());
            ps.setBigDecimal(++k, clothing.getActualPrice());
            ps.setString(++k, clothing.getSex().toString().toUpperCase());
            ps.setLong(++k, Long.parseLong(clothing.getId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findById(clothing.getId());
    }

    @Override
    public void delete(String id) {
        try (PreparedStatement st = con.prepareStatement(FIND_CLOTHING_IN_ORDER)) {
            st.setLong(1, Long.parseLong(id));
            try (ResultSet resultSet = st.executeQuery()) {
                if (resultSet.next())
                    throw new SQLException("delete failed. " +
                            "To delete clothing, please, firstly delete order with this clothing");
                try (PreparedStatement statement = con.prepareStatement(DELETE)) {
                    statement.setLong(1, Long.parseLong(id));
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Clothing findById(String  id) {
        try (PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, Long.parseLong(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapClothing(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Clothing> findAll() {
        List<Clothing> clothingList = new ArrayList<>();
            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery(GET_ALL_CLOTHES)) {
                    while (rs.next()) {
                        clothingList.add(mapClothing(rs));
                    }
                    return clothingList;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }

    private Clothing mapClothing(ResultSet rs) throws SQLException {

        String id = rs.getString("id");
        String name = rs.getString("name");
        Size size = Size.valueOf(rs.getString("size").toUpperCase());
        String color = rs.getString("color");
        Season season = Season.valueOf(rs.getString("season").toUpperCase());
        int amount = rs.getInt("amount");
        BigDecimal actualPrice= rs.getBigDecimal("actual_price");
        Sex sex = Sex.valueOf(rs.getString("sex").toUpperCase());
        return new Clothing.Builder(name,size,color, season, sex)
                .setId(id)
                .setAmount(amount)
                .setActualPrice(actualPrice)
                .build();
    }

    @Override
    public List<Clothing> getClothingBySize(Size size) {
        List<Clothing> clothingList = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(GET_CLOTHING_BY_SIZE)) {
                int k = 0;
                ps.setString(++k, size.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        clothingList.add(mapClothing(rs));
                    }
                    return clothingList;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }

    @Override
    public void updateClothingAmount(String clothingId, int amount)  {
            try (PreparedStatement ps = con.prepareStatement(UPDATE_AMOUNT)) {
                int k = 0;
                ps.setInt(++k, amount);
                ps.setLong(++k, Long.parseLong(clothingId));
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }

    @Override
    public List<Clothing> findByMultipleKeys(String name, Size size, String color) {
        List<Clothing> clothingList = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `clothing` WHERE name=? AND size=? AND color = ?")) {
            int k = 0;
            ps.setString(++k, name);
            ps.setString(++k, String.valueOf(size).toUpperCase());
            ps.setString(++k, color);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clothingList.add(mapClothing(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return clothingList;
    }

    @Override
    public List<Document> aggregationShowClothing() {
        return null;
    }

    @Override
    public List<Document> aggregateGroupBySize() {
        return null;
    }

    @Override
    public List<Document> aggregationFilterBySeason(Season targetSeason) {
        return null;
    }

    @Override
    public List<Document> aggregationAveragePricePerSize() {
        return null;
    }

    @Override
    public List<Document> showClothing() {
        return null;
    }

    @Override
    public List<Document> filterBySeason(Season targetSeason) {
        return null;
    }

    @Override
    public List<Document> groupBySize() {
        return null;
    }

    @Override
    public List<Document> averagePricePerSize() {
        return null;
    }

}
