package com.storehouse.app.order.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * This represents each order item of an order.
 *
 * We have it in the model in the case we want to support multiple products and
 * multiple order items from an order.
 *
 * We also modelled the price in case we want to support the pricing model.
 *
 * @author ejiafzh
 *
 */
@Embeddable // an order contains one or more order item
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 3520003746949600860L;

    @NotNull
    private Integer quantity;

    // we don't support the price now, by default sets to 0
    @NotNull
    private Double price = 0.00;

    /**
     * Constructor with no args.
     */
    public OrderItem() {
    }

    /**
     * Constructs an order item with quantity and price.
     *
     * @param quantity
     *            the quantity of the order item
     * @param price
     *            the price of the order item
     */
    public OrderItem(final Integer quantity, final Double price) {
        this(quantity);
        this.price = price;
    }

    /**
     * Constructs an order item with quantity only.
     *
     * @param quantity
     *            the quantity of the order item.
     */
    public OrderItem(final Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * Get the quantity of the order item.
     * 
     * @return the quantity of the order item
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Set the quantity of the order item.
     * 
     * @param quantity
     *            the quantity of the order item.
     */
    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * Get the price of the order item.
     * 
     * @return the price of the order item.
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Set the price of the order item.
     * 
     * @param price
     *            the price of the order item.
     */
    public void setPrice(final Double price) {
        this.price = price;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrderItem [quantity=" + quantity + ", price=" + price + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OrderItem other = (OrderItem) obj;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        if (quantity == null) {
            if (other.quantity != null)
                return false;
        } else if (!quantity.equals(other.quantity))
            return false;
        return true;
    }

}
