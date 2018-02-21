package com.hannula.ilkka.yhtalo;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

public class MyDragShadowBuilder extends View.DragShadowBuilder {

    private Point mScaleFactor;
    private int kerroin_;

    // Defines the constructor for myDragShadowBuilder
    public MyDragShadowBuilder(View v) {
        super(v);
        kerroin_ = 3;

    }
    public MyDragShadowBuilder(View v, int kerroin) {
        super(v);
        kerroin_ = kerroin;

    }


    @Override
    public void onProvideShadowMetrics(Point size, Point touch) {

        int width = getView().getWidth() * kerroin_;

        int height = getView().getHeight() * kerroin_;


        size.set(width, height);
        mScaleFactor = size;

        //touch.set(width / 2, height);
        touch.set(width / 2, height / 2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {

        canvas.scale(mScaleFactor.x / (float) getView().getWidth(), mScaleFactor.y / (float) getView().getHeight());
        getView().draw(canvas);
    }

}