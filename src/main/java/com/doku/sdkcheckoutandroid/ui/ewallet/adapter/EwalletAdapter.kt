package com.doku.sdkcheckoutandroid.ui.ewallet.adapter

import android.content.Context
import android.graphics.Color
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

class EwalletAdapter(private val context: Context,
                     private val ewallet: List<PaymentDetail>): BaseAdapter() {

    private var selectedWallet: String? = null

    fun setSelectedWallet(id: String?) {
        selectedWallet = id
    }

    override fun getCount(): Int {
        return ewallet.size
    }

    override fun getItem(p0: Int): Any? {
        return ewallet[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(
        p0: Int,
        p1: View?,
        p2: ViewGroup?
    ): View? {
        val view = p1 ?: LayoutInflater.from(context)
            .inflate(R.layout.item_payment_method, p2, false)

        val name = view.findViewById<TextView>(R.id.tvName)
        val icon = view.findViewById<ImageView>(R.id.ivIcon)
        val rightArrow = view.findViewById<ImageView>(R.id.ic_arrow_right)
        val layout = view.findViewById<View>(R.id.methodBackground)
        val checkCircle = view.findViewById<View>(R.id.checked)
        rightArrow.visibility = View.GONE
        val logo = PaymentLogo.from(ewallet[p0].payment_channel_id?.replace("EMONEY_", "")?.uppercase() ?: "")
        val url = logo.fullUrl

        if(ewallet[p0].payment_channel_id?.replace("EMONEY_", "")?.replace("_", " ")?.contains("DOKU") ?: false) {
            name.text = "DOKU Wallet"
        } else {
            name.text = ewallet[p0].payment_channel_id?.replace("EMONEY_", "")?.replace("_", " ")
        }
        val params = icon.layoutParams
        params.width = (36 * context.resources.displayMetrics.density).toInt()
        params.height = (36 * context.resources.displayMetrics.density).toInt()
        icon.layoutParams = params
        Glide.with(context)
            .load(url)
            .override(36,36)
            .into(icon)

        if (ewallet[p0].payment_channel_id == selectedWallet) {
            layout.setBackgroundColor(Color.parseColor("#E8F8F7"))
            checkCircle.visibility = View.VISIBLE
        } else {
            layout.setBackgroundColor(Color.WHITE)
            checkCircle.visibility = View.GONE
        }

        return view
    }
}