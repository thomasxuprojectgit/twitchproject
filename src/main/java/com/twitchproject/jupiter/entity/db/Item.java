package com.twitchproject.jupiter.entity.db;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity // Used with model class to specify that it is an entity and mapped to a table in the database.
@Table(name = "items") // Used with entity class to define the corresponding table name in the database.
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item implements Serializable {

    //https://www.baeldung.com/java-serial-version-uid
    private static final long serialVersionUID = 1L; //  correspond to table's version ID in database

    @Id // primary key
    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("url")
    private String url;

    @Column(name = "thumbnail_url")  // need to provide column name, if column name different from variable name
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "broadcaster_name")
    @JsonProperty("broadcaster_name")
    @JsonAlias({ "user_name" })
    private String broadcasterName;

    @Column(name = "game_id")
    @JsonProperty("game_id")
    private String gameId;

    @Enumerated(value = EnumType.STRING) // enum to table field
    @JsonProperty("item_type")
    private ItemType type;

    @JsonIgnore
    @ManyToMany(mappedBy = "itemSet") // Between two entities where one can have relations with multiple other entity instances, for example, item and user relationships on the first project.
    private Set<User> users= new HashSet<>();

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getBroadcasterName() {
        return broadcasterName;
    }

    public void setBroadcasterName(String broadcasterName) {
        this.broadcasterName = broadcasterName;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }
}

