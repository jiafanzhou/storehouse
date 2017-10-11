package com.storehouse.app.order.model;

import com.storehouse.app.user.model.Customer;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * This class represents an order placed by a customer.
 *
 * It will be sent to a JMS queue and eventually being consumed and being processed.
 *
 * @author ejiafzh
 *
 */
@Entity
@Table(name = "storehouse_order")
public class Order implements Serializable {
    private static final long serialVersionUID = -8589662328013809186L;

    public static final int MAX_LOAD = 25; // this defines the maximum load

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP) // @Temporal is used for Java Date
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // we only allow a customer to place one item in the order as we only
    // have 1 product, once we have multiple product, we can add more items
    @NotNull
    @Valid // this annotation ensures all elements will be validated against
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "storehouse_order_item", joinColumns = @JoinColumn(name = "order_id"))
    @Size(min = 1, max = 1)
    private Set<OrderItem> items;

    @NotNull
    private Double total;

    /**
     * OrderStatus can be reserved, pending, delivered or cancelled.
     *
     * @author ejiafzh
     *
     */
    public enum OrderStatus {
        RESERVED, PENDING, DELIVERED, CANCELLED
    }

    // track the history of the order
    @NotNull
    @Valid // this annotation ensures all elements will be validated against
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "storehouse_order_orderhistory", joinColumns = @JoinColumn(name = "order_id"))
    @Size(min = 1)
    private Set<OrderHistoryEntry> historyEntries;

    @NotNull
    @Column(name = "current_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus currentStatus;

    /**
     * Constructor of Order and it sets the current date timestamp.
     */
    public Order() {
        createdAt = new Date(); // set the createAt to be the current date
    }

    /**
     * Gets the order id.
     *
     * @return the order id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the order id.
     *
     * @param id
     *            the order id.
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Gets the createdAt timestamp.
     *
     * @return the createdAt timestamp.
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the createdAt timestamp.
     *
     * @param createdAt
     */
    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the customer.
     *
     * @return the customer.
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Set the customer.
     *
     * @param customer
     *            to be set
     */
    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    /**
     * Get the items to be ordered.
     *
     * @return the items to be ordered.
     */
    public Set<OrderItem> getItems() {
        if (items == null) {
            items = new HashSet<>();
        }
        return items;
    }

    /**
     * Set the items to be ordered.
     *
     * @param items
     *            the items to be ordered.
     */
    public void setItems(final Set<OrderItem> items) {
        this.items = items;
    }

    /**
     * Add an item to the order items.
     *
     * @param quantity
     *            quantity of the items.
     * @return whether or not adding item is successful.
     */
    public boolean addItem(final int quantity) {
        return getItems().add(new OrderItem(quantity));
    }

    /**
     * Get the total price of this order.
     *
     * @return the total price of this order.
     */
    public Double getTotalPrice() {
        return total;
    }

    /**
     * Set the total price of this order.
     *
     * @param total
     *            the total price of this order.
     */
    public void setTotal(final Double total) {
        this.total = total;
    }

    /**
     * Calculate the total price.
     * This field is not in use at the moment.
     */
    public void calculateTotalPrice() {
        total = 0D; // reset to 0 first
        if (items != null) {
            items.forEach(e -> {
                total += e.getPrice();
            });
        }
    }

    /**
     * Calculate the total quantity.
     *
     * @return the total quantity.
     */
    public int calculateTotalQuantity() {
        int totalQuantity = 0;
        for (final OrderItem item : items) {
            totalQuantity += item.getQuantity();
        }
        return totalQuantity;
    }

    /**
     * Gets the history entries of the ordering items.
     *
     * @return the history entries of the ordering items.
     */
    public Set<OrderHistoryEntry> getHistoryEntries() {
        if (historyEntries == null) {
            historyEntries = new HashSet<>();
        }
        return historyEntries;
    }

    /**
     * Sets the history entries of the ordering items.
     *
     * @param historyEntries
     *            the history entries of the ordering items.
     */
    public void setHistoryEntries(final Set<OrderHistoryEntry> historyEntries) {
        this.historyEntries = historyEntries;
    }

    /**
     * Add a new order status to the history entries.
     *
     * @param newStatus
     *            a new order status to the history entries.
     */
    public void addHistoryEntry(final OrderStatus newStatus) {
        if (currentStatus != null) {
            // any order except RESERVED cannot change its state
            if (currentStatus != OrderStatus.RESERVED) {
                throw new IllegalArgumentException("An order in the state " + currentStatus
                        + " cannot have its state changed.");
            }

            if (currentStatus == newStatus) {
                throw new IllegalArgumentException("The new status must be different from the current state");
            }
        }
        getHistoryEntries().add(new OrderHistoryEntry(newStatus));
        currentStatus = newStatus; // this method also sets the currentStatus if it is full
    }

    /**
     * Get the current status of this order.
     *
     * @return the current status of this order.
     */
    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Set the current status of this order.
     *
     * @param currentStatus
     *            the current status of this order.
     */
    public void setCurrentStatus(final OrderStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    /**
     * Set the initial order status.
     * It will clear the historical entries in case it was used, then initialise the
     * current status and add into the historical entry.
     */
    public void setInitialStatus() {
        getHistoryEntries().clear();
        setCurrentStatus(null);
        addHistoryEntry(OrderStatus.RESERVED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        final Order other = (Order) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Order [id=" + id + ", createdAt=" + createdAt + ", customer=" + customer
                + ", items=" + items + ", total=" + total + ", historyEntries="
                + historyEntries + ", currentStatus=" + currentStatus + "]";
    }

}
