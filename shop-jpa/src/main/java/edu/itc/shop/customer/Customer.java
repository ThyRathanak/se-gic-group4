
package edu.itc.shop.customer;

import edu.itc.shop.common.Address;
import jakarta.persistence.*;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "customer_seq"
    )
    @SequenceGenerator(
        name = "customer_seq",
        sequenceName = "customer_seq",
        allocationSize = 50
    )
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Embedded
    private Address billingAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
            name = "street",
            column = @Column(name = "ship_street")
        ),
        @AttributeOverride(
            name = "city",
            column = @Column(name = "ship_city")
        ),
        @AttributeOverride(
            name = "country",
            column = @Column(name = "ship_country")
        )
    })
    private Address shippingAddress;

    protected Customer() {
    }

    public Customer(
            String name,
            String email,
            Address billingAddress,
            Address shippingAddress
    ) {
        this.name = name;
        this.email = email;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
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

    public Address getBillingAddress() {
        return billingAddress;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }
}
