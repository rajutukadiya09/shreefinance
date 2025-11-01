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
import com.shreefinance.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var parentActivity: DashboardActivity? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

        val items = listOf(
            DashboardItem("Apply For Loan", R.drawable.loanapply),
            DashboardItem("Loan Information", R.drawable.loadninfo),
            DashboardItem("Collection", R.drawable.collection),
            DashboardItem("Overdue EMI", R.drawable.overduemi),
            DashboardItem("Invoice", R.drawable.invoice),
            DashboardItem("Statistics", R.drawable.statistics),
            DashboardItem("Add Customer", R.drawable.loanapply),
            DashboardItem("Personal Loan", R.drawable.person)
        )

        val adapter = DashboardAdapter(items) {
            if (it.title == "Apply For Loan") {
                parentActivity?.navController?.navigate(
                    R.id.action_navigation_home_to_applyForLoanFragment,
                    null
                )
            } else {
                Toast.makeText(requireActivity(), "Coming soon ", Toast.LENGTH_SHORT).show()

            }


        }

        _binding?.recyclerDashboard?.adapter = adapter
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}