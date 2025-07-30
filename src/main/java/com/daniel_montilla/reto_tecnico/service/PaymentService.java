package com.daniel_montilla.reto_tecnico.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.daniel_montilla.reto_tecnico.entity.Client;
import com.daniel_montilla.reto_tecnico.entity.CreditCardDetails;
import com.daniel_montilla.reto_tecnico.entity.Order;
import com.daniel_montilla.reto_tecnico.exception.PaymentFailedException;
import com.daniel_montilla.reto_tecnico.repository.CartItemRepository;
import com.daniel_montilla.reto_tecnico.repository.OrderRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentService {

  private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

  @Value("${payment.rejection.probability}")
  private double rejectionProbability;

  @Value("${payment.process.maxRetries}")
  private int maxRetries;

  private final EmailSenderService emailSenderService;
  private final OrderRepository orderRepository;

  public PaymentService(EmailSenderService emailSenderService, OrderRepository orderRepository,
      CartItemRepository cartItemRepository) {
    this.emailSenderService = emailSenderService;
    this.orderRepository = orderRepository;
  }

  public void makePayment(Client client, Order order, CreditCardDetails creditCardDetails)
      throws PaymentFailedException {
    var retries = Math.max(0, maxRetries);

    for (int attempt = 0; attempt <= retries; attempt++) {
      try {

        logger.info("Attempt {} of {}: Processing payment...", attempt + 1, retries + 1);

        if (Math.random() < rejectionProbability) {
          throw new PaymentFailedException();
        }

        logger.info("✅ Payment successful!");
        emailSenderService.send("Farmatodo_reto Payment successful", client.getEmail(),
            EmailSenderService.generatePaymentSuccessfullContent(order));
        return;
      } catch (PaymentFailedException e) {

        if (attempt == retries) {
          logger.error("❌ Payment failed. Max retries reached.");
          emailSenderService.send("Farmatodo_reto Payment failed", client.getEmail(),
              EmailSenderService.generatePaymentFailedContent(order));
          throw new PaymentFailedException();
        }

        logger.warn("❌ Payment failed. Retrying...");

      }
    }
  }

  @Async
  @Transactional
  public void processPayment(Client client, Order _order,
      CreditCardDetails creditCardDetails) {

    logger.info("Asynchronously processing payment for order ID: {} (Amount: {}).", _order.getId(),
        _order.getTotalPrice());

    var orderOptional = orderRepository.findById(_order.getId());

    if (orderOptional.isEmpty()) {
      logger.error("Failed to locate order of id {}", _order.getId());
      return;
    }

    var order = orderOptional.get();

    try {
      makePayment(client, order, creditCardDetails);

      order.setStatus(Order.Status.COMPLETED);
      orderRepository.save(order);

      logger.info("✅ Payment successful for order ID: {}.", order.getId());

    } catch (PaymentFailedException e) {
      logger.warn("Handling payment failure for order ID: {}.", order.getId());

      order.setStatus(Order.Status.FAILED);
      orderRepository.save(order);

      logger.warn("❌ Payment failed for order ID: {}", order.getId());
    } catch (Exception e) {
      order.setStatus(Order.Status.FAILED);
      orderRepository.save(order);

      logger.error("An unexpected error occurred during payment processing for order ID: {}. Error: {}", order.getId(),
          e.getMessage(), e);
    }
  }
}
