package com.doku.sdkcheckoutandroid

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.doku.sdkcheckoutandroid.model.response.PromoModel
import com.doku.sdkcheckoutandroid.viewmodel.initialPayment.InitialPaymentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PromoBottomSheet(val viewModel: InitialPaymentViewModel) : BottomSheetDialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.layout_promo_bottom_sheet, container, false)
    }


    private fun displayPromos(list: List<PromoModel>) {
        val promoContainer = view?.findViewById<LinearLayout>(R.id.promoListContainer) ?: return
        val emptyState = view?.findViewById<LinearLayout>(R.id.emptyStateContainer) ?: return

        promoContainer.removeAllViews()

        if (list.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            promoContainer.visibility = View.GONE
            return
        }

        emptyState.visibility = View.GONE
        promoContainer.visibility = View.VISIBLE

        list.forEach { promo ->
            val item = layoutInflater.inflate(R.layout.item_promo_item, promoContainer, false)

            item.findViewById<TextView>(R.id.tvPromoTitle).text = promo.title
            item.findViewById<TextView>(R.id.tvPromoDesc).text = promo.desc
            item.findViewById<TextView>(R.id.tvPromoBadge).text = promo.badge
            item.findViewById<TextView>(R.id.tvCode).text = promo.code
            item.findViewById<TextView>(R.id.tvExpiry).text = "Expiry ${promo.expiry}"
            val tvCode = item.findViewById<TextView>(R.id.tvCode)
            val ivCopy = item.findViewById<ImageView>(R.id.ivCopy)

            ivCopy.setOnClickListener {
                val code = tvCode.text.toString()
                val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Promo Code", code)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Copied: $code", Toast.LENGTH_SHORT).show()
            }

            item.findViewById<TextView>(R.id.btnApply).setOnClickListener {
                dismiss()
            }

            promoContainer.addView(item)
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose = view.findViewById<ImageView>(R.id.btnClosePromo)
        val promoContainer = view.findViewById<LinearLayout>(R.id.promoListContainer)
        val etSearch = view.findViewById<EditText>(R.id.etSearchPromo)
        val emptyState = view.findViewById<LinearLayout>(R.id.emptyStateContainer)

        btnClose.setOnClickListener { dismiss() }
        viewModel.doShowPromos()

        // 🔥 Observe promos
        viewModel.showPromosResponse.observe(viewLifecycleOwner) { response ->
            if (response == null || response.promos.isNullOrEmpty()) {
                emptyState.visibility = View.VISIBLE
                promoContainer.visibility = View.GONE
                return@observe
            }

            val promos = response.promos!!.map {
                PromoModel(
                    title = it.title ?: "-",
                    desc = it.description ?: "-",
                    badge = "-",
                    code = it.code ?: "-",
                    expiry = it.end_date_time ?: "-"
                )
            }

            displayPromos(promos)

            etSearch.addTextChangedListener { text ->
                val filtered = promos.filter {
                    it.title.contains(text.toString(), ignoreCase = true)
                }
                displayPromos(filtered)
            }
        }
    }


}
