package com.daniel_montilla.reto_tecnico.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.daniel_montilla.reto_tecnico.entity.Client;
import com.daniel_montilla.reto_tecnico.entity.CreditCardDetails;
import com.daniel_montilla.reto_tecnico.entity.Order;
import com.daniel_montilla.reto_tecnico.exception.NoItemsInCartException;
import com.daniel_montilla.reto_tecnico.exception.NotFoundException;
import com.daniel_montilla.reto_tecnico.exception.TokenizationRejectedException;
import com.daniel_montilla.reto_tecnico.repository.CartItemRepository;
import com.daniel_montilla.reto_tecnico.repository.ClientRepository;
import com.daniel_montilla.reto_tecnico.repository.CreditCardDetailsRespository;
import com.daniel_montilla.reto_tecnico.repository.OrderRepository;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderCreationService {

  private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

  private final OrderRepository orderRepository;
  private final ClientRepository clientRepository;
  private final CreditCardDetailsRespository creditCardDetailsRespository;
  private final CartItemRepository cartItemRepository;

  private final TokenizationService tokenizationService;
  private final CartService cartService;

  public OrderCreationService(OrderRepository orderRepository, ClientRepository clientRepository,
      CreditCardDetailsRespository creditCardDetailsRespository, CartItemRepository cartItemRepository,
      TokenizationService tokenizationService, PaymentService paymentService, CartService cartService) {

    this.orderRepository = orderRepository;
    this.clientRepository = clientRepository;
    this.creditCardDetailsRespository = creditCardDetailsRespository;
    this.cartItemRepository = cartItemRepository;
    this.tokenizationService = tokenizationService;
    this.cartService = cartService;
  }

  public class CreateOrderReturn {
    public final Order order;
    public final Client client;
    public final CreditCardDetails creditCardDetails;

    public CreateOrderReturn(Order order,
        Client client,
        CreditCardDetails creditCardDetails) {
      this.order = order;
      this.client = client;
      this.creditCardDetails = creditCardDetails;
    }
  }

  @Transactional
  public CreateOrderReturn createOrder(Long clientId, Optional<String> deliveryAddress, String creditCardNumeber,
      String expirationDate, String cvv, String creditCardHolderName)
      throws NoItemsInCartException, TokenizationRejectedException, NotFoundException {

    logger.info("Attempting to place order for clientId: {}", clientId);

    var token = tokenizationService.tokenizeCard(creditCardNumeber);

    logger.info("Card tokenized. Received token: {}", token);

    var creditCardDetails = creditCardDetailsRespository.save(CreditCardDetails.builder()
        .token(token)
        .creditCardNumber(creditCardNumeber)
        .expirationDate(expirationDate)
        .cvv(cvv)
        .cardHolderName(creditCardHolderName)
        .build());

    logger.info("Credit card details saved with token: {}", creditCardDetails.getToken());

    var clientOption = clientRepository.findById(clientId);

    if (clientOption.isEmpty()) {
      throw new NotFoundException("client");
    }

    var client = clientOption.get();

    logger.info("Client reference obtained for ID: {}", clientId);

    var cartItems = cartItemRepository.findAllByClientIdAndFulfilledFalse(clientId);

    logger.info("Found {} cart items for client ID: {}", cartItems.size(), clientId);

    if (cartItems.size() <= 0) {
      logger.info("No items in cart for client ID: {}. Throwing NoItemsInCartException.", clientId);
      throw new NoItemsInCartException();
    }

    var total = cartService.getTotal(cartItems);

    logger.info("Calculated total price for order: {}", total.doubleValue());

    var order = orderRepository.save(Order.builder()
        .client(client)
        .cartItems(cartItems)
        .totalPrice(total)
        .deliveryAddress(deliveryAddress.orElse(client.getAddress()))
        .creditCardDetails(creditCardDetails)
        .status(Order.Status.PROCESSING)
        .build());

    logger.info("Order saved with ID: {} and status: {}", order.getId(), order.getStatus());

    cartItemRepository.fulfillAllByClientId(clientId);

    logger.info("Order placed. Cart items fulfilled for client ID: {}", clientId);

    return new CreateOrderReturn(order, client, creditCardDetails);
  }

  public List<Order> getOrdersOfClient(Long clientId) {
    return orderRepository.findAllByClientId(clientId);
  }
}
