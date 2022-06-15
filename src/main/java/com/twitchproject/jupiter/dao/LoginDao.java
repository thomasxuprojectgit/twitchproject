package com.twitchproject.jupiter.dao;

import com.twitchproject.jupiter.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * check user name and password from LoginService with database
 */
@Repository
public class LoginDao {

    @Autowired
    private SessionFactory sessionFactory;

    // Verify if the given user Id and password are correct. Returns the user name when it passes
    // password has been encrypted from caller method
    public String verifyLogin(String userId, String password) {
        String name = "";

        // try with resource, it will automatically close the obj (session here) after try catch finish
        try (Session session = sessionFactory.openSession()) {

            // use get function to get user obj from database
            User user = session.get(User.class, userId);
            if(user != null && user.getPassword().equals((password))) {
                name = user.getFirstName();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return name;
    }
}

