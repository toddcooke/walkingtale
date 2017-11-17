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

package com.android.example.github.ui.overview;

import com.android.example.github.repository.RepoRepository;
import com.android.example.github.util.AbsentLiveData;
import com.android.example.github.util.Objects;
import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import java.util.List;

import javax.inject.Inject;

public class OverviewViewModel extends ViewModel {
    @VisibleForTesting
    private final MutableLiveData<Integer> repoId;
    private final LiveData<Resource<Repo>> repo;

    @Inject
    public OverviewViewModel(RepoRepository repository) {
        this.repoId = new MutableLiveData<>();
        repo = Transformations.switchMap(repoId, input -> {
            if (input == null) {
                return AbsentLiveData.create();
            }
            return repository.loadRepo(input);
        });
    }

    public LiveData<Resource<Repo>> getRepo() {
        return repo;
    }

    void setId(Integer id) {
        repoId.setValue(id);
    }
}
