package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.email_layout.view.*
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
                Toast.makeText(this, "Không được để trống email hoặc mật khẩu", Toast.LENGTH_LONG)
                    .show()
            } else {
                login(log_email.text.toString(), log_password.text.toString())
            }
        }

        log_forgot_password.setOnClickListener {
            val myLayoutInflater = layoutInflater.inflate(R.layout.email_layout, null)

            val dialog = AlertDialog.Builder(this)
                .setView(myLayoutInflater)
                .setCancelable(true)
                .show()

            myLayoutInflater.el_button.setOnClickListener {
                val em = myLayoutInflater.el_email.text.toString().trim()
                if (em.isNotEmpty() && em.isNotBlank()) {
                    sendEmail(em)
                    dialog.dismiss()
                } else {
                    myLayoutInflater.el_email.setText("")
                    Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_LONG).show()
                }
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
                    val errorString = task.exception.toString()
                    when {
                        errorString.contains("The email address is badly formatted") -> {
                            Toast.makeText(this, "Email không đúng định dạng", Toast.LENGTH_LONG)
                                .show()
                        }
                        errorString.contains("There is no user record") -> {
                            Toast.makeText(this, "Email không tồn tại", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(
                                    this,
                                    "Có lỗi xảy ra. Vui lòng thử lại",
                                    Toast.LENGTH_LONG
                                )
                                .show()
                        }
                    }
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
