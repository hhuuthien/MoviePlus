package com.thien.movieplus

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_permission.*
import java.io.File

class PermissionActivity : AppCompatActivity() {

    var downloadID = 0.toLong()

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                Toast.makeText(this@PermissionActivity, "Đã lưu hình ảnh", Toast.LENGTH_LONG)
                    .show()
                progress_circular.visibility = GONE
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        setSupportActionBar(m_toolbar)
        progress_circular.visibility = VISIBLE

        if (intent.getStringExtra("type") == "toChangeImage") {
            requirePermission1()
        } else if (intent.getStringExtra("type") == "toDownloadImage") {
            requirePermission2()
        }

        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun requirePermission1() {
        progress_circular.visibility = GONE
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1111
        )
    }

    private fun requirePermission2() {
        progress_circular.visibility = GONE
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            2222
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1111 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    progress_circular.visibility = VISIBLE
                    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this)
                } else {
                    //denied
                    progress_circular.visibility = GONE
                    Snackbar.make(
                        permissionLayout,
                        "Để chỉnh sửa ảnh đại diện, hãy cấp quyền cho ứng dụng truy cập vào bộ nhớ của thiết bị.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Đồng ý") {
                        requirePermission1()
                    }.setTextColor(Color.BLACK).setActionTextColor(Color.BLACK).show()
                }
                return
            }
            2222 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    download(intent.getStringExtra("imageString")!!)
                } else {
                    //denied
                    progress_circular.visibility = GONE
                    Snackbar.make(
                        permissionLayout,
                        "Để lưu hình ảnh, hãy cấp quyền cho ứng dụng truy cập vào bộ nhớ của thiết bị.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Đồng ý") {
                        requirePermission2()
                    }.setTextColor(Color.BLACK).setActionTextColor(Color.BLACK).show()
                }
                return
            }
        }
    }

    @SuppressLint("SdCardPath")
    private fun download(path: String) {
        progress_circular.visibility = VISIBLE
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            val f = File("/sdcard/Movius")
            if (!f.exists()) f.mkdirs()

            val timestamp = System.currentTimeMillis().toString()
            val fileName = "$timestamp.jpg"
            val link = "https://image.tmdb.org/t/p/original$path"
            val uri = Uri.parse(link)
            val request = DownloadManager.Request(uri)
            request.setTitle(fileName)
                .setDescription("Đang tải")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir("/Movius", fileName)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadID = downloadManager.enqueue(request)
        } else {
            val timestamp = System.currentTimeMillis().toString()
            val fileName = "$timestamp.jpg"
            val link = "https://image.tmdb.org/t/p/original$path"
            val uri = Uri.parse(link)
            val request = DownloadManager.Request(uri)
            request.setTitle(fileName)
                .setDescription("Đang tải")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadID = downloadManager.enqueue(request)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Vui lòng chờ", Toast.LENGTH_LONG).show()

                val resultUri = result.uri

                val currentUserUID = FirebaseAuth.getInstance().currentUser!!.uid
                val timestamp = System.currentTimeMillis().toString()

                val storage = FirebaseStorage.getInstance()
                val ref = storage.reference.child("images/${currentUserUID}_${timestamp}.jpg")
                val uploadTask = ref.putFile(resultUri)
                uploadTask.addOnSuccessListener {
                    val urlTask = uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        ref.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                            savetoDatabase(downloadUri.toString())
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_LONG)
                        .show()
                    finish()
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_LONG).show()
                finish()
            } else {
                finish()
            }
        }
    }

    private fun savetoDatabase(url: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference(FirebaseAuth.getInstance().currentUser!!.uid).child("user_image")
        ref.setValue(url).addOnSuccessListener {
            Toast.makeText(this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }
}
