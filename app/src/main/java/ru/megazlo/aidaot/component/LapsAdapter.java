package ru.megazlo.aidaot.component;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.*;
import android.widget.*;

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
			holder.timeOt = cView.findViewById(R.id.tv_time);
			holder.timeTm = cView.findViewById(R.id.tv_time_tm);
			holder.state = cView.findViewById(R.id.table_run_state);
			cView.setTag(holder);
		} else {
			holder = (LapsAdapter.ViewHolder) cView.getTag();
		}
		LapItem item = getItem(position);
		holder.order.setText(String.format("%02d", item.getPosition()));
		holder.timeOt.setText(String.format("OT %s", item.getTime().toString("HH:mm:ss")));
		holder.timeTm.setText(String.format("2 minutes %s", item.getTime().minusMinutes(2).toString("HH:mm:ss")));
		holder.state.setVisibility(item.isStarted() ? View.VISIBLE : View.INVISIBLE);
		return cView;
	}

	private class ViewHolder {
		TextView order;
		TextView timeOt;
		TextView timeTm;
		ImageView state;
	}
}
