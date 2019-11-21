package com.thien.movieplus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.progress.view.*

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        supportActionBar?.title = "Đăng kí tài khoản"

        sign_link_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        sign_button.setOnClickListener {
            if (sign_email.text.isEmpty() || sign_password.text.isEmpty()) {
                Snackbar.make(
                    sign_layout,
                    "Không được để trống email hoặc mật khẩu",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                if (!isEmailValid(sign_email.text.toString())) {
                    Snackbar.make(
                        sign_layout,
                        "Email không đúng định dạng",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    if (!isPasswordValid(sign_password.text.toString())) {
                        Snackbar.make(
                            sign_layout,
                            "Mật khẩu phải có tối thiểu 6 kí tự",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        createAccount(sign_email.text.toString(), sign_password.text.toString())
                    }
                }
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        val myLayout = layoutInflater.inflate(R.layout.progress, null)
        myLayout.p_text.text = "Đang tạo tài khoản ..."
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(myLayout)
            .create()
        dialog.show()
        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    dialog.dismiss()
                    Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        "Tạo tài khoản không thành công. Vui lòng thử lại",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
}
