package com.doku.sdkcheckoutandroid.ui.card.adapter

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
import com.doku.sdkcheckoutandroid.model.response.CardTokenizeResponse
import com.doku.sdkcheckoutandroid.model.response.PaymentDetail

class CardAdapter(private val context: Context,
                  private val card: MutableList<CardTokenizeResponse>,
                  private val onDeleteAction: (token: String, position: Int) -> Unit)
: BaseAdapter() {

    private var selectedCard: String? = null

    fun setSelectedCard(id: String?) {
        selectedCard = id
    }

    override fun getCount(): Int {
        return card.size
    }

    override fun getItem(p0: Int): Any? {
        return card[p0]
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
            .inflate(R.layout.card_token_list, p2, false)

        val name = view.findViewById<TextView>(R.id.tvName)
        val icon = view.findViewById<ImageView>(R.id.ivIcon)
        val removeIcon = view.findViewById<ImageView>(R.id.removeIcon)
        val layout = view.findViewById<View>(R.id.cardItemBackground)
        val checkCircle = view.findViewById<View>(R.id.checked)
        val logo = PaymentLogo.from(card[p0].credit_card.brand.uppercase())
        val url = logo.fullUrl

        name.text = card[p0].credit_card.masked_card
        val params = icon.layoutParams
        params.width = (36 * context.resources.displayMetrics.density).toInt()
        params.height = (36 * context.resources.displayMetrics.density).toInt()
        icon.layoutParams = params
        Glide.with(context)
            .load(url)
            .override(36,36)
            .into(icon)

        if (card[p0].credit_card.token_id == selectedCard) {
            layout.setBackgroundColor(Color.parseColor("#E8F8F7"))
            checkCircle.visibility = View.VISIBLE
        } else {
            layout.setBackgroundColor(Color.WHITE)
            checkCircle.visibility = View.GONE
        }

        removeIcon.setOnClickListener {
            onDeleteAction(card[p0].credit_card.token_id, p0)
        }

        return view
    }

    fun updateData(newItems: List<CardTokenizeResponse>) {
        card.clear()
        card.addAll(newItems.toList())
        notifyDataSetChanged()
    }

}