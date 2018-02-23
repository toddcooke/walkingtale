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

package com.android.example.github;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.android.example.github.ui.common.NavigationController;
import com.android.example.github.vo.User;
import com.auth0.android.jwt.JWT;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    public static final boolean DEBUG_MODE = false;
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    @Inject
    NavigationController navigationController;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    Dialog playServicesErrorDialog;
    private MainViewModel mainViewModel;
    private Toolbar toolbar;

    public static String getCognitoToken() {
        return IdentityManager.getDefaultIdentityManager().getCurrentIdentityProvider().getToken();
    }

    public static String getCognitoId() {
        return IdentityManager.getDefaultIdentityManager().getCachedUserID();
    }

    public static String getCognitoUsername() {
        return new JWT(getCognitoToken()).getClaim("cognito:username").asString();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        userSetup(savedInstanceState);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        menuClickListener();
        showToolbar();
    }

    private void menuClickListener() {
        toolbar.setTitleTextColor(getColor(R.color.white));
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_profile:
                    navigationController.navigateToProfile();
                    break;
                case R.id.action_create:
                    navigationController.navigateToCreateStory();
                    break;
                case R.id.action_search:
                    navigationController.navigateToSearch();
                    break;
            }
            return true;
        });
    }

    /**
     * Get user if they exist in the dynamo db
     * Put user if they do not exist
     */
    private void userSetup(Bundle savedInstanceState) {
        mainViewModel.getUser(getCognitoId()).observe(this, userResource -> {
            if (userResource != null) {
                switch (userResource.status) {
                    case ERROR:
                        createNewUser();
                        break;
                    case LOADING:
                        break;
                    case SUCCESS:
                        navigationController.navigateToStoryFeed();
                        break;
                }
            }
        });
    }

    private void createNewUser() {
        mainViewModel.createUser(new User(
                getCognitoId(),
                new ArrayList<>(),
                new ArrayList<>(),
                getCognitoUsername(),
                "none"
        )).observe(this, newUserResource -> {
            if (newUserResource != null) {
                switch (newUserResource.status) {
                    case SUCCESS:
                        navigationController.navigateToStoryFeed();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    /**
     * Prevents user from using the app unless they have google play services installed.
     * Not having it will prevent the google map from working.
     */
    private void checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                if (playServicesErrorDialog == null) {
                    playServicesErrorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404);
                    playServicesErrorDialog.setCancelable(false);
                }

                if (!playServicesErrorDialog.isShowing())
                    playServicesErrorDialog.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    public void showToolbar() {
        toolbar.setVisibility(View.VISIBLE);
    }

    public void hideToolbar() {
        toolbar.setVisibility(View.GONE);
    }
}