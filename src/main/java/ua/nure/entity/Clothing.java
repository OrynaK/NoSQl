package ua.nure.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.nure.entity.enums.Season;
import ua.nure.entity.enums.Sex;
import ua.nure.entity.enums.Size;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Clothing {
    private String id;
    private String name;
    private Size size;
    private String color;
    private Season season;
    private int amount;
    private BigDecimal actualPrice;
    private Sex sex;

    public Clothing(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.size = builder.size;
        this.color = builder.color;
        this.season = builder.season;
        this.amount = builder.amount;
        this.actualPrice = builder.actualPrice;
        this.sex = builder.sex;
    }

    public static class Builder {
        private String id;
        private final String name;
        private final Size size;
        private final String color;
        private final Season season;
        private int amount;
        private BigDecimal actualPrice;
        private final Sex sex;

        public Builder(String name, Size size, String color, Season season, Sex sex) {
            this.name = name;
            this.size = size;
            this.color = color;
            this.season = season;
            this.sex = sex;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder setActualPrice(BigDecimal actualPrice) {
            this.actualPrice = actualPrice;
            return this;
        }

        public Clothing build() {
            if (name == null || size == null || color == null || season == null || amount < 0 || actualPrice == null || sex == null) {
                throw new IllegalStateException("Can`t create Clothing without enough parameters");
            }
            return new Clothing(this);
        }
    }
}
