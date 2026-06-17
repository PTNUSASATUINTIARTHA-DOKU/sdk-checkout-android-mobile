package com.doku.sdkcheckoutandroid

import Helper
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.doku.sdkcheckoutandroid.data.TransactionRepository
import com.doku.sdkcheckoutandroid.databinding.LayoutPaymentBottomSheetBinding
import com.doku.sdkcheckoutandroid.db.TransactionDatabase
import com.doku.sdkcheckoutandroid.entity.Transaction
import com.doku.sdkcheckoutandroid.enum.PaymentMethodEnum
import com.doku.sdkcheckoutandroid.enum.PaymentStatusEnum
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.request.CheckoutRequest
import com.doku.sdkcheckoutandroid.model.request.LineItem
import com.doku.sdkcheckoutandroid.model.request.Order
import com.doku.sdkcheckoutandroid.model.request.Origin
import com.doku.sdkcheckoutandroid.model.response.CardPaymentResponse
import com.doku.sdkcheckoutandroid.model.response.GenerateCodeResponse
import com.doku.sdkcheckoutandroid.model.response.JumpAppWalletResponse
import com.doku.sdkcheckoutandroid.model.response.QRISPaymentResponse
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.ui.InitialPayment.PaymentMethodFragment
import com.doku.sdkcheckoutandroid.ui.bankTransfer.BankTransferSuccessFragment
import com.doku.sdkcheckoutandroid.ui.card.CardTokenization
import com.doku.sdkcheckoutandroid.ui.convenienceStore.ConvenienceStoreSuccessFragment
import com.doku.sdkcheckoutandroid.ui.ewallet.EwalletFormFragment
import com.doku.sdkcheckoutandroid.ui.ewallet.EwalletListFragment
import com.doku.sdkcheckoutandroid.ui.general.AmountDetailFragment
import com.doku.sdkcheckoutandroid.ui.general.InfoPaymentPendingBottomSheet
import com.doku.sdkcheckoutandroid.ui.general.QuitConfirmationBottomSheet
import com.doku.sdkcheckoutandroid.ui.general.SdkWebviewFragment
import com.doku.sdkcheckoutandroid.ui.general.UnselectedPaymentMethodFragment
import com.doku.sdkcheckoutandroid.ui.paymentState.PaymentExpiredStateFragment
import com.doku.sdkcheckoutandroid.ui.paymentState.PaymentFailedStateFragment
import com.doku.sdkcheckoutandroid.ui.paymentState.PaymentPendingStateFragment
import com.doku.sdkcheckoutandroid.ui.paymentState.PaymentSuccessStateFragment
import com.doku.sdkcheckoutandroid.ui.qris.QrisSuccessPage
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

interface PaymentBottomSheetListener {
    fun onUpdateTitle(title: String)
    fun onUpdateCloseAction(action: () -> Unit)
    fun onShowBottomSection(show: Boolean)
    fun closeParentBottomSheet()
    fun onShowCloseIcon(visible: Boolean)
    fun onShowBackIcon(visible: Boolean)
    fun onUpdateBackAction(title: String, showBackIcon: Boolean, showCloseIcon: Boolean)
}

interface OnSdkComplete {
    fun onPaymentComplete(status: String)
}

class PaymentBottomSheet() : BottomSheetDialogFragment(), PaymentBottomSheetListener {

    private var clientId: String? = null
    private var requestId: String? = null
    private var requestTimestamp: String? = null
    private var invoiceNumber: String? = null
    private var signature: String? = null

    private var checkoutRequest: CheckoutRequest? = null

    private lateinit var  initialPaymentViewModel: InitialPaymentViewModel

    private var showPaymentMethodResponse: ShowPaymentMethodResponse? = null

    private var selectedPaymentMethod: PaymentMethodEnum? = null

    private lateinit var layoutPaymentBottomSheetBinding: LayoutPaymentBottomSheetBinding

    private lateinit var promoSection: View

    private lateinit var checkStatusSection: View

    private var closeAction: (() -> Unit)? = null

    private var resultListener: OnSdkComplete? = null

    private var production: Boolean = false

    fun setOnSdkCompleteListener(listener: OnSdkComplete) {
        resultListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            clientId = it.getString(ARG_CLIENT_ID)
            requestTimestamp = it.getString(ARG_TIMESTAMP)
            requestId = it.getString(ARG_REQ_ID)
            invoiceNumber = it.getString(ARG_INVOICE)
            signature = it.getString(ARG_SIGNATURE)
            checkoutRequest = it.getSerializable(ARG_CHECKOUT_REQUEST) as CheckoutRequest?
            production = it.getBoolean(ARG_PRODUCTION)
            DokuConfig.colorPallete = it.getString(ARG_COLOR_PALLETE)
        }

        DokuConfig.setEnvironment(production)
        DokuConfig.setMerchantConfig(clientId = clientId!!, requestTimestamp = requestTimestamp!!,
            requestId = requestId!!, invoiceNumber = invoiceNumber!!, signature = signature!!)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setOnShowListener { _ ->
            val bottomSheet =
                (dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                        as FrameLayout)

            val behavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

            behavior.isDraggable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            ViewCompat.setOnApplyWindowInsetsListener(bottomSheet) { v, insets ->
                v.setPadding(0, 0, 0, 0)
                insets
            }
        }
        closeAction = {
            dismiss()
        }
        return dialog
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        checkoutRequest?.origin = Origin(product = "Checkout SDK", "sdk-checkout-android", system = "sdk-checkout-android")
        val dao = TransactionDatabase.getInstance().transactionDao()
        val repository = TransactionRepository(dao)
        val factory = InitialPaymentViewModelFactory(repository, this)

        initialPaymentViewModel = ViewModelProvider(this, factory)
            .get(InitialPaymentViewModel::class.java)

        layoutPaymentBottomSheetBinding =
            LayoutPaymentBottomSheetBinding.inflate(inflater, container, false)

        val view = layoutPaymentBottomSheetBinding.root
        val totalPayment = layoutPaymentBottomSheetBinding.tvTotalPayment
        val btnClose = layoutPaymentBottomSheetBinding.btnClose
        val btnCheckPayment = layoutPaymentBottomSheetBinding.btnCheckStatus
        promoSection = layoutPaymentBottomSheetBinding.promoSection
        checkStatusSection = layoutPaymentBottomSheetBinding.checkStatusSection

        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
                val parsedColor = Color.parseColor(color)
                btnCheckPayment.backgroundTintList =
                    ColorStateList.valueOf(parsedColor)
                val textView = btnCheckPayment.getChildAt(0) as? TextView
                textView?.setTextColor(Color.WHITE)
            }

        val formattedAmount = NumberFormat.getNumberInstance(Locale("in", "ID")).format(checkoutRequest?.order?.amount ?: 0)
        "IDR $formattedAmount".also { totalPayment.text = it }

        btnClose.setOnClickListener {
            closeAction?.invoke()
        }

        btnCheckPayment.setOnClickListener {
            lifecycleScope.launch {
                initialPaymentViewModel.checkPaymentStatus()
            }
        }

        lifecycleScope.launch {
            initialPaymentViewModel.getCache(checkoutRequest!!)
        }
        initObserver()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutPaymentBottomSheetBinding.btnPayNow.setOnClickListener {
            selectedPaymentMethod?.let { method ->
                initialPaymentViewModel.payNow(methodEnum = method)
            } ?: run {
                val sheet = UnselectedPaymentMethodFragment()
                sheet.show(requireActivity().supportFragmentManager, "")
            }
        }
        val btnPayNow = view.findViewById<Button>(R.id.btnPayNow)
        val totalPayment = view.findViewById<TextView>(R.id.tvTotalPayment)
        val tvCountdown = view.findViewById<TextView>(R.id.tvCountdown)

        if(checkoutRequest?.order?.line_items?.size == 0 || checkoutRequest?.order?.line_items == null) {
            totalPayment.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, 0, 0
            )
        } else {
            totalPayment.setOnClickListener {
                val sheet = AmountDetailFragment(checkoutRequest = checkoutRequest!!)
                sheet.show(requireActivity().supportFragmentManager, "PendingPayment")
            }
        }

        DokuConfig.colorPallete
            ?.takeIf { it.isNotBlank() }
            ?.let { color ->
            val parsedColor = Color.parseColor(color)
            btnPayNow.backgroundTintList =
                ColorStateList.valueOf(parsedColor)
            totalPayment.setTextColor(parsedColor)
            tvCountdown.setTextColor(parsedColor)
        }

        Glide.with(this)
            .asGif()
            .load(R.drawable.doku_loading)
            .into(layoutPaymentBottomSheetBinding.imgLoading)

        val btnUsePromo = view.findViewById<LinearLayout>(R.id.btnUsePromo)

        btnUsePromo.setOnClickListener {
            Log.d("Promo", "Use Promo clicked")
            Handler(Looper.getMainLooper()).postDelayed({
                PromoBottomSheet(initialPaymentViewModel).show(
                    requireActivity().supportFragmentManager,
                    "PromoBottomSheet"
                )
            }, 180)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startCountdown(timeoutString: String) {
        val tvCountdown = layoutPaymentBottomSheetBinding.tvCountdown

        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            val timeoutInstant = java.time.OffsetDateTime.parse(timeoutString, formatter).toInstant()
            val now = java.time.Instant.now()
            val millisUntilFinished = java.time.Duration.between(now, timeoutInstant).toMillis()

            if (millisUntilFinished <= 0) {
                "00:00:00".also { tvCountdown.text = it }
                return
            }

            // simpan timer agar tidak kehapus oleh GC
            object : CountDownTimer(millisUntilFinished, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = (millisUntilFinished / 1000) / 3600
                    val minutes = ((millisUntilFinished / 1000) % 3600) / 60
                    val seconds = (millisUntilFinished / 1000) % 60

                    val formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    tvCountdown.text = formatted
                }

                override fun onFinish() {
                    "00:00:00".also { tvCountdown.text = it }
                }
            }.start()

        } catch (e: Exception) {
            Log.e("DOKU_COUNTDOWN", "Error parsing timeout: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initObserver() {

        initialPaymentViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            layoutPaymentBottomSheetBinding.scrollViewContent.visibility = if (loading) View.GONE else View.VISIBLE
            layoutPaymentBottomSheetBinding.promoSection.visibility = if (loading) View.GONE else View.VISIBLE
            layoutPaymentBottomSheetBinding.btnBack.visibility = if (loading) View.GONE else View.VISIBLE
            layoutPaymentBottomSheetBinding.btnClose.visibility = if (loading) View.VISIBLE else View.GONE
            layoutPaymentBottomSheetBinding.layoutLoading.visibility = if (loading) View.VISIBLE else View.GONE
        }

        initialPaymentViewModel.tokenIdResponse.observe(viewLifecycleOwner) { tokenResponse ->
            if(tokenResponse.contains("ERROR_DOKU")) {
                layoutPaymentBottomSheetBinding.scrollViewContent.visibility = View.GONE
                layoutPaymentBottomSheetBinding.promoSection.visibility = View.VISIBLE
                layoutPaymentBottomSheetBinding.layoutLoading.visibility = View.VISIBLE
                layoutPaymentBottomSheetBinding.tvErrorTitle.text = "Error"
                layoutPaymentBottomSheetBinding.imgLoading.visibility = View.GONE
                layoutPaymentBottomSheetBinding.tvErrorSubTitle.text = tokenResponse.split(";").get(1)
                layoutPaymentBottomSheetBinding.tvErrorTitle.setTextColor(Color.parseColor("#E1251B"))
                layoutPaymentBottomSheetBinding.tvErrorSubTitle.setTextColor(Color.parseColor("#E1251B"))
            }
        }

        initialPaymentViewModel.paymentMethodResponse.observe(viewLifecycleOwner) { response ->
            showPaymentMethodResponse = response
            DokuConfig.setMerchantConfig(clientId = clientId!!, requestTimestamp = requestTimestamp!!,
                requestId = response.request_id ?: "", invoiceNumber = invoiceNumber!!, signature = signature!!)
            if(response.payment_method_type?.isNotEmpty() == true) {
                val timeoutString = response.timeout ?: "2025-11-13T01:48:55.000+00:00"
                activity?.runOnUiThread {
                    if (timeoutString.isNotEmpty()) {
                        startCountdown(timeoutString)
//                        calculateExpiryTime()
                        childFragmentManager.beginTransaction()
                            .replace(R.id.child_fragment_container, PaymentMethodFragment.newInstance(
                                request = checkoutRequest!!,
                                showPaymentMethodResponse = showPaymentMethodResponse,
                                listener = this
                            ))
                            .addToBackStack("main")
                            .commit()
                    }
                }
            } else {
                Log.d("DOKU_SDK", "FAILED TO SHOW PAYMENT METHOD")
            }
        }

        initialPaymentViewModel.selectedPaymentMethod.observe(viewLifecycleOwner) { method ->
            selectedPaymentMethod = method
        }

        initialPaymentViewModel.paymentResponse.observe(viewLifecycleOwner) { response ->
            if(response != null) {
                selectedPaymentMethod?.let { handlingPayment(it, response) }
            } else {
                val sheet = PaymentFailedStateFragment(checkoutRequest = checkoutRequest!!) {
                    onUpdateTitle("Payment Method")
                    onShowBottomSection(true)
                    onUpdateCloseAction {
                        closeParentBottomSheet()
                    }
                    (1..<childFragmentManager.backStackEntryCount).forEach { _ ->
                        childFragmentManager.popBackStack()
                    }
                }
                sheet.show(requireActivity().supportFragmentManager, "FailedPayment")
            }
        }

        initialPaymentViewModel.paymentError.observe(viewLifecycleOwner) { error ->
            if(error) {
                val sheet =
                    PaymentFailedStateFragment(checkoutRequest = checkoutRequest!!) {
                        onUpdateTitle("Payment Method")
                        onShowBottomSection(true)
                        onUpdateCloseAction {
                            closeParentBottomSheet()
                        }
                        (1..<childFragmentManager.backStackEntryCount).forEach { _ ->
                            childFragmentManager.popBackStack()
                        }
                    }
                sheet.show(requireActivity().supportFragmentManager, "FailedPayment")
            }
        }

        initialPaymentViewModel.cacheTransaction.observe(viewLifecycleOwner) {response ->
            if(response.isNullOrEmpty()) {
                calculateExpiryTime()
                initialPaymentViewModel.doGetTokenId(checkoutRequest = checkoutRequest!!, clientId = clientId!!,
                    requestId = requestId!!, requestTimestamp = requestTimestamp!!, signatureKey = signature!!)
            } else {
                // TODO: Implement Navigate Existing Invoice
                checkoutRequest = convertDataFromStorage(response)
                initialPaymentViewModel.setFetchedData(response.first().tokenId, PaymentMethodEnum.valueOf(response.first().paymentMethod), response.first().acquirer)
                lifecycleScope.launch {
                    initialPaymentViewModel.checkPaymentStatus()
                }
            }
        }

        initialPaymentViewModel.paymentStatus.observe(viewLifecycleOwner) { status ->
            when(status) {
                PaymentStatusEnum.PENDING.toString() -> {
                    initialPaymentViewModel.paymentStatus.value = ""
                    val sheet = InfoPaymentPendingBottomSheet()
                    sheet.show(requireActivity().supportFragmentManager, "InfoPendingPayment")
//                    if(initialPaymentViewModel.selectedPaymentMethod.value == PaymentMethodEnum.CREDIT_CARD) {
//                        val pendingSheet = requireActivity().supportFragmentManager.findFragmentByTag("PendingCreditCard")
//                        if(pendingSheet != null) {
//                            initialPaymentViewModel.paymentStatus.value = ""
//                            val sheet = InfoPaymentPendingBottomSheet()
//                            sheet.show(requireActivity().supportFragmentManager, "CreditCardInfoPendingPayment")
//                        } else {
//                            val sheet = PaymentPendingStateFragment(checkoutRequest = checkoutRequest!!, initialPaymentViewModel) {
//                                resultListener?.onPaymentComplete(status = status)
//                                closeAll()
//                            }
//                            sheet.show(requireActivity().supportFragmentManager, "PendingCreditCard")
//                        }
//                    } else {
//                        initialPaymentViewModel.paymentStatus.value = ""
//                        val sheet = InfoPaymentPendingBottomSheet()
//                        sheet.show(requireActivity().supportFragmentManager, "InfoPendingPayment")
//                    }
                }

                PaymentStatusEnum.PAID.toString() -> {
                    initialPaymentViewModel.paymentStatus.value = ""
                    initialPaymentViewModel.deleteCache(checkoutRequest = checkoutRequest!!, acquirer = initialPaymentViewModel.additionalRequest.value.toString())
                    if(checkoutRequest != null) {
                        // close pending payment bottomsheet if exist
                        val pendingSheet = requireActivity().supportFragmentManager.findFragmentByTag("PendingPayment")
                        val pendingStorageSheet = requireActivity().supportFragmentManager.findFragmentByTag("PendingStorage")
                        if (pendingSheet is BottomSheetDialogFragment) {
                            pendingSheet.dismiss()
                        } else if (pendingStorageSheet is BottomSheetDialogFragment) {
                            pendingStorageSheet.dismiss()
                        }
                        val sheet = PaymentSuccessStateFragment(checkoutRequest = checkoutRequest!!) {
                            resultListener?.onPaymentComplete(status = status)
                            closeAll()
                        }
                        sheet.show(requireActivity().supportFragmentManager, "PendingPayment")
                    }
                }

                PaymentStatusEnum.EXPIRED.toString() -> {
                    initialPaymentViewModel.paymentStatus.value = ""
                    if(checkoutRequest != null) {
                        // close pending payment bottomsheet if exist
                        val pendingSheet = requireActivity().supportFragmentManager.findFragmentByTag("PendingPayment")
                        if (pendingSheet is BottomSheetDialogFragment) {
                            pendingSheet.dismiss()
                        }
                        val sheet =
                            PaymentExpiredStateFragment(checkoutRequest = checkoutRequest!!) {
                                resultListener?.onPaymentComplete(status = status)
                                initialPaymentViewModel.deleteCache(checkoutRequest = checkoutRequest!!, acquirer = initialPaymentViewModel.additionalRequest.value.toString())
                                closeAll()
                            }
                        sheet.show(requireActivity().supportFragmentManager, "ExpiryPayment")
                    }
                }

                PaymentStatusEnum.FAILED.toString() -> {
                    initialPaymentViewModel.paymentStatus.value = ""
                    if(checkoutRequest != null) {
                        // close pending payment bottomsheet if exist
                        val pendingSheet = requireActivity().supportFragmentManager.findFragmentByTag("PendingPayment")
                        if (pendingSheet is BottomSheetDialogFragment) {
                            pendingSheet.dismiss()
                        }
                        val sheet =
                            PaymentFailedStateFragment(checkoutRequest = checkoutRequest!!) {
                                onUpdateTitle("Payment Method")
                                onShowBottomSection(true)
                                onUpdateCloseAction {
                                    closeParentBottomSheet()
                                }
                                (1..<childFragmentManager.backStackEntryCount).forEach { _ ->
                                    childFragmentManager.popBackStack()
                                }
                            }
                        sheet.show(requireActivity().supportFragmentManager, "FailedPayment")
                    }
                }

                PaymentStatusEnum.PENDING_STORAGE.toString() -> {
                    initialPaymentViewModel.paymentStatus.value = ""
                    when(initialPaymentViewModel.selectedPaymentMethod.value) {
                        PaymentMethodEnum.QRIS -> {
                            initialPaymentViewModel.doShowPaymentMethod(initialPaymentViewModel.tokenIdResponse.value ?: "")
                            lifecycleScope.launch {
                                delay(500)
                                initialPaymentViewModel.doGenerateQris()
                            }
                        }

                        PaymentMethodEnum.BANK_TRANSFER, PaymentMethodEnum.CONVENIENCE_STORE -> {
                            initialPaymentViewModel.doShowPaymentMethod(initialPaymentViewModel.tokenIdResponse.value ?: "")
                            lifecycleScope.launch {
                                delay(1000)
                                initialPaymentViewModel.doGenerateVa()
                            }
                        }

                        else -> {
                            if(checkoutRequest != null) {
                                onShowBottomSection(true)
                                initialPaymentViewModel.doShowPaymentMethod(initialPaymentViewModel.tokenIdResponse.value ?: "")
                                lifecycleScope.launch {
                                    delay(500)
                                    val sheet =
                                        PaymentPendingStateFragment(checkoutRequest = checkoutRequest!!, initialPaymentViewModel) {
                                            resultListener?.onPaymentComplete(status = status)
                                            initialPaymentViewModel.selectedPaymentMethod.value = null
                                            val pendingSheet = requireActivity().supportFragmentManager.findFragmentByTag("PendingStorage")
                                            if (pendingSheet is BottomSheetDialogFragment) {
                                                pendingSheet.dismiss()
                                            }
                                        }
                                    sheet.show(requireActivity().supportFragmentManager, "PendingStorage")
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    fun closeAll() {
        dismiss()
    }

    fun handlingPayment(method: PaymentMethodEnum, response: Any) {
        initialPaymentViewModel.storeNewCache(checkoutRequest = checkoutRequest!!, acquirer = initialPaymentViewModel.additionalRequest.value.toString())
        Log.d("RESPONSE", response.toString())
        when (method) {
            PaymentMethodEnum.QRIS -> {
                onShowBottomSection(false)
                val qrisResponse = response as? QRISPaymentResponse
                childFragmentManager.beginTransaction()
                    .replace(R.id.child_fragment_container, QrisSuccessPage.newInstance(response = qrisResponse, this))
                    .addToBackStack(null)
                    .commit()
            }

            PaymentMethodEnum.BANK_TRANSFER -> {
                onShowBottomSection(false)
                val vaResponse = response as? GenerateCodeResponse
                onShowCloseIcon(true)
                onShowBackIcon(false)
                childFragmentManager.beginTransaction()
                    .replace(R.id.child_fragment_container, BankTransferSuccessFragment(this, response = vaResponse, checkoutResponse = showPaymentMethodResponse, bankDetail = initialPaymentViewModel.bankDetail.value))
                    .addToBackStack(null)
                    .commit()
            }

            PaymentMethodEnum.CONVENIENCE_STORE -> {
                onShowBottomSection(false)
                val vaResponse = response as? GenerateCodeResponse
                onShowCloseIcon(true)
                onShowBackIcon(false)
                childFragmentManager.beginTransaction()
                    .replace(R.id.child_fragment_container, ConvenienceStoreSuccessFragment(this, response = vaResponse, checkoutResponse = showPaymentMethodResponse, bankDetail = initialPaymentViewModel.bankDetail.value))
                    .addToBackStack(null)
                    .commit()
            }

            PaymentMethodEnum.DANA, PaymentMethodEnum.SHOPEE_PAY, PaymentMethodEnum.OVO, PaymentMethodEnum.LINK_AJA -> {
                promoSection.visibility = View.GONE
                val response = response as? JumpAppWalletResponse
                val request = checkoutRequest ?: return

                val sheet = if (response?.redirectUrlHttp != null) {
                    PaymentPendingStateFragment(checkoutRequest = request, initialPaymentViewModel) {
                        onUpdateTitle("Payment Method")
                        onShowBottomSection(true)
                        onUpdateCloseAction {
                            closeParentBottomSheet()
                        }
                        (1..<childFragmentManager.backStackEntryCount).forEach { _ ->
                            childFragmentManager.popBackStack()
                        }
                    }
                } else {
                    PaymentFailedStateFragment(checkoutRequest = request) {
                        onUpdateTitle("Payment Method")
                        onShowBottomSection(true)
                        onUpdateCloseAction {
                            closeParentBottomSheet()
                        }
                        (1..<childFragmentManager.backStackEntryCount).forEach { _ ->
                            childFragmentManager.popBackStack()
                        }
                    }
                }

                if (sheet is PaymentPendingStateFragment) {
                    sheet.show(
                        requireActivity().supportFragmentManager,
                        "PendingPayment"
                    )

                    lifecycleScope.launch {
                        delay(500)
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(response?.redirectUrlHttp)))
                    }
                } else {
                    sheet.show(
                        requireActivity().supportFragmentManager,
                        "FailedPayment"
                    )
                }


            }

            PaymentMethodEnum.DOKU -> {
                lifecycleScope.launch {
                    delay(100)
                    onShowBackIcon(false)
                    onShowCloseIcon(true)
                    promoSection.visibility = View.GONE
                }
                onUpdateCloseAction {
                    val sheet = QuitConfirmationBottomSheet { closeAll() }
                    sheet.show(requireActivity().supportFragmentManager, "DokuClose")
                }
                val response = response as? JumpAppWalletResponse
                childFragmentManager
                    .beginTransaction()
                    .replace(R.id.child_fragment_container, SdkWebviewFragment(response?.redirectUrlHttp ?: ""))
                    .addToBackStack(null)
                    .commit()
            }

            PaymentMethodEnum.CREDIT_CARD -> {
                lifecycleScope.launch {
                    delay(100)
                    onShowBackIcon(false)
                    onShowCloseIcon(true)
                    onUpdateTitle("Verify Cards")
                    promoSection.visibility = View.GONE
                }
                onUpdateCloseAction {
                    val sheet = QuitConfirmationBottomSheet { closeAll() }
                    sheet.show(requireActivity().supportFragmentManager, "DokuClose")
                }
                val response = response as? CardPaymentResponse
                childFragmentManager
                    .beginTransaction()
                    .replace(R.id.child_fragment_container, SdkWebviewFragment(response?.url ?: ""))
                    .addToBackStack(null)
                    .commit()
            }

            else -> {}
        }
    }

    override fun onUpdateTitle(title: String) {
        layoutPaymentBottomSheetBinding.tvTitle.setText(title)
    }

    override fun onUpdateCloseAction(action: () -> Unit) {
        closeAction = action
        layoutPaymentBottomSheetBinding.btnClose.setOnClickListener {
            closeAction?.invoke()
        }
    }

    override fun onShowBottomSection(show: Boolean) {
        if(show) {
            promoSection.visibility = View.VISIBLE
            checkStatusSection.visibility = View.GONE
        } else {
            promoSection.visibility = View.GONE
            checkStatusSection.visibility = View.VISIBLE
        }
    }

    override fun closeParentBottomSheet() {
        closeAll()
    }

    override fun onShowCloseIcon(visible: Boolean) {
        if(visible) {
            layoutPaymentBottomSheetBinding.btnClose.visibility = View.VISIBLE
        } else {
            layoutPaymentBottomSheetBinding.btnClose.visibility = View.GONE
        }
    }

    override fun onShowBackIcon(visible: Boolean) {
        if(visible) {
            layoutPaymentBottomSheetBinding.btnBack.visibility = View.VISIBLE
        } else {
            layoutPaymentBottomSheetBinding.btnBack.visibility = View.GONE
        }
    }

    override fun onUpdateBackAction(title: String, showBackIcon: Boolean, showCloseIcon: Boolean) {
        layoutPaymentBottomSheetBinding.btnBack.setOnClickListener {
            initialPaymentViewModel.selectedPaymentMethod.value = null
            onUpdateTitle(title)
            onShowBackIcon(showBackIcon)
            onShowCloseIcon(showCloseIcon)
            childFragmentManager.popBackStack()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateExpiryTime() {
        val now = LocalDateTime.now()
        val expiryTime = now.plusMinutes(checkoutRequest?.payment?.payment_due_date?.toLong() ?: 0)
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH.mm", Locale.US)
        val result = expiryTime.format(formatter)
        DokuConfig.setExpiryTime(result)
    }

    fun navigateDOKUPage() {
        if(initialPaymentViewModel.additionalRequest.value != "" && Helper().isValidEmailOrPhone(initialPaymentViewModel.additionalRequest.value ?: "")) {
            initialPaymentViewModel.selectedPaymentMethod.value = PaymentMethodEnum.DOKU
            initialPaymentViewModel.payNow(PaymentMethodEnum.DOKU)
        } else if (initialPaymentViewModel.hasNavigateDoku.value == false) {
            initialPaymentViewModel.hasNavigateDoku.value = true
            onUpdateTitle("DOKU Wallet")
            onShowBackIcon(true)
            onShowCloseIcon(false)
            onUpdateBackAction("e-Wallet", true, false)
            childFragmentManager
                .beginTransaction()
                .replace(R.id.child_fragment_container, EwalletFormFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    fun navigateLinkAja() {
        if(initialPaymentViewModel.additionalRequest.value != "" && Helper().isValidPhoneNumber(initialPaymentViewModel.additionalRequest.value ?: "", "0")) {
            initialPaymentViewModel.selectedPaymentMethod.value = PaymentMethodEnum.LINK_AJA
            initialPaymentViewModel.payNow(PaymentMethodEnum.LINK_AJA)
        } else if (initialPaymentViewModel.hasNavigateLinkAja.value == false) {
            initialPaymentViewModel.hasNavigateLinkAja.value = true
            onUpdateTitle("Link Aja")
            onShowBackIcon(true)
            onShowCloseIcon(false)
            onUpdateBackAction("e-Wallet", true, false)
            childFragmentManager
                .beginTransaction()
                .replace(R.id.child_fragment_container, EwalletFormFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    fun navigateOvo() {
        if(initialPaymentViewModel.additionalRequest.value != "" && Helper().isValidPhoneNumber(initialPaymentViewModel.additionalRequest.value ?: "", "62")) {
            initialPaymentViewModel.selectedPaymentMethod.value = PaymentMethodEnum.OVO
            initialPaymentViewModel.payNow(PaymentMethodEnum.OVO)
        } else if (initialPaymentViewModel.hasNavigateOvo.value == false) {
            initialPaymentViewModel.hasNavigateOvo.value = true
            onUpdateTitle("OVO")
            onShowBackIcon(true)
            onShowCloseIcon(false)
            onUpdateBackAction("e-Wallet", true, false)
            childFragmentManager
                .beginTransaction()
                .replace(R.id.child_fragment_container, EwalletFormFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    fun convertDataFromStorage(response: List<Transaction>): CheckoutRequest {
        val data = response.first()
        val type = object : TypeToken<List<LineItem>>() {}.type
        var list: List<LineItem>? = null
        DokuConfig.setExpiryTime(data.paymentDueDate)
        if(data.paymentList != null) {
            list = Gson().fromJson(data.paymentList, type)
        }

        val order = Order(
            amount = data.amount,
            invoice_number = data.invoiceNumber,
            currency = "IDR",
            language = "EN",
            line_items = list
        )

        return CheckoutRequest(
            order = order
        )
    }
    fun isValidColor(color: String?): Boolean {
        if (color.isNullOrBlank()) return false
        return try {
            Color.parseColor(color)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    companion object {
        private const val ARG_CLIENT_ID = "client_id"
        private const val ARG_REQ_ID = "request_id"
        private const val ARG_TIMESTAMP = "timestamp"
        private const val ARG_SIGNATURE = "signature"
        private const val ARG_INVOICE = "invoice"

        private const val ARG_CHECKOUT_REQUEST = "request_checkout"

        private const val ARG_PRODUCTION = "is_production"
        private const val ARG_COLOR_PALLETE = "colorPallete"

        fun newInstance(
            clientId: String,
            requestId: String,
            requestTimestamp: String,
            signatureKey: String,
            invoiceNumber: String,
            checkoutRequest: CheckoutRequest,
            production: Boolean,
            colorPallete: String? = null
        ): PaymentBottomSheet {
            val fragment = PaymentBottomSheet()
            val args = Bundle().apply {
                putString(ARG_CLIENT_ID, clientId)
                putString(ARG_REQ_ID, requestId)
                putString(ARG_TIMESTAMP, requestTimestamp)
                putString(ARG_SIGNATURE, signatureKey)
                putString(ARG_INVOICE, invoiceNumber)
                putSerializable(ARG_CHECKOUT_REQUEST, checkoutRequest)
                putBoolean(ARG_PRODUCTION, production)
                putString(ARG_COLOR_PALLETE, colorPallete)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
