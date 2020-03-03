package com.thien.movieplus

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.progress.view.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        log_link_signup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        log_button.setOnClickListener {
            if (log_email.text.isEmpty() || log_password.text.isEmpty()) {
                Snackbar.make(
                    log_layout,
                    "Không được để trống email hoặc mật khẩu",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                login(log_email.text.toString(), log_password.text.toString())
            }
        }

        log_forgot_password.setOnClickListener {
            if (log_email.text.isEmpty()) {
                Snackbar.make(
                    log_layout,
                    "Hãy nhập địa chỉ email để lấy lại mật khẩu",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                val dialog = AlertDialog.Builder(this)
                    .setMessage("Movie Plus sẽ gửi một email đến địa chỉ ${log_email.text}. Bạn hãy kiểm tra hộp thư của mình và làm theo hướng dẫn trong email để khôi phục mật khẩu.")
                    .setPositiveButton("Đồng ý") { _, _ ->
                        sendEmail(log_email.text.toString())
                    }
                    .setNegativeButton("Nhập email khác") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(true)
                    .create()
                dialog.show()
            }
        }
    }

    private fun sendEmail(email: String) {
        auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đã gửi email", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Có lỗi xảy ra. Vui lòng thử lại", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun login(email: String, password: String) {
        val myLayout = layoutInflater.inflate(R.layout.progress, null)
        myLayout.p_text.text = "Đang đăng nhập ..."
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(myLayout)
            .create()
        dialog.show()
        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    dialog.dismiss()
                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        "Đăng nhập không thành công. Vui lòng thử lại",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
