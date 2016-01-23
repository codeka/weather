package au.com.codeka.weather;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

/** A {@link LinearLayout} which assumes it's children are "cards". */
public class CardLinearLayout extends LinearLayout {
  private ChildViewAnimator childViewAnimator;

  public CardLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    initLayoutObserver();
  }

  public CardLinearLayout(Context context) {
    super(context);
    initLayoutObserver();
  }

  private void initLayoutObserver() {
    setOrientation(LinearLayout.VERTICAL);
    childViewAnimator = new ChildViewAnimator();
    getViewTreeObserver().addOnGlobalLayoutListener(childViewAnimator);
    getViewTreeObserver().addOnScrollChangedListener(childViewAnimator);
  }

  private class ChildViewAnimator implements ViewTreeObserver.OnGlobalLayoutListener,
      ViewTreeObserver.OnScrollChangedListener{
    @Override
    public void onGlobalLayout() {
      animateChildren();
    }

    @Override
    public void onScrollChanged() {
      animateChildren();
    }

    private void animateChildren() {
      final int heightPx = getContext().getResources().getDisplayMetrics().heightPixels;

      final int childCount = getChildCount();

      int[] location = new int[2];
      for (int i = 0; i < childCount; i++) {
        View child = getChildAt(i);
        if (child.getTag(R.id.card_layout_tag) != null) {
          continue;
        }

        child.getLocationOnScreen(location);
        if (location[1] > heightPx) {
          continue;
        }

        child.setTag(R.id.card_layout_tag, new Object());
        if (i % 2 == 0) {
          child.startAnimation(AnimationUtils.loadAnimation(getContext(),
              R.anim.slide_up_left));
        } else {
          child.startAnimation(AnimationUtils.loadAnimation(getContext(),
              R.anim.slide_up_right));
        }
      }
    }
  }
}