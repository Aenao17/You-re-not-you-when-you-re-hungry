package nttdata.orderservice.repository;

import nttdata.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUsernameOrderByCreatedAtDesc(String username);
    List<Order> findAllByOrderByCreatedAtDesc();
}