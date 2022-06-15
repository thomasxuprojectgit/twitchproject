package com.twitchproject.jupiter.entity.request;

import com.twitchproject.jupiter.entity.db.Item;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FavoriteRequestBody {


    // Spring MVC will convert json to Item obj per jackson library
    @JsonProperty("favorite")
    private Item favoriteItem;

    public Item getFavoriteItem() {
        return favoriteItem;
    }
}
