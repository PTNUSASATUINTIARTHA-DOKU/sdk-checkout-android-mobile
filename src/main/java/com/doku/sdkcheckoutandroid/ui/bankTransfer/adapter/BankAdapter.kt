package com.doku.sdkcheckoutandroid.ui.bankTransfer.adapter

import Helper
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.enum.PaymentLogo
import com.doku.sdkcheckoutandroid.model.response.PaymentDetail

class BankAdapter(private val context: Context,
                  private val bankList: List<PaymentDetail>): BaseAdapter() {

    private var selectedBankId: String? = null

    fun setSelectedBank(id: String?) {
        selectedBankId = id
    }

    override fun getCount(): Int {
        return bankList.size
    }

    override fun getItem(p0: Int): Any? {
        return bankList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {
        val view = p1 ?: LayoutInflater.from(context)
            .inflate(R.layout.item_payment_method, p2, false)

        val name = view.findViewById<TextView>(R.id.tvName)
        val icon = view.findViewById<ImageView>(R.id.ivIcon)
        val rightArrow = view.findViewById<ImageView>(R.id.ic_arrow_right)
        val layout = view.findViewById<View>(R.id.methodBackground)
        val checkCircle = view.findViewById<View>(R.id.checked)
        rightArrow.visibility = View.GONE
        val logo = PaymentLogo.from(bankList[p0].payment_channel_id?.replace("VIRTUAL_ACCOUNT_", "") ?: "")
        val url = logo.fullUrl
        val raw = bankList[p0].payment_channel_id?.replace("_", " ").orEmpty()

        name.text = raw
        val params = icon.layoutParams
        params.width = (36 * context.resources.displayMetrics.density).toInt()
        params.height = (36 * context.resources.displayMetrics.density).toInt()
        icon.layoutParams = params
        Glide.with(context)
            .load(url)
            .override(36,36)
            .into(icon)

        if (bankList[p0].payment_channel_id == selectedBankId) {
            layout.setBackgroundColor(Color.parseColor("#E8F8F7"))
            checkCircle.visibility = View.VISIBLE
        } else {
            layout.setBackgroundColor(Color.WHITE)
            checkCircle.visibility = View.GONE
        }

        return view
    }
}