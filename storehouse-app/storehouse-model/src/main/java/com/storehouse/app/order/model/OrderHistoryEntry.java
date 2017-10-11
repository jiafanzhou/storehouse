package com.storehouse.app.order.model;

import com.storehouse.app.order.model.Order.OrderStatus;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 * We will maintain the order history which is the purpose of this class.
 *
 * @author ejiafzh
 *
 */
@Embeddable
public class OrderHistoryEntry implements Serializable {
    private static final long serialVersionUID = -5544853563085399050L;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Temporal(TemporalType.TIMESTAMP) // @Temporal is used for Java Date
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    /**
     * Constructor with no args.
     */
    public OrderHistoryEntry() {

    }

    /**
     * Set the order history with status and initialise it with the current date and time.
     *
     * @param status
     *            the status of the order to add
     */
    public OrderHistoryEntry(final OrderStatus status) {
        this.status = status;
        this.createdAt = new Date();
    }

    /**
     * Get the order status of this history entry.
     * 
     * @return the order status of this history entry.
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Set the order status of this history entry.
     * 
     * @param status
     *            the order status of this history entry.
     */
    public void setStatus(final OrderStatus status) {
        this.status = status;
    }

    /**
     * Get the createdAt timestamp of this history entry.
     * 
     * @return the createdAt timestamp of this history entry.
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the createdAt timestamp of this history entry.
     * 
     * @param createdAt
     *            the createdAt timestamp of this history entry.
     */
    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((status == null) ? 0 : status.hashCode());
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
        final OrderHistoryEntry other = (OrderHistoryEntry) obj;
        if (status != other.status)
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrderHistoryEntry [status=" + status + ", createdAt=" + createdAt + "]";
    }

}
