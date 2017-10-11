package com.storehouse.app.common.repository;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.GenericFilter;
import com.storehouse.app.common.model.filter.PaginationData.OrderMode;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Abstract class of a generic repository class to interact with the database.
 *
 * @author ejiafzh
 *
 * @param <T>
 *            the generic entity class.
 */
public abstract class GenericRepository<T> {

    protected abstract Class<T> getPersistentClass();

    protected abstract EntityManager getEntityManager();

    /**
     * Persist a new entity into the database.
     *
     * @param entity
     *            entity to persist
     * @return a persisted entity.
     */
    public T add(final T entity) {
        getEntityManager().persist(entity);
        return entity;
    }

    /**
     * Find the entity based on its database primitive ID.
     *
     * @param id
     *            the database of the entity.
     * @return a found entity
     */
    public T findById(final Long id) {
        T found = null;
        if (id != null) {
            found = getEntityManager().find(getPersistentClass(), id);
        }
        return found;
    }

    /**
     * Update the entity in the database.
     *
     * @param entity
     *            to entity to be updated
     * @return an updated entity from the database
     */
    public T update(final T entity) {
        return getEntityManager().merge(entity);
    }

    /**
     * Find all the entities from the database for this generic type <T>.
     *
     * @return a list of the found entities.
     */
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return getEntityManager().createQuery("Select e from " + getPersistentClass().getSimpleName() + " e")
                .getResultList();
    }

    /**
     * Find all the entities from the database for this generic type <T> based
     * on the orderField.
     *
     * @param orderField
     *            field to order by for the list
     * @return a sorted list of the found entities.
     */
    @SuppressWarnings("unchecked")
    public List<T> findAll(final String orderField) {
        return getEntityManager()
                .createQuery("Select e from " + getPersistentClass().getSimpleName() + " e Order by e." + orderField)
                .getResultList();
    }

    /**
     * Check whether or not the entity already exists in the database based on the ID.
     *
     * @param id
     *            the ID of the entity.
     * @return true if exists false otherwise.
     */
    public boolean existsById(final Long id) {
        return getEntityManager()
                .createQuery("Select 1 from " + getPersistentClass().getSimpleName() + " e where e.id = :id")
                .setParameter("id", id).setMaxResults(1).getResultList().size() > 0;
    }

    /**
     * Check whether or not the entity exists based on the id, and property name/value.
     *
     * @param id
     *            entity ID
     * @param propertyName
     *            the name of the property
     * @param propertyValue
     *            the value of the property.
     * @return true if exists false otherwise.
     */
    public boolean propertyAlreadyExists(final Long id, final String propertyName, final String propertyValue) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Select 1 from " + getPersistentClass().getSimpleName() + " e where e." + propertyName
                + " = :propertyValue");
        if (id != null) {
            sb.append(" And e.id != :id");
        }

        final Query query = getEntityManager().createQuery(sb.toString());
        query.setParameter("propertyValue", propertyValue);
        if (id != null) {
            query.setParameter("id", id);
        }
        return query.setMaxResults(1).getResultList().size() > 0;
    }

    /**
     * Find the PaginatedData based on a number of parameters.
     * 
     * @param whereClause
     *            where clause in the SQL
     * @param filter
     *            filter to be used.
     * @param queryParameters
     *            query parameters.
     * @param defaultSort
     *            default sort field
     * @return PaginatedData, null if none
     */
    protected PaginatedData<T> findByParameters(final String whereClause, final GenericFilter filter,
            final Map<String, Object> queryParameters, final String defaultSort) {
        final String className = getPersistentClass().getSimpleName();
        final StringBuilder sortClause = populateSortClause(filter, defaultSort);

        final Query queryResults = getEntityManager()
                .createQuery("Select a From " + className + " a " + whereClause + " " + sortClause.toString());
        applyQueryParameters(queryParameters, queryResults);

        applyPaginationOnQuery(filter, queryResults);

        @SuppressWarnings("unchecked")
        final List<T> results = queryResults.getResultList();
        final Integer count = countNumberOfRows(whereClause, queryParameters, className);

        final PaginatedData<T> paginatedData = new PaginatedData<>(count, results);
        return paginatedData;
    }

    private Integer countNumberOfRows(final String whereClause, final Map<String, Object> queryParameters,
            final String className) {
        final Query queryCount = getEntityManager()
                .createQuery("Select Count(a) From " + className + " a " + whereClause);
        applyQueryParameters(queryParameters, queryCount);
        final Integer count = ((Long) queryCount.getSingleResult()).intValue();
        return count;
    }

    private void applyPaginationOnQuery(final GenericFilter filter, final Query queryResults) {
        if (filter.hasPaginationData()) {
            queryResults.setFirstResult(filter.getPaginationData().getFirstResult());
            queryResults.setMaxResults(filter.getPaginationData().getMaxResults());
        }
    }

    private StringBuilder populateSortClause(final GenericFilter filter, final String defaultSort) {
        final StringBuilder sortClause = new StringBuilder();
        if (filter.hasOrderField()) {
            sortClause.append("Order by a." + filter.getPaginationData().getOrderField());
            sortClause.append(filter.getPaginationData().getOrderMode() == OrderMode.ASCENDING ? " ASC" : " DESC");
        } else {
            sortClause.append("Order by a." + defaultSort); // default
        }
        return sortClause;
    }

    private void applyQueryParameters(final Map<String, Object> queryParameters, final Query query) {
        for (final Entry<String, Object> entryMap : queryParameters.entrySet()) {
            query.setParameter(entryMap.getKey(), entryMap.getValue());
        }
    }
}
