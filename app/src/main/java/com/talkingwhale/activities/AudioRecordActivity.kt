package com.talkingwhale.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.talkingwhale.R
import com.talkingwhale.util.createFile
import kotlinx.android.synthetic.main.activity_audio_record.*
import java.io.File
import java.io.IOException

/**
 * Records an audio clip
 */
class AudioRecordActivity : AppCompatActivity() {

    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private lateinit var audioFile: File
    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var mStartRecording = true
    private var mStartPlaying = true

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_record)
        audioFile = createFile(this)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recordButton()
        playButton()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_audio_record, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_finish_audio) {
            finishAudio()
            return true
        }
        return false
    }

    private fun onRecord(start: Boolean) {
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun onPlay(start: Boolean) {
        if (start) {
            startPlaying()
        } else {
            stopPlaying()
        }
        mStartPlaying = !mStartPlaying
    }

    private fun startPlaying() {
        mPlayer = MediaPlayer()
        try {
            mPlayer!!.setDataSource(audioFile.absolutePath)
            mPlayer!!.prepare()
            mPlayer!!.start()
            button_audio_play.setImageDrawable(resources.getDrawable(R.drawable.ic_stop_black_24dp, theme))
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
        mPlayer?.setOnCompletionListener {
            onPlay(false)
        }
    }

    private fun stopPlaying() {
        mPlayer?.release()
        mPlayer = null
        button_audio_play.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_black_24dp, theme))
    }

    private fun startRecording() {
        // From https://stackoverflow.com/questions/5010145/very-poor-quality-of-audio-recorded-on-my-droidx-using-mediarecorder-why
        val bitDepth = 16
        val sampleRate = 44100
        val bitRate = sampleRate * bitDepth
        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mRecorder!!.setAudioEncodingBitRate(bitRate)
        mRecorder!!.setAudioSamplingRate(sampleRate)
        mRecorder!!.setOutputFile(audioFile.absolutePath)

        try {
            mRecorder!!.prepare()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }

        mRecorder!!.start()
        button_audio_record.setImageDrawable(resources.getDrawable(R.drawable.ic_stop_black_24dp, theme))
    }

    private fun stopRecording() {
        mRecorder!!.stop()
        mRecorder!!.release()
        mRecorder = null
        button_audio_record.setImageDrawable(resources.getDrawable(R.drawable.ic_mic_black_24dp, theme))
    }

    public override fun onStop() {
        super.onStop()
        if (mRecorder != null) {
            mRecorder!!.release()
            mRecorder = null
        }

        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
        }
    }

    private fun recordButton() {
        button_audio_record.setOnClickListener {
            onRecord(mStartRecording)
            mStartRecording = !mStartRecording
        }
    }

    private fun playButton() {
        button_audio_play.setOnClickListener {
            onPlay(mStartPlaying)
        }
    }

    private fun finishAudio() {
        val result = Intent()
        var resultData: Uri? = null
        try {
            resultData = Uri.fromFile(audioFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        result.data = resultData
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    companion object {
        private const val LOG_TAG = "AudioRecordActivity"
        private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}