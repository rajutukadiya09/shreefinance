package com.shreefinance.activity.ui.applyfroloan

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.shreefinance.R
import com.shreefinance.activity.DashboardActivity
import com.shreefinance.databinding.FragmentApplyForLoanBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ApplyForLoanFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private var parentActivity: DashboardActivity? = null
    private var view: View?=null
    lateinit var binding: FragmentApplyForLoanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parentActivity =
            if (activity is DashboardActivity) activity as DashboardActivity else null
        view= inflater.inflate(R.layout.fragment_apply_for_loan, container, false)
        binding = FragmentApplyForLoanBinding.inflate(inflater, container, false)
        loadData()
        return  view
    }

    fun loadData()
    {
        val loanTypes = listOf("Clean Credit", "EMI")
        val loanTypeValues = listOf(0, 1)

        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, loanTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spLoanType.adapter = adapter

        // When EditText is clicked → open spinner
        binding.etspLoanType.setOnClickListener {
           Log.e("etspLoanType","etspLoanType")
            binding.spLoanType.visibility=View.VISIBLE
            binding.spLoanType.performClick()
        }

        binding.spLoanType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: android.view.View?,
                position: Int, id: Long
            ) {
                val selectedText = loanTypes[position]
                val selectedValue = loanTypeValues[position]

                binding.etspLoanType.setText(selectedText)
                println("Selected: $selectedText ($selectedValue)")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    }

