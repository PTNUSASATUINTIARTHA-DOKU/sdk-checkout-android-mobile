package com.doku.sdkcheckoutandroid.ui.bankTransfer.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.doku.sdkcheckoutandroid.R

class BankHowToPayAdapter(private val context: Context,
                          private val titleList: MutableList<String>,
                          private val dataList: HashMap<String, String>): BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return titleList.size
    }

    override fun getChildrenCount(p0: Int): Int {
        return 1
    }

    override fun getGroup(p0: Int): Any? {
        return titleList[p0]
    }

    override fun getChild(p0: Int, p1: Int): Any? {
        return dataList[titleList[p0]]?.get(p1) ?: ""
    }

    override fun getGroupId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getChildId(p0: Int, p1: Int): Long {
        return p1.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        p0: Int,
        isExpanded: Boolean,
        p2: View?,
        p3: ViewGroup?
    ): View? {
        val view = p2 ?: LayoutInflater.from(context)
            .inflate(R.layout.how_to_pay_header, p3, false)

        val title = view.findViewById<TextView>(R.id.tvName)
        title.text = titleList[p0]

        val icon = view.findViewById<ImageView>(R.id.ic_arrow_right)
        icon.animate()
            .rotation(if(isExpanded) 180f else 0f)
            .setDuration(200)
            .start()

        if(p0 == 0 || p0 == titleList.size - 1) {
            val drawable = GradientDrawable().apply {
                setColor(Color.WHITE)

                setStroke(2, Color.parseColor("#E5E8EC"))

                cornerRadii = floatArrayOf(
                    8f,8f,   // top left right
                    8f,8f,
                    0f,0f,     // bottom left right
                    0f,0f
                )
            }
            view.background = drawable
        } else {
            val drawable = GradientDrawable().apply {
                setColor(Color.WHITE)

                setStroke(2, Color.parseColor("#E5E8EC"))

                cornerRadii = floatArrayOf(
                    0f,0f,   // top left right
                    0f,0f,
                    0f,0f,     // bottom left right
                    0f,0f
                )
            }
            view.background = drawable

        }
        return view
    }

    override fun getChildView(
        p0: Int,
        p1: Int,
        p2: Boolean,
        p3: View?,
        p4: ViewGroup?
    ): View? {
        val view = p3 ?: LayoutInflater.from(context)
            .inflate(R.layout.how_to_pay_content, p4, false)

        val childText = view.findViewById<TextView>(R.id.tvStep)
        childText.text = dataList[titleList[p0]]
        val drawable = GradientDrawable().apply {
            setColor(Color.WHITE)

            setStroke(2, Color.parseColor("#E5E8EC"))

            cornerRadii = floatArrayOf(
                0f,0f,   // top left right
                0f,0f,
                0f,0f,     // bottom left right
                0f,0f
            )
        }
        view.background = drawable
        return view
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return false
    }
}