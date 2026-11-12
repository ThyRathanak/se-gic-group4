package edu.itc.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.SequenceGenerator;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
    @SequenceGenerator(
        name = "product_seq_gen",
        sequenceName = "product_seq",
        allocationSize = 50
    )
    private Long id;

    @Column(unique = true)
    private String sku;

    private String name;

    @Embedded
    private Money price;

    @Enumerated(EnumType.STRING)
    private Category category;

    private int stock;

    @ManyToMany
    @JoinTable(
        name = "product_tag",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    protected Product() {
    }

    public Product(String sku, String name, Money price, Category category, int stock) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stock = stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product other = (Product) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Product.class.hashCode();
    }

    @Override
    public String toString() {
        return "Product[id=%s, sku=%s, name=%s, price=%s, category=%s, stock=%d]"
            .formatted(id, sku, name, price, category, stock);
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public Money getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public int getStock() {
        return stock;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void tag(Tag tag) {
        if (tags.add(tag)) {
            tag.getProducts().add(this);
        }
    }
}