package com.daniel_montilla.reto_tecnico.service;

import com.daniel_montilla.reto_tecnico.entity.Order;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

  // Corrected the logger to reference this class
  private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);

  // 1. Inject the JavaMailSender bean
  @Autowired
  private JavaMailSender mailSender;

  @Value("${email.sender.address}")
  private String senderEmail;

  @Value("${email.sender.alias:}")
  private String senderAlias;

  /**
   * Asynchronously sends an HTML email.
   * 
   * @param subject     The subject of the email.
   * @param recipient   The recipient's email address.
   * @param htmlContent The HTML body of the email.
   */
  @Async
  public void send(String subject, String recipient, String htmlContent) {
    logger.info("Attempting to send email '{}' to {}", subject, recipient);

    MimeMessage message = mailSender.createMimeMessage();

    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      if (senderAlias != null && !senderAlias.isBlank()) {
        helper.setFrom(senderEmail, senderAlias);
      } else {
        helper.setFrom(senderEmail);
      }

      helper.setTo(recipient);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);

      mailSender.send(message);
      logger.info("Successfully sent email to {}", recipient);

    } catch (Exception e) {
      logger.error("Failed to send email to {}: {}", recipient, e.getMessage(), e);
    }
  }

  public static String generatePaymentSuccessfullContent(Order order) {
    // This method correctly generates HTML content
    return String.format(
        """
            <html>
                <body>
                    <h2>Payment Confirmed! 🎉</h2>
                    <p>Thank you for your purchase!</p>
                    <p><strong>Order ID:</strong> %s</p>
                    <p><strong>Amount Paid:</strong> $%.2f</p>
                    <p>Your order is being processed and will be shipped soon.</p>
                    <p>Need help? Reply to this email or contact support.</p>
                </body>
            </html>
            """,
        order.getId(),
        order.getTotalPrice());
  }

  public static String generatePaymentFailedContent(Order order) {
    // This method correctly generates HTML content
    return String.format(
        """
            <html>
                <body>
                    <h2>Payment Failed ❌</h2>
                    <p>We couldn’t process your payment for:</p>
                    <p><strong>Order ID:</strong> %s</p>
                    <p><strong>Amount:</strong> $%.2f</p>
                    <p>Please check your payment method and try again.</p>
                    <p><a href="https://example.com/retry-payment?orderId=%s">Click here to retry</a></p>
                    <p>Contact support if the issue persists.</p>
                </body>
            </html>
            """,
        order.getId(),
        order.getTotalPrice(),
        order.getId());
  }
}
