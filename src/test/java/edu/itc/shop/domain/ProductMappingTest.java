// package edu.itc.shop.domain;

// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

// import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// class ProductMappingTest {

//     @Autowired
//     private TestEntityManager entityManager;

//     @Test
//     void testProductMappingAndRoundTrip() {
//         Product product = new Product("SKU-123", "Mechanical Keyboard", Money.eur("149.99"), Category.HARDWARE, 25);
//         Product persisted = entityManager.persistAndFlush(product);
//         entityManager.clear();

//         Product found = entityManager.find(Product.class, persisted.getId());

//         assertThat(found).isNotNull();
//         assertThat(found.getSku()).isEqualTo("SKU-123");
//         assertThat(found.getName()).isEqualTo("Mechanical Keyboard");
//         assertThat(found.getCategory()).isEqualTo(Category.HARDWARE);
//         assertThat(found.getStock()).isEqualTo(25);
//         assertThat(found.getPrice()).isNotNull();
//         assertThat(found.getPrice().amount()).isEqualByComparingTo("149.99");
//         assertThat(found.getPrice().currency()).isEqualTo("EUR");

//         // Verify native query string representation for EnumType.STRING
//         String categoryDb = (String) entityManager.getEntityManager()
//                 .createNativeQuery("SELECT category FROM product WHERE id = ?")
//                 .setParameter(1, persisted.getId())
//                 .getSingleResult();

//         assertThat(categoryDb).isEqualTo("HARDWARE");
//     }
// }