package nttdata.menuservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restaurantId;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MenuItem> menuItems = new ArrayList<>();

    public void addMenuItem(MenuItem menuItem) {
        menuItems.add(menuItem);
        menuItem.setRestaurant(this);
    }

    public void removeMenuItem(MenuItem menuItem) {
        menuItems.remove(menuItem);
        menuItem.setRestaurant(null);
    }
}