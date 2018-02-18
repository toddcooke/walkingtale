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

package com.android.example.github.binding;

import android.databinding.BindingAdapter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.example.github.aws.ConstantsKt;
import com.android.example.github.ui.common.CreateFileKt;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

/**
 * Binding adapters that work with a fragment instance.
 */
public class FragmentBindingAdapters {
    final Fragment fragment;
    private final String TAG = this.getClass().getSimpleName();
    MediaPlayer mp = new MediaPlayer();

    @Inject
    public FragmentBindingAdapters(Fragment fragment) {
        this.fragment = fragment;
    }

    @BindingAdapter("imageUrl")
    public void bindImage(ImageView imageView, String url) {

        Log.i(TAG, "image url: " + url);
        if (url == null) return;

        TransferUtility transferUtility = TransferUtility.builder()
                .defaultBucket(ConstantsKt.getS3BucketName())
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                .context(fragment.getContext())
                .build();

        File file = null;
        try {
            file = CreateFileKt.createFile(fragment.getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        }
        transferUtility.download(url, file).setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.i(TAG, "state " + state);
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                Log.i(TAG, "error " + ex);

            }
        });

        Glide.with(fragment).load(url).into(imageView);
    }

    @BindingAdapter("audioUrl")
    public void bindAudio(View view, String url) {
        view.setOnClickListener(v -> {
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (mp.isPlaying()) {
                mp.stop();
                mp.reset();
            } else {
                try {
                    mp.setDataSource(url);
                } catch (IOException e) {
                    // Todo: the user should never have to see this, instead: check all urls before starting story
                    Toast.makeText(view.getContext(), "Error: Invalid audio url", Toast.LENGTH_SHORT).show();
                    return;
                }
                mp.prepareAsync();
                mp.setOnPreparedListener(MediaPlayer::start);
            }
        });
    }
}
