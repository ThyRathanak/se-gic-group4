package edu.itc.shop.service;

import edu.itc.shop.order.OrderStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class OrderFacade {

    private static final int MAX_ATTEMPTS = 3;

    private final OrderService orderService;

    public OrderFacade(OrderService orderService) {
        this.orderService = orderService;
    }

    public void changeStatusWithRetry(Long id, OrderStatus next) {
        ObjectOptimisticLockingFailureException lastFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                orderService.changeStatus(id, next);
                return;
            } catch (ObjectOptimisticLockingFailureException failure) {
                lastFailure = failure;
            }
        }

        throw lastFailure;
    }
}
