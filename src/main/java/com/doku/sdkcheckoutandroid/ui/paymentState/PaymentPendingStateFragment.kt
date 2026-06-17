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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import asIDR
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentPaymentPendingStateBinding
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.request.CheckoutRequest
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentPendingStateFragment(private val checkoutRequest: CheckoutRequest, private val paymentViewModel: InitialPaymentViewModel, private val onChangePaymentMethod: () -> Unit) : BottomSheetDialogFragment() {

    public lateinit var fragment: FragmentPaymentPendingStateBinding

    var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment =
            FragmentPaymentPendingStateBinding.inflate(inflater, container, false)

        val btnCheckPayment = fragment.btnCheckPayment
        val btnChangePaymentMethod = fragment.btnChangePaymentMethod

        val amountColor = fragment.tvTotalPayment
        val tvTotalAmountPayment = fragment.tvTotalAmountPayment

        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
                try {
                    val parsedColor = Color.parseColor(color)
                    val tint = ColorStateList.valueOf(parsedColor)

                    btnCheckPayment.backgroundTintList =
                        ColorStateList.valueOf(parsedColor)

                    btnCheckPayment.setTextColor(Color.WHITE)

                    btnChangePaymentMethod.setCardBackgroundColor(Color.WHITE)
                    btnChangePaymentMethod.setStrokeColor(tint)
                    (btnChangePaymentMethod.getChildAt(0) as? TextView)
                        ?.setTextColor(parsedColor)
                    amountColor.setTextColor(parsedColor)
                    tvTotalAmountPayment.setTextColor(parsedColor)


                } catch (e: IllegalArgumentException) {
                    // invalid color → skip
                }
            }
        val view = fragment.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        fragment.tvExpired.text = DokuConfig.checkoutExpiryTime
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

        fragment.btnCheckPayment.setOnClickListener {
            paymentViewModel.checkPaymentStatus()
        }

        fragment.btnChangePaymentMethod.setOnClickListener {
            onChangePaymentMethod.invoke()
            dismiss()
        }

    }
}