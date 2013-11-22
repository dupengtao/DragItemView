package com.dpt.dragitemviewexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dpt.dragitemview.widget.DragItemLayout;
import com.dpt.dragitemview.widget.DragListener.OnItemClickListener;
import com.dpt.dragitemview.widget.DragListener.OnItemLongClickListener;
import com.dpt.dragitemview.widget.DragListener.OnItemMoveListener;
import com.dpt.dragitemview.widget.DragListener.OnItemMoveUpClickListener;
import com.dpt.dragitemview.widget.DragView;

/**
 * 
 * @author dupengtao88@gmail.com
 *
 * 2013-11-22
 */
public class MainActivity extends Activity {

	private int mRowNum = 3;
	private int offset;
	private DragView mDragView;
	private ImageView mIvCustomGameTrash;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView() {

		mDragView = (DragView) findViewById(R.id.dragview);
		int rowNum;
		int itemSize = getItemSize();
		if (itemSize % mRowNum == 0) {
			rowNum = itemSize / mRowNum;
		} else {
			rowNum = itemSize / mRowNum;
			rowNum += 1;
		}
		for (int i = 0; i < itemSize; i++) {
			DragItemLayout item = new DragItemLayout(this, getItemView(i));
			mDragView.setColAndRow(mRowNum, rowNum);
			mDragView.addItem(item);
		}

		offset = (int) getResources().getDimension(
				R.dimen.main_external_top_margins);

		mDragView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(View view, int position, boolean isLastItem) {
				if (isLastItem) {
					Toast.makeText(MainActivity.this, "添加", Toast.LENGTH_SHORT)
							.show();
				}
			}

		});

		mDragView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public void onItemLongClick(View view, int position) {
				mIvCustomGameTrash.setVisibility(View.VISIBLE);
			}
		});

		mDragView.setOnItemMoveUpClickListener(new OnItemMoveUpClickListener() {

			@Override
			public void onItemMoveUpClick(View view, int position, int lastX,
					int lastY) {

				int[] locations = new int[2];
				mIvCustomGameTrash.getLocationOnScreen(locations);
				int imageX = locations[0];// 获取组件当前位置的横坐标
				int imageY = locations[1] - offset * 4;// 获取组件当前位置的纵坐标

				boolean b = lastX > imageX
						&& lastX < imageX + mIvCustomGameTrash.getWidth();
				boolean b2 = lastY < imageY
						&& lastY > imageY - mIvCustomGameTrash.getHeight();

				if (b && b2) {
					mDragView.removeView(view);
				}

				mIvCustomGameTrash.setVisibility(View.GONE);
			}
		});

		mDragView.setOnItemMoveListener(new OnItemMoveListener() {

			@Override
			public void onItemMove(View view, int position, int lastX, int lastY) {

				int[] locations = new int[2];
				mIvCustomGameTrash.getLocationOnScreen(locations);
				int imageX = locations[0];// 获取组件当前位置的横坐标
				int imageY = locations[1] - offset * 4;// 获取组件当前位置的纵坐标

				boolean b = lastX > imageX
						&& lastX < imageX + mIvCustomGameTrash.getWidth();
				boolean b2 = lastY < imageY
						&& lastY > imageY - mIvCustomGameTrash.getHeight();

				if (b && b2) {
					mIvCustomGameTrash.setImageResource(R.drawable.del_custom);
				} else {
					mIvCustomGameTrash
							.setImageResource(R.drawable.del_custom_null);
				}
			}
		});

		mIvCustomGameTrash = (ImageView) findViewById(R.id.ivCustomGameTrash);
	}

	private int getItemSize() {
		return 14;
	}

	private View getItemView(int num) {

		if (getItemSize() - 1 == num) {

			View view = View.inflate(MainActivity.this, R.layout.item_drag_add, null);
			return view;
		}

		View view = View.inflate(MainActivity.this, R.layout.item_drag, null);
		TextView tvDragitemLable = (TextView) view
				.findViewById(R.id.tvDragitemLable);
		tvDragitemLable.setText("Item_"+num);
		return view;
	}
}
