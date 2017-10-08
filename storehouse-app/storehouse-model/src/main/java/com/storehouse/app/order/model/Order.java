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

@Entity
@Table(name = "storehouse_order")
public class Order implements Serializable {
    private static final long serialVersionUID = -8589662328013809186L;

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
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "storehouse_order_item", joinColumns = @JoinColumn(name = "order_id"))
    @Size(min = 1, max = 1)
    private Set<OrderItem> items;

    @NotNull
    private Double total;

    // jiafanz: document this
    public enum OrderStatus {
        RESERVED, DELIVERED, CANCELLED
    }

    // track the history of the order
    @NotNull
    @Valid // this annotation ensures all elements will be validated against
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "storehouse_order_orderhistory", joinColumns = @JoinColumn(name = "order_id"))
    @Size(min = 1)
    private Set<OrderHistoryEntry> historyEntries;

    @NotNull
    @Column(name = "current_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus currentStatus;

    public Order() {
        createdAt = new Date(); // set the createAt to be the current date
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    public Set<OrderItem> getItems() {
        if (items == null) {
            items = new HashSet<>();
        }
        return items;
    }

    public void setItems(final Set<OrderItem> items) {
        this.items = items;
    }

    public boolean addItem(final int quantity) {
        return getItems().add(new OrderItem(quantity));
    }

    public Double getTotalPrice() {
        return total;
    }

    public void setTotal(final Double total) {
        this.total = total;
    }

    public void calculateTotalPrice() {
        total = 0D; // reset to 0 first
        if (items != null) {
            items.forEach(e -> {
                total += e.getPrice();
            });
        }
    }

    public int calculateTotalQuantity() {
        int totalQuantity = 0;
        for (final OrderItem item : items) {
            totalQuantity += item.getQuantity();
        }
        return totalQuantity;
    }

    public Set<OrderHistoryEntry> getHistoryEntries() {
        if (historyEntries == null) {
            historyEntries = new HashSet<>();
        }
        return historyEntries;
    }

    public void setHistoryEntries(final Set<OrderHistoryEntry> historyEntries) {
        this.historyEntries = historyEntries;
    }

    // jiafanz: document this
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

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(final OrderStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public void setInitialStatus() {
        getHistoryEntries().clear();
        setCurrentStatus(null);
        addHistoryEntry(OrderStatus.RESERVED);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        final Order other = (Order) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Order [id=" + id + ", createdAt=" + createdAt + ", customer=" + customer
                + ", items=" + items + ", total=" + total + ", historyEntries="
                + historyEntries + ", currentStatus=" + currentStatus + "]";
    }

}
