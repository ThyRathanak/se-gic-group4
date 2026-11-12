package edu.itc.shop.service;

import edu.itc.shop.customer.Customer;
import edu.itc.shop.domain.Address;
import edu.itc.shop.order.Order;
import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.repository.CustomerRepository;
import edu.itc.shop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OptimisticLockingTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void concurrentStatusChangesRejectOneStaleVersionAndKeepWinner() throws Exception {
        Address address = new Address("1 Market Street", "Phnom Penh", "12000");
        Customer customer = customerRepository.save(
                new Customer("Concurrent Customer", "concurrent@example.com", address, address));
        Order order = orderRepository.save(new Order(
                customer, OrderStatus.NEW, Instant.parse("2026-11-12T00:00:00Z")));
        Long orderId = order.getId();
        long initialVersion = order.getVersion();

        CountDownLatch bothLoaded = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<OrderStatus> committedStatuses = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            List<Future<?>> futures = List.of(
                    executor.submit(() -> changeStatusInTransaction(
                            transactionTemplate, orderId, OrderStatus.PAID, initialVersion,
                            bothLoaded, committedStatuses, failures)),
                    executor.submit(() -> changeStatusInTransaction(
                            transactionTemplate, orderId, OrderStatus.CANCELLED, initialVersion,
                            bothLoaded, committedStatuses, failures))
            );

            for (Future<?> future : futures) {
                future.get(20, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(failures).hasSize(1);
        assertThat(failures.get(0)).isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(committedStatuses).hasSize(1);

        Order persisted = orderRepository.findById(orderId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(committedStatuses.get(0));
        assertThat(persisted.getVersion()).isEqualTo(initialVersion + 1);
    }

    @Test
    void facadeRetriesSoBothConcurrentStatusChangesEventuallyCommit() throws Exception {
        Address address = new Address("4 Market Street", "Phnom Penh", "12000");
        Customer customer = customerRepository.save(
                new Customer("Retry Customer", "retry@example.com", address, address));
        Order order = orderRepository.save(new Order(
                customer, OrderStatus.NEW, Instant.parse("2026-11-12T00:00:00Z")));
        Long orderId = order.getId();
        long initialVersion = order.getVersion();

        CountDownLatch workersReady = new CountDownLatch(2);
        CountDownLatch startTogether = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<OrderStatus> committedStatuses = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        try {
            List<Future<?>> futures = List.of(
                    executor.submit(() -> changeStatusWithRetry(
                            orderId, OrderStatus.PAID, workersReady, startTogether,
                            committedStatuses, failures)),
                    executor.submit(() -> changeStatusWithRetry(
                            orderId, OrderStatus.CANCELLED, workersReady, startTogether,
                            committedStatuses, failures))
            );

            assertThat(workersReady.await(10, TimeUnit.SECONDS)).isTrue();
            startTogether.countDown();
            for (Future<?> future : futures) {
                future.get(20, TimeUnit.SECONDS);
            }
        } finally {
            startTogether.countDown();
            executor.shutdownNow();
        }

        assertThat(failures).isEmpty();
        assertThat(committedStatuses).containsExactlyInAnyOrder(OrderStatus.PAID, OrderStatus.CANCELLED);

        Order persisted = orderRepository.findById(orderId).orElseThrow();
        assertThat(persisted.getStatus()).isIn(OrderStatus.PAID, OrderStatus.CANCELLED);
        assertThat(persisted.getVersion()).isEqualTo(initialVersion + 2);
    }

    private void changeStatusWithRetry(
            Long orderId,
            OrderStatus nextStatus,
            CountDownLatch workersReady,
            CountDownLatch startTogether,
            List<OrderStatus> committedStatuses,
            List<Throwable> failures
    ) {
        try {
            workersReady.countDown();
            if (!startTogether.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for the other worker");
            }
            orderFacade.changeStatusWithRetry(orderId, nextStatus);
            committedStatuses.add(nextStatus);
        } catch (Throwable failure) {
            failures.add(failure);
        }
    }

    private void changeStatusInTransaction(
            TransactionTemplate transactionTemplate,
            Long orderId,
            OrderStatus nextStatus,
            long expectedVersion,
            CountDownLatch bothLoaded,
            List<OrderStatus> committedStatuses,
            List<Throwable> failures
    ) {
        try {
            transactionTemplate.executeWithoutResult(transaction -> {
                Order managed = orderRepository.findById(orderId).orElseThrow();
                if (managed.getVersion() != expectedVersion) {
                    throw new AssertionError("Both transactions must read the same initial version");
                }
                bothLoaded.countDown();
                try {
                    if (!bothLoaded.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timed out waiting for both transactions to load");
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while synchronizing transactions", interrupted);
                }

                orderService.changeStatus(orderId, nextStatus);
            });
            committedStatuses.add(nextStatus);
        } catch (Throwable failure) {
            failures.add(failure);
        }
    }
}
