package com.doku.sdkcheckoutandroid.ui.general

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.databinding.FragmentSdkWebviewBinding
import com.doku.sdkcheckoutandroid.enum.PaymentMethodEnum
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import java.net.URL

class SdkWebviewFragment(private val url: String) : Fragment() {

    private lateinit var fragment: FragmentSdkWebviewBinding

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
            FragmentSdkWebviewBinding.inflate(inflater, container, false)
        val view = fragment.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment.webView.settings.apply {
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        fragment.webView.settings.javaScriptEnabled = true
        fragment.webView.webViewClient = WebViewClient()
        fragment.webView.loadUrl(url)

        fragment.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                if(url.startsWith("${DokuConfig.baseUrl}/checkout-link-v2")) {
                    Log.d("URL", "VALID URL")
                    paymentViewModel.checkPaymentStatus()
                } else {
                    Log.d("URL", "INVALID URL")
                }

                return false
            }
        }
    }

    override fun onDestroyView() {
        fragment.webView.destroy()
        super.onDestroyView()
    }

}