package com.shreefinance.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.shreefinance.R
import com.shreefinance.api.LogoutResponse
import com.shreefinance.api.RetrofitClient
import com.shreefinance.api.hideLoading
import com.shreefinance.api.showLoading
import com.shreefinance.databinding.ActivityDashboradBinding
import com.shreefinance.ui.PrefsHelper.clearAll
import com.shreefinance.ui.PrefsHelper.getAccessToken
import retrofit2.Call

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboradBinding
    var navController: NavController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboradBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.lightcolor)
            );
        }
        val navView: BottomNavigationView = binding.navView

         navController = findNavController(R.id.nav_host_fragment_activity_dashborad)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        NavigationUI.setupWithNavController(binding.topAppBar, navController!!)

             navController?.addOnDestinationChangedListener { _, destination, _ ->
                 when (destination.id) {
                     R.id.applyForLoanFragment -> {
                         binding.txtTitle.setText("Apply for Loan")
                         binding.imgback.visibility= View.VISIBLE
                     }
                     R.id.loanListFragment -> {
                         binding.txtTitle.setText("Loan List")
                         binding.imgback.visibility= View.VISIBLE
                     }

                     else -> {
                         binding.txtTitle.setText("Home")
                         binding.imgback.visibility= View.GONE

                     }
                 }
             }
        binding.imgback.setOnClickListener {
            onBackPressed()
        }
        binding.imgclose.setOnClickListener {
            showLogoutDialog()
        }

        setupActionBarWithNavController(navController!!, appBarConfiguration)
        navView.setupWithNavController(navController!!)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)

        return true
    }

    fun setTitle(title: String)
    {
        binding.txtTitle.text=title;
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemdata -> {

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                // Perform logout action here
                logoutUser()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun logoutUser() {
        runOnUiThread {
            showLoading(this@DashboardActivity)
        }
        val token = "Bearer "+getAccessToken(this)  // Replace with your token

        val call = RetrofitClient.api.logoutUser(token)
        call.enqueue(object : retrofit2.Callback<LogoutResponse> {
            override fun onResponse(
                call: Call<LogoutResponse>,
                response: retrofit2.Response<LogoutResponse>
            ) {
                hideLoading()
                if (response.isSuccessful) {
                    val logoutResponse = response.body()
                    println("Logout: ${logoutResponse?.message}")
                    clearAll(this@DashboardActivity)
                    val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } else {
                    println("Logout failed with status code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                hideLoading()
                println("Error: ${t.message}")
            }
        })



    }
    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() ?: false
    }
    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }
}