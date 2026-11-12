package edu.itc.shop.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.repository.OrderRepository;

@Service
public class ReportService {

    private final OrderRepository orders;

    public ReportService(OrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> linesPerPaidOrder() {
        List<Order> paidOrders = orders.findByStatusWithLines(OrderStatus.PAID);

        Map<Long, Integer> result = new LinkedHashMap<>();

        for (Order order : paidOrders) {
            result.put(order.getId(), order.getLines().size());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> linesPerPaidOrderJoinFetch() {
        List<Order> paidOrders =
                orders.findByStatusWithLines(OrderStatus.PAID);

        Map<Long, Integer> result = new LinkedHashMap<>();

        for (Order order : paidOrders) {
            result.put(order.getId(), order.getLines().size());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> linesPerPaidOrderEntityGraph() {
        List<Order> paidOrders =
                orders.findWithGraphByStatus(OrderStatus.PAID);

        Map<Long, Integer> result = new LinkedHashMap<>();

        for (Order order : paidOrders) {
            result.put(order.getId(), order.getLines().size());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> linesPerPaidOrderBatch() {
        List<Order> paidOrders = orders.findAllByStatus(OrderStatus.PAID);

        Map<Long, Integer> result = new LinkedHashMap<>();

        for (Order order : paidOrders) {
            result.put(order.getId(), order.getLines().size());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> productNamesPerPaidOrder() {
        List<Order> paidOrders = orders.findAllByStatus(OrderStatus.PAID);

        Map<Long, List<String>> result = new LinkedHashMap<>();

        for (Order order : paidOrders) {
            List<String> productNames = new ArrayList<>();

            for (var line : order.getLines()) {
                productNames.add(line.getProduct().getName());
            }

            result.put(order.getId(), productNames);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> productNamesPerPaidOrderJoinFetch() {

        List<Order> paidOrders =
                orders.findWithLinesAndProductsByStatus(OrderStatus.PAID);

        Map<Long, List<String>> result = new LinkedHashMap<>();

        for (Order order : paidOrders) {
            List<String> productNames = new ArrayList<>();

            for (var line : order.getLines()) {
                productNames.add(line.getProduct().getName());
            }

            result.put(order.getId(), productNames);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> productNamesPerPaidOrderEntityGraph() {

        List<Order> paidOrders =
                orders.findWithGraphByStatus(OrderStatus.PAID);

        Map<Long, List<String>> result = new LinkedHashMap<>();

        for (Order order : paidOrders) {
            List<String> productNames = new ArrayList<>();

            for (var line : order.getLines()) {
                productNames.add(line.getProduct().getName());
            }

            result.put(order.getId(), productNames);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public Map<Long, List<String>> productNamesPerPaidOrderBatch() {

        List<Order> paidOrders =
                orders.findAllByStatus(OrderStatus.PAID);

        Map<Long, List<String>> result = new LinkedHashMap<>();

        for (Order order : paidOrders) {
            List<String> productNames = new ArrayList<>();

            for (var line : order.getLines()) {
                productNames.add(line.getProduct().getName());
            }

            result.put(order.getId(), productNames);
        }

        return result;
    }
}