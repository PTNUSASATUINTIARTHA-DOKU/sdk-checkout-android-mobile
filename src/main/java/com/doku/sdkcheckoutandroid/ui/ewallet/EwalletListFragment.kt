package com.doku.sdkcheckoutandroid.ui.ewallet

import Helper
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.PaymentBottomSheet
import com.doku.sdkcheckoutandroid.PaymentBottomSheetListener
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentEwalletListBinding
import com.doku.sdkcheckoutandroid.enum.PaymentLogo
import com.doku.sdkcheckoutandroid.model.response.PaymentDetail
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.ui.ewallet.adapter.EwalletAdapter
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import org.json.JSONObject

class EwalletListFragment(private val checkoutResponse: ShowPaymentMethodResponse?, private val listener: PaymentBottomSheetListener) : Fragment() {

    private lateinit var adapter: EwalletAdapter

    lateinit var fragment: FragmentEwalletListBinding
    private lateinit var listView: ListView

    private var selectedWallet: String? = null

    private val phoneRequired: List<String> = listOf("OVO", "LINKAJA", "DOKU")

    private val paymentViewModel: InitialPaymentViewModel by lazy {
        ViewModelProvider(requireParentFragment())[InitialPaymentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragment =
            FragmentEwalletListBinding.inflate(inflater, container, false)
        val view = fragment.root
        listView = view.findViewById(R.id.listViewPaymentMethod)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val items = mutableListOf<PaymentDetail>()
        val emoneyData = checkoutResponse?.payment_method_type?.firstOrNull { it.category_name == "EMONEY" }
            ?.detail ?: emptyList()

        emoneyData.forEach { emoney ->
            if (!emoney.payment_channel_id!!.contains("EMONEY_LINKAJA") && !emoney.payment_channel_id.contains("EMONEY_ISAKU") && !emoney.payment_channel_id.contains("EMONEY_OVO")) {
                items.add(emoney)
            }
        }
        adapter = EwalletAdapter(requireContext(), ewallet = items)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val bank = items[position]

            selectedWallet = bank.payment_channel_id
            val walletName = bank.payment_channel_id?.replace("EMONEY_", "")
            val logo = PaymentLogo.from(walletName ?: "")
            val url = logo.fullUrl
            val walletDetail = JSONObject()
            walletDetail.put("name", bank.name)
            walletDetail.put("logo", url)
            paymentViewModel.bankDetail.value = walletDetail
            if(selectedWallet != null) {
                paymentViewModel.additionalRequest.value = ""
            }
            paymentViewModel.selectedPaymentMethod.value = Helper().mappingCategoryPaymentMethod(walletName ?: "")
//            if(phoneRequired.contains(walletName!!)) {
//                listener.onUpdateTitle(walletName)
//                listener.onShowBackIcon(true)
//                listener.onShowCloseIcon(false)
//                listener.onUpdateBackAction("e-Wallet", true, false)
//                (parentFragment as? PaymentBottomSheet)
//                    ?.childFragmentManager
//                    ?.beginTransaction()
//                    ?.replace(R.id.child_fragment_container, EwalletFormFragment())
//                    ?.addToBackStack(null)
//                    ?.commit()
//            } else {
//                adapter.setSelectedWallet(selectedWallet)
//            }
            adapter.setSelectedWallet(selectedWallet)
            adapter.notifyDataSetChanged()
        }
    }
}