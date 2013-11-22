package com.dpt.dragitemview.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

public class DragItemLayout extends RelativeLayout {


	public DragItemLayout(Context context,View dragView) {
		super(context);
		this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.setGravity(Gravity.CENTER);
		this.addView(dragView);
	}
}
