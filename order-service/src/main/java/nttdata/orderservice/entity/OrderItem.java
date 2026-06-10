package nttdata.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_items_seq")
    @SequenceGenerator(
            name = "order_items_seq",
            sequenceName = "order_items_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false)
    private Long menuItemId;

    // stored as a snapshot from Menu Service at creation time
    @Column(nullable = false, length = 255)
    private String menuItemName;

    @Column(nullable = false)
    private Integer quantity;

    // stored as a snapshot from Menu Service at creation time
    // if the menu item price changes later, this order still keeps the original price
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}