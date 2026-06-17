package com.doku.sdkcheckoutandroid.ui.convenienceStore

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentBankHowToPageBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentConvenienceStoreHowToPayBinding
import com.doku.sdkcheckoutandroid.model.response.GenerateCodeResponse
import com.doku.sdkcheckoutandroid.ui.bankTransfer.adapter.BankHowToPayAdapter
import com.doku.sdkcheckoutandroid.ui.convenienceStore.adapter.ConvenienceStoreHowToPayAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConvenienceStoreHowToPayFragment(private val response: GenerateCodeResponse?) : BottomSheetDialogFragment() {

    private lateinit var fragment: FragmentConvenienceStoreHowToPayBinding
    private lateinit var adapter: ConvenienceStoreHowToPayAdapter
    private lateinit var titleList: MutableList<String>
    private lateinit var dataList: HashMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment =
            FragmentConvenienceStoreHowToPayBinding.inflate(inflater, container, false)
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
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val expandableList = fragment.howTopayListView

        fragment.btnClose.setOnClickListener { p0 -> dismiss() }

        titleList = mutableListOf<String>()
        val instruction = response?.paymentInstructionEN?.forEach { it ->
            titleList.add(it.channel ?: "")
        }

        dataList = HashMap()
        response?.paymentInstructionEN?.forEach { instr ->
            val title = instr.channel ?: ""
            val steps = instr.step?.withIndex()?.joinToString("\n") { (index, value) -> "${index + 1}. $value" }
            dataList[title] = steps ?: ""
        }

        var lastExpandedPosition = -1

        fragment.howTopayListView.setOnGroupExpandListener { groupPosition ->

            if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                fragment.howTopayListView.collapseGroup(lastExpandedPosition)
            }

            lastExpandedPosition = groupPosition
        }

        adapter = ConvenienceStoreHowToPayAdapter(requireContext(), titleList, dataList)
        expandableList.setAdapter(adapter)
    }
}