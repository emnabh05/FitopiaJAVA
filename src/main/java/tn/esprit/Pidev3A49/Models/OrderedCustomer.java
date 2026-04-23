package tn.esprit.Pidev3A49.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderedCustomer {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final String city;
    private final int orderCount;
    private final BigDecimal totalSpent;
    private final LocalDateTime lastOrderAt;

    public OrderedCustomer(String firstName, String lastName, String email, String phone, String city,
                           int orderCount, BigDecimal totalSpent, LocalDateTime lastOrderAt) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.orderCount = orderCount;
        this.totalSpent = totalSpent;
        this.lastOrderAt = lastOrderAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public LocalDateTime getLastOrderAt() {
        return lastOrderAt;
    }
}
