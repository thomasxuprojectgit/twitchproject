package com.twitchproject.jupiter.controller;

import com.twitchproject.jupiter.entity.db.Item;
import com.twitchproject.jupiter.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    @Autowired
    private GameService gameService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody  // convert map to json
    public Map<String, List<Item>> search(@RequestParam(value = "game_id") String gameId) {
        return gameService.searchItems(gameId);
    }
}

