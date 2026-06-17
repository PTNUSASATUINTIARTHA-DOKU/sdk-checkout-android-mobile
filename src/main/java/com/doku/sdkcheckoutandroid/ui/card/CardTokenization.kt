package com.doku.sdkcheckoutandroid.ui.card

import Helper
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.PaymentBottomSheet
import com.doku.sdkcheckoutandroid.PaymentBottomSheetListener
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentCardTokenizationBinding
import com.doku.sdkcheckoutandroid.enum.PaymentLogo
import com.doku.sdkcheckoutandroid.enum.PaymentMethodEnum
import com.doku.sdkcheckoutandroid.model.response.CardTokenizeResponse
import com.doku.sdkcheckoutandroid.model.response.PaymentDetail
import com.doku.sdkcheckoutandroid.ui.card.adapter.CardAdapter
import com.doku.sdkcheckoutandroid.ui.convenienceStore.adapter.ConvenienceStoreAdapter
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import org.json.JSONObject
import java.util.Locale.getDefault

class CardTokenization(private val cardTokenization: List<CardTokenizeResponse>,
                       private val fromAllList: Boolean, private val listener: PaymentBottomSheetListener) : Fragment() {

    private lateinit var fragment: FragmentCardTokenizationBinding

    private lateinit var listView: ListView

    private lateinit var adapter: CardAdapter
    private var selectedCard: String? = null

    private var allCard: MutableList<CardTokenizeResponse> = cardTokenization.toMutableList()

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
            FragmentCardTokenizationBinding.inflate(inflater, container, false)
        val view = fragment.root
        listView = view.findViewById(R.id.listCardTokenization)
        initObserver()
        return view
    }

    fun initObserver() {
        paymentViewModel.deleteCardTokenize.observe(viewLifecycleOwner) { response ->
            if(response != null) {
                allCard.find { it.credit_card.token_id == response.id }?.let {
                    allCard.remove(it)
                }
                if(allCard.isEmpty()) {
                    if(fromAllList) {
                        listener.onUpdateTitle("Other Payment Method")
                    } else {
                        listener.onUpdateTitle("Payment Method")
                    }
                    parentFragmentManager.popBackStack()
                } else {
                    adapter.updateData(allCard)
                }
                adapter.notifyDataSetChanged()
                setListViewHeightBasedOnChildren(listView)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CardAdapter(requireContext(), card = allCard.toMutableList()) { item, position ->
            paymentViewModel.doDeleteCardTokenization(allCard[position].credit_card.token_id)
        }
        listView.adapter = adapter
        setListViewHeightBasedOnChildren(listView)

        fragment.layoutAddCard.setOnClickListener {
            listener.onUpdateBackAction("Cards", true, false)
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

        listView.setOnItemClickListener { _, _, position, _ ->
            val card = allCard[position]
            val cardDetail = JSONObject()
            cardDetail.put("token_id", card.credit_card.token_id)
            cardDetail.put("expiry", card.credit_card.expiry)
            cardDetail.put("save", true)
            paymentViewModel.cardDetail.value = cardDetail
            selectedCard = card.credit_card.token_id
            adapter.setSelectedCard(selectedCard)
            paymentViewModel.selectedPaymentMethod.value = PaymentMethodEnum.CREDIT_CARD
            adapter.notifyDataSetChanged()
            setListViewHeightBasedOnChildren(listView)
        }
    }

    fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return

        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(
                View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.UNSPECIFIED
            )
            totalHeight += listItem.measuredHeight
        }

        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1))
        listView.layoutParams = params
        listView.requestLayout()
    }
}