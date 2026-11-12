
package edu.itc.shop.customer;

import edu.itc.shop.domain.Address;
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
    private Address billing;

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
            name = "zip",
            column = @Column(name = "ship_zip")
        )
    })
    private Address shipping;

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
        this.billing = billingAddress;
        this.shipping = shippingAddress;
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
        return billing;
    }

    public Address getShippingAddress() {
        return shipping;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Customer customer = (Customer) object;
        return id != null && id.equals(customer.id);
    }

    @Override
    public int hashCode() {
        return Customer.class.hashCode();
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", billing=" + billing +
                ", shipping=" + shipping +
                '}';
    }
}
