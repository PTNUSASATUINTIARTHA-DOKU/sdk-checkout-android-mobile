package com.doku.sdkcheckoutandroid.ui.ewallet

import Helper
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.databinding.FragmentEwalletFormBinding
import com.doku.sdkcheckoutandroid.enum.PaymentMethodEnum
import com.doku.sdkcheckoutandroid.ui.qris.QrisHowToPay
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel

class EwalletFormFragment : Fragment() {

    lateinit var fragment: FragmentEwalletFormBinding

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
            FragmentEwalletFormBinding.inflate(inflater, container, false)
        val view = fragment.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        paymentViewModel.hasNavigateDoku.value = false
        paymentViewModel.hasNavigateOvo.value = false
        paymentViewModel.hasNavigateLinkAja.value = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment.layoutHowToPay.setOnClickListener {
            val sheet = EwalletHowToPayFragment(paymentViewModel.selectedPaymentMethod.value ?: PaymentMethodEnum.NONE)
            sheet.show(requireActivity().supportFragmentManager, "EwalletHowToPay")
        }

        when(paymentViewModel.selectedPaymentMethod.value) {
            PaymentMethodEnum.DOKU_NAVIGATE -> {
                fragment.requiredTag.visibility = View.VISIBLE
                fragment.tvPhoneLabel.text = "Phone Number/DOKU ID/Email"
                fragment.etPhoneNumber.hint = "Input your phone number/DOKU ID/email"
                fragment.etPhoneNumber.inputType = InputType.TYPE_CLASS_TEXT
            }
            else -> {}
        }

        fragment.etPhoneNumber.addTextChangedListener{
            val phone = it.toString()
            val prefix = when(paymentViewModel.selectedPaymentMethod.value) {
                PaymentMethodEnum.LINK_AJA -> "08"
                PaymentMethodEnum.OVO -> "62"
                else -> "62"
            }

            when(paymentViewModel.selectedPaymentMethod.value) {
                PaymentMethodEnum.LINK_AJA,PaymentMethodEnum.OVO  -> {
                    fragment.etPhoneNumber.error = when {
                        phone.isEmpty() -> null
                        !Helper().isValidPhoneNumber(phone, prefix) -> "Invalid phone number"
                        else -> null
                    }
                }
                 PaymentMethodEnum.DOKU_NAVIGATE-> {
                     val isEmail = Patterns.EMAIL_ADDRESS.matcher(phone).matches()
                     val isPhone = phone.matches(Regex("^\\+?[0-9]{8,15}$"))
                     val isValid = Helper().isValidEmailOrPhone(phone)

                     fragment.etPhoneNumber.error = when {
                         phone.isEmpty() -> null
                         isEmail && !isValid -> "Invalid email"
                         isPhone && !isValid -> "Invalid phone number"
                         !isValid -> "Enter valid email or phone number"
                         else -> null
                     }
                 }
                else -> {}
            }

            when(paymentViewModel.selectedPaymentMethod.value) {
                PaymentMethodEnum.OVO, PaymentMethodEnum.LINK_AJA -> if(Helper().isValidPhoneNumber(phone, prefix)) paymentViewModel.additionalRequest.value = phone
                PaymentMethodEnum.DOKU_NAVIGATE -> {
                    if(Helper().isValidEmailOrPhone(phone)) {
                        paymentViewModel.additionalRequest.value = phone
                    } else {
                        paymentViewModel.additionalRequest.value = null
                    }
                }
                else -> {}
            }
        }
    }

}