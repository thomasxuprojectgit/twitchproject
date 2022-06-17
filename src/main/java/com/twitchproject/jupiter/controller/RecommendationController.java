package com.twitchproject.jupiter.controller;

import com.twitchproject.jupiter.entity.db.Item;
import com.twitchproject.jupiter.service.RecommendationException;
import com.twitchproject.jupiter.service.RecommendationService;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @RequestMapping(value = "/recommendation", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<Item>> recommendation(HttpServletRequest request) throws ServletException {
        HttpSession session = request.getSession(false);


        Map<String, List<Item>> itemMap;

        try {
            // if user does not log in, use top game from twitch to get recommendation
            if (session == null) {
                itemMap = recommendationService.recommendItemsByDefault();

            // if user log in, use user's favorite games to get recommendation
            } else {
                String userId = (String) session.getAttribute("user_id");
                itemMap = recommendationService.recommendItemsByUser(userId);
            }
        } catch (RecommendationException e) {
            throw new ServletException(e);
        }

        return itemMap;
    }
}

