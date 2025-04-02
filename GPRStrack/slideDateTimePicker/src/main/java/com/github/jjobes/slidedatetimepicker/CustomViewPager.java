package com.github.jjobes.slidedatetimepicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.viewpager.widget.ViewPager;


public class CustomViewPager extends ViewPager
{
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private float x1, y1, x2, y2;
    private float mTouchSlop;

    public CustomViewPager(Context context)
    {
        super(context);

        init(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context);
    }

    private void init(Context context)
    {
        mTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() > 0)
        {
            View childView = getChildAt(0);

            if (childView != null)
            {
                childView.measure(widthMeasureSpec, heightMeasureSpec);
                int h = childView.getMeasuredHeight();
                setMeasuredDimension(getMeasuredWidth(), h);
                getLayoutParams().height = h;
            }
        }

        mDatePicker = (DatePicker) findViewById(R.id.datePicker);
        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                y1 = event.getY();

                break;

           case MotionEvent.ACTION_MOVE:
               x2 = event.getX();
               y2 = event.getY();

               if (isScrollingHorizontal(x1, y1, x2, y2))
               {
                   return super.dispatchTouchEvent(event);
               }

               break;
         }

         switch (getCurrentItem())
         {
         case 0:

             if (mDatePicker != null)
                 mDatePicker.dispatchTouchEvent(event);

             break;

         case 1:

             if (mTimePicker != null)
                 mTimePicker.dispatchTouchEvent(event);

             break;
         }

         return super.onTouchEvent(event);
    }

    private boolean isScrollingHorizontal(float x1, float y1, float x2, float y2)
    {
        float deltaX = x2 - x1;
        float deltaY = y2 - y1;

        if (Math.abs(deltaX) > mTouchSlop &&
            Math.abs(deltaX) > Math.abs(deltaY))
        {

            return true;
        }

        return false;
    }
}
