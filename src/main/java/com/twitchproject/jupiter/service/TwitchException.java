package com.twitchproject.jupiter.service;

// build special exception to throw possible issue from twitch (seperate from other issue)
public class TwitchException extends RuntimeException {
    public TwitchException(String errorMessage) {
        super(errorMessage);
    }
}
