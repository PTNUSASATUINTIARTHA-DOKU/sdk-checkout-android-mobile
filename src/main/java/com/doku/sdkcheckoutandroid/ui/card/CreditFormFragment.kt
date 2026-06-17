package com.doku.sdkcheckoutandroid.ui.card

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.PopupWindow
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.databinding.FragmentCreditFormBinding
import com.doku.sdkcheckoutandroid.model.request.InstallmentRequest
import com.doku.sdkcheckoutandroid.model.response.Installment
import com.doku.sdkcheckoutandroid.ui.general.InfoPaymentPendingBottomSheet
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Calendar

class CreditFormFragment : Fragment() {
    companion object {
        private const val VISA_IMAGE_URL = "https://cdn-doku.oss-ap-southeast-5.aliyuncs.com/doku-ui-framework/doku/img/card/visa.png"
        private const val MASTER_IMAGE_URL = "https://cdn-doku.oss-ap-southeast-5.aliyuncs.com/doku-ui-framework/doku/img/card/mastercard.png"
    }

    lateinit var fragment: FragmentCreditFormBinding

    var installmentChoice = mutableListOf<Installment>()

    lateinit var adapter: ArrayAdapter<Installment>

    val defaultTenor = Installment(id=9999999, tenor = 9999999)

    var isValidCard = false
    var isValidExpiryDate = false
    var isValidCvv = false

    var isSaved = false
    private var latestRequestedBin: String? = null
    private var lastImageUrl: String? = null

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
        fragment =
            FragmentCreditFormBinding.inflate(inflater, container, false)
        val view = fragment.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            installmentChoice
        )
        setCardNumberPadding(hasBrand = false)
        clearCardBrandImage()
        initObserver()
        didEndEditTextType()
        setupOnClickView()
        adapter.add(defaultTenor)
        fragment.dropdownAnchor.setBackgroundResource(R.drawable.bg_dropdown)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        latestRequestedBin = null
        paymentViewModel.binEligibilityResponse.value = null
    }

    fun initObserver() {
        paymentViewModel.binEligibilityResponse.observe(viewLifecycleOwner) { response ->
            val currentDigits = fragment.etCardNumber.text.toString().filter { it.isDigit() }
            val currentBin = if (currentDigits.length >= 8) currentDigits.substring(0, 8) else null

            if(response != null) {
                if (currentBin == null) {
                    return@observe
                }

                val responseBin = response.bin_number?.filter { it.isDigit() }
                val isSameBinFamily = !responseBin.isNullOrEmpty() &&
                    (currentBin.startsWith(responseBin) || responseBin.startsWith(currentBin))

                if (!responseBin.isNullOrEmpty() && !isSameBinFamily) {
                    Log.d("Card BIN", "Ignore stale BIN response. response=$responseBin, current=$currentBin")
                    return@observe
                }

                if(response.bin_number?.isNotEmpty() == true) {
                    isValidCard = true
                    fragment.etCardNumber.error = null
                    isValidForm()
                    if(response.installments.isNotEmpty()) {
                        adapter.clear()
                        adapter.add(defaultTenor)
                        adapter.addAll(response.installments)
                        adapter.notifyDataSetChanged()
                        fragment.tvDesc.text = "Available Installment Plan will be listed here"
                        fragment.dropdownAnchor.setBackgroundResource(R.drawable.bg_dropdown)
                        fragment.dropdownAnchor.isClickable = true
                        fragment.dropdownAnchor.isEnabled = true
                    } else {
                        fragment.tvDesc.text = "Cards doesn’t have installment features so you only can pay with full payment"
                        fragment.tvSelectedTenor.text = "Full Payment"
                        fragment.dropdownAnchor.setBackgroundColor(Color.parseColor("#E5E8EC"))
                        fragment.dropdownAnchor.isClickable = false
                        fragment.dropdownAnchor.isEnabled = false
                    }

                    setCardBrandImage(response.brand_image)
//                        .load(response.brand_image)
//                        .into(object : CustomTarget<Drawable>() {
//                            override fun onResourceReady(
//                                resource: Drawable,
//                                transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
//                            ) {
//                                val leftSize = (48 * fragment.etCardNumber.context.resources.displayMetrics.density).toInt()
//                                val rightSize = (24 * fragment.etCardNumber.context.resources.displayMetrics.density).toInt()
//                                resource.setBounds(0, 0, leftSize, rightSize)
//
//                                fragment.etCardNumber.setCompoundDrawables(
//                                    null,
//                                    null,
//                                    resource,
//                                    null
//                                )
//                            }
//
//                            override fun onLoadCleared(placeholder: Drawable?) {
//                                // optional
//                            }
//                        })
                } else {
                    isValidCard = false
                    updateCardBrandByNumber(currentDigits)
                    isValidForm()
                }
            } else {
                if (currentBin == null) {
                    clearCardBrandImage()
                } else {
                    updateCardBrandByNumber(currentDigits)
                }
            }
        }
    }

    private fun didEndEditTextType() {
        var searchJob: Job? = null

        fragment.etCardNumber.addTextChangedListener(object : TextWatcher {
            var isEditing = false
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                s?.let {
                    var digits = it.toString().filter { value -> value.isDigit() }
                    if (digits.length > 19) {
                        digits = digits.substring(0, 19)
                    }
                    val formatted = digits.chunked(4).joinToString(" ")

                    it.replace(0, it.length, formatted)
                }

                isEditing = false

                val text = s.toString().filter { it.isDigit() }

                searchJob?.cancel()

                if (text.length >= 8) {
                    fragment.etCardNumber.error = null
                    val requestedBin = text.substring(0,8)
                    latestRequestedBin = requestedBin
                    searchJob = lifecycleScope.launch {
                        delay(300)
                        val latestDigits = fragment.etCardNumber.text.toString().filter { it.isDigit() }
                        if (latestDigits.length < 8) return@launch

                        val latestBin = latestDigits.substring(0, 8)
                        if (latestBin != requestedBin || latestRequestedBin != requestedBin) {
                            return@launch
                        }
                        paymentViewModel.doGetBinInformation(requestedBin)
                    }
                } else {
                    latestRequestedBin = null
                    isValidCard = false
                    if (text.isEmpty()) {
                        clearCardBrandImage()
                        fragment.etCardNumber.error = "Card not valid"
                    } else {
                        fragment.etCardNumber.error = null
                    }
                    installmentChoice.clear()
                    adapter.add(defaultTenor)
                    fragment.tvSelectedTenor.text = "Select tenor"
                    fragment.dropdownAnchor.setBackgroundResource(R.drawable.bg_dropdown)
                    fragment.tvDesc.text = "Cards doesn’t have installment features so you only can pay with full payment"
                    fragment.dropdownAnchor.isClickable = true
                    fragment.dropdownAnchor.isEnabled = true
                    isValidForm()
                }

                if (text.isNotEmpty()) {
                    updateCardBrandByNumber(text)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.isNullOrEmpty()) {
                    isValidCard = false
                    fragment.etCardNumber.error = "Card not valid"
                    isValidForm()
                }
            }
        })

        fragment.etExpiry.addTextChangedListener(object : TextWatcher {

            private var isEditing = false

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                s?.let {
                    var text = it.toString().replace("/", "")
                    if (text.length > 4) {
                        text = text.substring(0, 4)
                    }

                    if (text.length >= 2) {
                        val month = text.substring(0, 2).toIntOrNull()
                        if (month == null || month !in 1..12) {
                            it.clear()
                            isEditing = false
                            isValidExpiryDate = false
                            fragment.etExpiry.error = "Invalid expired date"
                            isValidForm()
                            return
                        }
                    }

                    val formatted = when {
                        text.length <= 2 -> text
                        else -> text.substring(0, 2) + "/" + text.substring(2)
                    }

                    it.replace(0, it.length, formatted)

                    if (formatted.length == 5) {
                        if (isExpired(formatted)) {
                            isValidExpiryDate = false
                            fragment.etExpiry.error = "Invalid expired date"
                            isValidForm()
                        } else {
                            isValidExpiryDate = true
                            fragment.etExpiry.error = null
                            isValidForm()
                        }
                    }
                }

                isEditing = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(count == 0) {
                    isValidExpiryDate = false
                    fragment.etExpiry.error = "Expiry date is required"
                    isValidForm()
                }
            }
        })

        fragment.etCvv.addTextChangedListener{
            val cvv = it.toString()
            if(cvv.length < 3) {
                isValidCvv = false
                fragment.etCvv.error = "Invalid CVV"
                isValidForm()
            } else {
                isValidCvv = true
                isValidForm()
            }
        }

    }

    private fun updateCardBrandByNumber(cardNumber: String) {
        val isVisa = cardNumber.startsWith("4")
        val isMaster = isMastercard(cardNumber)
        val brandImageUrl = when {
            isVisa -> VISA_IMAGE_URL
            isMaster -> MASTER_IMAGE_URL
            else -> null
        }

        Log.d(
            "CardBrandTrace",
            "updateCardBrandByNumber digits=$cardNumber length=${cardNumber.length} isVisa=$isVisa isMaster=$isMaster brandImageUrl=$brandImageUrl"
        )

        if (brandImageUrl != null) {
            Log.d("CardBrandTrace", "setCardBrandImage from local detection: $brandImageUrl")
            setCardBrandImage(brandImageUrl)
        } else if (cardNumber.length < 2) {
            Log.d("CardBrandTrace", "clear drawable because cardNumber length < 2")
            clearCardBrandImage()
        } else {
            Log.d("CardBrandTrace", "no brand matched yet, keep current drawable")
        }
    }

    private fun isMastercard(cardNumber: String): Boolean {
        val firstTwo = cardNumber.take(2).toIntOrNull()
        if (firstTwo != null && firstTwo in 51..55) return true

        val firstFour = cardNumber.take(4).toIntOrNull()
        if (firstFour != null && firstFour in 2221..2720) return true

        return false
    }

    private fun setCardBrandImage(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) return

        lastImageUrl = imageUrl
        fragment.ivCardBrand.visibility = View.VISIBLE
        Glide.with(this)
            .load(imageUrl)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                ) {
                    if (imageUrl != lastImageUrl) {
                        Log.d("Glide", "Image load skipped due to race condition")
                        return
                    }

                    fragment.ivCardBrand.setImageDrawable(resource)
                    fragment.ivCardBrand.visibility = View.VISIBLE
                    setCardNumberPadding(hasBrand = true)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.d("Glide", "Card brand image failed to load: $imageUrl")
                    fragment.ivCardBrand.visibility = View.GONE
                    setCardNumberPadding(hasBrand = false)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    fragment.ivCardBrand.visibility = View.GONE
                    setCardNumberPadding(hasBrand = false)
                }
            })
    }

    private fun clearCardBrandImage() {
        fragment.ivCardBrand.setImageDrawable(null)
        fragment.ivCardBrand.visibility = View.GONE
        setCardNumberPadding(hasBrand = false)
    }

    private fun setCardNumberPadding(hasBrand: Boolean) {
        val start = dpToPx(16)
        val end = if (hasBrand) dpToPx(64) else dpToPx(16)
        fragment.etCardNumber.setPaddingRelative(start, fragment.etCardNumber.paddingTop, end, fragment.etCardNumber.paddingBottom)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    fun isValidForm(): Boolean {
        Log.d("Valid Form", "$isValidCard $isValidExpiryDate $isValidCvv")
        var isAllValid = isValidCard && isValidExpiryDate && isValidCvv
        if(isAllValid) {
            val cardDetail = JSONObject()
            cardDetail.put("number", fragment.etCardNumber.text.toString().filter { it.isDigit() })
            cardDetail.put("cvv", fragment.etCvv.text.toString())
            cardDetail.put("expiry", fragment.etExpiry.text.toString().replace("/", ""))
            cardDetail.put("save", isSaved)
            paymentViewModel.cardDetail.value = cardDetail
        }else {
            paymentViewModel.cardDetail.value = null
        }

        return (isValidCard && isValidExpiryDate && isValidCvv)
    }

    fun setupOnClickView() {
        val anchor = fragment.dropdownAnchor
        val tvValue = fragment.tvSelectedTenor
        fragment.ivInfoCvv.setOnClickListener {
            val sheet = InfoPaymentPendingBottomSheet(title = "Apa itu CVV?", subtitle = "CVV adalah kode keamanan berupa 3 digit angka yang umumnya terletak di belakang kartu")
            sheet.show(requireActivity().supportFragmentManager, "")
        }

        tvValue.setTextColor(Color.parseColor("#111827"))

//        fragment.cbSave.setOnCheckedChangeListener{_, checked ->
//            if(checked) {
//                isSaved = true
//                isValidForm()
//            } else {
//                isSaved = false
//                isValidForm()
//            }
//        }

        anchor.setOnClickListener {
            val popupView = layoutInflater.inflate(R.layout.popup_dropdown, null)
            val listView = popupView.findViewById<ListView>(R.id.listTenor)

            listView.adapter = adapter

            val popupWindow = PopupWindow(
                popupView,
                anchor.width,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow.elevation = 12f
            popupWindow.isOutsideTouchable = true

            listView.setOnItemClickListener { _, _, position, _ ->
                if(installmentChoice[position].tenor == 9999999) {
                    tvValue.text = "Full Payment"
                } else {
                    tvValue.text = "${installmentChoice[position].tenor} Month"
                }

                tvValue.setTextColor(Color.parseColor("#111827"))
                paymentViewModel.installmentCard.value = InstallmentRequest(
                    id = installmentChoice[position].id,
                    tenor = installmentChoice[position].tenor
                )
                popupWindow.dismiss()
            }

            popupWindow.showAsDropDown(anchor, 0, 8)
        }
    }

    fun isExpired(mmYY: String): Boolean {
        val parts = mmYY.split("/")
        if (parts.size != 2) return true

        val month = parts[0].toInt()
        val year = 2000 + parts[1].toInt()

        val now = Calendar.getInstance()
        val exp = Calendar.getInstance()
        exp.set(year, month - 1, 1)
        exp.add(Calendar.MONTH, 1)

        return now.after(exp)
    }
}
