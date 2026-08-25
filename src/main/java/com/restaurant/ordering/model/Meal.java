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

    /**
     * Returns the database-generated identifier of this meal.
     *
     * @return meal identifier
     */
    public Long getId() { return id; }

    /**
     * Returns the name displayed in the restaurant menu.
     *
     * @return meal name
     */
    public String getName() { return name; }

    /**
     * Changes the name displayed for this meal.
     *
     * @param name new meal name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the descriptive text shown with the meal.
     *
     * @return meal description
     */
    public String getDescription() { return description; }

    /**
     * Changes the descriptive text shown with the meal.
     *
     * @param description new meal description
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Returns the current menu price.
     *
     * @return current meal price
     */
    public BigDecimal getPrice() { return price; }

    /**
     * Changes the current menu price.
     *
     * @param price new menu price
     */
    public void setPrice(BigDecimal price) { this.price = price; }

    /**
     * Indicates whether customers may currently order this meal.
     *
     * @return {@code true} when the meal is available for ordering
     */
    public boolean isAvailable() { return available; }

    /**
     * Changes whether customers may currently order this meal.
     *
     * @param available whether the meal is available for ordering
     */
    public void setAvailable(boolean available) { this.available = available; }

    /**
     * Returns the image URL used by the Angular menu.
     *
     * @return configured meal-image URL
     */
    public String getImageUrl() { return imageUrl; }

    /**
     * Changes the image URL used by the Angular menu.
     *
     * @param imageUrl new image URL
     */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Returns the category that groups this meal in the menu.
     *
     * @return associated meal category
     */
    public Category getCategory() { return category; }

    /**
     * Assigns this meal to a restaurant category.
     *
     * @param category category to assign to the meal
     */
    public void setCategory(Category category) { this.category = category; }

    /**
     * Returns availability using the wrapper type expected by DTO mapping code.
     *
     * @return current availability as a {@link Boolean}
     */
    public Boolean getAvailable() { return available; }
}
