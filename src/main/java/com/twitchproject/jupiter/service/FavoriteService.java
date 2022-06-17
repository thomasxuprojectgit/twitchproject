package com.twitchproject.jupiter.service;

import com.twitchproject.jupiter.dao.FavoriteDao;
import com.twitchproject.jupiter.entity.db.Item;
import com.twitchproject.jupiter.entity.db.ItemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteDao favoriteDao;

    public void setFavoriteItem(String userId, Item item) {
        favoriteDao.setFavoriteItem(userId, item);
    }

    public void unsetFavoriteItem(String userId, String itemId) {
        favoriteDao.unsetFavoriteItem(userId, itemId);
    }

    /**
     * get user's favorite video, stream,clips per user id
     * @param userId user id
     * @return a map <ItemType string, list of item for this item type> of favorite items
     */
    public Map<String, List<Item>> getFavoriteItems(String userId) {
        Map<String, List<Item>> itemMap = new HashMap<>();

        // create empty list for the three types
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }
        Set<Item> favorites = favoriteDao.getFavoriteItems(userId);

        // iterate over all items in favorite item list and put them into map per item type
        for(Item item : favorites) {
            itemMap.get(item.getType().toString()).add(item);
        }
        return itemMap;
    }
}

