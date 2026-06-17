package com.doku.sdkcheckoutandroid.ui.InitialPayment

import Helper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.PaymentBottomSheet
import com.doku.sdkcheckoutandroid.PaymentBottomSheetListener
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentAllPaymentMethodBinding
import com.doku.sdkcheckoutandroid.enum.PaymentMethodEnum
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.response.PaymentMethodType
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.ui.InitialPayment.adapter.PaymentMethodAdapter
import com.doku.sdkcheckoutandroid.ui.bankTransfer.BankTransferFragment
import com.doku.sdkcheckoutandroid.ui.card.CardTokenization
import com.doku.sdkcheckoutandroid.ui.card.CreditFormFragment
import com.doku.sdkcheckoutandroid.ui.convenienceStore.ConvenienceStoreFragment
import com.doku.sdkcheckoutandroid.ui.ewallet.EwalletListFragment
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel

class AllPaymentMethodFragment(private val checkoutResponse: ShowPaymentMethodResponse?, private val listener: PaymentBottomSheetListener,
                               private val otherPaymentMethod: MutableList<PaymentMethodType>) : Fragment() {

    private lateinit var adapter: PaymentMethodAdapter

    private lateinit var fragmentAllPaymentMethodBinding: FragmentAllPaymentMethodBinding

    private lateinit var listView: ListView

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
        fragmentAllPaymentMethodBinding =
            FragmentAllPaymentMethodBinding.inflate(inflater, container, false)
        val view = fragmentAllPaymentMethodBinding.root
        listView = view.findViewById(R.id.listViewPaymentMethod)
        return view
    }

    fun initObserver() {
        paymentViewModel.cardTokenize.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { token ->
                if(token.isNotEmpty()) {
                    listener.onUpdateTitle("Cards")
                    listener.onShowBackIcon(true)
                    listener.onShowCloseIcon(false)
                    listener.onUpdateBackAction("Other Payment Method", true, false)
                    (parentFragment as? PaymentBottomSheet)?.childFragmentManager?.beginTransaction()
                        ?.addToBackStack(null)
                        ?.replace(
                            R.id.child_fragment_container,
                            CardTokenization(cardTokenization = token, fromAllList = true, listener = listener)
                        )
                        ?.commit()
                } else {
                    listener.onUpdateTitle("Cards")
                    listener.onShowBackIcon(true)
                    listener.onShowCloseIcon(false)
                    listener.onUpdateBackAction("Other Payment Method", true, false)
                    paymentViewModel.selectedPaymentMethod.value = PaymentMethodEnum.CREDIT_CARD
                    (parentFragment as? PaymentBottomSheet)
                        ?.childFragmentManager
                        ?.beginTransaction()
                        ?.replace(
                            R.id.child_fragment_container,
                            CreditFormFragment()
                        )
                        ?.addToBackStack(null)
                        ?.commit()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        val items = mutableListOf<String>()
        val method = otherPaymentMethod
        method.forEach { detail ->

            if(detail.category_name?.isNotEmpty() == true && !DokuConfig.nextPhasePayment.contains(detail.category_name))
                items.add(detail.category_name)
        }
        adapter = PaymentMethodAdapter(requireContext(), paymentMethod = items)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val methodName = items[position]
            when(methodName) {
                "VIRTUAL_ACCOUNT" -> {
                    listener.onUpdateTitle("Bank Transfer")
                    listener.onShowBackIcon(true)
                    listener.onShowCloseIcon(true)
                    listener.onUpdateBackAction("Other Payment Method", true, false)
                    (parentFragment as? PaymentBottomSheet)
                        ?.childFragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.child_fragment_container, BankTransferFragment(listener = listener, checkoutResponse = checkoutResponse))
                        ?.addToBackStack(null)
                        ?.commit()
                }

                "ONLINE_TO_OFFLINE" -> {
                    listener.onUpdateTitle("Convenience Store")
                    listener.onShowBackIcon(true)
                    listener.onShowCloseIcon(true)
                    listener.onUpdateBackAction("Other Payment Method", true, false)
                    (parentFragment as? PaymentBottomSheet)
                        ?.childFragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.child_fragment_container, ConvenienceStoreFragment(listener = listener, checkoutResponse = checkoutResponse))
                        ?.addToBackStack(null)
                        ?.commit()
                }

                "QRIS" -> {
                    listener.onShowCloseIcon(true)
                    adapter.updateBackground("QRIS")
                    paymentViewModel.selectedPaymentMethod.value = Helper().mappingCategoryPaymentMethod("QRIS")
                    adapter.notifyDataSetChanged()
                }

                "EMONEY" -> {
                    listener.onUpdateTitle("e-Wallet")
                    listener.onShowBackIcon(true)
                    listener.onShowCloseIcon(false)
                    listener.onUpdateBackAction("Other Payment Method", true, false)
                    (parentFragment as? PaymentBottomSheet)
                        ?.childFragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.child_fragment_container, EwalletListFragment(checkoutResponse = checkoutResponse, listener))
                        ?.addToBackStack(null)
                        ?.commit()
                }

                "CREDIT_CARD" -> {
                    paymentViewModel.doCardTokenization()
                }
            }
        }
    }
}