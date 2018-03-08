package com.MapPost.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.MapPost.db.WalkingTaleTypeConverters;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;

@DynamoDBTable(tableName = "mappost-mobilehub-452475001-Users")
@Entity(indices = {@Index("userId")}, primaryKeys = {"userId"})
@TypeConverters(WalkingTaleTypeConverters.class)
public class User {
    @NonNull
    private String userId;
    private String userName;
    private List<String> createdPosts;
    private String userImage;
    private String viewedPosts;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(final String _userId) {
        this.userId = _userId;
    }

    @DynamoDBRangeKey(attributeName = "userName")
    @DynamoDBAttribute(attributeName = "userName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(final String _userName) {
        this.userName = _userName;
    }

    @DynamoDBAttribute(attributeName = "createdPosts")
    public List<String> getCreatedPosts() {
        return createdPosts;
    }

    public void setCreatedPosts(final List<String> _createdPosts) {
        this.createdPosts = _createdPosts;
    }

    @DynamoDBAttribute(attributeName = "userImage")
    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(final String _userImage) {
        this.userImage = _userImage;
    }

    @DynamoDBAttribute(attributeName = "viewedPosts")
    public String getViewedPosts() {
        return viewedPosts;
    }

    public void setViewedPosts(final String _viewedPosts) {
        this.viewedPosts = _viewedPosts;
    }

}