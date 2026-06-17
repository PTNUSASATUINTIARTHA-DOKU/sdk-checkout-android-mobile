package com.doku.sdkcheckoutandroid.ui.paymentState

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import asIDR
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentChangePaymentMethodBottomSheetBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentPaymentSuccessStateBinding
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.request.CheckoutRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentSuccessStateFragment(private val checkoutRequest: CheckoutRequest, private val onCloseAll: () -> Unit) : BottomSheetDialogFragment() {

    public lateinit var fragment: FragmentPaymentSuccessStateBinding

    var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragment =
            FragmentPaymentSuccessStateBinding.inflate(inflater, container, false)
        val view = fragment.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGoToMerchant = view.findViewById<Button>(R.id.btnGoToMerchant)
        val tvTotalAmountPayment = view.findViewById<TextView>(R.id.tvTotalAmountPayment)
        val tvTotalPayment = view.findViewById<TextView>(R.id.tvTotalPayment)
        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
                try {
                    val parsedColor = Color.parseColor(color)
                    val tint = ColorStateList.valueOf(parsedColor)

                    btnGoToMerchant.backgroundTintList = tint
                    tvTotalAmountPayment.setTextColor(parsedColor)
                    tvTotalPayment.setTextColor(parsedColor)
                } catch (e: IllegalArgumentException) {
                    // invalid color → skip
                }
            }
        setupOrderDetailView()
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

    fun setupOrderDetailView() {
        fragment.tvInvoiceNumber.text = checkoutRequest.order.invoice_number
        fragment.tvTotalPayment.text = checkoutRequest.order.amount.toDouble().asIDR()
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        fragment.imgCopy.setOnClickListener {
            val clip = ClipData.newPlainText("amount", checkoutRequest.order.invoice_number)
            clipboard.setPrimaryClip(clip)
        }

        fragment.itemSection.removeAllViews()

        checkoutRequest.order.line_items?.forEach { item ->
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_order_detail, fragment.itemSection, false)
            val tvItemName = view.findViewById<TextView>(R.id.tvItemName)
            val tvItemQty = view.findViewById<TextView>(R.id.tvItemQty)
            val tvItemPrice = view.findViewById<TextView>(R.id.tvItemPrice)
            tvItemName.text = item.name
            tvItemQty.text = "${item.quantity} x ${item.price.toDouble().asIDR()}"
            val price = item.quantity * item.price.toDouble()
            tvItemPrice.text = price.asIDR()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                tvItemName.typeface = Typeface.create(tvItemName.typeface, 700, false)
                tvItemPrice.typeface = Typeface.DEFAULT_BOLD
            }
            fragment.itemSection.addView(view)
        }

        val totalPrice = checkoutRequest.order.line_items?.sumOf { it.price * it.quantity }
        fragment.tvSubtotal.text = totalPrice?.toDouble()?.asIDR()
        fragment.tvTotalAmountPayment.text = totalPrice?.toDouble()?.asIDR()

        fragment.viewOrderDetail.setOnClickListener {
            isExpanded = !isExpanded
            fragment.chevronDetail.animate().rotation(if (fragment.chevronDetail.rotation == 0f) 180f else 0f)
            fragment.orderHiddenSection.visibility = if(isExpanded) {View.VISIBLE} else View.GONE
            fragment.tvTotalAmountPayment.text = checkoutRequest.order.amount.toDouble().asIDR()

        }

        fragment.btnGoToMerchant.setOnClickListener {
            onCloseAll.invoke()
            dismiss()
        }
    }
}