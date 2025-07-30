package com.daniel_montilla.reto_tecnico.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailSenderService {

  private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

  @Value("${email.sender.address}")
  private String senderEmail;

  @Value("${email.sender.alias:}")
  private String senderAlias;

  @Async
  public void send(String recipient, String content) {
    logger.debug("Sent email from {} to {}. With content: \n{}", senderEmail, recipient, content);
    return;
  }

  public static String generatePaymentSuccessfullContent() {
    return "";
  }

  public static String generatePaymentFailedContent() {
    return "";
  }
}
