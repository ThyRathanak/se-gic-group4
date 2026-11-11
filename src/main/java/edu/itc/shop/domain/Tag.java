package edu.itc.shop.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq")
    @SequenceGenerator(name = "tag_seq", sequenceName = "tag_seq", allocationSize = 50)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Product> products = new HashSet<>();

    protected Tag() {}

    public Tag(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Set<Product> getProducts() { return products; }
}