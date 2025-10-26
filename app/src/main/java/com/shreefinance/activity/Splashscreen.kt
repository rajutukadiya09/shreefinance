package com.shreefinance.activity

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.shreefinance.R
import com.shreefinance.ui.PrefsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.logging.Handler

class Splashscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splashscreen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        GlobalScope.launch(Dispatchers.Main) {
            delay(2000) // Delay for 2 seconds (Splash display time)

            val token = PrefsHelper.getAccessToken(this@Splashscreen)

            if (!token.isNullOrEmpty()) {
                // ✅ Token exists → open Dashboard
                startActivity(Intent(this@Splashscreen, DashboardActivity::class.java))
            } else {
                // 🚪 No token → open Login
                startActivity(Intent(this@Splashscreen, LoginActivity::class.java))
            }

            overridePendingTransition(0, 0)
            finish()
        }
 // 3 seconds del
    }
}

