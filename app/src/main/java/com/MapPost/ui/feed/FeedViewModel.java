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

package com.MapPost.ui.feed;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.MapPost.repository.PostRepository;
import com.MapPost.vo.Resource;
import com.MapPost.vo.Story;

import java.util.List;

import javax.inject.Inject;

public class FeedViewModel extends ViewModel {

    private final String TAG = this.getClass().getSimpleName();
    LiveData<Resource<List<Story>>> results = new MutableLiveData<>();
    private PostRepository postRepository;
    private MutableLiveData<Boolean> shouldFetch = new MutableLiveData<>();

    @Inject
    FeedViewModel(PostRepository postRepository) {
        this.postRepository = postRepository;
        this.shouldFetch.setValue(false);
        results = Transformations.switchMap(shouldFetch, postRepository::getFeedStories);
    }

    void setShouldFetch() {
        shouldFetch.setValue(true);
    }
}
