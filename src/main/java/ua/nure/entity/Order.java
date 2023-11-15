package ua.nure.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import ua.nure.entity.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Order {
    private String id;
    private LocalDateTime dateTime;
    private Status status;
    private List<Clothing> clothesInOrder;
    private List<User> usersInOrder;
    private Delivery delivery;
    private String description;

    public Order() {
        clothesInOrder = new ArrayList<>();
        usersInOrder = new ArrayList<>();
    }

    public Order(List<Clothing> clothesInOrder, List<User> usersInOrder) {
        this.clothesInOrder = clothesInOrder;
        this.usersInOrder = usersInOrder;
    }

    public Order(Order.Builder builder) {
        this.id = builder.id;
        this.dateTime = builder.dateTime;
        this.status = builder.status;
        this.usersInOrder = builder.usersInOrder;
        this.clothesInOrder = builder.clothesInOrder;
        this.delivery = builder.delivery;
        this.description = builder.description;
    }

    public void addClothing(Clothing clothingOrder) {
        clothesInOrder.add(clothingOrder);
    }

    public void putUser(User userOrder) {
        usersInOrder.add(userOrder);
    }

    public static class Builder {
        private String id;
        private LocalDateTime dateTime;
        private Status status;
        private List<Clothing> clothesInOrder = new ArrayList<>();
        private List<User> usersInOrder = new ArrayList<>();
        private Delivery delivery;
        private String description;

        public Builder() {
        }

        public Order.Builder addClothing(Clothing clothingOrder) {
            clothesInOrder.add(clothingOrder);
            return this;
        }

        public Order.Builder putUser(User userOrder) {
            usersInOrder.add(userOrder);
            return this;
        }

        public Order.Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Order.Builder setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Order.Builder setStatus(Status status) {
            this.status = status;
            return this;
        }

        public Order.Builder setClothesInOrder(List<Clothing> clothesInOrder) {
            this.clothesInOrder = clothesInOrder;
            return this;
        }

        public Order.Builder setUsersInOrder(List<User> usersInOrder) {
            this.usersInOrder = usersInOrder;
            return this;
        }

        public Order.Builder setDelivery(Delivery delivery) {
            this.delivery = delivery;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Order build() {
            return new Order(this);
        }

        public String getId() {
            return id;
        }
    }
}
