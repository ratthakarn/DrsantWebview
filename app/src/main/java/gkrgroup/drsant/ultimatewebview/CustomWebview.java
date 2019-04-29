package gkrgroup.drsant.ultimatewebview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by AbhiAndroid
 */

public class CustomWebview extends WebView {

    public CustomWebview(Context context) {
        super(context);
    }

    public CustomWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomWebview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)     {
        switch (ev.getAction())         {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                if (!hasFocus())
                    requestFocus();
                break;
        }

        return super.onTouchEvent(ev);
    }
}
