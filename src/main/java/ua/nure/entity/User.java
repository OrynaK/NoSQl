package ua.nure.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ua.nure.entity.enums.Role;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class User {
    private String id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String phone;
    private Role role;

    private User(Builder builder) {
        id = builder.id;
        name = builder.name;
        surname = builder.surname;
        email = builder.email;
        password = builder.password;
        phone = builder.phone;
        role = builder.role;
    }

    public static class Builder {
        private String id;
        private final String name;
        private final String surname;
        private final String email;
        private String password;
        private final String phone;
        private Role role = Role.USER;

        public Builder(String name, String surname, String email, String phone) {
            this.name = name;
            this.surname = surname;
            this.email = email;
            this.phone = phone;
        }

        public Builder setId(String userId) {
            this.id = userId;
            return this;
        }

        public Builder setRole(String role) {
            this.role = Role.valueOf(role);
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public User build() {
            if (name == null || surname == null || email == null || phone == null) {
                throw new IllegalStateException("Name, surname, email, phone are required to build a user");
            }
            return new User(this);
        }
    }
}
