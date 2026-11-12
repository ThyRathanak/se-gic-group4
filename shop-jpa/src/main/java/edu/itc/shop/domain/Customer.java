package edu.itc.shop.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_seq_gen")
    @SequenceGenerator(
        name = "customer_seq_gen",
        sequenceName = "customer_seq",
        allocationSize = 50
    )
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @Embedded
    private Address billing;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "ship_street")),
        @AttributeOverride(name = "city", column = @Column(name = "ship_city")),
        @AttributeOverride(name = "zip", column = @Column(name = "ship_zip"))
    })
    private Address shipping;

    protected Customer() {
    }

    public Customer(String name, String email, Address billing, Address shipping) {
        this.name = name;
        this.email = email;
        this.billing = billing;
        this.shipping = shipping;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Customer other = (Customer) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Customer.class.hashCode();
    }

    @Override
    public String toString() {
        return "Customer[id=%s, name=%s, email=%s]"
            .formatted(id, name, email);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Address getBilling() {
        return billing;
    }

    public Address getShipping() {
        return shipping;
    }
}