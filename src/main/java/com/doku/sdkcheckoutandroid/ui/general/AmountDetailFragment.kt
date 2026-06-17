package com.doku.sdkcheckoutandroid.ui.general

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import asIDR
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentAmountDetailBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentPaymentSuccessStateBinding
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.request.CheckoutRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AmountDetailFragment(private val checkoutRequest: CheckoutRequest) : BottomSheetDialogFragment() {

    lateinit var fragment: FragmentAmountDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment =
            FragmentAmountDetailBinding.inflate(inflater, container, false)
        val view = fragment.root
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setOnShowListener { d ->
            val bottomSheet =
                (dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                        as FrameLayout)

            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.isDraggable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotalAmountPayment = view.findViewById<TextView>(R.id.tvTotalAmountPayment)
        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
            val parsedColor = Color.parseColor(color)
            tvTotalAmountPayment.setTextColor(parsedColor)
        }
        setupView()
    }

    fun setupView() {
        fragment.itemSection.removeAllViews()
        checkoutRequest.order.line_items?.forEach { item ->
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_order_detail, fragment.itemSection, false)
            val tvItemName = view.findViewById<TextView>(R.id.tvItemName)
            val tvItemQty = view.findViewById<TextView>(R.id.tvItemQty)
            val tvItemPrice = view.findViewById<TextView>(R.id.tvItemPrice)
            tvItemName.text = item.name
            tvItemQty.text = "Qty ${item.quantity}"
            val price = item.quantity * item.price.toDouble()
            tvItemPrice.text = price.asIDR()
            fragment.itemSection.addView(view)
        }

        val totalPrice = checkoutRequest.order.line_items?.sumOf { it.price * it.quantity }
        fragment.tvSubtotal.text = totalPrice?.toDouble()?.asIDR()
        fragment.tvTotalAmountPayment.text = totalPrice?.toDouble()?.asIDR()

        fragment.btnClose.setOnClickListener {
            dismiss()
        }
    }
}