package com.easemob.widget.factory;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class EMWidgetFactory {
	protected Context context;
	protected LayoutInflater inflater;
	
	public EMWidgetFactory(Context context) {
		this.context = context;
		inflater = LayoutInflater.from(context);
	}
	
	public abstract void onPreUpdateData(List<?> datas);
	
	public abstract View generateView(Object obj, View convertView, ViewGroup parentView);
}
