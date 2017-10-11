package com.storehouse.app.user.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

/**
 * A user can either be a customer or employee.
 *
 * Employee can be either a administrator or just employee.
 *
 * Administrator must be set directly into the database. (not through the Java API here)
 *
 * @author ejiafzh
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Table(name = "storehouse_user")
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1050881026659874901L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP) // @Temporal is used for Java Date
    @Column(name = "created_at", updatable = false)
    private Date createdAt; // this specifies when user was created.

    @NotNull
    @Size(min = 3, max = 40)
    private String name;

    @NotNull
    @Email
    @Column(unique = true)
    @Size(max = 70)
    private String email;

    @NotNull
    private String password;

    public enum Roles {
        CUSTOMER, EMPLOYEE, ADMIN
    }

    @CollectionTable(name = "storehouse_user_role", joinColumns = @JoinColumn(name = "user_id"), uniqueConstraints = @UniqueConstraint(columnNames = {
            "user_id", "role" }))
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private List<Roles> roles;

    public enum UserType {
        CUSTOMER, EMPLOYEE
    }

    @Column(name = "type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private UserType userType; // this field is managed by JPA

    /**
     * Return a list of roles for a given user.
     */
    protected abstract List<Roles> getDefaultRoles();

    /**
     * Default constructor with no args.
     *
     * Whenever a new user is created, it will associate the current timestamp to
     * the newly created user.
     *
     */
    public User() {
        this.createdAt = new Date();
        this.roles = getDefaultRoles();
    }

    /**
     * Get the user ID of the user.
     *
     * @return the user Id of the user.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the user ID of the user.
     *
     * @param id
     *            user ID of the user.
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Get the timestamp when the user is created.
     * 
     * @return the timestamp when the user is created.
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the timestamp when the user is created.
     * 
     * @param createdAt
     *            the timestamp when the user is created.
     */
    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the name of the user.
     * 
     * @return the name of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the user.
     * 
     * @param name
     *            the name of the user.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the email address of the user account.
     * 
     * @return the email address of the user account.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the email address of the user account.
     * 
     * @param email
     *            email address of the user account.
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Get the password for the user.
     * 
     * @return the password for the user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password for the user.
     * 
     * @param password
     *            the password for the user.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Get the roles associated with this user.
     * 
     * @return the roles associated with this user.
     */
    public List<Roles> getRoles() {
        return roles;
    }

    /**
     * Set the roles associated with this user.
     * 
     * @param roles
     *            the roles associated with this user.
     */
    public void setRoles(final List<Roles> roles) {
        this.roles = roles;
    }

    /**
     * Get the user type.
     * 
     * @return the user type.
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Set the user type.
     * 
     * @param userType
     *            the user type.
     */
    public void setUserType(final UserType userType) {
        this.userType = userType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final User other = (User) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "User [id=" + id + ", createdAt=" + createdAt + ", name=" + name + ", email=" + email + ", roles="
                + roles + ", userType=" + userType + "]";
    }

}
