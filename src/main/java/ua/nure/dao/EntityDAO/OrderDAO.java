package ua.nure.dao.EntityDAO;

import ua.nure.dao.CRUDRepository;
import ua.nure.entity.Order;
import ua.nure.entity.User;
import ua.nure.entity.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDAO extends CRUDRepository<Order> {
    List<Order> getOrdersByUserId(String userId);
    void updateStatus(String orderId, Status status);
    List<Order> findByMultipleKeys(Status status, LocalDateTime dateTime);
}