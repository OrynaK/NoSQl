package ua.nure.dao.EntityDAOImpl.mysql;

import ua.nure.dao.EntityDAO.UserDAO;
import ua.nure.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLUserDAOImpl implements UserDAO {

    private final Connection con;

    public MySQLUserDAOImpl(Connection connection) {
        con = connection;
    }

    private static final String GET_USER_BY_ID = "SELECT * from user WHERE id=?";
    private static final String UPDATE = "UPDATE user SET name=?, surname=?, email=?, password=?, phone = ? WHERE id=?";
    private static final String GET_ALL_USERS = "SELECT * from user";
    private static final String ADD_USER = "INSERT INTO user (name, surname, password, email, phone) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_USER_IN_ORDER = "SELECT * FROM user_order WHERE user_id=?";
    private static final String DELETE = "DELETE FROM user WHERE id=?";

    @Override
    public String add(User user) {
        try (PreparedStatement ps = con.prepareStatement(ADD_USER, Statement.RETURN_GENERATED_KEYS)) {
            int k = 0;
            ps.setString(++k, user.getName());
            ps.setString(++k, user.getSurname());
            ps.setString(++k, user.getPassword());
            ps.setString(++k, user.getEmail());
            ps.setString(++k, user.getPhone());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(String.valueOf(keys.getInt(1)));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return user.getId();
    }

    @Override
    public User update(User user) {
        try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
            int k = 0;
            ps.setString(++k, user.getName());
            ps.setString(++k, user.getSurname());
            ps.setString(++k, user.getEmail());
            ps.setString(++k, user.getPassword());
            ps.setString(++k, user.getPhone());
            ps.setLong(++k, Long.parseLong(user.getId()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findById(user.getId());
    }


    @Override
    public void delete(String id) {
        try (PreparedStatement st = con.prepareStatement(FIND_USER_IN_ORDER)) {
            st.setLong(1, Long.parseLong(id));
            try (ResultSet resultSet = st.executeQuery()) {
                if (resultSet.next())
                    throw new SQLException("delete failed. " +
                            "To delete user, please, firstly delete order with this user");
                try (PreparedStatement statement = con.prepareStatement(DELETE)) {
                    statement.setLong(1, Long.parseLong(id));
                    statement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public User findById(String id) {
        User user = new User();
        try (PreparedStatement ps = con.prepareStatement(GET_USER_BY_ID)) {
            int k = 0;
            ps.setLong(++k, Long.parseLong(id));
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    user = mapUsers(resultSet);
                }
                return user;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> userList = new ArrayList<>();
        try (Statement st = con.createStatement()) {
            try (ResultSet rs = st.executeQuery(GET_ALL_USERS)) {
                while (rs.next()) {
                    userList.add(mapUsers(rs));
                }
                return userList;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private User mapUsers(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String surname = rs.getString("surname");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String role = rs.getString("role").toUpperCase();
        String phone = rs.getString("phone");
        return new User.Builder(name, surname, email, phone)
                .setId(id)
                .setRole(role)
                .setPassword(password)
                .build();
    }

    public List<User> findByMultipleKeys(String email, String password) {
        List<User> users = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM `user` WHERE email=? AND password=?")) {
            int k = 0;
            ps.setString(++k, email);
            ps.setString(++k, password);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUsers(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return users;
    }

}
