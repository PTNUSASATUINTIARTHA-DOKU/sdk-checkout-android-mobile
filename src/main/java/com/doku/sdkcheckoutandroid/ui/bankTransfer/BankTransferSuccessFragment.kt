package com.doku.sdkcheckoutandroid.ui.bankTransfer

import Helper
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import asIDR
import com.bumptech.glide.Glide
import com.doku.sdkcheckoutandroid.PaymentBottomSheet
import com.doku.sdkcheckoutandroid.PaymentBottomSheetListener
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentBankTransferSuccessBinding
import com.doku.sdkcheckoutandroid.databinding.FragmentQrisSuccessPageBinding
import com.doku.sdkcheckoutandroid.enum.PaymentLogo
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.response.GenerateCodeResponse
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.ui.general.ChangePaymentMethodBottomSheet
import com.doku.sdkcheckoutandroid.ui.general.QuitConfirmationBottomSheet
import com.doku.sdkcheckoutandroid.ui.qris.QrisHowToPay
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import org.json.JSONObject
import paymentDetail

class BankTransferSuccessFragment(private val listener: PaymentBottomSheetListener,
                                  private val response: GenerateCodeResponse?,
                                  private val checkoutResponse: ShowPaymentMethodResponse?,
                                  private val bankDetail: JSONObject?) : Fragment() {


    private lateinit var fragmentBankTransferSuccessBinding: FragmentBankTransferSuccessBinding

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
        fragmentBankTransferSuccessBinding =
            FragmentBankTransferSuccessBinding.inflate(inflater, container, false)
        val view = fragmentBankTransferSuccessBinding.root
        return view
    }

    fun updateBottomSheetFunctional() {

        listener.onUpdateTitle("Bank Transfer")
        listener.onUpdateCloseAction {
            val sheet = QuitConfirmationBottomSheet(
                { (parentFragment as? PaymentBottomSheet)?.closeAll() }
            )
            sheet.show(requireActivity().supportFragmentManager, "VaClose")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateBottomSheetFunctional()
        val detailBank = checkoutResponse?.paymentDetail("VIRTUAL_ACCOUNT", paymentViewModel.additionalRequest.value ?: "")
        fragmentBankTransferSuccessBinding.tvNumber.text = response?.paymentCode ?: "-"
        val formattedBank = detailBank?.payment_channel_id.orEmpty().replace("_", " ")
        fragmentBankTransferSuccessBinding.tvName.text = bankDetail?.getString("name") ?: formattedBank
        val logo = PaymentLogo.from(detailBank?.payment_channel_id?.replace("VIRTUAL_ACCOUNT_", "") ?: "")
        val url = bankDetail?.getString("logo") ?: logo.fullUrl
        if (context != null) {
            Glide.with(context!!)
                .load(url)
                .override(36,36)
                .into(fragmentBankTransferSuccessBinding.logoBank)
        }
        fragmentBankTransferSuccessBinding.tvAmount.text = checkoutResponse?.amount?.toDouble()?.asIDR()
        fragmentBankTransferSuccessBinding.tvDate.text = Helper().convertDateFormat(response?.expiredDateUTC ?: "")
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        fragmentBankTransferSuccessBinding.ivCopy.setOnClickListener {
            val clip = ClipData.newPlainText("va", response?.paymentCode ?: "-")
            clipboard.setPrimaryClip(clip)
        }

        fragmentBankTransferSuccessBinding.ivCopyAmount.setOnClickListener {
            val clip = ClipData.newPlainText("amount", checkoutResponse?.amount.toString())
            clipboard.setPrimaryClip(clip)
        }

        fragmentBankTransferSuccessBinding.layoutHowToPay.setOnClickListener {
            val sheet = BankHowToPageFragment(vaResponse = response)
            sheet.show(requireActivity().supportFragmentManager, "VAHowToPay")
        }

        val changePaymentMethod = fragmentBankTransferSuccessBinding.btnChangePaymentMethod
        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
            val parsedColor = Color.parseColor(color)
                changePaymentMethod.setTextColor(parsedColor)
        }
        changePaymentMethod.setOnClickListener {
            val sheet = ChangePaymentMethodBottomSheet{
                paymentViewModel.selectedPaymentMethod.value = null
                goToHomepage()
            }
            sheet.show(requireActivity().supportFragmentManager, "vaChangePaymentMethod")
        }
    }
    // This function is to reset functionality on Parent Bottomsheet
    fun goToHomepage() {

        listener.onUpdateTitle("Payment Method")
        listener.onShowBottomSection(true)
        listener.onUpdateCloseAction {
            listener.closeParentBottomSheet()
        }

        for (stack in 1..<parentFragmentManager.backStackEntryCount) {
            parentFragmentManager.popBackStack()
        }

    }
}