package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class UserDaoHibernateImpl implements UserDao, AutoCloseable {
    private final SessionFactory sessionFactory;

    public UserDaoHibernateImpl() {
        try {
            Configuration configuration = new Configuration()
                    .addAnnotatedClass(User.class)
                    .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                    .setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC")
                    .setProperty("hibernate.connection.username", "root")
                    .setProperty("hibernate.connection.password", "m2k28z")
                    .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect")
                    .setProperty("hibernate.show_sql", "true")
                    .setProperty("hibernate.hbm2ddl.auto", "update");

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public void createUsersTable() {
        executeInTransaction(session -> {
            session.createSQLQuery(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                            "name VARCHAR(50) NOT NULL, " +
                            "last_name VARCHAR(50) NOT NULL, " +  // Исправлено на last_name
                            "age TINYINT NOT NULL)"
            ).executeUpdate();
        });
    }

    @Override
    public void dropUsersTable() {
        executeInTransaction(session -> {
            session.createSQLQuery("DROP TABLE IF EXISTS users").executeUpdate();
        });
    }

    @Override
    public void saveUser(String name, String lastName, byte age) {
        executeInTransaction(session -> {
            session.save(new User(name, lastName, age));
        });
    }

    @Override
    public void removeUserById(long id) {
        executeInTransaction(session -> {
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
            }
        });
    }

    @Override
    public List<User> getAllUsers() {
        return executeInSession(session -> {
            return session.createQuery("FROM User", User.class).list();
        });
    }

    @Override
    public void cleanUsersTable() {
        executeInTransaction(session -> {
            session.createQuery("DELETE FROM User").executeUpdate();
        });
    }

    private void executeInTransaction(Consumer<Session> action) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            action.accept(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Transaction failed", e);
        }
    }

    private <T> T executeInSession(Function<Session, T> action) {
        try (Session session = sessionFactory.openSession()) {
            return action.apply(session);
        } catch (Exception e) {
            throw new RuntimeException("Session operation failed", e);
        }
    }

    @Override
    public void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}