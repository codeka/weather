package au.com.codeka.weather

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.LinearLayout

/** A [LinearLayout] which assumes it's children are "cards".  */
class CardLinearLayout : LinearLayout {
  private var childViewAnimator: ChildViewAnimator? = null

  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    initLayoutObserver()
  }

  constructor(context: Context?) : super(context) {
    initLayoutObserver()
  }

  private fun initLayoutObserver() {
    orientation = VERTICAL
    childViewAnimator = ChildViewAnimator()
    viewTreeObserver.addOnGlobalLayoutListener(childViewAnimator)
    viewTreeObserver.addOnScrollChangedListener(childViewAnimator)
  }

  private inner class ChildViewAnimator : ViewTreeObserver.OnGlobalLayoutListener, ViewTreeObserver.OnScrollChangedListener {
    override fun onGlobalLayout() {
      animateChildren()
    }

    override fun onScrollChanged() {
      animateChildren()
    }

    private fun animateChildren() {
      val heightPx = context.resources.displayMetrics.heightPixels
      val childCount = childCount
      val location = IntArray(2)
      for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child.getTag(R.id.card_layout_tag) != null) {
          continue
        }
        child.getLocationOnScreen(location)
        if (location[1] > heightPx) {
          continue
        }
        child.setTag(R.id.card_layout_tag, Any())
        if (i % 2 == 0) {
          child.startAnimation(AnimationUtils.loadAnimation(context,
              R.anim.slide_up_left))
        } else {
          child.startAnimation(AnimationUtils.loadAnimation(context,
              R.anim.slide_up_right))
        }
      }
    }
  }
}