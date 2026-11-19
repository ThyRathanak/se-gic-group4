
package edu.itc.shop.api.dto;

import java.math.BigDecimal;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import edu.itc.shop.domain.Product;

@XmlRootElement(name = "product")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProductDto {

    @XmlAttribute
    public long id;

    public String name;
    public String sku;
    public BigDecimal price;
    public int stock;

    public ProductDto() {
    }

    public static ProductDto from(Product p) {
        ProductDto dto = new ProductDto();

        dto.id = p.id();
        dto.name = p.name();
        dto.sku = p.sku();
        dto.price = p.price();
        dto.stock = p.stock();

        return dto;
    }

    public Product toProduct(long id, int version) {
        return new Product(id, name, sku, price, stock, version);
    }
}
