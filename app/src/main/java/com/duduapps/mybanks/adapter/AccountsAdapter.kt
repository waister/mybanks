package com.duduapps.mybanks.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.duduapps.mybanks.R
import com.duduapps.mybanks.activity.AccountDetailsActivity
import com.duduapps.mybanks.model.Account
import com.duduapps.mybanks.util.PARAM_ID


class AccountsAdapter(private val context: Context) :
    RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {

    private var accounts: List<Account>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setData(accounts: List<Account>?) {
        this.accounts = accounts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_accounts, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (accounts != null) {
            holder.setData(accounts!![position])
        }
    }

    override fun getItemCount(): Int {
        if (accounts != null) {
            return accounts!!.size
        }
        return 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
        private var tvDetails = itemView.findViewById<TextView>(R.id.tv_details)

        fun setData(account: Account) {
            var details = "${account.bank!!.name} (${account.bank!!.code})"

            if (account.pixCode.isNotEmpty())
                details += " | PIX: " + account.pixCode

            tvTitle.text = account.label
            tvDetails.text = details

            itemView.setOnClickListener {
                val myIntent = Intent(context, AccountDetailsActivity::class.java)
                myIntent.putExtra(PARAM_ID, account.id)
                context.startActivity(myIntent)
            }
        }
    }
}