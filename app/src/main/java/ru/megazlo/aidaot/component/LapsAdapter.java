package ru.megazlo.aidaot.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ru.megazlo.aidaot.LapItem;
import ru.megazlo.aidaot.R;

/** Created by iGurkin on 23.11.2018. */
public class LapsAdapter extends ArrayAdapter<LapItem> {

	private final LayoutInflater inflater;

	public LapsAdapter(Context context) {
		super(context, R.layout.table_detail_row);
		inflater = LayoutInflater.from(context);
	}

	@NonNull
	@Override
	public View getView(int position, View cView, @NonNull ViewGroup parent) {
		LapsAdapter.ViewHolder holder;
		if (cView == null) {
			holder = new LapsAdapter.ViewHolder();
			cView = inflater.inflate(R.layout.table_detail_row, parent, false);
			holder.order = cView.findViewById(R.id.tv_order);
			holder.time = cView.findViewById(R.id.tv_time);
			cView.setTag(holder);
		} else {
			holder = (LapsAdapter.ViewHolder) cView.getTag();
		}
		LapItem item = getItem(position);
		holder.order.setText(Integer.toString(item.getPosition()));
		holder.time.setText(item.getTime().toString("HH:mm:ss"));
		return cView;
	}

	private class ViewHolder {
		TextView order;
		TextView time;
	}
}
