package com.storehouse.app.commontests;

import javax.persistence.EntityManager;

public class TestsRepositoryUtils {
    @SuppressWarnings("unchecked")
    public static <T> T findByPropertyNameAndValue(final EntityManager em, final Class<T> clazz,
            final String propertyName, final String propertyValue) {
        return (T) em.createQuery("Select o From " + clazz.getSimpleName() +
                " o where o." + propertyName + " = :propertyValue")
                .setParameter("propertyValue", propertyValue).getSingleResult();
    }
}