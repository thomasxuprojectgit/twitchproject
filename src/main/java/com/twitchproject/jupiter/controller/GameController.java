package com.twitchproject.jupiter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitchproject.jupiter.service.GameService;
import com.twitchproject.jupiter.service.TwitchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * GameController will receive info from user browser (user browser send URL request) to know user's need
 * Then GameController call GameService methods to request from twitch API
 */
// Use @Controller to mark a class its role as a web component
@Controller
public class GameController {

    private GameService gameService;

    /**
     * Constructor, use spring dependency injection to create GameService obj and
     * inject to GameController constructor
     * @Autowired mark this contractor to use spring dependency injection
     * @param gameService GameService obj
     */
    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @RequestMapping(value = "/game", method = RequestMethod.GET)
    public void getGame(@RequestParam(value = "game_name", required = false) String gameName, HttpServletResponse response)
        throws IOException, ServletException {

        // set response type is json to user browser
        response.setContentType("application/json;charset=UTF-8");
        try {
            // Return the dedicated game information if gameName is provided in the request URL, otherwise return the top x games.
            // if user want a specific game, gameName is not null
            if (gameName != null) {
                response.getWriter().print(new ObjectMapper().writeValueAsString(gameService.searchGame(gameName)));

            // if user want top games
            } else {
                // if limit is 0, default is 20, see topGames method under GameService
                response.getWriter().print(new ObjectMapper().writeValueAsString(gameService.topGames(0)));
            }
        } catch (TwitchException e) {
            throw new ServletException(e);
        }
    }
}
