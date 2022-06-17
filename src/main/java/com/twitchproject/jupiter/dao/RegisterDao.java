package com.twitchproject.jupiter.dao;

import com.twitchproject.jupiter.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;

@Repository
public class RegisterDao {

    // use sessionFactory obj to connect and do action with database (created in ApplicationConfig.java)
    @Autowired
    private SessionFactory sessionFactory;

    public boolean register(User user) {
        Session session = null;

        try {
            // create session to connect with database
            session = sessionFactory.openSession();
            // transaction, make sure atomic for one action
            session.beginTransaction();
            session.save(user);

            // end of transaction and commit all actions above
            session.getTransaction().commit();

        // if data base has issue | or user already existed
        } catch (PersistenceException | IllegalStateException ex) {
            // if hibernate throws this exception, it means the user already be register
            ex.printStackTrace();
            session.getTransaction().rollback();
            return false;
        } finally {
            // close session to release threads for connections with database
            if (session != null) session.close();
        }
        return true;
    }
}

