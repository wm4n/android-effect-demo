package com.wm4n.effectdemo.webview.permission.geolocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.wm4n.effectdemo.R
import com.wm4n.effectdemo.databinding.WebviewPermissionFragmentBinding

private const val REQUEST_PERMISSION_GEOLOCATION = 5000

class GeolocationPermissionFragment(private val url: String) : DialogFragment() {

  private var _binding : WebviewPermissionFragmentBinding? = null
  private var onGeolocationPermissionCallback : GeolocationPermissions.Callback? = null
  private var onGeoPermissionGrant : (() -> Unit)? = null
  private var onGeoPermissionReject : (() -> Unit)? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setStyle(
      STYLE_NORMAL,
      R.style.BottomDialogFragment
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = WebviewPermissionFragmentBinding.inflate(inflater, container, false)
    return _binding?.viewRoot
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    WebView.setWebContentsDebuggingEnabled(true)
    _binding?.webView?.settings?.javaScriptEnabled = true
    _binding?.webView?.settings?.setGeolocationEnabled(true)
    _binding?.webView?.loadUrl(url)

    _binding?.webView?.webChromeClient = getCustomWebChromeClient()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode == REQUEST_PERMISSION_GEOLOCATION) {
      var isPermissionGrant = true
      if (grantResults.isNotEmpty()) {
        for (result in grantResults) {
          if (result != PackageManager.PERMISSION_GRANTED) {
            // one of permission is not grant
            isPermissionGrant = false
            break
          }
        }
      } else {
        // empty means no permission
        isPermissionGrant = false
      }
      if (isPermissionGrant) {
        onGeoPermissionGrant?.let { it() }
      } else {
        onGeoPermissionReject?.let { it() }
      }
    }
  }

  private fun showNoGeoPermissionUI(ctx: Context) {
    AlertDialog.Builder(ctx)
      .setTitle("Missing Permission 😔")
      .setMessage("No permission to retrieve location!\nBut not to worry, you can turn on in Settings. 👇")
      .setPositiveButton("Open Settings") { dialog, _ ->
        val uri = Uri.fromParts("package", ctx.packageName, null)
        val intent = Intent().apply {
          action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
          data = uri
        }
        ctx.startActivity(intent)
        dialog.dismiss()
      }
      .show()
  }

  fun askGeolocationPermission(
    onPermissionGrant: () -> Unit,
    onPermissionReject: () -> Unit
  ) {
    onGeoPermissionGrant = onPermissionGrant
    onGeoPermissionReject = onPermissionReject
    context?.let {
      if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
          shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
          showNoGeoPermissionUI(it)
          onPermissionReject()
        }
        requestPermissions(
          arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
          REQUEST_PERMISSION_GEOLOCATION
        )
      }
      else {
        onPermissionGrant()
      }
    }
  }

  private fun getCustomWebChromeClient() = object : WebChromeClient() {

    override fun onGeolocationPermissionsShowPrompt(
      origin: String,
      callback: GeolocationPermissions.Callback
    ) {
      super.onGeolocationPermissionsShowPrompt(origin, callback)
      onGeolocationPermissionCallback = callback

      context?.let {
        askGeolocationPermission(
          {
            onGeolocationPermissionCallback?.invoke(origin, true, false)
          },
          {
            showNoGeoPermissionUI(it)
            onGeolocationPermissionCallback?.invoke(origin, false, false)
          }
        )
      }
    }
  }
}