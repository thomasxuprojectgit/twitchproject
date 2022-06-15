package com.twitchproject.jupiter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitchproject.jupiter.entity.db.Item;
import com.twitchproject.jupiter.entity.db.ItemType;
import com.twitchproject.jupiter.entity.response.Game;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;


/**
 * GameController call this class to interact with twitch API
 */
@Service
public class GameService {
    private static final String TOKEN = "Bearer zhyb9plja250p1q0bnwt0cewceabg0";
    private static final String CLIENT_ID = "j6v3dwifvnqm52xyfvr3lfbdbje13m";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;

    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;


    /**
     * Per user input in our website, construct URL(for sending to twitch API)
     * 1. if input gameName, will produce game search URL
     * 2. if input limit, will produce top game URL
     * @param url url before format
     * @param gameName provided game name, per user input in our website
     * @param limit provided top game limit
     * @return final URL
     */
    private String buildGameURL(String url, String gameName, int limit) {

        // if find top game
        if (gameName.equals("")) {
            return String.format(url, limit);

        // if find specific game
        } else {
            try {
                //  space will transfer to %20, others will transfer to other, then twitch API will recognize
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }

    /**
     * Similar to buildGameURL, build Search URL that will be used when calling Twitch API.
     * e.g. https://api.twitch.tv/helix/clips?game_id=12924.
     * @param url slected URL template
     * @param gameId game id as string
     * @param limit limit of search
     * @return final URL for sending to twitch
     */
    private String buildSearchURL(String url, String gameId, int limit) {
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format(url, gameId, limit);
    }

    /**
     * Use apache http client to send search URL to twitch API, twitch API will response a json file
     * @param url URL send to twitch API
     * @return json string (for transfering to Game obj later)
     * @throws TwitchException
     */
    private String searchTwitch(String url) throws TwitchException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // build handler for execute(send URL and receive json)  later
        ResponseHandler<String> responseHandler = response -> {

            // get status
            int responseCode = response.getStatusLine().getStatusCode();

            // status indicate error
            if (responseCode != 200) {
                System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get result from Twitch API");
            }

            // get result
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new TwitchException("Failed to get result from Twitch API");
            }

            // transfer http entity to json
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));

            // we only need info under data field of this json file from twitch API
            return obj.getJSONArray("data").toString();
        };

        // execute handler to http request to receive data
        try {
            // Define the HTTP request, TOKEN and CLIENT_ID are used for user authentication on Twitch backend
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", TOKEN);
            request.setHeader("Client-Id", CLIENT_ID);
            return httpclient.execute(request, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to get result from Twitch API");
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * For get top games list
     * Convert JSON format data returned from Twitch to an Arraylist of Game objects
     * Use ObjectMapper and annotation in Game class to map json fields to objs
     * @param data json string
     * @return List of Game objs
     */
    private List<Game> getGameList(String data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse game data from Twitch API");
        }
    }

    /**
     * Integrate search() and getGameList() together, returns the top x popular games from Twitch.
     * @param limit limit of how many top games to get
     * @return List of Game objs (transferred from json file)
     * @throws TwitchException issue from sending request to twitch
     */
    public List<Game> topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        String url = buildGameURL(TOP_GAME_URL, "", limit);
        String data = searchTwitch(url);
        return getGameList(data);
    }

    /**
     * Integrate search() and getGameList() together, returns the dedicated game based on the game name.
     * @param gameName game name provided by user
     * @return Game obj (transferred from json file)
     */
    public Game searchGame(String gameName){
        String url = buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0);
        String data = searchTwitch(url);
        List<Game> gameList = getGameList(data);

        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }


    /**
     * Similar to getGameList, convert the json data returned from Twitch to a list of Item objects.
     * @param data Json obj array string
     * @return list of Item objs
     * @throws TwitchException issue from sending request to twitch API
     */
    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse item data from Twitch API");
        }
    }

    /**
     * Returns the top x streams based on game ID.
     * @param gameId  game id
     * @param limit limit of search
     * @return list of Item objs
     * @throws TwitchException issue from sending request to twitch API
     */
    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        List<Item> streams = getItemList(searchTwitch(buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : streams) {
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
        }
        return streams;
    }


    /**
     * Returns the top x clips based on game ID.
     * @param gameId game id
     * @param limit limit of search
     * @return list of Item objs
     * @throws TwitchException issue from sending request to twitch API
     */
    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }
        return clips;
    }

    /**
     * Returns the top x videos based on game ID.
     * @param gameId game id
     * @param limit limit of search
     * @return list of Item objs
     * @throws TwitchException issue from sending request to twitch API
     */
    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    /**
     * Based on provided item type to call different search function
     * @param gameId game id
     * @param type item type needs to be search
     * @param limit search limit
     * @return list of item from twitch
     * @throws TwitchException issue from sending request to twitch API
     */
    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }


    /**
     * Search game streams, clips and videos by call searchByType
     * @param gameId game id
     * @return map (key is search type STREAM, VIDEO and CLIP, value is list of item for each type)
     * @throws TwitchException issue from sending request to twitch API
     */
    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        return itemMap;
    }
}
