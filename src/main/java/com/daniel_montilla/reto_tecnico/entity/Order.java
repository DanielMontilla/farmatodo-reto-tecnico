package com.daniel_montilla.reto_tecnico.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  private Client client;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CartItem> cartItems;

  @Column(nullable = false)
  private BigDecimal totalPrice;

  @Column(nullable = true)
  private String deliveryAddress;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(referencedColumnName = "token")
  private CreditCardDetails creditCardDetails;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private Status status = Status.PROCESSING;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public enum Status {
    PROCESSING,
    COMPLETED,
    FAILED,
  }
}