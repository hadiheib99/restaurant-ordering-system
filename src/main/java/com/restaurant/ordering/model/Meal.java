package com.restaurant.ordering.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * JPA entity representing one meal offered by the restaurant.
 *
 * <p>A meal stores its menu name, description, price, availability, optional
 * image URL and the category to which it belongs.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private boolean available = true;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Creates an empty meal required by JPA. */
    public Meal() {
    }

    /** @return database-generated meal identifier */
    public Long getId() { return id; }

    /** @return meal name shown in the menu */
    public String getName() { return name; }

    /** @param name new meal name */
    public void setName(String name) { this.name = name; }

    /** @return meal description */
    public String getDescription() { return description; }

    /** @param description new meal description */
    public void setDescription(String description) { this.description = description; }

    /** @return current menu price */
    public BigDecimal getPrice() { return price; }

    /** @param price new menu price */
    public void setPrice(BigDecimal price) { this.price = price; }

    /** @return true when the meal may currently be ordered */
    public boolean isAvailable() { return available; }

    /** @param available whether customers may order this meal */
    public void setAvailable(boolean available) { this.available = available; }

    /** @return URL used to display the meal image */
    public String getImageUrl() { return imageUrl; }

    /** @param imageUrl new image URL */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /** @return category that groups the meal */
    public Category getCategory() { return category; }

    /** @param category category to assign to the meal */
    public void setCategory(Category category) { this.category = category; }

    /**
     * Wrapper-style availability accessor used by DTO mapping code.
     * @return current availability as a Boolean
     */
    public Boolean getAvailable() { return available; }
}
