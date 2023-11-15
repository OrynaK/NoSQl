package ua.nure.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Delivery {
    public String order_id;
    private String city;
    private String street;
    private String houseNumber;
    private int entrance;
    private int apartmentNumber;


    public Delivery(Builder builder) {
        this.order_id = builder.order_id;
        this.city = builder.city;
        this.street = builder.street;
        this.houseNumber = builder.houseNumber;
        this.entrance = builder.entrance;
    }

    public static class Builder {
        public String order_id;
        private final String city;
        private final String street;
        private final String houseNumber;
        private int entrance = 0;
        private int apartmentNumber = 0;

        public Builder(String city, String street, String houseNumber) {
            this.city = city;
            this.street = street;
            this.houseNumber = houseNumber;
        }

        public Builder setOrder_id(String order_id) {
            this.order_id = order_id;
            return this;
        }

        public Builder setEntrance(int entrance) {
            this.entrance = entrance;
            return this;
        }

        public Builder setApartmentNumber(int apartmentNumber) {
            this.apartmentNumber = apartmentNumber;
            return this;
        }

        public Delivery build() {
            if (city == null || street == null || houseNumber == null) {
                throw new IllegalStateException("Can`t create Delivery without enough parameters");
            }
            return new Delivery(this);
        }
    }


}
