package com.thien.movieplus

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
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


class PermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        requirePermission()
    }

    private fun requirePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1111
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
                    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(this)
                } else {
                    //denied
                    Snackbar.make(
                        permissionLayout,
                        "Để chỉnh sửa ảnh đại diện, hãy cấp quyền cho ứng dụng truy cập vào bộ nhớ của thiết bị.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Đồng ý") {
                        requirePermission()
                    }.setActionTextColor(Color.WHITE).show()
                }
                return
            }
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
}
