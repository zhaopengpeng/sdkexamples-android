package com.easemob.widget.factory;

import android.content.Context;

public abstract class EMMessageWidgetFactory extends EMWidgetFactory {
	
	private static EMMessageWidgetFactory instance = null; 
	
	public static EMMessageWidgetFactory getInstance(Context context) {
		if (instance == null) {
			instance = new EMMessageWidgetFactoryImp(context);
		}
		return instance;
	}
	
	public static void setFactory(EMMessageWidgetFactory factory) {
		instance = factory;
	}
	
	public EMMessageWidgetFactory(Context context) {
		super(context);
	}

}
