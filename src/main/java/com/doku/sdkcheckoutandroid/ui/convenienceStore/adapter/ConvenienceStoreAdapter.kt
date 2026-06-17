package com.doku.sdkcheckoutandroid.ui.convenienceStore.adapter

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

class ConvenienceStoreAdapter(private val context: Context,
                              private val convenienceStore: List<PaymentDetail>): BaseAdapter() {
    private var selectedStore: String? = null

    fun setSelectedBank(id: String?) {
        selectedStore = id
    }
    override fun getCount(): Int {
        return convenienceStore.size
    }

    override fun getItem(p0: Int): Any? {
        return convenienceStore[p0]
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
        val logo = PaymentLogo.from(convenienceStore[p0].payment_channel_id?.replace("ONLINE_TO_OFFLINE_", "")?.uppercase() ?: "")
        val url = logo.fullUrl

        name.text = convertStoreName(convenienceStore[p0].payment_channel_id ?: "")
        val params = icon.layoutParams
        params.width = (36 * context.resources.displayMetrics.density).toInt()
        params.height = (36 * context.resources.displayMetrics.density).toInt()
        icon.layoutParams = params
        Glide.with(context)
            .load(url)
            .override(36,36)
            .into(icon)

        if (convenienceStore[p0].payment_channel_id == selectedStore) {
            layout.setBackgroundColor(Color.parseColor("#E8F8F7"))
            checkCircle.visibility = View.VISIBLE
        } else {
            layout.setBackgroundColor(Color.WHITE)
            checkCircle.visibility = View.GONE
        }

        return view
    }

    private fun convertStoreName(name: String): String {
        if(name.lowercase().contains("alfa")) {
            return "Alfamart"
        } else if (name.lowercase().contains("indo")) {
            return "Indomaret"
        }
        return ""
    }
}