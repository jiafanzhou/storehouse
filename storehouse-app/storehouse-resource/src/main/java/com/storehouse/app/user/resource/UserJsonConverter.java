package com.storehouse.app.user.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.storehouse.app.common.json.EntityJsonConverter;
import com.storehouse.app.common.json.JsonReader;
import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.user.model.Customer;
import com.storehouse.app.user.model.Employee;
import com.storehouse.app.user.model.User;
import com.storehouse.app.user.model.User.Roles;
import com.storehouse.app.user.model.User.UserType;

import javax.enterprise.context.ApplicationScoped;

/**
 * User specific json converter.
 *
 * @author ejiafzh
 *
 */
// @ApplicationScoped is a CDI annotation, which means only one instance of
// this class will be automatically injected for the lifecycle of application.
@ApplicationScoped
public class UserJsonConverter implements EntityJsonConverter<User> {
    /**
     * {@inheritDoc}
     */
    @Override
    public User convertFrom(final String json) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(json);

        final User user = getUserInstance(jsonObject);
        user.setName(JsonReader.getStringOrNull(jsonObject, "name"));
        user.setEmail(JsonReader.getStringOrNull(jsonObject, "email"));
        user.setPassword(JsonReader.getStringOrNull(jsonObject, "password"));

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement convertToJsonElement(final User user) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", user.getId());
        jsonObject.addProperty("name", user.getName());
        jsonObject.addProperty("email", user.getEmail());
        jsonObject.addProperty("type", user.getUserType().toString());

        final JsonArray roles = new JsonArray();
        for (final Roles role : user.getRoles()) {
            roles.add(new JsonPrimitive(role.toString()));
        }
        jsonObject.add("roles", roles);
        jsonObject.addProperty("createdAt", DateUtils.formatDateTime(user.getCreatedAt()));

        return jsonObject;
    }

    private User getUserInstance(final JsonObject userJson) {
        final UserType userType = UserType.valueOf(JsonReader.getStringOrNull(userJson, "type"));
        return UserType.CUSTOMER.equals(userType) ? new Customer() : new Employee();
    }
}
