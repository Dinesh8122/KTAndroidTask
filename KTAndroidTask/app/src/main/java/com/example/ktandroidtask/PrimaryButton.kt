package com.example.ktandroidtask

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton


class PrimaryButton: MaterialButton {

constructor(context: Context) : super(context)
constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(context, attrs, attributeSetId)

    init {
        this.setTextColor(Color.WHITE)
        this.setSupportAllCaps(false)
        this.backgroundTintList = ColorStateList.valueOf(Color.BLUE)

    }

}


class KTFloatingActionButton: FloatingActionButton {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(context, attrs, attributeSetId)


    init {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            this.imageTintList = null
        }
        this.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
    }

}

