package com.android.example.github.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.android.example.github.ui.album.AlbumViewModel;
import com.android.example.github.ui.feed.FeedViewModel;
import com.android.example.github.ui.overview.OverviewViewModel;
import com.android.example.github.ui.play.PlayViewModel;
import com.android.example.github.ui.create.CreateViewModel;
import com.android.example.github.viewmodel.GithubViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(OverviewViewModel.class)
    abstract ViewModel bindRepoViewModel(OverviewViewModel overviewViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(CreateViewModel.class)
    abstract ViewModel bindStoryCreateViewModel(CreateViewModel createViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(FeedViewModel.class)
    abstract ViewModel bindStoryFeedViewModel(FeedViewModel feedViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PlayViewModel.class)
    abstract ViewModel bindStoryPlayViewModel(PlayViewModel playViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AlbumViewModel.class)
    abstract ViewModel bindExpositionViewerViewModel(AlbumViewModel albumViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(GithubViewModelFactory factory);
}
