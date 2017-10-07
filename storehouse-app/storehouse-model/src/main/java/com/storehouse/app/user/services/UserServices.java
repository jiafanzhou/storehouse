package com.storehouse.app.user.services;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.user.model.User;

import java.util.List;

import javax.ejb.Local;

@Local
public interface UserServices {
    User add(User user);

    User update(User user);

    User findById(Long id);

    User findByEmail(String email);

    List<User> findAll();

    List<User> findAll(String orderField);

    void updatePassword(Long l, String password);

    User findByEmailAndPassword(String email, String password);

    PaginatedData<User> findByFilter(UserFilter userFilter);

}
