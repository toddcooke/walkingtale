/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.github.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.android.example.github.db.GithubTypeConverters;

import java.util.List;

@Entity(primaryKeys = "id")
@TypeConverters(GithubTypeConverters.class)
public class User {

    @NonNull
    public String id;
    public List<String> createdStories;
    public List<String> playedStories;

    public User(@NonNull String id, List<String> createdStories, List<String> playedStories) {
        this.id = id;
        this.createdStories = createdStories;
        this.playedStories = playedStories;
    }
}
