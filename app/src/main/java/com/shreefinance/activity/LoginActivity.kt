package com.shreefinance.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.shreefinance.api.LoginResponse
import com.shreefinance.api.RetrofitClient
import com.shreefinance.api.hideLoading
import com.shreefinance.api.showLoading
import com.shreefinance.databinding.ActivityLoginBinding
import com.shreefinance.ui.PrefsHelper
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {


    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imgClose.setOnClickListener {
            finish()
        }

        binding.btnLogin.setOnClickListener {

            val email=binding.etEmail.text.toString()
            val password=binding.etPassword.text.toString()
            if (email.isEmpty()) {
                binding.etEmail.error = "Email is required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etPassword.error = "Password is required"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }
            doLogin(email,password)
        }

    }

   fun doLogin(email: String, password: String)
    {
        runOnUiThread {
            showLoading(this@LoginActivity)
        }
        RetrofitClient.api.loginUser(email, password)
            .enqueue(object : retrofit2.Callback<LoginResponse> {
                override fun onResponse(
                    call: retrofit2.Call<LoginResponse>,
                    response: retrofit2.Response<LoginResponse>
                ) {
                    hideLoading()
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == true) {
                            println("Login successful: ${body.data?.access_token}")
                            val token = body.data?.access_token
                            if (!token.isNullOrEmpty()) {
                                PrefsHelper.saveAccessToken(this@LoginActivity, token)
                                Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                                println("Access token saved: $token")
                                startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                            }
                            Toast.makeText(this@LoginActivity, body.message, Toast.LENGTH_SHORT).show()
                        } else {
                            println("Login failed: ${body?.message}")
                        }
                    } else {
                        val errorMsg = try {
                            val errorJson = JSONObject(response.errorBody()?.string() ?: "{}")
                            errorJson.optString("message", "Unknown error")
                        } catch (e: Exception) {
                            "Unknown error"
                        }

                        Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        println("Server error ${response.code()}: $errorMsg")
                    }
                }

                override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                    hideLoading()
                    println("Network error: ${t.message}")
                }
            })

    }
    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }
}
