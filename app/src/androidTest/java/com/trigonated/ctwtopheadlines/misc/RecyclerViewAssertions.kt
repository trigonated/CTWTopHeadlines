package com.trigonated.ctwtopheadlines.misc

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion

/** Asserts that the [RecyclerView] has a view with the [text] on the item with [index]. **/
fun hasViewWithTextAtPosition(index: Int, text: CharSequence): ViewAssertion {
    return hasViewWithTextAtPosition(index, text.toString())
}

/** Asserts that the [RecyclerView] has a view with the [text] on the item with [index]. **/
fun hasViewWithTextAtPosition(index: Int, text: String): ViewAssertion {
    return ViewAssertion { view, e ->
        if (view !is RecyclerView) throw e!!

        val outViews: ArrayList<View> = ArrayList()
        view.scrollToPosition(index)
        Thread.sleep(500)
        view.findViewHolderForAdapterPosition(index)!!.itemView.findViewsWithText(
            outViews,
            text,
            View.FIND_VIEWS_WITH_TEXT
        )
        assert(outViews.isNotEmpty()) { "There's no view at index $index of recyclerview that has text : $text" }
    }
}