package com.camera.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.camera.R
import com.camera.databinding.ActivityVideoBinding
import com.camera.ui.utils.KohiiProvider
import com.camera.ui.utils.URIPathHelper
import kohii.v1.core.Common
import kohii.v1.core.MemoryMode
import kohii.v1.core.Playback
import kohii.v1.exoplayer.Kohii
import kotlinx.android.synthetic.main.activity_video.*
import java.io.File


class VideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoBinding
    private val PERMISSION_CODE = 1000;
    private lateinit var kohii: Kohii

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)
        initControls()

    }

    private fun initControls() {
        binding.lifecycleOwner = this
        kohii = KohiiProvider.get(this)
        kohii.register(this, memoryMode = MemoryMode.HIGH)
            .addBucket(rlSelectVideo)

        capture_btn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    //permission was not enabled
                    val permission = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    //show popup to request permission
                    requestPermissions(permission, PERMISSION_CODE)
                } else {
                    //permission already granted
                    openCameraToCaptureVideo()
                }
            } else {
                //system os is < marshmallow
                openCameraToCaptureVideo()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //called when user presses ALLOW or DENY from Permission Request Popup
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup was granted
                    openCameraToCaptureVideo()
                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openCameraToCaptureVideo() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) { // First check if camera is available in the device
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            startActivityForResult(intent, 1);
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            if (intent?.data != null) {
                val uriPathHelper = URIPathHelper()
                val videoFullPath = uriPathHelper.getPath(
                    this,
                    intent.data!!
                ) // Use this video path according to your logic
                // if you want to play video just after recording it to check is it working (optional)
                if (videoFullPath != null) {

                    kohii.setUp(videoFullPath) {
                        preload = true
                        repeatMode = Common.REPEAT_MODE_ONE
                        controller = object : Playback.Controller {
                            override fun kohiiCanStart(): Boolean = true
                            override fun kohiiCanPause(): Boolean = true

                        }
                    }.bind(IvVideoThumb)


                }
            }
        }
    }
}