package com.doku.sdkcheckoutandroid.ui.general

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.doku.sdkcheckoutandroid.PaymentBottomSheet
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentQrisSuccessPageBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentQuitConfirmationBottomSheetBinding
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QuitConfirmationBottomSheet(private val onCloseAll: () -> Unit) : BottomSheetDialogFragment() {

    private lateinit var fragmentQuitConfirmationBottomSheetBinding: FragmentQuitConfirmationBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        fragmentQuitConfirmationBottomSheetBinding =
            FragmentQuitConfirmationBottomSheetBinding.inflate(inflater, container, false)

        val positiveBtn = fragmentQuitConfirmationBottomSheetBinding.positiveBtn
        val negativeBtn = fragmentQuitConfirmationBottomSheetBinding.negativeBtn

        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
                try {
                    val parsedColor = Color.parseColor(color)
                    val tint = ColorStateList.valueOf(parsedColor)

                    // 🔥 POSITIVE BUTTON → BACKGROUND TINT
                    positiveBtn.backgroundTintList = null   // reset dulu
                    positiveBtn.backgroundTintList = tint

                    // text
                    (positiveBtn.getChildAt(0) as? TextView)
                        ?.setTextColor(Color.WHITE)

                    negativeBtn.setCardBackgroundColor(Color.WHITE)
                    negativeBtn.setStrokeColor(ColorStateList.valueOf(parsedColor))
                    (negativeBtn.getChildAt(0) as? TextView)
                        ?.setTextColor(parsedColor)

                } catch (e: IllegalArgumentException) {
                    // invalid color → ignore
                }
            }

        return fragmentQuitConfirmationBottomSheetBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentQuitConfirmationBottomSheetBinding.negativeBtn.setOnClickListener {
            onCloseAll.invoke()
            dismiss()
        }

        fragmentQuitConfirmationBottomSheetBinding.positiveBtn.setOnClickListener {
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