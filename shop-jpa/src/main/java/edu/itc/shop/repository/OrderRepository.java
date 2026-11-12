package edu.itc.shop.repository;

import java.util.List;

import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import edu.itc.shop.web.dto.OrderSummary;
import org.springframework.data.jpa.repository.EntityGraph;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerEmailAndStatusOrderByPlacedAtDesc(
            String email,
            OrderStatus status
    );

    long countByStatus(OrderStatus status);

    Order findFirstByCustomerIdOrderByPlacedAtDesc(Long customerId);

    @Query("""
        select distinct o from Order o
        join fetch o.lines l
        join fetch l.product
        where o.customer.email = :email
        order by o.placedAt desc
        """)
    List<Order> findWithLinesByCustomerEmail(@Param("email") String email);

    @Query("""
        select count(l) from OrderLine l
        where l.order.status = :status
        """)
    long countLinesByStatus(@Param("status") OrderStatus status);

    Page<OrderSummary> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findAllByStatus(OrderStatus status);

    @Query("""
        select distinct o
        from Order o
        join fetch o.lines
        where o.status = :status
    """)
    List<Order> findByStatusWithLines(
            @Param("status") OrderStatus status
    );

    @EntityGraph(attributePaths = {"lines", "lines.product"})
    List<Order> findWithGraphByStatus(OrderStatus status);

    @Query("""
        select distinct o
        from Order o
        join fetch o.lines l
        join fetch l.product
        where o.status = :status
    """)
    List<Order> findWithLinesAndProductsByStatus(
            @Param("status") OrderStatus status
    );
}