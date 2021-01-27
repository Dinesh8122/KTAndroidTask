package com.example.ktandroidtask

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton


class SecondaryButton: MaterialButton {

constructor(context: Context) : super(context)
constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(context, attrs, attributeSetId)

    val scale = context.resources.displayMetrics.density

    init {
        this.setTextColor(Color.BLACK)
        this.setSupportAllCaps(false)
        this.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
//        this.strokeColor = ColorStateList.valueOf(Color.BLACK)
//        this.strokeWidth = (1*scale).toInt()
    }

}


