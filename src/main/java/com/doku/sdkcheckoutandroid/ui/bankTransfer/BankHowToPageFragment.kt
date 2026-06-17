package com.doku.sdkcheckoutandroid.ui.bankTransfer

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListAdapter
import android.widget.FrameLayout
import android.widget.ListAdapter
import android.widget.SimpleExpandableListAdapter
import android.widget.Toast
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentBankHowToPageBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentQrisHowToPayBinding
import com.doku.sdkcheckoutandroid.model.response.GenerateCodeResponse
import com.doku.sdkcheckoutandroid.model.response.PaymentDetail
import com.doku.sdkcheckoutandroid.ui.bankTransfer.adapter.BankHowToPayAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BankHowToPageFragment(private val vaResponse: GenerateCodeResponse?) : BottomSheetDialogFragment() {

    private lateinit var frameBankHowToPageFragment: FragmentBankHowToPageBinding
    private lateinit var adapter: BankHowToPayAdapter
    private lateinit var titleList: MutableList<String>
    private lateinit var dataList: HashMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        frameBankHowToPageFragment =
            FragmentBankHowToPageBinding.inflate(inflater, container, false)
        val view = frameBankHowToPageFragment.root
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
        val expandableList = frameBankHowToPageFragment.howTopayListView

        frameBankHowToPageFragment.btnClose.setOnClickListener { p0 -> dismiss() }

        titleList = mutableListOf<String>()
        val instruction = vaResponse?.paymentInstructionEN?.forEach { it ->
            titleList.add(it.channel ?: "")
        }

        dataList = HashMap()
        vaResponse?.paymentInstructionEN?.forEach { instr ->
            val title = instr.channel ?: ""
            val steps = instr.step?.withIndex()?.joinToString("\n") { (index, value) -> "${index + 1}. $value" }
            dataList[title] = steps ?: ""
        }

        var lastExpandedPosition = -1

        frameBankHowToPageFragment.howTopayListView.setOnGroupExpandListener { groupPosition ->

            if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                frameBankHowToPageFragment.howTopayListView.collapseGroup(lastExpandedPosition)
            }

            lastExpandedPosition = groupPosition
        }

        adapter = BankHowToPayAdapter(requireContext(), titleList, dataList)
        expandableList.setAdapter(adapter)
    }

}