package com.easemob.widget.factory;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.easemob.uidemo.R;

public class EMMessageWidgetFactoryImp extends EMMessageWidgetFactory {

	public EMMessageWidgetFactoryImp(Context context) {
		super(context);
	}

	@Override
	public View generateView(Object obj, View convertView, ViewGroup parentView) {
		return inflater.inflate(R.layout.em_conversation_item, null);
	}

	@Override
	public void onPreUpdateData(List<?> datas) {
		// TODO Auto-generated method stub
		
	}
}
