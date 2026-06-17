package com.doku.sdkcheckoutandroid.ui.convenienceStore

import Helper
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.PaymentBottomSheetListener
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentConvenienceStoreBinding
import com.doku.sdkcheckoutandroid.enum.PaymentLogo
import com.doku.sdkcheckoutandroid.model.response.PaymentDetail
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.ui.convenienceStore.adapter.ConvenienceStoreAdapter
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import org.json.JSONObject
import java.util.Locale.getDefault

class ConvenienceStoreFragment(private val checkoutResponse: ShowPaymentMethodResponse?, private val listener: PaymentBottomSheetListener) : Fragment() {

    private lateinit var adapter: ConvenienceStoreAdapter

    private lateinit var fragmentConvenienceStoreBinding: FragmentConvenienceStoreBinding

    private lateinit var listView: ListView

    private var selectedStore: String? = null

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
        fragmentConvenienceStoreBinding =
            FragmentConvenienceStoreBinding.inflate(inflater, container, false)
        val view = fragmentConvenienceStoreBinding.root
        listView = view.findViewById(R.id.listConvenience)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val items = mutableListOf<PaymentDetail>()
        val stores = checkoutResponse?.payment_method_type?.firstOrNull { it.category_name == "ONLINE_TO_OFFLINE" }
            ?.detail ?: emptyList()
        stores.forEach { store ->
            items.add(store)
        }
        adapter = ConvenienceStoreAdapter(requireContext(), convenienceStore = items)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val store = items[position]

            selectedStore = store.payment_channel_id
            val logo = PaymentLogo.from(store.payment_channel_id?.replace("ONLINE_TO_OFFLINE_", "") ?: "")
            val url = logo.fullUrl
            val storeDetail = JSONObject()
            storeDetail.put("logo", url)
            adapter.setSelectedBank(selectedStore)
            paymentViewModel.selectedPaymentMethod.value = Helper().mappingCategoryPaymentMethod("ONLINE_TO_OFFLINE")
            if(selectedStore != null) {
                if(selectedStore!!.lowercase().contains("alfa")) {
                    paymentViewModel.additionalRequest.value = "alfamart"
                } else if(selectedStore!!.lowercase().contains("indo")) {
                    paymentViewModel.additionalRequest.value = "indomaret"
                }
            }
            storeDetail.put("name",
                paymentViewModel.additionalRequest.value?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString()
                })
            paymentViewModel.bankDetail.value = storeDetail
            adapter.notifyDataSetChanged()
        }
    }
}