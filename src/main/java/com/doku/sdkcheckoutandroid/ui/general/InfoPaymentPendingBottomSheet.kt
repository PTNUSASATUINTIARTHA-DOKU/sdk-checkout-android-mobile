package com.doku.sdkcheckoutandroid.ui.general

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentInfoPaymentPendingBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentQuitConfirmationBottomSheetBinding
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InfoPaymentPendingBottomSheet(private val title: String? = null,
                                    private val subtitle: String? = null) : BottomSheetDialogFragment() {

    private lateinit var fragmentInfoPaymentPendingBinding: FragmentInfoPaymentPendingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentInfoPaymentPendingBinding =
            FragmentInfoPaymentPendingBinding.inflate(inflater, container, false)


        val btnOk = fragmentInfoPaymentPendingBinding.negativeBtn

        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
                try {
                    val parsedColor = Color.parseColor(color)
                    val tint = ColorStateList.valueOf(parsedColor)


                    btnOk.setCardBackgroundColor(Color.WHITE)
                    btnOk.setStrokeColor(tint)
                    (btnOk.getChildAt(0) as? TextView)
                        ?.setTextColor(parsedColor)


                } catch (e: IllegalArgumentException) {
                    // invalid color → skip
                }
            }
        val view = fragmentInfoPaymentPendingBinding.root
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
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(title != null && subtitle != null) {
            fragmentInfoPaymentPendingBinding.title.text = title
            fragmentInfoPaymentPendingBinding.subtitle.text = subtitle
        }
        fragmentInfoPaymentPendingBinding.negativeBtn.setOnClickListener {
            dismiss()
        }
    }

}