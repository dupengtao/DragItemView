package com.dpt.dragitemview.widget;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.dpt.dragitemview.widget.DragListener.OnItemClickListener;
import com.dpt.dragitemview.widget.DragListener.OnItemLongClickListener;
import com.dpt.dragitemview.widget.DragListener.OnItemMoveListener;
import com.dpt.dragitemview.widget.DragListener.OnItemMoveUpClickListener;

/**
 * 参考代码:1.DraggableGridView 2.Pageddragdropgird
 * @author dupengtao88@gmail.com
 *
 * 2013-11-22
 */
public class DragView extends ViewGroup implements View.OnTouchListener,
		View.OnClickListener, View.OnLongClickListener {

	// 布局变量
	private int colCount = 0;
	private int rowCount = 0;
	private int colWidth = 0;
	private int rowHeight = 0;

	// 拖动变量
	private int dragged = -1;
	private int lastX = -1;
	private int lastY = -1;
	private int lastTarget = -1;
	private boolean enabled = false;

	// 子视图序号列表
	protected ArrayList<Integer> newPositions = new ArrayList<Integer>();

	// 编号是按照子视图添加顺序获得的
	// 序号是为了方便交换位置而引入的.

	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;
	private OnItemMoveUpClickListener onItemMoveUpClickListener;
	private OnItemMoveListener onItemMoveListener;

	private boolean mIsDraging;

	/*------------------构造方法------------------*/

	public DragView(Context context) {
		this(context, null);
	}

	public DragView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setListeners();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		adaptChildrenMeasuresToViewSize(widthSize, heightSize);
		computeColWidthAndRowHeight(widthSize, heightSize);
		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		layoutChild();
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			enabled = true;
			lastX = (int) event.getX();
			lastY = (int) event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			int delta = lastY - (int) event.getY();
			if (dragged != -1) {
				int x = (int) event.getX(), y = (int) event.getY();
				getChildAt(dragged).layout(
						x - getChildAt(dragged).getMeasuredWidth() / 2,
						y - getChildAt(dragged).getMeasuredHeight() / 2,
						x + getChildAt(dragged).getMeasuredWidth() / 2,
						y + getChildAt(dragged).getMeasuredHeight() / 2);
				int target = getTargetFromCoor(x, y);
				/** 不能移动最后一个的后面,如果没有限制去掉target!=newPositions.size()-1*/
				if (lastTarget != target&&target!=newPositions.size()-1) {
					if (target != -1) {
						animateGap(target);
						lastTarget = target;
					}
				}
				if(mIsDraging&&onItemMoveListener!=null){
				onItemMoveListener.onItemMove(getChildAt(dragged), dragged, lastX, lastY);
				}
				
			} else {
				if (Math.abs(delta) > 2)
					enabled = false;
			}
			lastX = (int) event.getX();
			lastY = (int) event.getY();
			break;
		case MotionEvent.ACTION_UP:
			if (dragged != -1) {
				View v = getChildAt(dragged);
				// 在有效位置释放
				if (lastTarget != -1) {
					reorderChildren();
				}
				// 在无效位置释放
				else {
					Point xy = getCoorFromIndex(dragged);
					v.layout(xy.x - v.getMeasuredWidth() / 2,
							xy.y - v.getMeasuredHeight() / 2,
							xy.x + v.getMeasuredWidth() / 2,
							xy.y + v.getMeasuredHeight() / 2);
				}
				v.clearAnimation();
				lastTarget = -1;
				dragged = -1;
				
				if(mIsDraging&&onItemMoveUpClickListener!=null){
					onItemMoveUpClickListener.onItemMoveUpClick(v, dragged,lastX,lastY);
				}
				
				mIsDraging=false;
			}
			break;
		}
		if (dragged != -1)
			return true;
		return false;
	}

	@Override
	public void onClick(View v) {
		if (enabled) {
			if (onItemClickListener != null && getLastIndex() != -1){
				//是否是最后一个
				boolean isLast=getLastIndex()==newPositions.size()-1;
				onItemClickListener.onItemClick(getChildAt(getLastIndex()), getLastIndex(),isLast);
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// 判断是否选中
		if (!enabled)
			return false;
		// 获得手指的最后位置
		int index = getLastIndex();
		// 判断该位置是否存在子视图
		if (index != -1&&index!=newPositions.size()-1) {
			if (onItemLongClickListener != null && getLastIndex() != -1){
				onItemLongClickListener.onItemLongClick(v, index);
			}
			mIsDraging=true;
			// 进入拖拽状态
			dragged = index;
			animateDragged();
			return true;
		}
		return false;
	}
	

    @Override
    public void removeView(View view) {
    	super.removeView(view);
    	newPositions.remove(0);
    	
    }

	/**
	 * 设置行和列</p> <em>需要在添加子视图之前调用</em>
	 * 
	 * @param col
	 * @param row
	 */
	public void setColAndRow(int col, int row) {
		colCount = col;
		rowCount = row;
	}

	/**
	 * 添加子视图</p> <em>需要先设置行和列，此方法才可执行</em></p> <em>只接受LauncherItem类型</em></p>
	 * 
	 * @param child
	 */
	public void addItem(View child) {
		if (!isMaxCount() && child instanceof DragItemLayout) {
			super.addView(child);
			// 子视图的默认序号都为-1
			newPositions.add(-1);
		}
	}

	/* 设置监听器 */

	public void setOnItemClickListener(OnItemClickListener l) {
		this.onItemClickListener = l;
	}
	/**
	 * 长按
	 * @param l
	 */
	public void setOnItemLongClickListener(OnItemLongClickListener l) {
		this.onItemLongClickListener = l;
	}
	/**
	 * 放下
	 * @param l
	 */
	public void setOnItemMoveUpClickListener(OnItemMoveUpClickListener l) {
		this.onItemMoveUpClickListener = l;
	}
	/**
	 * 移动
	 * @param l
	 */
	public void setOnItemMoveListener(OnItemMoveListener l) {
		this.onItemMoveListener = l;
	}

	/*------------------私有方法------------------*/


	/**
	 * 设置监听器
	 */
	private void setListeners() {
		setOnTouchListener(this);
		setOnClickListener(this);
		setOnLongClickListener(this);
	}

	/* 布局 */

	/**
	 * 判断子视图数量是否超出限制
	 * 
	 * @return
	 */
	private boolean isMaxCount() {
		if (getChildCount() < colCount * rowCount)
			return false;
		return true;
	}

	/**
	 * 适配所有子视图的尺寸
	 * 
	 * @param widthSize
	 * @param heightSize
	 */
	private void adaptChildrenMeasuresToViewSize(int widthSize, int heightSize) {
		if (colCount != 0 && rowCount != 0) {
			int desiredGridItemWidth = widthSize / colCount;
			int desiredGridItemHeight = heightSize / rowCount;
			measureChildren(MeasureSpec.makeMeasureSpec(desiredGridItemWidth,
					MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
					desiredGridItemHeight, MeasureSpec.AT_MOST));
		} else {
			measureChildren(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		}
	}

	/**
	 * 计算行和列的实际宽和高
	 * 
	 * @param widthSize
	 * @param heightSize
	 */
	private void computeColWidthAndRowHeight(int widthSize, int heightSize) {
		// 计算列的宽度，计算行的高度
		colWidth = widthSize / colCount;
		rowHeight = heightSize / rowCount;
		// 根据所有子视图的大小，重新计算行的高度。
		int tempHeight = 0;
		for (int i = 0; i < getChildCount(); i++) {
			if (tempHeight < getChildAt(i).getMeasuredHeight()) {
				tempHeight = getChildAt(i).getMeasuredHeight();
			}
		}

		if (rowHeight > tempHeight) {
			rowHeight = tempHeight;
		}

	}

	/**
	 * 设置所有子视图位置
	 */
	private void layoutChild() {
		for (int i = 0; i < getChildCount(); i++) {
			if (i != dragged) {
				Point xy = getCoorFromIndex(i);
				getChildAt(i).layout(
						xy.x - getChildAt(i).getMeasuredWidth() / 2,
						xy.y - getChildAt(i).getMeasuredHeight() / 2,
						xy.x + getChildAt(i).getMeasuredWidth() / 2,
						xy.y + getChildAt(i).getMeasuredHeight() / 2);
			}
		}
	}

	/**
	 * 根据编号获得坐标位置(中心)
	 * 
	 * @param index
	 * @return
	 */
	private Point getCoorFromIndex(int index) {
		int col = index % colCount;
		int row = index / colCount;
		return new Point(colWidth * col + colWidth / 2, rowHeight * row
				+ rowHeight / 2);
	}

	/**
	 * 获得手指最后位置的子视图编号
	 * 
	 * @return
	 */
	private int getLastIndex() {
		return getIndexFromCoor(lastX, lastY);
	}

	/**
	 * 根据坐标获得子视图编号
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int getIndexFromCoor(int x, int y) {
		int col = getColFromCoor(x), row = getRowFromCoor(y);
		if (col == -1 || row == -1)
			return -1;
		int index = row * colCount + col;
		if (index >= getChildCount())
			return -1;
		return index;
	}

	/**
	 * 根据坐标获得目标子视图编号
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int getTargetFromCoor(int x, int y) {
		int leftPos = getIndexFromCoor(x - (colWidth / 4), y);
		int rightPos = getIndexFromCoor(x + (colWidth / 4), y);
		if (leftPos == -1 && rightPos == -1)
			return -1;
		if (leftPos == rightPos)
			return -1;

		int target = -1;
		if (rightPos > -1)
			target = rightPos;
		else if (leftPos > -1)
			target = leftPos + 1;
		if (dragged < target)
			return target - 1;
		return target;
	}

	private int getColFromCoor(int coor) {
		for (int i = 0; coor > 0; i++) {
			if (coor < colWidth)
				return i;
			coor -= (colWidth);
		}
		return -1;
	}

	private int getRowFromCoor(int coor) {
		for (int i = 0; coor > 0; i++) {
			if (coor < rowHeight)
				return i;
			coor -= (rowHeight);
		}
		return -1;
	}

	/* 动画 */

	/**
	 * 拖拽动画
	 */
	private void animateDragged() {
		View v = getChildAt(dragged);

		AnimationSet animSet = new AnimationSet(true);
		ScaleAnimation scale = new ScaleAnimation(.667f, 1f, .667f, 1f,
				colWidth / 2, rowHeight / 2);
		scale.setDuration(150);
		AlphaAnimation alpha = new AlphaAnimation(1, .5f);
		alpha.setDuration(150);

		animSet.addAnimation(scale);
		animSet.addAnimation(alpha);
		animSet.setFillEnabled(true);
		animSet.setFillAfter(true);

		v.clearAnimation();
		v.startAnimation(animSet);
	}

	/**
	 * 填补间隙动画
	 * 
	 * @param target
	 */
	protected void animateGap(int target) {
		// 遍历所有子视图
		for (int i = 0; i < getChildCount(); i++) {
			// 如果编号为拖拽子视图的编号，则不作处理
			if (i == dragged)
				continue;

			// 当前视图新序号,默认根据当前视图编号获得
			int newPos = i;
			// 当前视图旧序号,默认根据当前视图编号获得
			int oldPos = i;
			// 从存储子视图序号的列表中读取，如果不为-1，则表明存储过新的序号，读取序号。
			if (newPositions.get(i) != -1) {
				oldPos = newPositions.get(i);
			}

			// 当拖拽的视图编号小于目标位置的编号
			// 且当前视图编号大于拖拽视图编号
			// 且当前视图编号小于等于目标视图编号
			if (dragged < target && i > dragged && i <= target) {
				newPos--;
			}
			// 当拖拽的视图编号大于目标位置的编号
			// 且当前视图编号小于拖拽视图编号
			// 且当前视图编号大于等于目标视图编号
			else if (target < dragged && i < dragged && i >= target) {
				newPos++;
			}

			if (oldPos == newPos)
				continue;

			Point oldXY = getCoorFromIndex(oldPos);
			Point newXY = getCoorFromIndex(newPos);

			// 根据编号获得子视图
			View v = getChildAt(i);
			Point oldOffset = new Point(oldXY.x - v.getLeft()
					- v.getMeasuredWidth() / 2, oldXY.y - v.getTop()
					- v.getMeasuredHeight() / 2);
			Point newOffset = new Point(newXY.x - v.getLeft()
					- v.getMeasuredWidth() / 2, newXY.y - v.getTop()
					- v.getMeasuredHeight() / 2);

			TranslateAnimation translate = new TranslateAnimation(
					Animation.ABSOLUTE, oldOffset.x, Animation.ABSOLUTE,
					newOffset.x, Animation.ABSOLUTE, oldOffset.y,
					Animation.ABSOLUTE, newOffset.y);
			translate.setDuration(150);
			translate.setFillEnabled(true);
			translate.setFillAfter(true);
			v.clearAnimation();
			v.startAnimation(translate);

			newPositions.set(i, newPos);
		}
	}

	/**
	 * 重新排列子视图
	 */
	protected void reorderChildren() {
		ArrayList<View> children = new ArrayList<View>();
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).clearAnimation();
			children.add(getChildAt(i));
		}
		removeAllViews();

		while (dragged != lastTarget) {
			// 如果目标位置是临时列表的结尾，直接进行处理
			if (lastTarget == children.size()) {
				// 删除当前拖拽的视图并添加到临时列表的结尾
				children.add(children.remove(dragged));
				dragged = lastTarget;
			} else if (dragged < lastTarget) {
				Collections.swap(children, dragged, dragged + 1);
				dragged++;
			} else if (dragged > lastTarget) {
				Collections.swap(children, dragged, dragged - 1);
				dragged--;
			}
		}

		// 重置所有子视图的序号,并添加到主视图中
		for (int i = 0; i < children.size(); i++) {
			newPositions.set(i, -1);
			addView(children.get(i));
		}
	}
}
