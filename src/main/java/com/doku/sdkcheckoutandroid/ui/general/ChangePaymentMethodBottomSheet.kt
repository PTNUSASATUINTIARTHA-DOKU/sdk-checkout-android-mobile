package com.doku.sdkcheckoutandroid.ui.general

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentChangePaymentMethodBottomSheetBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentQuitConfirmationBottomSheetBinding
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.ui.qris.QrisSuccessPage
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChangePaymentMethodBottomSheet(private val changePaymentMethodAction: (() -> Unit)? = null) : BottomSheetDialogFragment() {

    private lateinit var fragmentChangePaymentMethodBottomSheetBinding: FragmentChangePaymentMethodBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentChangePaymentMethodBottomSheetBinding =
            FragmentChangePaymentMethodBottomSheetBinding.inflate(inflater, container, false)
        val view = fragmentChangePaymentMethodBottomSheetBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val positiveBtn = fragmentChangePaymentMethodBottomSheetBinding.positiveBtn
        val negativeBtn = fragmentChangePaymentMethodBottomSheetBinding.negativeBtn

        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
                try {
                    val parsedColor = Color.parseColor(color)
                    val tint = ColorStateList.valueOf(parsedColor)

                    positiveBtn.backgroundTintList = null
                    positiveBtn.backgroundTintList = tint


                    (positiveBtn.getChildAt(0) as? TextView)
                        ?.setTextColor(Color.WHITE)

                    negativeBtn.setCardBackgroundColor(Color.WHITE)
                    negativeBtn.setStrokeColor(ColorStateList.valueOf(parsedColor))
                    (negativeBtn.getChildAt(0) as? TextView)
                        ?.setTextColor(parsedColor)

                } catch (e: IllegalArgumentException) {
                    // invalid color → skip
                }
            }

        negativeBtn.setOnClickListener { dismiss() }

        positiveBtn.setOnClickListener {
            changePaymentMethodAction?.invoke()
            dismiss()
        }
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
}