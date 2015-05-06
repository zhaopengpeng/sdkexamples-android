package com.easemob.widget.factory;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class EMContactWidgetFactory extends EMWidgetFactory {

	private static EMContactWidgetFactory instance = null; 
	
	public static EMContactWidgetFactory getInstance(Context context) {
		if (instance == null) {
			instance = new EMContactWidgetFactoryImpl(context);
		}
		return instance;
	}
	
	public static void setFactory(EMContactWidgetFactory factory) {
		instance = factory;
	}
	
	public EMContactWidgetFactory(Context context) {
		super(context);
	}
	
	public abstract void onPreUpdateData(List<?> datas);
	
	public abstract View generateView(Object obj, View convertView, ViewGroup parentView);

	public abstract View generateView(Object obj, View convertView, ViewGroup parentView, boolean isSectionHeader);
}
