package com.storehouse.app.commontests.utils;

import com.storehouse.app.order.model.Order;
import com.storehouse.app.user.model.User;

import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Ignore;

@Ignore
@Stateless
public class TestRepositoryEJB {
    @PersistenceContext
    private EntityManager em;

    // Book.class must be defined first to delete, otherwise, there will
    // be database constraint problem.
    private static final List<Class<?>> ENTITIES_TO_REMOVE = Arrays.asList(Order.class, User.class);

    public void deleteAll() {
        for (final Class<?> entityClass : ENTITIES_TO_REMOVE) {
            deleteAllForEntity(entityClass);
        }
    }

    private void deleteAllForEntity(final Class<?> entityClass) {
        @SuppressWarnings("unchecked")
        final List<Object> rows = em.createQuery("Select c From "
                + entityClass.getSimpleName() + " c").getResultList();
        for (final Object row : rows) {
            em.remove(row);
        }
    }

    public void add(final Object entity) {
        em.persist(entity);
    }
}
