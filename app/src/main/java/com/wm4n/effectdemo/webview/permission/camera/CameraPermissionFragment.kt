package com.wm4n.effectdemo.webview.permission.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.wm4n.effectdemo.R
import com.wm4n.effectdemo.databinding.WebviewPermissionFragmentBinding
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

private const val REQUEST_PERMISSION_CAMERA = 6000
private const val REQUEST_PERMISSION_STORAGE = 6001
private const val REQUEST_IMAGE_CAPTURE = 6005
private const val REQUEST_IMAGE_PICK = 6006

class CameraPermissionFragment(private val url: String) : DialogFragment() {

  private var _binding: WebviewPermissionFragmentBinding? = null
  private var imagePathCallback: ValueCallback<Array<Uri>>? = null
  private var cameraImagePath: String? = null
  private var onCameraPermissionGrant : (() -> Unit)? = null
  private var onCameraPermissionReject : (() -> Unit)? = null
  private var onGalleryPermissionGrant : (() -> Unit)? = null
  private var onGalleryPermissionReject : (() -> Unit)? = null

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
    _binding?.webView?.settings?.allowFileAccess = true
    _binding?.webView?.settings?.allowContentAccess = true
    _binding?.webView?.loadUrl(url)
    _binding?.webView?.webChromeClient = getCustomWebChromeClient()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_IMAGE_CAPTURE) {
      if(resultCode == RESULT_OK) {
        Log.i("blah", "cameraImagePath: $cameraImagePath")

        imagePathCallback?.onReceiveValue(arrayOf(Uri.parse(cameraImagePath)))

        val html =
          "<html><head></head><body> <img width=\"360\" height=\"550\" src=\"${cameraImagePath}\"> </body></html>"
        _binding?.webView?.loadDataWithBaseURL("", html, "text/html", "utf-8", "")
      }
      else {
        imagePathCallback?.onReceiveValue(null)
      }
    }
    else if(requestCode == REQUEST_IMAGE_PICK) {
      if(resultCode == RESULT_OK) {
        val dataUri = data?.data ?: return
        val ctx = activity ?: return
        if(ctx.isFinishing) return
        var contentInputStream: InputStream? = null
        var fileOutputStream: OutputStream? = null
        var filePath: String? = null
        try {
          contentInputStream = ctx.contentResolver.openInputStream(dataUri)?.let { inputStream ->
            val file = createImageFile(true)
            fileOutputStream = file?.let {
              val outputStream = FileOutputStream(it)
              val buffer = ByteArray(10240)
              var len: Int
              while (inputStream.read(buffer).also { size -> len = size } > 0) {
                outputStream.write(buffer, 0, len)
              }
              filePath = "file://${file.absolutePath}"
              outputStream
            }
            inputStream
          }
        } catch (e: Exception) {
          Log.w("blah", "Failed picking photo", e)
        } finally {
          contentInputStream?.close()
          fileOutputStream?.close()
          filePath?.let {
            imagePathCallback?.onReceiveValue(arrayOf(Uri.parse(it)))
            val html =
              "<html><head></head><body> <img width=\"360\" src=\"${it}\"> </body></html>"
            _binding?.webView?.loadDataWithBaseURL("", html, "text/html", "utf-8", "")
          }?:imagePathCallback?.onReceiveValue(null)
        }
      }
      else {
        imagePathCallback?.onReceiveValue(null)
      }
    }
  }

  private fun createImageFile(useCacheDir: Boolean): File? {
    activity?.let {
      val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
      val storageDir: File? =
        if(useCacheDir)
          it.externalCacheDir
        else it.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
      return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
      )
    }
    return null
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
    when(requestCode) {
      REQUEST_PERMISSION_CAMERA -> {
        if (isPermissionGrant) {
          onCameraPermissionGrant?.let { it() }
        } else {
          onCameraPermissionReject?.let { it() }
        }
      }
      REQUEST_PERMISSION_STORAGE -> {
        if (isPermissionGrant) {
          onGalleryPermissionGrant?.let { it() }
        } else {
          onGalleryPermissionReject?.let { it() }
        }
      }
    }
  }

  private fun showNoCameraPermissionUI(ctx: Context) {
    AlertDialog.Builder(ctx)
      .setTitle("Missing Permission 😔")
      .setMessage("No permission to save photo from Camera!\nBut not to worry, you can turn on in Settings. 👇")
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

  private fun showNoStoragePermissionUI(ctx: Context) {
    AlertDialog.Builder(ctx)
      .setTitle("Missing Permission 😔")
      .setMessage("No permission to save photo!\nBut not to worry, you can turn on in Settings. 👇")
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

  fun askCameraGalleryPermission(
    onPermissionGrant: () -> Unit,
    onPermissionReject: () -> Unit
  ) {
    onCameraPermissionGrant = onPermissionGrant
    onCameraPermissionReject = onPermissionReject
    context?.let {
      if (checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
          shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
          showNoCameraPermissionUI(it)
          onPermissionReject()
        }
        requestPermissions(
          arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
          REQUEST_PERMISSION_CAMERA
        )
      }
      else {
        onPermissionGrant()
      }
    }
  }

  fun askGalleryPermission(
    onPermissionGrant: () -> Unit,
    onPermissionReject: () -> Unit
  ) {
    onGalleryPermissionGrant = onPermissionGrant
    onGalleryPermissionReject = onPermissionReject
    context?.let {
      if (checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
          showNoStoragePermissionUI(it)
          onPermissionReject()
        }
        requestPermissions(
          arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
          REQUEST_PERMISSION_STORAGE
        )
      }
      else {
        onPermissionGrant()
      }
    }
  }

  private fun getCustomWebChromeClient() = object : WebChromeClient() {

    override fun onShowFileChooser(
      view: WebView?,
      filePath: ValueCallback<Array<Uri>>?,
      fileChooserParams: FileChooserParams?
    ): Boolean {
      val isCamera: Boolean = fileChooserParams?.isCaptureEnabled?:false
      context?.let {
//        fileChooserParams?.run {
//          Log.i(
//            "blah", "fileChooserParams isCaptureEnabled: ${this.isCaptureEnabled}, acceptType: ${
//              TextUtils.join(
//                ",",
//                this.acceptTypes
//              )
//            }, mode: ${this.mode}"
//          )
//        }

        imagePathCallback = filePath

        if(isCamera) {
          askCameraGalleryPermission(
            {
              try {
                dispatchTakePictureIntent()
              } catch (e: ActivityNotFoundException) {
                Toast.makeText(it, "Unable to use Camera", Toast.LENGTH_LONG).show()
                imagePathCallback?.onReceiveValue(null)
              }
            },
            {
              showNoCameraPermissionUI(it)
              imagePathCallback?.onReceiveValue(null)
            })
          return true
        }
        else {
          askGalleryPermission(
            {
              try {
                dispatchPickFileIntent(fileChooserParams?.acceptTypes?.run {
                  TextUtils.join(
                    ",",
                    this
                  )
                })
              } catch (e: ActivityNotFoundException) {
                Toast.makeText(it, "Unable to browse files", Toast.LENGTH_LONG).show()
                imagePathCallback?.onReceiveValue(null)
              }
            }, {
              showNoStoragePermissionUI(it)
              imagePathCallback?.onReceiveValue(null)
            }
          )
          return true
        }
      }
      filePath?.onReceiveValue(null)
      return true
    }

    private fun dispatchTakePictureIntent() {
      activity?.let { holderActivity ->
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
          // Ensure that there's a camera activity to handle the intent
          takePictureIntent.resolveActivity(holderActivity.packageManager)?.also {
            // Create the File where the photo should go
            val photoFile: File? = try {
              createImageFile(false)
            } catch (ex: IOException) {
              // Error occurred while creating the File
              Log.e("blah", "dispatchTakePictureIntent()", ex)
              null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
              cameraImagePath = "file://" + it.absolutePath
              val photoURI: Uri = FileProvider.getUriForFile(
                holderActivity,
                "com.example.android.fileprovider",
                it
              )
              takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
              startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
          }
        }
      }
    }

    private fun dispatchPickFileIntent(type: String?) {
      val intent = Intent()
      intent.type = type?:"image/*"
      intent.action = Intent.ACTION_GET_CONTENT
      startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_IMAGE_PICK)
    }
  }

}