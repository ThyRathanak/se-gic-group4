package edu.itc.shop.service;

import edu.itc.shop.order.Order;
import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.web.dto.OrderDto;
import edu.itc.shop.web.dto.OrderLineDto;
import edu.itc.shop.web.dto.OrderSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void markPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(OrderStatus.PAID);
    }

    @Transactional
    public void changeStatus(Long id, OrderStatus next) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        order.setStatus(next);
    }

    @Transactional(readOnly = true)
    public OrderDto view(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummary> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findByCustomerEmail(String email) {
        return orderRepository.findWithLinesByCustomerEmail(email).stream()
                .map(this::toDto)
                .toList();
    }

    private OrderDto toDto(Order order) {
        List<OrderLineDto> lines = order.getLines().stream()
                .map(line -> new OrderLineDto(
                        line.getId(),
                        line.getProduct().getId(),
                        line.getProduct().getSku(),
                        line.getProduct().getName(),
                        line.getQuantity(),
                        line.getUnitPrice()
                ))
                .toList();

        return new OrderDto(order.getId(), order.getStatus(), order.getPlacedAt(), order.total(), lines);
    }
}
