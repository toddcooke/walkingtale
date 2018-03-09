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

package com.MapPost

import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.location.Location
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import com.MapPost.ui.common.LocationLiveData
import com.MapPost.vo.Post
import com.MapPost.vo.PostType
import com.MapPost.vo.Status
import com.MapPost.vo.User
import com.amazonaws.mobile.auth.core.IdentityManager
import com.auth0.android.jwt.JWT
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_chapter_list.*
import java.io.File
import java.util.*


class MainActivity :
        AppCompatActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private val TAG = this.javaClass.simpleName
    private lateinit var mMap: GoogleMap
    internal var playServicesErrorDialog: Dialog? = null
    private lateinit var mainViewModel: MainViewModel
    private lateinit var location: LatLng
    private var file: File? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var editText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(this.layoutInflater.inflate(R.layout.bottom_sheet_chapter_list, bottom_sheet))
        editText = bottomSheetDialog.findViewById<TextInputEditText>(R.id.post_edit_text)!!
        Analytics.init(this)
        userSetup(savedInstanceState)
        cameraButton()
        audioButton()
        textButton()
        myLocationButton()
        nearbyPosts()
    }

    private fun nearbyPosts() {
        mainViewModel.getNearbyPosts().observe(this, Observer {

        })
    }

    private fun textButton() {
        text_button.setOnClickListener({
            //            bottomSheetDialog.show()
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(editText, SHOW_IMPLICIT)
            val post = Post()
            post.postId = UUID.randomUUID().toString()
            post.userId = MainActivity.cognitoId
            post.content = "nice"
            post.type = PostType.TEXT
            post.latitude = location.latitude
            post.longitude = location.longitude
            post.dateTime = Date().time.toString()
            Log.i(TAG, post.toString())
            mainViewModel.putPost(post).observe(this, Observer {
                if (it != null && it.status == Status.SUCCESS) {
                    mainViewModel.
                }
            })
        })
    }

    private fun audioButton() {
        audio_button.setOnClickListener({

        })
    }

    private fun cameraButton() {
        camera_button.setOnClickListener({

        })
    }

    private fun myLocationButton() {
        my_location_button.setOnClickListener({
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().zoom(DEFAULT_ZOOM).target(location).build()))
        })
    }

    private fun locationListener() {
        LocationLiveData(this).observe(this, Observer {
            if (it != null) location = locationToLatLng(it)
        })
    }

    /**
     * Get user if they exist in the dynamo db
     * Put user if they do not exist
     */
    private fun userSetup(savedInstanceState: Bundle?) {
        mainViewModel.getUser(cognitoId).observe(this, Observer { userResource ->
            if (userResource != null) {
                when (userResource.status) {
                    Status.ERROR -> createNewUser()
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        Analytics.logEvent(Analytics.EventType.UserLogin, TAG)
                        if (savedInstanceState == null) {

                        }
                    }
                }
            }
        })
    }

    private fun createNewUser() {
        val user = User()
        user.userId = cognitoId
        user.userName = cognitoUsername
        user.createdPosts = listOf()
        user.viewedPosts = listOf()
        user.userImage = "none"
        mainViewModel.putUser(user).observe(this, Observer {
            if (it != null) {
                when (it.status) {
                    Status.SUCCESS -> {
                        Analytics.logEvent(Analytics.EventType.CreatedUser, TAG)
                    }
                    Status.ERROR -> TODO()
                    Status.LOADING -> TODO()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * Prevents user from using the app unless they have google play services installed.
     * Not having it will prevent the google map from working.
     */
    private fun checkPlayServices() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                if (playServicesErrorDialog == null) {
                    playServicesErrorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404)
                    playServicesErrorDialog!!.setCancelable(false)
                }

                if (!playServicesErrorDialog!!.isShowing)
                    playServicesErrorDialog!!.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    enum class DEBUG_STATE {
        OFF, CREATE, PLAY, PROFILE
    }

    private fun initCurrentChapterObserver() {
//        playViewModel!!.posts.observe(this, Observer { listResource ->
//            if (listResource == null || listResource!!.data == null) return@Observer
//
//            for (post in listResource!!.data!!) {
//
//                val iconGenerator = IconGenerator(this)
//                val imageView = ImageView(this)
//                imageView.layoutParams = ViewGroup.LayoutParams(PlayFragment.MARKER_WIDTH, PlayFragment.MARKER_HEIGHT)
//                val markerOptions: MarkerOptions
//                val location = LatLng(post.latitude!!, post.longitude!!)
//
//                when (post.type) {
//                    PostType.TEXT -> {
//                        imageView.setImageResource(R.drawable.ic_textsms_black_24dp)
//                        iconGenerator.setContentView(imageView)
//                        markerOptions = MarkerOptions()
//                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
//                                .position(location)
//                        markers.add(mMap!!.addMarker(markerOptions))
//                    }
//                    PostType.AUDIO -> {
//                        imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp)
//                        iconGenerator.setContentView(imageView)
//                        markerOptions = MarkerOptions()
//                                .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
//                                .position(location)
//                        markers.add(mMap!!.addMarker(markerOptions))
//                    }
////                    PostType.PICTURE -> Glide.with(this!!)
////                            .asBitmap()
////                            .load(s3HostName + post.content!!)
////                            .apply(RequestOptions().centerCrop())
////                            .into<Bitmap>(object : SimpleTarget<Bitmap>(MARKER_WIDTH, MARKER_HEIGHT) {
////                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
////                                    imageView.setImageBitmap(resource)
////                                    iconGenerator.setContentView(imageView)
////                                    val markerOptions = MarkerOptions()
////                                            .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()))
////                                            .position(location)
////                                    markers.add(mMap!!.addMarker(markerOptions))
////                                }
////                            })
//                }
//            }
//
//        })
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        if (PermissionManager.checkLocationPermission(this)) {
            mMap.isMyLocationEnabled = true
            locationListener()
        } else {
//            finish()
        }
        mMap.setOnMarkerClickListener(this)

        // Change tilt
        val cameraPosition = CameraPosition.Builder()
                .target(mMap.cameraPosition.target)
                .tilt(60f).build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        val mUiSettings = mMap.uiSettings
        mUiSettings.isMapToolbarEnabled = false
        mUiSettings.isZoomControlsEnabled = false
        mUiSettings.isScrollGesturesEnabled = true
        mUiSettings.isZoomGesturesEnabled = true
        mUiSettings.isTiltGesturesEnabled = false
        mUiSettings.isRotateGesturesEnabled = false
        mUiSettings.isCompassEnabled = false
        mUiSettings.isMyLocationButtonEnabled = false
    }

    companion object {

        fun locationToLatLng(location: Location): LatLng {
            return LatLng(location.latitude, location.longitude)
        }

        fun latLngToLocation(latLng: LatLng): Location {
            val location = Location("")
            location.latitude = latLng.latitude
            location.longitude = latLng.longitude
            return location
        }

        val DEFAULT_ZOOM = 18f
        val DEBUG_MODE = DEBUG_STATE.OFF

        val cognitoId: String
            get() = IdentityManager.getDefaultIdentityManager().cachedUserID

        val cognitoUsername: String?
            get() {
                val cognitoToken = IdentityManager.getDefaultIdentityManager().currentIdentityProvider.token
                val jwt = JWT(cognitoToken)
                val username = jwt.getClaim("cognito:username").asString()
                return username ?: jwt.getClaim("given_name").asString()
            }
    }
}