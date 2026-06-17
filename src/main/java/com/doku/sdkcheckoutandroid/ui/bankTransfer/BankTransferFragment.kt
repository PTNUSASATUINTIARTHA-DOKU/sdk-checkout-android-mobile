package com.doku.sdkcheckoutandroid.ui.bankTransfer

import Helper
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.PaymentBottomSheetListener
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentBankTransferBinding
import com.doku.sdkcheckoutandroid.enum.PaymentLogo
import com.doku.sdkcheckoutandroid.model.response.PaymentDetail
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.ui.bankTransfer.adapter.BankAdapter
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import org.json.JSONObject
import kotlin.math.log

class BankTransferFragment(private val checkoutResponse: ShowPaymentMethodResponse?, private val listener: PaymentBottomSheetListener) : Fragment() {

    private lateinit var adapter: BankAdapter

    private lateinit var fragmentBankTransferBinding: FragmentBankTransferBinding

    private lateinit var listView: ListView

    private var selectedBankId: String? = null

    private val paymentViewModel: InitialPaymentViewModel by lazy {
        ViewModelProvider(requireParentFragment())[InitialPaymentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentBankTransferBinding =
            FragmentBankTransferBinding.inflate(inflater, container, false)
        val view = fragmentBankTransferBinding.root
        listView = view.findViewById(R.id.listViewPaymentMethod)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val items = mutableListOf<PaymentDetail>()
        val virtualAccountDetails = checkoutResponse?.payment_method_type?.firstOrNull { it.category_name == "VIRTUAL_ACCOUNT" }
            ?.detail ?: emptyList()

        virtualAccountDetails.forEach { detailVa ->
            items.add(detailVa)
        }
        adapter = BankAdapter(requireContext(), bankList = items)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val bank = items[position]
            selectedBankId = bank.payment_channel_id
            val logo = PaymentLogo.from(bank.payment_channel_id?.replace("VIRTUAL_ACCOUNT_", "") ?: "")
            val url = logo.fullUrl
            val bankDetail = JSONObject()
            val raw = bank.payment_channel_id.orEmpty().replace("_", " ")
            bankDetail.put("name", raw)
            bankDetail.put("logo", url)
            paymentViewModel.bankDetail.value = bankDetail
            adapter.setSelectedBank(selectedBankId)
            paymentViewModel.selectedPaymentMethod.value = Helper().mappingCategoryPaymentMethod("VIRTUAL_ACCOUNT")
            if(selectedBankId != null) {
                paymentViewModel.additionalRequest.value = selectedBankId?.split("_")?.last()?.lowercase()
            }
            adapter.notifyDataSetChanged()
        }
    }
}