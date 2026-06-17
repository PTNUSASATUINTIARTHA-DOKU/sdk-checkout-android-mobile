package com.doku.sdkcheckoutandroid.ui.InitialPayment

import Helper
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.PaymentBottomSheet
import com.doku.sdkcheckoutandroid.PaymentBottomSheetListener
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentPaymentMethodBinding
import com.doku.sdkcheckoutandroid.enum.PaymentMethodEnum
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.request.CheckoutRequest
import com.doku.sdkcheckoutandroid.model.response.PaymentMethodType
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.ui.bankTransfer.BankTransferFragment
import com.doku.sdkcheckoutandroid.ui.card.CardTokenization
import com.doku.sdkcheckoutandroid.ui.card.CreditFormFragment
import com.doku.sdkcheckoutandroid.ui.convenienceStore.ConvenienceStoreFragment
import com.doku.sdkcheckoutandroid.ui.ewallet.EwalletListFragment
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel

class PaymentMethodFragment(private val listener: PaymentBottomSheetListener) : Fragment() {

    private var request: CheckoutRequest? = null
    private var showPaymentMethodResponse: ShowPaymentMethodResponse? = null

    private lateinit var fragmentPaymentMethodBinding: FragmentPaymentMethodBinding

    private var selectedCategory: String? = null

    private val paymentViewModel: InitialPaymentViewModel by lazy {
        ViewModelProvider(requireParentFragment())[InitialPaymentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            request = it.getSerializable(ARG_REQUEST) as CheckoutRequest?
            showPaymentMethodResponse =
                it.getSerializable(ARG_RESPONSE) as ShowPaymentMethodResponse?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentPaymentMethodBinding =
            FragmentPaymentMethodBinding.inflate(inflater, container, false)
        val view = fragmentPaymentMethodBinding.root
        Log.d("DOKU_SDK", showPaymentMethodResponse?.invoice_number ?: "")
        selectedCategory = null
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        listener.onUpdateTitle("Payment Method")
        listener.onShowBackIcon(false)
        listener.onShowCloseIcon(true)
        if (showPaymentMethodResponse?.payment_method_type != null) {
            showPaymentMethodResponse?.payment_method_type?.let { updatePaymentMethodsUI(it.toMutableList()) }
        }
    }

    fun initObserver() {
        paymentViewModel.cardTokenize.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { token ->
                if(token.isNotEmpty()) {
                    listener.onUpdateTitle("Cards")
                    listener.onShowBackIcon(true)
                    listener.onShowCloseIcon(false)
                    listener.onUpdateBackAction("Payment Method", false, true)
                    (parentFragment as? PaymentBottomSheet)?.childFragmentManager?.beginTransaction()
                        ?.addToBackStack(null)
                        ?.replace(
                            R.id.child_fragment_container,
                            CardTokenization(cardTokenization = token, fromAllList = false, listener=listener)
                        )
                        ?.commit()
                } else {
                    listener.onUpdateTitle("Cards")
                    listener.onShowBackIcon(true)
                    listener.onShowCloseIcon(false)
                    listener.onUpdateBackAction("Payment Method", false, true)
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

    private fun updatePaymentMethodsUI(categories: MutableList<PaymentMethodType>) {
        val container = fragmentPaymentMethodBinding.paymentMethodsContainer
        val tvViewAll = fragmentPaymentMethodBinding.tvViewAll
        val layoutViewAll = fragmentPaymentMethodBinding.layoutViewAll

        val inflater = LayoutInflater.from(requireContext())

        var visibleCategories = mutableListOf<PaymentMethodType>()
        categories.forEach { c ->
            if(!DokuConfig.nextPhasePayment.contains(c.category_name)) {
                visibleCategories.add(c)
            }
        }

        var otherPaymentMethod = visibleCategories - visibleCategories.take(3)

        layoutViewAll.setOnClickListener {
            listener.onUpdateTitle("Other Payment Method")
            listener.onShowBackIcon(true)
            listener.onShowCloseIcon(false)
            listener.onUpdateBackAction("Payment Method", false, true)
            paymentViewModel.selectedPaymentMethod.value = null
            selectedCategory = null
            (parentFragment as? PaymentBottomSheet)
                ?.childFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.child_fragment_container, AllPaymentMethodFragment(listener = listener, checkoutResponse = showPaymentMethodResponse, otherPaymentMethod = otherPaymentMethod.toMutableList()))
                ?.addToBackStack(null)
                ?.commit()
        }

        container.removeAllViews()
        for (category in visibleCategories.take(3)) {
            if(!DokuConfig.nextPhasePayment.contains(category.category_name)) {
                val categoryLabel = when (category.category_name) {
                    "VIRTUAL_ACCOUNT" -> "Bank Transfer"
                    "EMONEY" -> "e-Wallet"
                    "PEER_TO_PEER" -> "Pay Later"
                    "DIRECT_DEBIT" -> "Direct Debit"
                    "QRIS" -> "QRIS"
                    "CREDIT_CARD" -> "Credit Card"
                    "ONLINE_TO_OFFLINE" -> "Convenience Store"
                    else -> category
                }

                val itemView = inflater.inflate(R.layout.item_payment_method, container, false)
                val icon = itemView.findViewById<ImageView>(R.id.ivIcon)
                val name = itemView.findViewById<TextView>(R.id.tvName)
                val arrow = itemView.findViewById<ImageView>(R.id.ic_arrow_right)
                val background = itemView.findViewById<View>(R.id.methodBackground)
                val checkCircle = itemView.findViewById<View>(R.id.checked)
                name.text = categoryLabel.toString()

                val iconRes = when (category.category_name) {
                    "VIRTUAL_ACCOUNT" -> R.drawable.ic_bank
                    "EMONEY" -> R.drawable.ic_wallet
                    "PEER_TO_PEER" -> R.drawable.ic_paylater
                    "DIRECT_DEBIT" -> R.drawable.ic_direct_deb
                    "QRIS" -> R.drawable.ic_qris
                    "CREDIT_CARD" -> R.drawable.ic_card
                    "ONLINE_TO_OFFLINE" -> R.drawable.ic_convinience
                    else -> R.drawable.ic_wallet
                }
                icon.setImageResource(iconRes)

                if (category.category_name == "QRIS") {
                    arrow.visibility = View.GONE
                }

                if (selectedCategory == category.category_name) {
                    if (selectedCategory == "QRIS") {
                        background.setBackgroundColor(Color.parseColor("#E8F8F7"))
                        checkCircle.visibility = View.VISIBLE
                    } else {
                        background.setBackgroundColor(Color.WHITE)
                        checkCircle.visibility = View.GONE
                    }
                } else {
                    background.setBackgroundColor(Color.WHITE)
                }

                itemView.setOnClickListener {
                    selectedCategory = category.category_name
                    if (category.category_name == "QRIS") {
                        paymentViewModel.selectedPaymentMethod.value =
                            Helper().mappingCategoryPaymentMethod(selectedCategory ?: "")
                    } else if (category.category_name == "VIRTUAL_ACCOUNT") {
                        listener.onUpdateTitle("Bank Transfer")
                        listener.onShowBackIcon(true)
                        listener.onShowCloseIcon(true)
                        listener.onUpdateBackAction("Payment Method", false, true)
                        (parentFragment as? PaymentBottomSheet)
                            ?.childFragmentManager
                            ?.beginTransaction()
                            ?.replace(R.id.child_fragment_container, BankTransferFragment(listener = listener, checkoutResponse = showPaymentMethodResponse))
                            ?.addToBackStack(null)
                            ?.commit()
                    } else if (category.category_name == "ONLINE_TO_OFFLINE") {
                        listener.onUpdateTitle("Convenience Store")
                        listener.onShowBackIcon(true)
                        listener.onShowCloseIcon(true)
                        listener.onUpdateBackAction("Payment Method", false, true)
                        (parentFragment as? PaymentBottomSheet)
                            ?.childFragmentManager
                            ?.beginTransaction()
                            ?.replace(R.id.child_fragment_container, ConvenienceStoreFragment(listener = listener, checkoutResponse = showPaymentMethodResponse))
                            ?.addToBackStack(null)
                            ?.commit()
                    } else if (category.category_name == "EMONEY") {
                        listener.onUpdateTitle("e-Wallet")
                        listener.onShowBackIcon(true)
                        listener.onShowCloseIcon(false)
                        listener.onUpdateBackAction("Payment Method", false, true)
                        (parentFragment as? PaymentBottomSheet)
                            ?.childFragmentManager
                            ?.beginTransaction()
                            ?.replace(
                                R.id.child_fragment_container,
                                EwalletListFragment(checkoutResponse = showPaymentMethodResponse, listener)
                            )
                            ?.addToBackStack(null)
                            ?.commit()
                    } else if (category.category_name == "CREDIT_CARD") {
                        paymentViewModel.doCardTokenization()
                    }
                    updatePaymentMethodsUI(categories)
                }


                container.addView(itemView)
                activity?.runOnUiThread {
                    if (categories.size > 3) {
                        layoutViewAll.visibility = View.VISIBLE
                    } else {
                        layoutViewAll.visibility = View.GONE
                    }
                }
            }

        }

    }

    companion object {
        private const val ARG_REQUEST = "request"
        private const val ARG_RESPONSE = "response"
        @JvmStatic
        fun newInstance(
            request: CheckoutRequest,
            showPaymentMethodResponse: ShowPaymentMethodResponse?,
            listener: PaymentBottomSheetListener
        ) =
            PaymentMethodFragment(listener).apply {
                val fragment = PaymentMethodFragment(listener)
                val bundle = Bundle()
                bundle.putSerializable(ARG_REQUEST, request)
                bundle.putSerializable(ARG_RESPONSE, showPaymentMethodResponse)
                fragment.arguments = bundle
                return fragment
            }
    }
}