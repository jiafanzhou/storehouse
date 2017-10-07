package com.storehouse.app.user.repository;

import static com.storehouse.app.commontests.user.UserForTestsRepository.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.PaginationData;
import com.storehouse.app.common.model.filter.PaginationData.OrderMode;
import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.commontests.utils.TestBaseRepository;
import com.storehouse.app.user.model.User;
import com.storehouse.app.user.model.User.UserType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRepositoryUTest extends TestBaseRepository {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UserRepository userRepository;

    @Override
    @Before
    public void initTestCase() {
        super.initTestCase();
        userRepository = new UserRepository();
        userRepository.em = em;
    }

    @Override
    @After
    public void closeEntityManager() {
        super.closeEntityManager();
    }

    private void assertUser(final User actualUser, final User expectedUser) {
        assertThat(actualUser.getName(), is(equalTo(expectedUser.getName())));
        assertThat(actualUser.getEmail(), is(equalTo(expectedUser.getEmail())));
        assertThat(actualUser.getPassword(), is(equalTo(expectedUser.getPassword())));
        assertThat(actualUser.getRoles().toArray(), is(equalTo(expectedUser.getRoles().toArray())));
        assertThat(actualUser.getUserType(), is(equalTo(expectedUser.getUserType())));
        assertThat(actualUser.getCreatedAt(), is(notNullValue()));
    }

    @Test
    public void addCustomerAndFindIt() {
        final Long customerAddedId = dbTxExecutor.executeCommand(() -> {
            final User customer = userRepository.add(marySimpson());
            return customer.getId();
        });

        assertThat(customerAddedId, is(notNullValue()));

        final User customer = userRepository.findById(customerAddedId);
        logger.info("jiafanz: {}", customer);
        assertUser(customer, marySimpson());
    }

    @Test
    public void findUserByIdNotFound() {
        final User user = userRepository.findById(999L);
        assertThat(user, is(nullValue()));
    }

    @Test
    public void findUserByIdWithNull() {
        final User user = userRepository.findById(null);
        assertThat(user, is(nullValue()));
    }

    @Test
    public void updateCustomer() {
        final Long customerAddedId = dbTxExecutor.executeCommand(() -> {
            return userRepository.add(marySimpson()).getId();
        });
        assertThat(customerAddedId, is(notNullValue()));

        final User customerAfterAdd = userRepository.findById(customerAddedId);
        logger.info("jiafanz: {}", customerAfterAdd);
        assertUser(customerAfterAdd, marySimpson());

        customerAfterAdd.setName("Jiafan Zhou");
        final Long customerUpdatedId = dbTxExecutor.executeCommand(() -> {
            return userRepository.update(customerAfterAdd).getId();
        });

        final User customerAfterUpdate = userRepository.findById(customerUpdatedId);
        logger.info("jiafanz: {}", customerAfterUpdate);
        assertThat(customerAfterUpdate, is(notNullValue()));
        assertThat(customerAfterUpdate.getName(), is(equalTo("Jiafan Zhou")));
    }

    @Test
    public void alreadyExistsUserWithId() {
        dbTxExecutor.executeCommand(() -> {
            return userRepository.add(johnDoe());
        });

        assertThat(userRepository.isUserNameAlreadyExists(johnDoe()), is(equalTo(true)));
        assertThat(userRepository.isUserNameAlreadyExists(marySimpson()), is(equalTo(false)));
    }

    @Test
    public void findUserByEmail() {
        dbTxExecutor.executeCommand(() -> {
            return userRepository.add(marySimpson());
        });

        final User user = userRepository.findByEmail(marySimpson().getEmail());
        assertUser(user, marySimpson());

    }

    @Test
    public void findUserByEmailNotFound() {
        final User user = userRepository.findByEmail(marySimpson().getEmail());
        assertThat(user, is(nullValue()));
    }

    private void loadDataForFindByFilter() {
        dbTxExecutor.executeCommand(() -> {
            userRepository.add(marySimpson());
            userRepository.add(johnDoe());
            userRepository.add(admin());
            return null;
        });
    }

    @Test
    public void findByFilterNoFilter() {
        loadDataForFindByFilter();

        final PaginatedData<User> result = userRepository.findByFilter(new UserFilter());
        assertThat(result.getNumberOfRows(), is(equalTo(3)));
        assertThat(result.getRows().size(), is(equalTo(3)));
        // order is ASC by name
        assertThat(result.getRow(0).getName(), is(equalTo(admin().getName())));
        assertThat(result.getRow(1).getName(), is(equalTo(johnDoe().getName())));
        assertThat(result.getRow(2).getName(), is(equalTo(marySimpson().getName())));
    }

    @Test
    public void findByFilterFilteringByName() {
        loadDataForFindByFilter();

        final UserFilter filter = new UserFilter();
        filter.setPaginationData(new PaginationData(0, 2, "name", OrderMode.DESCENDING));

        PaginatedData<User> result = userRepository.findByFilter(filter);
        assertThat(result.getNumberOfRows(), is(equalTo(3)));
        assertThat(result.getRows().size(), is(equalTo(2)));
        assertThat(result.getRow(0).getName(), is(equalTo(marySimpson().getName())));
        assertThat(result.getRow(1).getName(), is(equalTo(johnDoe().getName())));

        filter.setPaginationData(new PaginationData(2, 2, "name", OrderMode.DESCENDING));

        result = userRepository.findByFilter(filter);
        assertThat(result.getNumberOfRows(), is(equalTo(3)));
        assertThat(result.getRows().size(), is(equalTo(1)));
        assertThat(result.getRow(0).getName(), is(equalTo(admin().getName())));
    }

    @Test
    public void findByFilterFilteringByNameAndPaginatingAndOrderingDescending() {
        loadDataForFindByFilter();

        // a filter has a name of 'o', descending order;
        // use pagination, only 2 maxResults
        final UserFilter filter = new UserFilter();
        filter.setName("o");
        filter.setUserType(UserType.CUSTOMER);
        filter.setPaginationData(new PaginationData(0, 2, "name", OrderMode.DESCENDING));

        PaginatedData<User> result = userRepository.findByFilter(filter);
        assertThat(result.getNumberOfRows(), is(equalTo(2)));
        assertThat(result.getRows().size(), is(equalTo(2)));
        assertThat(result.getRow(0).getName(), is(equalTo(marySimpson().getName())));
        assertThat(result.getRow(1).getName(), is(equalTo(johnDoe().getName())));

        filter.setPaginationData(new PaginationData(1, 2, "name", OrderMode.DESCENDING));

        result = userRepository.findByFilter(filter);
        assertThat(result.getNumberOfRows(), is(equalTo(2)));
        assertThat(result.getRows().size(), is(equalTo(1)));
        assertThat(result.getRow(0).getName(), is(equalTo(johnDoe().getName())));
    }
}
