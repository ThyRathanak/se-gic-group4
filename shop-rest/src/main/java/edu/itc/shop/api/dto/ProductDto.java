package edu.itc.shop.api.dto;

import edu.itc.shop.domain.Product;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;

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

    public static ProductDto from(Product product) {
        ProductDto dto = new ProductDto();
        dto.id = product.id();
        dto.name = product.name();
        dto.sku = product.sku();
        dto.price = product.price();
        dto.stock = product.stock();
        return dto;
    }

    public Product toProduct(long id, int version) {
        return new Product(id, name, sku, price, stock, version);
    }
}
