package com.storehouse.app.order.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable // an order contains one or more order item
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 3520003746949600860L;

    @NotNull
    private Integer quantity;

    // we don't support the price now, by default sets to 0
    @NotNull
    private Double price = 0.00;

    public OrderItem() {
    }

    public OrderItem(final Integer quantity, final Double price) {
        this(quantity);
        this.price = price;
    }

    public OrderItem(final Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(final Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderItem [quantity=" + quantity + ", price=" + price + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
        return result;
    }

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
