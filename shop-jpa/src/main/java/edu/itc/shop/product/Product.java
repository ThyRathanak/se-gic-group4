
package edu.itc.shop.product;

import edu.itc.shop.domain.Money;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "product")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
  @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 50)
  private Long id;

  @Column(nullable = false, unique = true)
  private String sku;

  @Column(nullable = false)
  private String name;

  @Embedded
  private Money price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Category category;

  @Column(nullable = false)
  private int stock;

  @ManyToMany
  @JoinTable(name = "product_tag", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  protected Product() {
  }

  public Product(
      String sku,
      String name,
      Money price,
      Category category,
      int stock) {
    this.sku = sku;
    this.name = name;
    this.price = price;
    this.category = category;
    this.stock = stock;
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
    return Collections.unmodifiableSet(tags);
  }

  public void tag(Tag tag) {
    if (tags.add(tag)) {
      tag.addProduct(this);
    }
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    Product product = (Product) object;
    return id != null && id.equals(product.id);
  }

  @Override
  public int hashCode() {
    return Product.class.hashCode();
  }

  @Override
  public String toString() {
    return "Product{" +
        "id=" + id +
        ", sku='" + sku + '\'' +
        ", name='" + name + '\'' +
        ", price=" + price +
        ", category=" + category +
        ", stock=" + stock +
        '}';
  }
}
