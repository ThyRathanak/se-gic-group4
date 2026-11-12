package edu.itc.shop.repository;

import edu.itc.shop.product.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class FlushOrderTest {

    @Autowired
    private TagRepository tags;

    @Test
    @Transactional
    void insertBeforeDeleteViolatesUniqueTagName() {
        Tag saleTag = tags.saveAndFlush(new Tag("sale"));

        tags.delete(saleTag);
        tags.save(new Tag("sale"));

        assertThatThrownBy(tags::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void flushingDeleteBeforeInsertAllowsReusingUniqueTagName() {
        Tag saleTag = tags.saveAndFlush(new Tag("sale"));

        tags.delete(saleTag);
        tags.flush();
        tags.save(new Tag("sale"));
        tags.flush();

        assertThat(tags.findAll())
                .extracting(Tag::getName)
                .containsExactly("sale");
    }
}
