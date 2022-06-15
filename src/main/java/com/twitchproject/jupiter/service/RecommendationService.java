package com.twitchproject.jupiter.service;




import com.twitchproject.jupiter.dao.FavoriteDao;
import com.twitchproject.jupiter.entity.db.Item;
import com.twitchproject.jupiter.entity.db.ItemType;
import com.twitchproject.jupiter.entity.response.Game;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;


@Service
public class RecommendationService {
    private static final int DEFAULT_GAME_LIMIT = 3;
    private static final int DEFAULT_PER_GAME_RECOMMENDATION_LIMIT = 10;
    private static final int DEFAULT_TOTAL_RECOMMENDATION_LIMIT = 20;
    @Autowired
    private GameService gameService;

    @Autowired
    private FavoriteDao favoriteDao;


    /**
     * Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip]. Add items are related
     * to the top games provided in the argument (not based on user favorite)
     * @param type item type
     * @param topGames list of top games from twitch
     * @return list of recommended items
     * @throws RecommendationException recommendation error from twitch
     */
    private List<Item> recommendByTopGames(ItemType type, List<Game> topGames) throws RecommendationException {
        List<Item> recommendedItems = new ArrayList<>();

        // iterate over all games
        for (Game game : topGames) {
            List<Item> items;
            try {
                // per game, get the top DEFAULT_PER_GAME_RECOMMENDATION_LIMIT amount (10) of items (limit for each game is 10)
                items = gameService.searchByType(game.getId(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }

            // put the items above into the final list for recommendation
            for (Item item : items) {
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) { // total limit for each type is 20
                    return recommendedItems;
                }
                recommendedItems.add(item);
            }
        }
        return recommendedItems;
    }

    /**
     * Return a map of Item objects as the recommendation result. Keys of the may are [Stream, Video, Clip].
     * Each key is corresponding to a list of Items objects, each item object is a recommended item based on the top
     * games currently on Twitch.
     * @return Map <item type, list of items> of recommended items
     * @throws RecommendationException Recommendation issue
     */
    public Map<String, List<Item>> recommendItemsByDefault() throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        List<Game> topGames;
        try {
            // get top game list from twitch
            topGames = gameService.topGames(DEFAULT_GAME_LIMIT);
        } catch (TwitchException e) {
            throw new RecommendationException("Failed to get game data for recommendation");
        }

        // for each item type
        for (ItemType type : ItemType.values()) {
            // get a list of recommended item per top games and item type, then put to map for this item type
            recommendedItemMap.put(type.toString(), recommendByTopGames(type, topGames));
        }
        return recommendedItemMap;
    }

    /**
     * Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip]. All items are related
     * to the items previously favorited by the user. E.g., if a user favorited some videos about game "Just Chatting",
     * then it will return some other videos about the same game.
     * @param favoritedItemIds favorite item ids by user (used to dedupe that we do not need to recommend items has
     *                         been favorite by user)
     * @param favoritedGameIds list of favorite game ids (more duplicate for same game means user is more like this game)
     * @param type item type
     * @return list of item recommended
     * @throws RecommendationException recommend issue from twitch
     */
    private List<Item> recommendByFavoriteHistory(
            Set<String> favoritedItemIds, List<String> favoritedGameIds, ItemType type) throws RecommendationException {
        // Count the favorite game IDs from the database for the given user. E.g. if the favorited game ID list is ["1234", "2345", "2345", "3456"], the returned Map is {"1234": 1, "2345": 2, "3456": 1}
        //  gameID  count
        Map<String, Long> favoriteGameIdByCount = new HashMap<>();

        // count the favorite for each game
        for(String gameId : favoritedGameIds) {
            favoriteGameIdByCount.put(gameId, favoriteGameIdByCount.getOrDefault(gameId, 0L) + 1);
        }
        // Sort the game Id by count. E.g. if the input is {"1234": 1, "2345": 2, "3456": 1},
        // the returned Map is {"2345": 2, "1234": 1, "3456": 1}
        // list of Entries<Game Id, count>
        List<Map.Entry<String, Long>> sortedFavoriteGameIdListByCount = new ArrayList<>(
                favoriteGameIdByCount.entrySet());
        sortedFavoriteGameIdListByCount.sort((Map.Entry<String, Long> e1, Map.Entry<String, Long> e2) -> Long
                .compare(e2.getValue(), e1.getValue()));
        // See also: https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values

        // only pick the limit amount(3) of games
        if (sortedFavoriteGameIdListByCount.size() > DEFAULT_GAME_LIMIT) {
            sortedFavoriteGameIdListByCount = sortedFavoriteGameIdListByCount.subList(0, DEFAULT_GAME_LIMIT);
        }


        List<Item> recommendedItems = new ArrayList<>();


        // Search Twitch based on the favorite game IDs returned to the last step.

        for (Map.Entry<String, Long> favoriteGame : sortedFavoriteGameIdListByCount) {
            List<Item> items;
            try {

                // per favorite game, get list of items from twitch
                items = gameService.searchByType(favoriteGame.getKey(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }

            // add items to recommendation list
            for (Item item : items) {
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    return recommendedItems;
                }
                // dedupe, if user have favorite the item, do not need to add to commendation list
                if (!favoritedItemIds.contains(item.getId())) {
                    recommendedItems.add(item);
                }
            }
        }
        return recommendedItems;
    }

    /**
     *  Return a map of Item objects as the recommendation result. Keys of the may are [Stream, Video, Clip].
     *  key is corresponding to a list of Items objects, each item object is a recommended item based on the
     *  previous favorite records by the user.
     * @param userId user id
     * @return Map of recommended list<item type, list of recommended item >
     * @throws RecommendationException twitch issue
     */
    public Map<String, List<Item>> recommendItemsByUser(String userId) throws RecommendationException {

        // final result -- Map of recommended list<item type, list of recommended item >
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        Set<String> favoriteItemIds;
        Map<String, List<String>> favoriteGameIds;

        // items favorite by user
        favoriteItemIds = favoriteDao.getFavoriteItemIds(userId);

        // sort items favorite by user and get map of favorite game ids<item type, list of game ids for each item type>
        // map includes three entries like {"Video": [game1, game2, game3], "Stream": [game1, game1, game6],
        // "Clip": [game7, game8, game7, ...]}
        favoriteGameIds = favoriteDao.getFavoriteGameIds(favoriteItemIds);


        for (Map.Entry<String, List<String>> entry : favoriteGameIds.entrySet()) {
            // if user has not favorite any games(items)
            if (entry.getValue().size() == 0) {
                List<Game> topGames;
                try {
                    topGames = gameService.topGames(DEFAULT_GAME_LIMIT);
                } catch (TwitchException e) {
                    throw new RecommendationException("Failed to get game data for recommendation");
                }

                // if no favorite by user, call recommendByTopGames (use top game from twitch) to get list of recommend items for this item type
                recommendedItemMap.put(entry.getKey(), recommendByTopGames(ItemType.valueOf(entry.getKey()), topGames));
            } else {

                // if has favorite items, call recommendByFavoriteHistory (use favorite games by user) to get a list of recommend items for this item type
                recommendedItemMap.put(entry.getKey(), recommendByFavoriteHistory(favoriteItemIds, entry.getValue(), ItemType.valueOf(entry.getKey())));
            }
        }
        return recommendedItemMap;
    }



}

