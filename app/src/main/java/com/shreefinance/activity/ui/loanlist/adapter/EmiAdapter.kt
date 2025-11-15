package com.shreefinance.activity.ui.loanlist.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shreefinance.R
import com.shreefinance.api.Emi
import com.shreefinance.api.LoanData

class EmiAdapter(
    private val emiList: List<Emi>,
    private val mobile_number: String,
    private val  loanData: LoanData
) :
    RecyclerView.Adapter<EmiAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAmount: TextView = view.findViewById(R.id.tvEmiAmount)
        val tvDate: TextView = view.findViewById(R.id.tvEmiDate)
        val tvStatus: TextView = view.findViewById(R.id.tvEmiStatus)
        val btnMessage: ImageButton = view.findViewById(R.id.btnMessage)
        val btnWhatsApp: ImageButton = view.findViewById(R.id.btnWhatsApp)
        val btnCall: ImageButton = view.findViewById(R.id.btnCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.emi_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = emiList[position]

        holder.tvAmount.text = "₹${item.emi_amount}"
        holder.tvDate.text = "Due: ${item.due_date}"
        holder.btnWhatsApp.setOnClickListener {

            val message = """
        EMI Reminder
        Dear ${loanData.customer_name},

        This is a friendly reminder that your EMI of ₹${item.emi_amount} 
        is due on ${item.due_date}. Please ensure timely payment.
        Thank you.
    """.trimIndent()

            val url = "https://wa.me/${loanData.mobile_number}?text=" + Uri.encode(message)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            holder.itemView.context.startActivity(intent)
        }

        holder.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${mobile_number}")
            holder.itemView.context.startActivity(intent)
        }

        holder.btnMessage.setOnClickListener {

            val message = """
        EMI Reminder
        Dear ${loanData.customer_name},

        This is a friendly reminder that your EMI of ₹${item.emi_amount} 
        is due on ${item.due_date}. Please ensure timely payment.
        Thank you.
    """.trimIndent()

            val smsUri = Uri.parse("smsto:${mobile_number}")
            val intent = Intent(Intent.ACTION_SENDTO, smsUri)
            intent.putExtra("sms_body", message)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount() = emiList.size
}
