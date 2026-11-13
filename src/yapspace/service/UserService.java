package yapspace.service;

import yapspace.exception.AuthenticationException;
import yapspace.exception.ValidationException;
import yapspace.model.RegularUser;
import yapspace.model.User;
import yapspace.storage.DataStore;

import java.time.LocalDateTime;

/** Registration and login (the register() and login() methods from the proposal). */
public class UserService {

    private static final String DUMMY_SALT = PasswordHasher.newSalt();

    private final DataStore store;

    public UserService(DataStore store) {
        this.store = store;
    }

    /** Creates a new account for someone aged 13 or above. */
    public User register(String username, String email, String password, String confirmPassword,
                         boolean ageConfirmed) {
        String cleanName = Validator.username(username);
        String cleanEmail = Validator.email(email);
        Validator.password(password);
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("The two passwords do not match.");
        }
        if (!ageConfirmed) {
            throw new ValidationException("YapSpace is for people aged 13 and above. Please confirm your age.");
        }
        synchronized (store) {
            if (findByUsername(cleanName) != null) {
                throw new ValidationException("That username is already taken.");
            }
            for (User u : store.users()) {
                if (u.getEmail().equalsIgnoreCase(cleanEmail)) {
                    throw new ValidationException("That email address is already registered.");
                }
            }
            String salt = PasswordHasher.newSalt();
            User user = new RegularUser(store.nextUserId(), cleanName, cleanEmail, salt,
                    PasswordHasher.hash(password, salt), LocalDateTime.now());
            store.users().add(user);
            store.saveUsers();
            return user;
        }
    }

    /** Checks the username and password. The error message is the same for both mistakes on purpose. */
    public User login(String username, String password) {
        String name = username == null ? "" : username.trim();
        if (name.isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("Please enter your username and password.");
        }
        User user = findByUsername(name);
        if (user == null) {
            PasswordHasher.hash(password, DUMMY_SALT); // same effort whether or not the name exists
            throw new AuthenticationException("Incorrect username or password.");
        }
        if (!PasswordHasher.matches(password, user.getSalt(), user.getPasswordHash())) {
            throw new AuthenticationException("Incorrect username or password.");
        }
        return user;
    }

    public User findByUsername(String username) {
        synchronized (store) {
            for (User u : store.users()) {
                if (u.getUsername().equalsIgnoreCase(username)) {
                    return u;
                }
            }
            return null;
        }
    }

    public User findById(int id) {
        synchronized (store) {
            for (User u : store.users()) {
                if (u.getId() == id) {
                    return u;
                }
            }
            return null;
        }
    }
}
