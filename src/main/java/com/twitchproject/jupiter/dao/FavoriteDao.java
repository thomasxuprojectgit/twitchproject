package com.twitchproject.jupiter.dao;

import com.twitchproject.jupiter.entity.db.Item;
import com.twitchproject.jupiter.entity.db.ItemType;
import com.twitchproject.jupiter.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FavoriteDao {
    @Autowired
    private SessionFactory sessionFactory;

    /**
     * Insert a favorite record to the database
     * @param userId user id
     * @param item item obj favorite from browser
     */
    public void setFavoriteItem(String userId, Item item) {
        Session session = null;

        try {
            // create connection with database
            session = sessionFactory.openSession();

            // get user info from database
            User user = session.get(User.class, userId);

            // get item set(favorite) from user obj
            // see cascade (all) setting in User obj (that setting make sure we can get/update all item objs for that user from/to database)
            user.getItemSet().add(item);

            // begin transaction to make sure atomic change
            session.beginTransaction();

            // will only update itemSet obj in user obj(will reflect on favorite_record table)
            session.save(user);
            session.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            session.getTransaction().rollback(); // can roll back to begin if error occur
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Remove a favorite record from the database
     * @param userId user id
     * @param itemId item id un-favorite from browser
     */
    public void unsetFavoriteItem(String userId, String itemId) {
        Session session = null;

        try {
            session = sessionFactory.openSession();
            User user = session.get(User.class, userId);

            // get Item obj from database
            Item item = session.get(Item.class, itemId);
            user.getItemSet().remove(item);
            session.beginTransaction();

            // will only update itemSet obj in user obj(will reflect on favorite_record table)
            session.update(user);
            session.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Get the list of favorite by a specific user
     * @param userId user id
     * @return list of favorite by given user id
     */
    public Set<Item> getFavoriteItems(String userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(User.class, userId).getItemSet();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new HashSet<>();
    }

    /**
     * Get favorite item ids for the given user
     * @param userId user id
     * @return set of favorite item by this user per user id
     */
    public Set<String> getFavoriteItemIds(String userId) {

        // use set as dedup item has been favorite by user
        // this is for final recommendation
        Set<String> itemIds = new HashSet<>();

        try (Session session = sessionFactory.openSession()) {

            // get set of games that has been favorite by user
            Set<Item> items = session.get(User.class, userId).getItemSet();
            for(Item item : items) {
                // put item id of each item into set of item ids
                itemIds.add(item.getId());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemIds;
    }

    /**
     * Get favorite items for the given user. The returned map includes three entries like {"Video":
     * [game1, game2, game3], "Stream": [game1, game1, game6], "Clip": [game7, game8, game7, ...]}
     * @param favoriteItemIds user favorite item ids
     * @return map of <item type, list of game id in same type> for favorite items
     */
    public Map<String, List<String>> getFavoriteGameIds(Set<String> favoriteItemIds) {
        Map<String, List<String>> itemMap = new HashMap<>();

        // initiate the map for different item type
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>()); // key: item type, value: list of game id
        }

        try (Session session = sessionFactory.openSession()) {
            // sort items per item types into map
            for(String itemId : favoriteItemIds) {
                Item item = session.get(Item.class, itemId);
                itemMap.get(item.getType().toString()).add(item.getGameId()); // key: item type, value: list of game id
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemMap;
    }
}



