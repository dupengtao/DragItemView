package com.dpt.dragitemview.widget;

import android.view.View;
/**
 * 
 * @author dupengtao88@gmail.com
 *
 * 2013-11-22
 */
public interface DragListener {
	
	public interface OnItemLongClickListener{
		void onItemLongClick(View view,int position);
	}
	public interface OnItemMoveUpClickListener{
		void onItemMoveUpClick(View view,int position, int lastX, int lastY);
	}
	
	public interface OnItemMoveListener{
		void onItemMove(View view,int position, int lastX, int lastY);
	}
	
	public interface OnItemClickListener{
		void onItemClick(View view,int position,boolean isLastItem);
	}
	
}
