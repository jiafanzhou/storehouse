package com.storehouse.app.commontests.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Ignore;

@Ignore
public class TestBaseRepository {
    protected EntityManagerFactory emf;
    protected EntityManager em;
    protected DBCommandTransactionalExecutor dbTxExecutor;

    protected void initTestCase() {
        emf = Persistence.createEntityManagerFactory("storehousePU");
        em = emf.createEntityManager();

        dbTxExecutor = new DBCommandTransactionalExecutor(em);

        // The following shows a GUI DatabaseManager for hsql in memory db
        // org.hsqldb.util.DatabaseManagerSwing.main(new String[] {
        // "--url", "jdbc:hsqldb:mem:testdb", "--noexit"
        // });
    }

    protected void closeEntityManager() {
        em.close();
        emf.close();
    }
}
