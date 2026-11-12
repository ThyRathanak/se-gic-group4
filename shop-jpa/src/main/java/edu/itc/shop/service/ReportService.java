package edu.itc.shop.service;

import edu.itc.shop.order.Order;
import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final OrderRepository orders;

    public ReportService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> linesPerPaidOrder() {
        return report(orders.findByStatus(OrderStatus.PAID));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> linesPerPaidOrderWithFetchJoin() {
        return report(orders.findByStatusWithLines(OrderStatus.PAID));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> linesPerPaidOrderWithEntityGraph() {
        return report(orders.findWithGraphByStatus(OrderStatus.PAID));
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> linesPerPaidOrderBatched() {
        return report(orders.findByStatus(OrderStatus.PAID));
    }

    private Map<Long, List<String>> report(List<Order> paidOrders) {
        return paidOrders.stream()
                .collect(Collectors.toMap(
                        Order::getId,
                        order -> order.getLines().stream()
                                .map(line -> line.getProduct().getName())
                                .toList()));
    }
}
