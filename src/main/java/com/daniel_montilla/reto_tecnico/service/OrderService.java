package com.daniel_montilla.reto_tecnico.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.daniel_montilla.reto_tecnico.entity.Order;
import com.daniel_montilla.reto_tecnico.exception.NoItemsInCartException;
import com.daniel_montilla.reto_tecnico.exception.NotFoundException;
import com.daniel_montilla.reto_tecnico.exception.TokenizationRejectedException;
import com.daniel_montilla.reto_tecnico.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService {

  private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

  private final OrderRepository orderRepository;

  private final OrderCreationService orderCreationService;
  private final PaymentService paymentService;

  public OrderService(OrderRepository orderRepository,
      OrderCreationService orderCreationService, PaymentService paymentService) {

    this.orderRepository = orderRepository;

    this.orderCreationService = orderCreationService;
    this.paymentService = paymentService;
  }

  public Order placeOrder(Long clientId, Optional<String> deliveryAddress, String creditCardNumeber,
      String expirationDate, String cvv, String creditCardHolderName)
      throws NoItemsInCartException, TokenizationRejectedException, NotFoundException {

    var result = orderCreationService.createOrder(clientId, deliveryAddress, creditCardNumeber, expirationDate, cvv,
        creditCardHolderName);

    logger.info("Payment processing initiated for order ID: {}", result.order.getId());
    paymentService.processPayment(result.client, result.order, result.creditCardDetails);

    return result.order;
  }

  public List<Order> getOrdersOfClient(Long clientId) {
    return orderRepository.findAllByClientId(clientId);
  }
}
