package com.shreefinance.activity.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.shreefinance.R
import com.shreefinance.activity.DashboardActivity
import com.shreefinance.adapter.DashboardAdapter
import com.shreefinance.adapter.DashboardItem
import com.shreefinance.api.PermissionsResponse
import com.shreefinance.api.RetrofitClient
import com.shreefinance.api.hideLoading
import com.shreefinance.api.showLoading
import com.shreefinance.databinding.FragmentHomeBinding
import com.shreefinance.ui.PrefsHelper.getAccessToken
import retrofit2.Call

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var parentActivity: DashboardActivity? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var adapter: DashboardAdapter
    private val binding get() = _binding!!
    private var allItems = mutableListOf<DashboardItem>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        parentActivity =
            if (activity is DashboardActivity) activity as DashboardActivity else null

        _binding?.recyclerDashboard?.layoutManager = GridLayoutManager(requireActivity(), 3)

        allItems = listOf(
            DashboardItem("Apply For Loan", R.drawable.loanapply),
            DashboardItem("Loan Information", R.drawable.loadninfo),
            DashboardItem("Collection", R.drawable.collection),
            DashboardItem("Overdue EMI", R.drawable.overduemi),
            DashboardItem("Invoice", R.drawable.invoice),
            DashboardItem("Statistics", R.drawable.statistics),
            DashboardItem("Add Customer", R.drawable.loanapply),
            DashboardItem("Personal Loan", R.drawable.person)
        ) as MutableList<DashboardItem>

         adapter = DashboardAdapter(allItems) {
            if (it.title == "Apply For Loan") {
                parentActivity?.navController?.navigate(
                    R.id.action_navigation_home_to_applyForLoanFragment,
                    null
                )
            } else   if (it.title == "Loan Information") {
                parentActivity?.navController?.navigate(
                    R.id.action_navigation_home_to_loanListFragment,
                    null)


         }else {
                Toast.makeText(requireActivity(), "Coming soon ", Toast.LENGTH_SHORT).show()

            }


        }
        getPermissions()
        _binding?.recyclerDashboard?.adapter = adapter
        return root
    }
    private fun getPermissions() {

        showLoading(requireActivity())
        val token = "Bearer " + getAccessToken(requireActivity()) // Replace with your stored token

        val call = RetrofitClient.api.getPermissions(token)
        call.enqueue(object : retrofit2.Callback<PermissionsResponse> {
            override fun onResponse(
                call: Call<PermissionsResponse>,
                response: retrofit2.Response<PermissionsResponse>
            ) {
                hideLoading()
                if (response.isSuccessful) {
                    val permissionsResponse = response.body()
                    permissionsResponse?.let {
                        val p = it.data.permissions

                        // Update each item visibility based on permission
                        val updatedItems = allItems.map { item ->
                            when (item.title) {
                                "Apply For Loan" -> item.copy(visible = p.apply_for_loan)
                                "Loan Information" -> item.copy(visible = p.view_loan_information)
                                "Collection" -> item.copy(visible = p.collection)
                                "Overdue EMI" -> item.copy(visible = p.overdue)
                                "Invoice" -> item.copy(visible = p.due)
                                "Statistics" -> item.copy(visible = p.penalty)
                                "Add Customer" -> item.copy(visible = p.other)
                                else -> item.copy(visible = false) // default hidden
                            }
                        }

                        // Filter only visible items
                        val visibleItems = updatedItems.filter { it.visible }

                        adapter.updateData(visibleItems)
                    }
                } else {
                    println("Get permissions failed: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<PermissionsResponse>, t: Throwable) {
                hideLoading()
                println("API error: ${t.message}")
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}