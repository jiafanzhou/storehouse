package com.storehouse.app.common.repository;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.GenericFilter;
import com.storehouse.app.common.model.filter.PaginationData.OrderMode;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public abstract class GenericRepository<T> {
    protected abstract Class<T> getPersistentClass();

    protected abstract EntityManager getEntityManager();

    public T add(final T entity) {
        getEntityManager().persist(entity);
        return entity;
    }

    public T findById(final Long id) {
        T found = null;
        if (id != null) {
            found = getEntityManager().find(getPersistentClass(), id);
        }
        return found;
    }

    public T update(final T entity) {
        return getEntityManager().merge(entity);
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return getEntityManager().createQuery("Select e from " + getPersistentClass().getSimpleName() + " e")
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll(final String orderField) {
        return getEntityManager()
                .createQuery("Select e from " + getPersistentClass().getSimpleName() + " e Order by e." + orderField)
                .getResultList();
    }

    public boolean existsById(final Long id) {
        return getEntityManager()
                .createQuery("Select 1 from " + getPersistentClass().getSimpleName() + " e where e.id = :id")
                .setParameter("id", id).setMaxResults(1).getResultList().size() > 0;
    }

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
