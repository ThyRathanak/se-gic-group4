package edu.itc.shop.repository;

import edu.itc.shop.order.Order;
import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.web.dto.OrderSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerEmailAndStatusOrderByPlacedAtDesc(String email, OrderStatus status);

    long countByStatus(OrderStatus status);

    Optional<Order> findFirstByCustomerIdOrderByPlacedAtDesc(Long customerId);

    @Query("""
            select distinct o
            from Order o
            join fetch o.lines l
            join fetch l.product
            where o.customer.email = :email
            """)
    List<Order> findWithLinesByCustomerEmail(@Param("email") String email);

    @Query("select count(line) from OrderLine line where line.order.status = :status")
    long countLinesByStatus(@Param("status") OrderStatus status);

    @Query(value = """
            select o.id as id, o.status as status, o.placedAt as placedAt, c.name as customerName
            from Order o join o.customer c
            where o.status = :status
            """,
            countQuery = "select count(o) from Order o where o.status = :status")
    Page<OrderSummary> findByStatus(@Param("status") OrderStatus status, Pageable pageable);
}
