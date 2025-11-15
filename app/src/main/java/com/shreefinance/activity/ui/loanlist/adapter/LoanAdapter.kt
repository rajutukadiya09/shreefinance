package com.shreefinance.activity.ui.loanlist.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shreefinance.R
import com.shreefinance.api.LoanData
import androidx.core.graphics.toColorInt

class LoanAdapter(
    private var items: List<LoanData>,
    private val onItemClick: (LoanData) -> Unit
) : RecyclerView.Adapter<LoanAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLoanId: TextView = view.findViewById(R.id.tvLoanId)
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)

        val btnemi: ImageButton = view.findViewById(R.id.btnemi)
        val btnMessage: ImageButton = view.findViewById(R.id.btnMessage)
        val btnWhatsApp: ImageButton = view.findViewById(R.id.btnWhatsApp)
        val btnCall: ImageButton = view.findViewById(R.id.btnCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.loan_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvLoanId.text ="Loan Id - "+ item.loan_id
        holder.tvCustomerName.text = "Customer  - "+item.customer_name
        holder.tvAmount.text ="Amount - ₹${item.amount}"
        holder.tvStatus.text = "Loan Status - "+item.loan_status
        val statusColor = if (item.loan_status.equals("approved", ignoreCase = true)) {
            "#4CAF50".toColorInt() // Green
        } else {
            "#F44336".toColorInt() // Red
        }
        holder.tvStatus.setTextColor(statusColor)

        holder.btnWhatsApp.setOnClickListener {

            val message = if (item.emis.isNotEmpty()) {

                val emiamount = item.emis[0].emi_amount

                """
        EMI Reminder
        Dear ${item.customer_name},

        This is a friendly reminder that your EMI of ₹$emiamount 
        is due on ${item.emis[0].due_date}. Please ensure timely payment.
        Thank you.
        """.trimIndent()

            } else {

                """
        Hello ${item.customer_name},

        Currently, there is no EMI scheduled for your loan.
        If you have any questions, please contact support.
        """.trimIndent()

            }

            val url = "https://wa.me/${item.mobile_number}?text=" + Uri.encode(message)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            holder.itemView.context.startActivity(intent)
        }
        holder.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${item.mobile_number}")
            holder.itemView.context.startActivity(intent)
        }

        holder.btnMessage.setOnClickListener {

            val message = if (item.emis.isNotEmpty()) {

                val emiamount = item.emis[0].emi_amount

                """
        EMI Reminder
        Dear ${item.customer_name},

        This is a friendly reminder that your EMI of ₹$emiamount
        is due on ${item.emis[0].due_date}. Please ensure timely payment.
        Thank you.
        """.trimIndent()

            } else {

                """
        Hello ${item.customer_name},

        Currently, there is no EMI scheduled for your loan.
        If you have any questions, please contact support.
        """.trimIndent()
            }

            val smsUri = Uri.parse("smsto:${item.mobile_number}")
            val intent = Intent(Intent.ACTION_SENDTO, smsUri)
            intent.putExtra("sms_body", message)
            holder.itemView.context.startActivity(intent)
        }
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.btnemi.setOnClickListener {
            if(item.emis.size>0)
            {
                showEmiDialog( holder.btnemi.context,item)
            }else
            {
                Toast.makeText(holder.btnemi.context,"No EMI's found", Toast.LENGTH_SHORT).show()
            }

        }

    }
    @SuppressLint("SetTextI18n")
    private fun showEmiDialog(context: Context, loanData: LoanData) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_emi_list, null)

        val rvEmi = dialogView.findViewById<RecyclerView>(R.id.rvEmiList)
        val tvEmiTitle = dialogView.findViewById<TextView>(R.id.tvEmiTitle)

        rvEmi.layoutManager = LinearLayoutManager(context)
        rvEmi.adapter = EmiAdapter(loanData.emis,loanData.mobile_number,loanData)
        tvEmiTitle.text= "EMI Details of "+loanData.customer_name
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialog.show()
    }
    fun updateData(newItems: List<LoanData>) {
        items = newItems
        notifyDataSetChanged()
    }
    override fun getItemCount() = items.size
}
