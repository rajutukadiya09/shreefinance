package com.shreefinance.activity.ui.loanlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shreefinance.activity.DashboardActivity
import com.shreefinance.activity.ui.loanlist.adapter.LoanAdapter
import com.shreefinance.api.LoanResponse
import com.shreefinance.api.RetrofitClient
import com.shreefinance.api.hideLoading
import com.shreefinance.api.showLoading
import com.shreefinance.databinding.FragmentLoanListBinding
import com.shreefinance.ui.PrefsHelper.getAccessToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoanListFragment : Fragment() {

    private var _binding: FragmentLoanListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LoanAdapter
    private var parentActivity: DashboardActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoanListBinding.inflate(inflater, container, false)
        val root = binding.root

        parentActivity =
            if (activity is DashboardActivity) activity as DashboardActivity else null

        // ✅ Setup RecyclerView once
        binding.recyclerLoanList.layoutManager = LinearLayoutManager(requireContext())
        adapter = LoanAdapter(emptyList()) { loan ->
            onLoanClick(loan)
        }
        binding.recyclerLoanList.adapter = adapter

        // ✅ Fetch API data
        getLoans()

        return root
    }

    private fun getLoans() {
        showLoading(requireActivity())
        val token = "Bearer " + getAccessToken(requireActivity())

        RetrofitClient.api.getLoans(token).enqueue(object : Callback<LoanResponse> {
            override fun onResponse(call: Call<LoanResponse>, response: Response<LoanResponse>) {
                hideLoading()
                if (response.isSuccessful) {
                    val loanResponse = response.body()
                    loanResponse?.data?.let { loans ->
                        adapter.updateData(loans) // ✅ update RecyclerView data
                    }
                } else {
                    println("❌ Get Loans failed: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoanResponse>, t: Throwable) {
                hideLoading()
                println("❌ API Error: ${t.message}")
            }
        })
    }

    private fun onLoanClick(loan: com.shreefinance.api.LoanData) {
        // TODO: navigate to loan detail screen
        println("Clicked on loan: ${loan.loan_id}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
