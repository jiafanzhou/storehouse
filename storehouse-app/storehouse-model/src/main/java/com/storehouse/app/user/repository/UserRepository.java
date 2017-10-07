package com.storehouse.app.user.repository;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.common.repository.GenericRepository;
import com.storehouse.app.user.model.User;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Stateless
public class UserRepository extends GenericRepository<User> {
    @PersistenceContext
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected Class<User> getPersistentClass() {
        return User.class;
    }

    /**
     * We consider a user exists if the email is already registered.
     */
    public boolean isUserNameAlreadyExists(final User user) {
        return propertyAlreadyExists(user.getId(), "email", user.getEmail());
    }

    public User findByEmail(final String email) {
        User found = null;
        if (email != null) {
            try {
                found = (User) getEntityManager().createQuery(
                        "Select u from " + getPersistentClass().getSimpleName() + " u where u.email = :email")
                        .setParameter("email", email).getSingleResult();
            } catch (final NoResultException nex) {
                found = null;
            }
        }
        return found;
    }

    /**
     * This is a real pagination implementation.
     *
     * @param filter
     *            find by the filter.
     * @return the paginated data.
     */
    public PaginatedData<User> findByFilter(final UserFilter filter) {
        final StringBuilder clause = new StringBuilder("WHERE a.id is not null");
        final Map<String, Object> queryParameters = new HashMap<>();
        if (filter.getName() != null) {
            clause.append(" And UPPER(a.name) Like UPPER(:name)");
            queryParameters.put("name", "%" + filter.getName() + "%");
        }
        if (filter.getUserType() != null) {
            clause.append(" And a.userType = :userType");
            queryParameters.put("userType", filter.getUserType());
        }

        return findByParameters(clause.toString(), filter, queryParameters, "name ASC");
    }
}
