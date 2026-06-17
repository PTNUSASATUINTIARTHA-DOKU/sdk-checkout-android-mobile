package com.doku.sdkcheckoutandroid.ui.qris

import android.content.ContentValues
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.PaymentBottomSheet
import com.doku.sdkcheckoutandroid.PaymentBottomSheetListener
import com.doku.sdkcheckoutandroid.databinding.FragmentQrisSuccessPageBinding
import com.doku.sdkcheckoutandroid.enum.PaymentStatusEnum
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.response.QRISPaymentResponse
import com.doku.sdkcheckoutandroid.ui.general.ChangePaymentMethodBottomSheet
import com.doku.sdkcheckoutandroid.ui.general.InfoPaymentPendingBottomSheet
import com.doku.sdkcheckoutandroid.ui.general.QuitConfirmationBottomSheet
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class QrisSuccessPage(
    private val listener: PaymentBottomSheetListener
) : Fragment() {
    private var response: QRISPaymentResponse? = null

    private lateinit var fragmentQrisSuccessPageBinding: FragmentQrisSuccessPageBinding

    private val paymentViewModel: InitialPaymentViewModel by lazy {
        ViewModelProvider(requireParentFragment())[InitialPaymentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            response = it.getSerializable(ARG_RESPONSE) as QRISPaymentResponse?

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentQrisSuccessPageBinding =
            FragmentQrisSuccessPageBinding.inflate(inflater, container, false)
        val view = fragmentQrisSuccessPageBinding.root
        return view
    }

    // This function is to set view / action on parent bottomsheet
    fun updateBottomSheetFunctional() {

        listener.onUpdateTitle("QRIS")
        listener.onShowBackIcon(false)
        listener.onShowCloseIcon(true)
        listener.onUpdateCloseAction {
            val sheet = QuitConfirmationBottomSheet(
                { (parentFragment as? PaymentBottomSheet)?.closeAll() }
            )
            sheet.show(requireActivity().supportFragmentManager, "QrisClose")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateBottomSheetFunctional()

        val nmidTv = fragmentQrisSuccessPageBinding.tvNmid
        nmidTv.setText("NMID: ${response?.nmid}")

        val codeQr = response?.qrCode ?: ""
        val qr = codeQr.toQrCodeBitmap()
        val qrImage = fragmentQrisSuccessPageBinding.ivQrCode
        qrImage.setImageBitmap(qr)

        val changePaymentMethod = fragmentQrisSuccessPageBinding.btnChangePaymentMethod
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
            sheet.show(requireActivity().supportFragmentManager, "QrisChangePaymentMethod")
        }

        val howToPayButton = fragmentQrisSuccessPageBinding.layoutHowToPay
        howToPayButton.setOnClickListener {
            val sheet = QrisHowToPay()
            sheet.show(requireActivity().supportFragmentManager, "QrisHowToPay")
        }

        val downloadButton = fragmentQrisSuccessPageBinding.btnDownloadQr
        downloadButton.setOnClickListener {
            val filename = "qris_${System.currentTimeMillis()}.png"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourAppFolder")
            }

            val uri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )

            uri?.let {
                requireContext().contentResolver.openOutputStream(it).use { out ->
                    if(qr != null)
                        qr.compress(Bitmap.CompressFormat.PNG, 100, out!!)
                }
                Toast.makeText(requireContext(), "Saved to Gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun String.toQrCodeBitmap(
        size: Int = 600,
        qrColor: Int = Color.rgb(31, 33, 38),
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                this,
                BarcodeFormat.QR_CODE,
                size,
                size
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix[x, y]) qrColor else backgroundColor
                }
            }

            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // This function is to reset functionality on Parent Bottomsheet
    fun goToHomepage() {

        listener.onUpdateTitle("Payment Method")
        listener.onShowCloseIcon(true)
        listener.onShowBackIcon(false)
        listener.onShowBottomSection(true)
        listener.onUpdateCloseAction {
            listener.closeParentBottomSheet()
        }
        for (stack in 1..<parentFragmentManager.backStackEntryCount) {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {

        private const val ARG_RESPONSE = "response"
        @JvmStatic
        fun newInstance(response: QRISPaymentResponse?, listener: PaymentBottomSheetListener) =
            QrisSuccessPage(listener).apply {
                val fragment = QrisSuccessPage(listener)
                val bundle = Bundle()
                bundle.putSerializable(ARG_RESPONSE, response)
                fragment.arguments = bundle
                return fragment
            }
    }
}