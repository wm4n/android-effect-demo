package com.wm4n.effectdemo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.wm4n.effectdemo.databinding.ActivityMainBinding
import com.wm4n.effectdemo.dialog.BottomDialogFragment
import com.wm4n.effectdemo.notification.NotificationHelper
import com.wm4n.effectdemo.webview.permission.camera.CameraPermissionFragment
import com.wm4n.effectdemo.webview.permission.geolocation.GeolocationPermissionFragment

class MainActivity : AppCompatActivity() {

  companion object {
    const val SNACK_BAR_MESSAGE = "SnackBar Message"
  }
  lateinit var mBinding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mBinding = ActivityMainBinding.inflate(layoutInflater)
    val view = mBinding.root
    setContentView(view)

    mBinding.buttonBottomDialogFragment.setOnClickListener {
      val f = BottomDialogFragment("https://wm4n.github.io")
      f.show(supportFragmentManager, BottomDialogFragment::javaClass.name)
    }

    mBinding.buttonUpdatableTabs.setOnClickListener {
      val intent = Intent(this, ContainerActivity::class.java).apply {
        putExtra("fragment", "UpdatableTab")
      }
      startActivity(intent)
    }

    mBinding.buttonCustomHeadsUpNotification.setOnClickListener {
      NotificationHelper.showCustomHeadsUpNotification(this, "Incoming Call From: Mom")
    }

    mBinding.buttonWebviewGeolocationPermission.setOnClickListener {
      val f = GeolocationPermissionFragment("https://coding-story.web.app/geolocation.html")
      f.show(supportFragmentManager, GeolocationPermissionFragment::class.java.simpleName)
    }

    mBinding.buttonWebviewCameraPermission.setOnClickListener {
      val f = CameraPermissionFragment("https://coding-story.web.app/camera.html")
      f.show(supportFragmentManager, CameraPermissionFragment::class.java.simpleName)
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    intent?.run {
      val message = intent.getStringExtra(SNACK_BAR_MESSAGE)
      message?.run {
        if(!TextUtils.isEmpty(message)) {
          Snackbar.make(mBinding.root, message, Snackbar.LENGTH_SHORT).show()
        }
      }
      val notificationId = intent.getIntExtra(NotificationHelper.TAG_NOTIFICATION_ID, 0)
      if(notificationId != 0) {
        NotificationHelper.cancelNotification(this@MainActivity, notificationId)
      }
      removeExtra(SNACK_BAR_MESSAGE)
      removeExtra(NotificationHelper.TAG_NOTIFICATION_ID)
    }
  }
}