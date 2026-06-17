package com.doku.sdkcheckoutandroid.ui.paymentState

import Helper
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.solver.state.State
import asIDR
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentPaymentExpiredStateBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentPaymentFailedStateBinding
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.request.CheckoutRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentFailedStateFragment(private val checkoutRequest: CheckoutRequest, private val retryPaymentAction: (() -> Unit)? = null) : BottomSheetDialogFragment() {

    lateinit var fragment: FragmentPaymentFailedStateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment =
            FragmentPaymentFailedStateBinding.inflate(inflater, container, false)
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
        setupView()
    }

    fun setupView() {
        fragment.tvInvoiceNumber.text = checkoutRequest.order.invoice_number
        fragment.tvTotalPayment.text = checkoutRequest.order.amount.toDouble().asIDR()
        fragment.checkoutDate.text = Helper().convertDateFormat(DokuConfig.requestTimestamp)
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        fragment.imgCopy.setOnClickListener {
            val clip = ClipData.newPlainText("amount", checkoutRequest.order.invoice_number)
            clipboard.setPrimaryClip(clip)
        }

        fragment.btnRetry.setOnClickListener {
            retryPaymentAction?.invoke()
            dismiss()
        }

        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
                try {
                    val parsedColor = Color.parseColor(color)
                    val tint = ColorStateList.valueOf(parsedColor)

                    fragment.btnRetry.backgroundTintList = tint
                    fragment.tvTotalPayment.setTextColor(parsedColor)
                } catch (e: IllegalArgumentException) {
                    // invalid color → skip
                }
            }
    }
}