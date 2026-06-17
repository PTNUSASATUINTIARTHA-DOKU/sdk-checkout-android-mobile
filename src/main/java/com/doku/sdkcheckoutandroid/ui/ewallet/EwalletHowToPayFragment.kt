package com.doku.sdkcheckoutandroid.ui.ewallet

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentEwalletHowToPayBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentQrisHowToPayBinding
import com.doku.sdkcheckoutandroid.enum.PaymentMethodEnum
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.collections.withIndex

class EwalletHowToPayFragment(private val ewallet: PaymentMethodEnum) : BottomSheetDialogFragment() {

    lateinit var fragment: FragmentEwalletHowToPayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment =
            FragmentEwalletHowToPayBinding.inflate(inflater, container, false)
        val view = fragment.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val closeImage = fragment.btnClose
        closeImage.setOnClickListener {
            dismiss()
        }
        generateHowToPayStep()
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
        }
        return dialog
    }

    private fun generateHowToPayStep() {
        when(ewallet) {
            PaymentMethodEnum.OVO_NAVIGATE -> {
                val step = listOf(
                    "You need an OVO account and the app on your phone. OVO can be downloaded from the App Store or Play Store",
                    "Make sure the Total Amount to Pay is correct",
                    "Input your valid phone number (e.g. 081234567890)",
                    "Click the “Pay Now” button",
                    "Go to the OVO notification and complete the payment"
                )
                fragment.tvTitleHowToPay.text = "OVO"
                fragment.tvHowToContent.text = step.withIndex().joinToString("\n\n") { (index, value) -> "${index + 1}. $value" }
            }

            PaymentMethodEnum.LINK_AJA_NAVIGATE -> {
                val step = listOf(
                    "You need a LinkAja account and the app on your phone. LinkAja can be downloaded from the App Store or Play Store",
                    "Make sure the Total Amount to Pay is correct",
                    "Input your valid phone number (e.g. 081234567890)",
                    "Click the “Pay Now” button",
                    "Finish your payment in the LinkAja app"
                )
                fragment.tvTitleHowToPay.text = "Link Aja"
                fragment.tvHowToContent.text = step.withIndex().joinToString("\n\n") { (index, value) -> "${index + 1}. $value" }
            }

            PaymentMethodEnum.DOKU_NAVIGATE -> {
                val step = listOf(
                    "You need a registered Doku Wallet account",
                    "Make sure the Total Amount to Pay is correct",
                    "Input your valid phone number/Doku ID/email and PIN/password",
                    "Click the “Continue Payment” button",
                    "Input your Doku Wallet PIN",
                    "Redeem your promo/campaign code (if any)",
                    "Click the “Pay Now” button to complete the payment",
                )
                fragment.tvTitleHowToPay.text = "DOKU Wallet"
                fragment.tvHowToContent.text = step.withIndex().joinToString("\n\n") { (index, value) -> "${index + 1}. $value" }
            }
            else -> {}
        }
    }
}