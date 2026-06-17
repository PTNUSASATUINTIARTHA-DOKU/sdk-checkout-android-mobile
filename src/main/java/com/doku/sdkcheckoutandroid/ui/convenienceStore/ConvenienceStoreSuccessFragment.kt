package com.doku.sdkcheckoutandroid.ui.convenienceStore

import Helper
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
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
import com.doku.sdkcheckoutandroid.databinding.FragmentConvenienceStoreSuccessBinding
import com.doku.sdkcheckoutandroid.enum.PaymentLogo
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.response.GenerateCodeResponse
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.ui.bankTransfer.BankHowToPageFragment
import com.doku.sdkcheckoutandroid.ui.general.ChangePaymentMethodBottomSheet
import com.doku.sdkcheckoutandroid.ui.general.QuitConfirmationBottomSheet
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import org.json.JSONObject
import paymentDetail
import java.util.EnumMap
import java.util.Locale

class ConvenienceStoreSuccessFragment(private val listener: PaymentBottomSheetListener,
                                      private val response: GenerateCodeResponse?,
                                      private val checkoutResponse: ShowPaymentMethodResponse?,
                                      private val bankDetail: JSONObject?) : Fragment() {

    private lateinit var fragmentConvenienceStoreSuccessBinding: FragmentConvenienceStoreSuccessBinding

    private val paymentViewModel: InitialPaymentViewModel by lazy {
        ViewModelProvider(requireParentFragment())[InitialPaymentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun updateBottomSheetFunctional() {

        listener.onUpdateTitle("Convenience Store")
        listener.onUpdateCloseAction {
            val sheet = QuitConfirmationBottomSheet(
                { (parentFragment as? PaymentBottomSheet)?.closeAll() }
            )
            sheet.show(requireActivity().supportFragmentManager, "StoreClose")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentConvenienceStoreSuccessBinding =
            FragmentConvenienceStoreSuccessBinding.inflate(inflater, container, false)
        val view = fragmentConvenienceStoreSuccessBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateBottomSheetFunctional()

        listener.onUpdateTitle(paymentViewModel.additionalRequest.value?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        } ?: "")

        var acquirer = ""
        if(paymentViewModel.additionalRequest.value == "alfamart") {
            acquirer = "alfa"
        } else if (paymentViewModel.additionalRequest.value == "indomaret") {
            acquirer = "indomaret"
        }

        val detailStore = checkoutResponse?.paymentDetail("ONLINE_TO_OFFLINE", acquirer)
        fragmentConvenienceStoreSuccessBinding.tvNumber.text = response?.paymentCode ?: "-"
        var storeName = ""
        if(detailStore?.payment_channel_id?.lowercase()?.contains("alfa") ?: false) {
            storeName = "Alfamart"
        } else if(detailStore?.payment_channel_id?.lowercase()?.contains("indo") ?: false) {
            storeName = "Indomaret"
        }

        fragmentConvenienceStoreSuccessBinding.tvName.text = (bankDetail?.getString("name") ?: storeName) + " Code Number"
        val logo = PaymentLogo.from(detailStore?.payment_channel_id?.replace("ONLINE_TO_OFFLINE_", "") ?: "")
        Log.d("TEST", "LOGO ${detailStore?.payment_channel_id?.replace("ONLINE_TO_OFFLINE_", "")}")
        val url = bankDetail?.getString("logo") ?: logo.fullUrl
        if (context != null) {
            Glide.with(context!!)
                .load(url)
                .override(36,36)
                .into(fragmentConvenienceStoreSuccessBinding.logoBank)
        }
        fragmentConvenienceStoreSuccessBinding.tvPaymentCode.text = response?.paymentCode ?: "-"
        val barcodeBitmap = generateBarcodeBitmap(response?.paymentCode ?: "")
        if (barcodeBitmap != null) {
            fragmentConvenienceStoreSuccessBinding.ivBarCode.setImageBitmap(barcodeBitmap)
        }

        fragmentConvenienceStoreSuccessBinding.tvAmount.text = checkoutResponse?.amount?.toDouble()?.asIDR()

        fragmentConvenienceStoreSuccessBinding.tvDate.text = Helper().convertDateFormat(response?.expiredDateUTC ?: "")
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        fragmentConvenienceStoreSuccessBinding.ivCopy.setOnClickListener {
            val clip = ClipData.newPlainText("code", response?.paymentCode ?: "-")
            clipboard.setPrimaryClip(clip)
        }

        fragmentConvenienceStoreSuccessBinding.ivCopyAmount.setOnClickListener {
            val clip = ClipData.newPlainText("amount", checkoutResponse?.amount.toString())
            clipboard.setPrimaryClip(clip)
        }

        fragmentConvenienceStoreSuccessBinding.layoutHowToPay.setOnClickListener {
            val sheet = ConvenienceStoreHowToPayFragment(response = response)
            sheet.show(requireActivity().supportFragmentManager, "VAHowToPay")
        }

        val changePaymentMethod = fragmentConvenienceStoreSuccessBinding.btnChangePaymentMethod
        DokuConfig.colorPallete?.let { color ->
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

    fun generateBarcodeBitmap(
        data: String,
        format: BarcodeFormat = BarcodeFormat.CODE_128,
        width: Int = 1500,
        height: Int = 600,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.MARGIN, 1) // default margin
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }

            val bitMatrix: BitMatrix = MultiFormatWriter().encode(data, format, width, height, hints)

            val bmp = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.ARGB_8888)
            for (x in 0 until bitMatrix.width) {
                for (y in 0 until bitMatrix.height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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