package com.storehouse.app.common.model.filter;

import com.storehouse.app.order.model.Order.OrderStatus;

import java.util.Date;

/**
 * Order field which includes startDate, endDate, customerId and status.
 *
 * @author ejiafzh
 *
 */
public class OrderFilter extends GenericFilter {
    private Date startDate;
    private Date endDate;
    private Long customerId;
    private OrderStatus status;

    /**
     * Get the start date of this filter.
     * 
     * @return the start date of this filter.
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Set the start date of this filter.
     * 
     * @param startDate
     *            the start date of this filter.
     */
    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Get the end date of this filter.
     * 
     * @return the end date of this filter.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of this filter.
     * 
     * @param endDate
     *            the end date of this filter.
     */
    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Get the customer ID for this order filter.
     * 
     * @return the customer ID for this order filter.
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * Set the customer ID for this order filter.
     * 
     * @param customerId
     *            the customer ID for this order filter.
     */
    public void setCustomerId(final Long customerId) {
        this.customerId = customerId;
    }

    /**
     * Get the status of the order.
     * 
     * @return the status of the order.
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the order.
     * 
     * @param status
     *            the status of the order.
     */
    public void setStatus(final OrderStatus status) {
        this.status = status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrderFilter [startDate=" + startDate + ", endDate=" + endDate + ", customerId=" + customerId
                + ", status=" + status + ", toString()=" + super.toString() + "]";
    }

}
