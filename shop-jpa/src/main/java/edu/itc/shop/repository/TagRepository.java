package edu.itc.shop.repository;

import edu.itc.shop.product.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
