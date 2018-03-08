package com.MapPost;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.MapPost.repository.PostRepository;
import com.MapPost.repository.UserRepository;
import com.MapPost.vo.Resource;
import com.MapPost.vo.User;

import javax.inject.Inject;

public class MainViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Inject
    MainViewModel(PostRepository repository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.postRepository = repository;
    }

    LiveData<Resource<User>> getUser(String userId) {
        return userRepository.loadUser(userId);
    }

    LiveData<Resource<Void>> createUser(User user) {
        return userRepository.putUser(user);
    }
}
