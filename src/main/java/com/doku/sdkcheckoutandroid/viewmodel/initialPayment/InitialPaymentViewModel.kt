package com.doku.sdkcheckoutandroid.viewmodel.initialPayment

import CheckoutRepository
import Helper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.doku.sdkcheckoutandroid.PaymentBottomSheet
import com.doku.sdkcheckoutandroid.data.TransactionRepository
import com.doku.sdkcheckoutandroid.entity.Transaction
import com.doku.sdkcheckoutandroid.enum.PaymentMethodEnum
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.helper.Event
import com.doku.sdkcheckoutandroid.model.request.CheckoutRequest
import com.doku.sdkcheckoutandroid.model.request.InstallmentRequest
import com.doku.sdkcheckoutandroid.model.request.PaymentRequest
import com.doku.sdkcheckoutandroid.model.response.BinEligibilityResponse
import com.doku.sdkcheckoutandroid.model.response.CardTokenizeResponse
import com.doku.sdkcheckoutandroid.model.response.DeleteCardTokenizeResponse
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.model.response.ShowPromosResponse
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class InitialPaymentViewModelFactory(
    private val repository: TransactionRepository,
    private val listener: PaymentBottomSheet? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InitialPaymentViewModel::class.java)) {
            return InitialPaymentViewModel(repository, listener) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class InitialPaymentViewModel(private val repository: TransactionRepository, private val listener: PaymentBottomSheet? = null): ViewModel() {

    private val checkoutRepository = CheckoutRepository()
    private val _tokenIdResponse: MutableLiveData<String> = MutableLiveData()
    val tokenIdResponse: LiveData<String> = _tokenIdResponse

    private val _cacheTransaction: MutableLiveData<List<Transaction>> = MutableLiveData()
    val cacheTransaction: LiveData<List<Transaction>> = _cacheTransaction

    private val _paymentMethodResponse: MutableLiveData<ShowPaymentMethodResponse> = MutableLiveData()
    val paymentMethodResponse: LiveData<ShowPaymentMethodResponse> = _paymentMethodResponse

    private val _paymentResponse: MutableLiveData<Any> = MutableLiveData()

    val paymentResponse: LiveData<Any> = _paymentResponse

    private val _paymentError: MutableLiveData<Boolean> = MutableLiveData()
    val paymentError: LiveData<Boolean> = _paymentError

    val selectedPaymentMethod: MutableLiveData<PaymentMethodEnum> = MutableLiveData()
    val additionalRequest: MutableLiveData<String> = MutableLiveData()
    val hasNavigateDoku: MutableLiveData<Boolean> = MutableLiveData(false)
    val hasNavigateOvo: MutableLiveData<Boolean> = MutableLiveData(false)
    val hasNavigateLinkAja: MutableLiveData<Boolean> = MutableLiveData(false)
    val installmentCard: MutableLiveData<InstallmentRequest> = MutableLiveData()

    val bankDetail: MutableLiveData<JSONObject> = MutableLiveData()
    val cardDetail: MutableLiveData<JSONObject> = MutableLiveData()

    private val _showPromosResponse = MutableLiveData<ShowPromosResponse?>()
    val showPromosResponse: LiveData<ShowPromosResponse?> = _showPromosResponse

    private val _cardTokenize = MutableLiveData<Event<List<CardTokenizeResponse>?>?>()
    val cardTokenize: MutableLiveData<Event<List<CardTokenizeResponse>?>?> = _cardTokenize

    private val _deleteCardTokenize = MutableLiveData<DeleteCardTokenizeResponse?>()
    val deleteCardTokenize: MutableLiveData<DeleteCardTokenizeResponse?> = _deleteCardTokenize

    private val _binEligibilityResponse = MutableLiveData<BinEligibilityResponse?>()
    val binEligibilityResponse: MutableLiveData<BinEligibilityResponse?> = _binEligibilityResponse

    private val _paymentStatus = MutableLiveData<String?>()
    val paymentStatus = _paymentStatus

    val usingCache: MutableLiveData<Boolean> = MutableLiveData()

    val isLoading = MutableLiveData<Boolean>()


    fun doGetTokenId(checkoutRequest: CheckoutRequest, clientId: String, requestId: String,
                     requestTimestamp: String, signatureKey: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            checkoutRepository.doGetTokenId(checkoutRequest = checkoutRequest, clientId = clientId, requestId = requestId, requestTimestamp=requestTimestamp, signatureKey = signatureKey) { token ->
                _tokenIdResponse.postValue(token)
                if(!token.isNullOrEmpty())
                    if(!token.contains("ERROR_DOKU")) {
                        doShowPaymentMethod(tokenId = token)
                    }
            }
        }
    }

    fun doShowPaymentMethod(tokenId: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            checkoutRepository
                .doShowPaymentMethod(tokenId) {response ->
                    isLoading.postValue(false)
                    _paymentMethodResponse.postValue(response)
                }
        }

    }

    fun payNow(methodEnum: PaymentMethodEnum) {
        try {
            _paymentError.postValue(false)
            when (methodEnum) {
                PaymentMethodEnum.QRIS -> {
                    val request = PaymentRequest(
                        token_id = _tokenIdResponse.value ?: "",
                    )
                    viewModelScope.launch {
                        isLoading.postValue(true)
                        checkoutRepository.doGenerateQRIS(request) { response ->
                            isLoading.postValue(false)
                            _paymentResponse.postValue(response)
                        }
                    }
                }

                PaymentMethodEnum.BANK_TRANSFER -> {
                    val request = PaymentRequest(
                        token_id = _tokenIdResponse.value ?: "",
                        bank = additionalRequest.value,
                    )
                    viewModelScope.launch {
                        isLoading.postValue(true)
                        checkoutRepository.doGenerateVa(request) { response ->
                            isLoading.postValue(false)
                            _paymentResponse.postValue(response)
                        }
                    }
                }

                PaymentMethodEnum.CONVENIENCE_STORE -> {
                    val request = PaymentRequest(
                        token_id = _tokenIdResponse.value ?: "",
                        bank = additionalRequest.value,
                    )
                    viewModelScope.launch {
                        isLoading.postValue(true)
                        checkoutRepository.doGenerateVa(request) { response ->
                            isLoading.postValue(false)
                            _paymentResponse.postValue(response)
                        }
                    }
                }

                PaymentMethodEnum.DOKU_NAVIGATE -> {
                    listener?.navigateDOKUPage()
                }

                PaymentMethodEnum.LINK_AJA_NAVIGATE -> {
                    listener?.navigateLinkAja()
                }

                PaymentMethodEnum.OVO_NAVIGATE -> {
                    listener?.navigateOvo()
                }

                PaymentMethodEnum.DANA,
                PaymentMethodEnum.SHOPEE_PAY, PaymentMethodEnum.OVO, PaymentMethodEnum.LINK_AJA, PaymentMethodEnum.DOKU -> {
                    val method = when (methodEnum) {
                        PaymentMethodEnum.DANA -> "emoney-dana"
                        PaymentMethodEnum.SHOPEE_PAY -> "emoney-shopeepay"
                        PaymentMethodEnum.OVO -> "emoney-ovo"
                        PaymentMethodEnum.LINK_AJA -> "internetbanking"
                        PaymentMethodEnum.DOKU -> "emoney-doku"
                        else -> return
                    }

                    val request: PaymentRequest? = when (methodEnum) {
                        PaymentMethodEnum.DANA, PaymentMethodEnum.SHOPEE_PAY -> PaymentRequest(
                            token_id = _tokenIdResponse.value.orEmpty(),
                            callback_url = "${DokuConfig.baseUrl}/checkout-link-v2/redirect/${_tokenIdResponse.value}",
                        )

                        PaymentMethodEnum.OVO -> {
                            if(Helper().isValidPhoneNumber(additionalRequest.value ?: "", "62")) {
                                PaymentRequest(
                                    token_id = _tokenIdResponse.value.orEmpty(),
                                    customer_phone = additionalRequest.value,
                                    retry_payment = false
                                )
                            } else {
                                null
                            }
                        }

                        PaymentMethodEnum.LINK_AJA -> {
                            if(Helper().isValidPhoneNumber(additionalRequest.value ?: "", "0")) {
                                PaymentRequest(
                                    token_id = _tokenIdResponse.value.orEmpty(),
                                    callback_url = "${DokuConfig.baseUrl}/checkout-link-v2/redirect/${_tokenIdResponse.value}",
                                    customer_phone = additionalRequest.value,
                                    retry_payment = false,
                                    acquirer_name = "EMONEY_LINKAJA"
                                )
                            } else {
                                null
                            }
                        }

                        PaymentMethodEnum.DOKU -> {
                            PaymentRequest(
                                token_id = _tokenIdResponse.value.orEmpty(),
                                callback_url = "${DokuConfig.baseUrl}/checkout-link-v2/redirect/${_tokenIdResponse.value}",
                                doku_id = additionalRequest.value
                            )
                        }

                        else -> null
                    }

                    if(request != null) {
                        viewModelScope.launch {
                            isLoading.postValue(true)
                            checkoutRepository.doPayJumpAppWallet(request, method) {
                                _paymentResponse.postValue(it)
                                isLoading.postValue(false)
                            }
                        }
                    }
                }

                PaymentMethodEnum.CREDIT_CARD -> {
                    if(cardDetail.value != null) {
                        val data = cardDetail.value
                        viewModelScope.launch {
                            isLoading.postValue(true)
                            checkoutRepository.doGeneratePublicKey(_tokenIdResponse.value ?: "") {response ->
                                val dataString = data.toString()
                                val publicKey = Helper().loadPublicKey(response?.public_key ?: "")
                                val encryptedData = Helper().rsaEncrypt(dataString, publicKey)
                                Log.d("ENCRYPTED", "RES $encryptedData")
                                val request: PaymentRequest = PaymentRequest(
                                    token_id = _tokenIdResponse.value ?: "",
                                    request_id = DokuConfig.requestId,
                                    client_id = DokuConfig.clientId,
                                    data = encryptedData,
                                    channel_id = "CREDIT_CARD",
                                    installment = installmentCard.value
                                )
                                doPaymentCard(request = request)
                            }
                        }
                    }

                }

                else -> {}
            }
        } catch (e: Exception) {
            isLoading.postValue(false)
            _paymentError.postValue(true)
        }

    }
    fun doShowPromos() {
        checkoutRepository.doShowPromos(_tokenIdResponse?.value ?: "") { response ->
            _showPromosResponse.postValue(response)
        }
    }


    fun getCache(checkoutRequest: CheckoutRequest) {
        viewModelScope.launch {
            isLoading.postValue(true)
            val transaction = repository.getByInvoiceNumber(checkoutRequest.order.invoice_number)
            if(transaction.isNotEmpty()) {
                _cacheTransaction.postValue(transaction)
            } else {
                _cacheTransaction.postValue(null)
            }
            isLoading.postValue(false)
        }
    }

    fun storeNewCache(checkoutRequest: CheckoutRequest, acquirer: String? = null) {
        val gson = Gson()
        viewModelScope.launch {

            if(_cacheTransaction.value?.isNotEmpty() ?: false) {
                deleteCache(checkoutRequest, acquirer)
            }
            delay(500)
            val transaction = Transaction(
                invoiceNumber = checkoutRequest.order.invoice_number,
                tokenId = _tokenIdResponse.value.toString(),
                paymentMethod = selectedPaymentMethod.value.toString(),
                amount = checkoutRequest.order.amount,
                paymentDueDate = DokuConfig.checkoutExpiryTime,
                paymentList = gson.toJson(checkoutRequest.order.line_items),
                acquirer = acquirer ?: ""
            )
            repository.insert(transaction)
        }
    }

    fun deleteCache(checkoutRequest: CheckoutRequest, acquirer: String? = null) {
        val gson = Gson()
        viewModelScope.launch {
            val transaction = Transaction(
                invoiceNumber = checkoutRequest.order.invoice_number,
                tokenId = _tokenIdResponse.value.toString(),
                paymentMethod = selectedPaymentMethod.value.toString(),
                amount = checkoutRequest.order.amount,
                paymentDueDate = DokuConfig.checkoutExpiryTime,
                paymentList = gson.toJson(checkoutRequest.order.line_items),
                acquirer = acquirer ?: ""
            )
            repository.delete(transaction)
        }
    }

    fun setFetchedData(tokenId: String, methodEnum: PaymentMethodEnum, acquirer: String?) {
        isLoading.postValue(true)
        _tokenIdResponse.value = tokenId
        selectedPaymentMethod.value = methodEnum
        additionalRequest.value = acquirer
        usingCache.value = true
        isLoading.postValue(false)
    }

    fun checkPaymentStatus() {
        viewModelScope.launch {
            checkoutRepository.doCheckStatusPayment(_tokenIdResponse.value ?: "") { response ->
                val status = response?.status.toString()
                if((usingCache.value ?: false)) {
                    if(status.equals("PENDING")) {
                        paymentStatus.postValue("PENDING_STORAGE")
                    } else {
                        _paymentStatus.postValue(response?.status.toString())
                    }
                    usingCache.postValue(null)
                } else {
                    _paymentStatus.postValue(response?.status.toString())
                }
            }
        }
    }

    fun doCardTokenization() {
        isLoading.postValue(true)
        viewModelScope.launch {
            checkoutRepository.doCardTokenization(_tokenIdResponse.value ?: "") {response ->
                if(response != null) {
                    _cardTokenize.postValue(Event(response))
                } else {
                    _cardTokenize.postValue(Event(listOf()))
                }
                isLoading.postValue(false)
            }
        }
    }

    fun doDeleteCardTokenization(tokenizationId: String) {
        isLoading.postValue(true)
        viewModelScope.launch {
            checkoutRepository.doDeleteCardTokenization(_tokenIdResponse.value ?: "", tokenizationId) {response ->
                _deleteCardTokenize.postValue(response)
                isLoading.postValue(false)
            }
        }
    }

    fun doGetBinInformation(binNumber: String) {
        viewModelScope.launch {
            checkoutRepository.doGetBinInformation(_tokenIdResponse.value ?: "", binNumber) {response ->
                _binEligibilityResponse.postValue(response)
            }
        }
    }

    fun doPaymentCard(request: PaymentRequest) {
        viewModelScope.launch {
            checkoutRepository.doPayCard(request = request) {response ->
                _paymentResponse.postValue(response)
                isLoading.postValue(false)
            }
        }
    }

    fun doGenerateQris() {
        val request = PaymentRequest(
            token_id = _tokenIdResponse.value ?: "",
        )
        viewModelScope.launch {
            isLoading.postValue(true)
            checkoutRepository.doGenerateQRIS(request) { response ->
                isLoading.postValue(false)
                _paymentResponse.postValue(response)
            }
        }
    }

    fun doGenerateVa() {
        val request = PaymentRequest(
            token_id = _tokenIdResponse.value ?: "",
            bank = additionalRequest.value,
        )
        viewModelScope.launch {
            isLoading.postValue(true)
            checkoutRepository.doGenerateVa(request) { response ->
                isLoading.postValue(false)
                _paymentResponse.postValue(response)
            }
        }
    }

}