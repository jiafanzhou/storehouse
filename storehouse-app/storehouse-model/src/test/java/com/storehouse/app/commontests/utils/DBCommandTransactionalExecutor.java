package com.storehouse.app.commontests.utils;

import javax.persistence.EntityManager;

import org.junit.Ignore;

@Ignore
public class DBCommandTransactionalExecutor {
    private EntityManager em;

    public DBCommandTransactionalExecutor(final EntityManager em) {
        this.em = em;
    }

    public <T> T executeCommand(final DBCommand<T> dbCommand) {
        try {
            em.getTransaction().begin();
            final T toReturn = dbCommand.execute();
            em.getTransaction().commit();
            em.clear();
            return toReturn;
        } catch (final Exception ex) {
            ex.printStackTrace();
            em.getTransaction().rollback();
            throw new IllegalStateException(ex);
        }
    }
}