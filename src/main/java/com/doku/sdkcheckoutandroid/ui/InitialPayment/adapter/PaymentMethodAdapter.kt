package com.doku.sdkcheckoutandroid.ui.InitialPayment.adapter

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
import com.doku.sdkcheckoutandroid.R

class PaymentMethodAdapter(
    private val context: Context,
    private val paymentMethod: List<String>
) : BaseAdapter() {

    private var selectedMethod: String? = null

    fun updateBackground(id: String?) {
        selectedMethod = id
    }

    override fun getCount(): Int {
        return paymentMethod.size
    }

    override fun getItem(p0: Int): Any? {
        return paymentMethod[p0]
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
        if(paymentMethod[p0] == "QRIS") {
            rightArrow.visibility = View.GONE
        }
        name.text = Helper().updateCategoryName(paymentMethod[p0])
        icon.setImageResource(Helper().updateCategoryIcon(paymentMethod[p0]))
        if (paymentMethod[p0] == selectedMethod) {
            layout.setBackgroundColor(Color.parseColor("#E8F8F7"))
            checkCircle.visibility = View.VISIBLE
        } else {
            layout.setBackgroundColor(Color.WHITE)
            checkCircle.visibility = View.GONE
        }
        return view
    }
}