package com.easemob.widget;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.easemob.uidemo.R;

public class EMExpressionWidget extends ViewPager {
	Context context;
	protected LayoutInflater inflater;
	private List<View> gridViews = new LinkedList<View>();
	private List<ExpressionAdapter> gridAdapters = new LinkedList<ExpressionAdapter>();
	
	public EMExpressionWidget(Context context) {
		super(context);
		init(context);
	}

	public EMExpressionWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.em_expression_widget, this);
		
		// 表情list
		List<String> resList = getExpressionRes(35);
		
		// 初始化表情viewpager
		for (int i = 1; i < 3; i++) {
			Pair<View, ExpressionAdapter> pair = getGridChildView(i, resList);
			gridViews.add(pair.first);
			gridAdapters.add(pair.second);
		}
		this.setAdapter(new ExpressionPagerAdapter(gridViews));
	}

	public List<String> getExpressionRes(int getSum) {
		List<String> reslist = new ArrayList<String>();
		for (int x = 1; x <= getSum; x++) {
			String filename = "ee_" + x;

			reslist.add(filename);
		}
		return reslist;
	}
	
	/**
	 * 获取表情的gridview的子view
	 * 
	 * @param i
	 * @return
	 */
	private Pair<View, ExpressionAdapter> getGridChildView(int i, List<String> resList) {
		ExpandGridView view = new ExpandGridView(context);
		view.setNumColumns(7);
		ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		view.setGravity(Gravity.CENTER);
		view.setLayoutParams(param);
		
		List<String> list = new ArrayList<String>();
		if (i == 1) {
			List<String> list1 = resList.subList(0, 20);
			list.addAll(list1);
		} else if (i == 2) {
			list.addAll(resList.subList(20, resList.size()));
		}
		list.add("delete_expression");
		ExpressionAdapter adapter = new ExpressionAdapter(context, 1, list);
		view.setAdapter(adapter);
		return Pair.create((View)view, adapter);
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		for (View view : gridViews) {
			((ExpandGridView)view).setOnItemClickListener(listener);
		}
	}
	
	public String getExpression(View parent, int position) {
		if (parent == gridViews.get(0)) {
			return gridAdapters.get(0).getItem(position);
		} else {
			return gridAdapters.get(1).getItem(position);
		}
	}
	
	private class ExpressionPagerAdapter extends PagerAdapter {
		private List<View> views;

		public ExpressionPagerAdapter(List<View> views) {
			this.views = views;
		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(views.get(arg1));
			return views.get(arg1);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(views.get(arg1));
		}
	}
	
	public class ExpressionAdapter extends ArrayAdapter<String>{

		public ExpressionAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = View.inflate(getContext(), R.layout.em_row_expression, null);
			}
			
			ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_expression);
			
			String filename = getItem(position);
			int resId = getContext().getResources().getIdentifier(filename, "drawable", getContext().getPackageName());
			imageView.setImageResource(resId);
			
			return convertView;
		}
	}
	
	public class ExpandGridView extends GridView {
		public ExpandGridView(Context context) {
			super(context);
		}

		public ExpandGridView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int expandSpec = MeasureSpec.makeMeasureSpec(
					Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
			super.onMeasure(widthMeasureSpec, expandSpec);
		}

	}
}
