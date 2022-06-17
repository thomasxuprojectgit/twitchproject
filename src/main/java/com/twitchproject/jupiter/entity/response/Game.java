package com.twitchproject.jupiter.entity.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

// when transferring from json file to Game obj, if field of json file from twich api contains field that does not cover, ignore the fields
@JsonIgnoreProperties(ignoreUnknown = true)
// if obj field is null, ignore when transferring from Game obj to json file
@JsonInclude(JsonInclude.Include.NON_NULL)
//  use builder to finish when transferring from json file to Game obj
@JsonDeserialize(builder = Game.Builder.class)
public class Game {

    // JsonProperty let json field id 1 to 1 correspondence to id field to this Game obj
    @JsonProperty("id")
    private final String id;

    // JsonProperty let json field name 1 to 1 correspondence to name field to this Game obj
    @JsonProperty("name")
    private final String name;

    // JsonProperty let json field box_art_url 1 to 1 correspondence to boxArtUrl field to this Game obj
    @JsonProperty("box_art_url")
    private final String boxArtUrl;

    private Game(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.boxArtUrl = builder.boxArtUrl;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBoxArtUrl() {
        return boxArtUrl;
    }

    // when transferring from json file to Game obj, if field of json file from twich api contains field that does not cover, ignore the fields
    @JsonIgnoreProperties(ignoreUnknown = true)
    // if obj field is null, ignore when transferring from Game obj to json file
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Builder {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("box_art_url")
        private String boxArtUrl;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder boxArtUrl(String boxArtUrl) {
            this.boxArtUrl = boxArtUrl;
            return this;
        }

        public Game build() {
            return new Game(this);
        }
    }


}
