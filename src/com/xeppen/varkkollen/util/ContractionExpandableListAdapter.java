package com.xeppen.varkkollen.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.haarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;
import com.xeppen.varkkollen.OverviewFragment;
import com.xeppen.varkkollen.R;
import com.xeppen.varkkollen.model.Contraction;
import com.xeppen.varkkollen.sql.FeedReaderDbHelper;

public class ContractionExpandableListAdapter extends
		ExpandableListItemAdapter<Contraction> {

	private Context mContext;
	List<Contraction> contractions;
	FeedReaderDbHelper dbHelper;
	FragmentManager fManager;
	private AlertDialog alert;

	/*
	 * This will create a new ExpandableListItemAdapter, providing a custom
	 * layout resource, and the two child ViewGroups' id's. If you don't want
	 * this, just pass either just the Context, or the Context and the List<T>
	 * up to super.
	 */
	public ContractionExpandableListAdapter(Context context,
			List<Contraction> items, FragmentManager fm) {
		super(context, R.layout.activity_expandablelistitem_card,
				R.id.activity_expandablelistitem_card_title,
				R.id.activity_expandablelistitem_card_content, items);
		mContext = context;
		contractions = items;
		dbHelper = new FeedReaderDbHelper(mContext);
		fManager = fm;
	}

	@Override
	public View getTitleView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolderTitle holder = new ViewHolderTitle();

		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.contraction_list_title, parent,
					false);
			// if ( position % 2 == 0 ){
			// row.setBackgroundColor(Color.LTGRAY);
			// }
			holder.v1 = (TextView) row
					.findViewById(R.id.ContractionDuractionLabel);
			holder.v2 = (TextView) row
					.findViewById(R.id.ContractionFrequencyLabel);
			holder.v3 = (TextView) row
					.findViewById(R.id.ContractionStartTimeLabel);
			holder.v4 = (TextView) row
					.findViewById(R.id.ContractionStartDateLabel);
			row.setTag(holder);
		} else {
			holder = (ViewHolderTitle) row.getTag();
		}
		final Contraction contraction = contractions.get(position);
		if (contraction != null) {
			holder.v1
					.setText(String.format("%02d:%02d",
							contraction.getDurationMin(),
							contraction.getDurationSec()));
			holder.v2.setText(getFrequencyOfContraction(contraction));
			holder.v3.setText(getTime(contraction.getStartTime()));
			holder.v4.setText(getDate(contraction.getStartTime()));
		}

		return row;
	}

	public static class ViewHolderTitle {
		TextView v1; // view1
		TextView v2; // view2
		TextView v3; // view3
		TextView v4; // view3
	}

	public static class ViewHolderContent {
		TextView v1; // view1
		TextView v3; // view3
		Button v4;
		Button v5;
	}

	@Override
	public View getContentView(int position, View convertView, ViewGroup parent) {
		View content = convertView;
		ViewHolderContent holder = new ViewHolderContent();
		String[] intensityValues = { "Lindrig", "Obehaglig", "Besvärlig",
				"Fruktansvärd", "Outhärdlig" };

		if (content == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			content = inflater.inflate(R.layout.contraction_list_content,
					parent, false);

			holder.v1 = (TextView) content
					.findViewById(R.id.Edit_IntensityLabel);
			holder.v3 = (TextView) content.findViewById(R.id.Edit_Note);
			holder.v4 = (Button) content.findViewById(R.id.Edit_UpdateButton);
			holder.v5 = (Button) content.findViewById(R.id.Edit_DeleteButton);

			content.setTag(holder);
		} else {
			holder = (ViewHolderContent) content.getTag();
		}
		final Contraction contraction = contractions.get(position);
		if (contraction != null) {
			holder.v1.setText(intensityValues[contraction.getIntencity()]);
			if (contraction.getNote().equals("")) {
				holder.v3.setText("< Notering saknas >");
				holder.v3.setTypeface(null, Typeface.ITALIC);
				holder.v3.setTextColor(Color.parseColor("#B5B5B5"));
			} else {
				holder.v3.setText(contraction.getNote());
				holder.v3.setTypeface(null, Typeface.NORMAL);
				holder.v3.setTextColor(Color.parseColor("#000000"));
			}
		}

		// Set Button listeners
		DeleteOnClickListener docl = new DeleteOnClickListener(
				contraction.getId(), position);
		holder.v5.setOnClickListener(docl);

		EditOnClickListener uocl = new EditOnClickListener(contraction);
		holder.v4.setOnClickListener(uocl);

		return content;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return contractions.size();
	}

	public class IntensityOnSeekBarChangeListener implements
			OnSeekBarChangeListener {
		TextView tw;
		String[] intensityValues = { "Lindrig", "Obehaglig", "Besvärlig",
				"Fruktansvärd", "Outhärdlig" };

		public IntensityOnSeekBarChangeListener(TextView tw) {
			this.tw = tw;
		}

		@Override
		public void onProgressChanged(SeekBar bar, int progress,
				boolean fromUser) {
			int discValue = Math.round(progress);
			bar.setProgress(discValue);

			tw.setText(intensityValues[discValue]);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
		}
	}

	public class DeleteOnClickListener implements OnClickListener {
		int contId;
		int position;

		public DeleteOnClickListener(int id, int position) {
			this.contId = id;
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					mContext);

			// set title
			alertDialogBuilder.setTitle("Ta bort värk?");

			// set dialog message
			alertDialogBuilder
					.setCancelable(false)
					.setMessage("Vill du ta bort denna värk?")
					.setPositiveButton("Ta bort",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									removeFromDB(contId, position);
								}
							})
					.setNegativeButton("Stäng",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

	};

	public class EditOnClickListener implements OnClickListener {
		Contraction c;

		public EditOnClickListener(Contraction c) {
			this.c = c;
		}

		@Override
		public void onClick(View v) {
			createEditDialog(c);
		}

	};

	protected String getFrequencyOfContraction(Contraction lastCur) {
		long elapsedTimeSinceLastCont = System.currentTimeMillis()
				- Long.valueOf(lastCur.getStopTime());
		int elapsedSeconds = (int) (elapsedTimeSinceLastCont / 1000) % 60;
		int elapsedMinutes = (int) ((elapsedTimeSinceLastCont / (1000 * 60)) % 60);

		// Set time elapsed since last contraction
		String elapsedTime = String.format("%02d:%02d", elapsedMinutes,
				elapsedSeconds);
		return elapsedTime;
	}

	protected void removeCurrentContraction(Integer id) {

	}

	private void alert(String s) {
		Toast.makeText(mContext, s, Toast.LENGTH_LONG).show();
	}

	private void removeFromDB(int id, int position) {
		dbHelper.deleteContraction(id);
		contractions.remove(position);
		// alert("Contraction " + id + " has been deleted from DB!");
		// this.notifyDataSetChanged();

		reloadFragment();
	}

	private String getTime(String startTime) {
		long timeMilli = Long.valueOf(startTime);

		Date date = new Date(timeMilli);
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		String timeFormatted = formatter.format(date);
		return timeFormatted;
	}

	private String getDate(String startTime) {
		long timeMilli = Long.valueOf(startTime);

		Date date = new Date(timeMilli);
		DateFormat formatter = new SimpleDateFormat("dd MMM yyyy",
				Locale.ENGLISH);
		String dateFormatted = formatter.format(date);
		return dateFormatted;
	}

	private void createEditDialog(Contraction cont) {
		String[] intensityValues = { "Lindrig", "Obehaglig", "Besvärlig",
				"Fruktansvärd", "Outhärdlig" };
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.fragment_edit, null);
		dialog.setTitle("Edit");

		// Define elements
		Button EditUpdateButton = (Button) view
				.findViewById(R.id.EditDialog_UpdateContractionButton);
		Button EditCloseButton = (Button) view
				.findViewById(R.id.EditDialog_CloseEditDialogButton);
		TextView Edit_StartTime = (TextView) view
				.findViewById(R.id.EditDialog_StartTime);
		TextView Edit_StartDate = (TextView) view
				.findViewById(R.id.EditDialog_StartDate);
		TextView Edit_StopTime = (TextView) view
				.findViewById(R.id.EditDialog_StopTime);
		TextView Edit_StopDate = (TextView) view
				.findViewById(R.id.EditDialog_StopDate);
		TextView Edit_IntensityLabel = (TextView) view
				.findViewById(R.id.EditDialog_IntensityLabel);
		TextView Edit_Duration = (TextView) view
				.findViewById(R.id.EditDialog_Duration);
		SeekBar Edit_IntensitySeekBar = (SeekBar) view
				.findViewById(R.id.EditDialog_IntensitySeekBar);
		EditText Edit_Note = (EditText) view.findViewById(R.id.EditDialog_Note);

		// Set start values
		Edit_StartTime.setText(getTime(cont.getStartTime()));
		Edit_StartDate.setText(getDate(cont.getStartTime()));
		Edit_StopTime.setText(getTime(cont.getStopTime()));
		Edit_StopDate.setText(getDate(cont.getStopTime()));
		Edit_IntensityLabel.setText(intensityValues[Math.round(cont
				.getIntencity())]);
		Edit_Duration.setText(getDuration(cont));
		Edit_Note.setText(cont.getNote());
		Edit_IntensitySeekBar.setProgress(cont.getIntencity());

		// Set listeners
		UpdateOnClickListener uocl = new UpdateOnClickListener(Edit_Note,
				Edit_IntensitySeekBar, cont.getId());
		EditUpdateButton.setOnClickListener(uocl);
		EditCloseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alert.cancel();
			}
		});

		IntensityOnChangeListener iocl = new IntensityOnChangeListener(
				Edit_IntensityLabel);
		Edit_IntensitySeekBar.setOnSeekBarChangeListener(iocl);

		dialog.setView(view);
		// dialog.show();
		alert = dialog.create();
		alert.show();
	}

	private class IntensityOnChangeListener implements OnSeekBarChangeListener {
		TextView tw;
		String[] intensityValues = { "Lindrig", "Obehaglig", "Besvärlig",
				"Fruktansvärd", "Outhärdlig" };

		private IntensityOnChangeListener(TextView tw) {
			this.tw = tw;
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			int discValue = Math.round(progress);
			seekBar.setProgress(discValue);

			tw.setText(intensityValues[discValue]);

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}
	}

	private class UpdateOnClickListener implements OnClickListener {

		EditText et;
		SeekBar sb;
		int id;

		public UpdateOnClickListener(EditText et, SeekBar sb, int id) {
			this.et = et;
			this.sb = sb;
			this.id = id;
		}

		@Override
		public void onClick(View arg0) {
			String note = et.getText().toString();
			int progress = sb.getProgress();

			dbHelper.updateContraction(note, progress, this.id);
			alert.cancel();
			reloadFragment();
		}

	}

	private String getDuration(Contraction c) {
		long duration = Long.valueOf(c.getStopTime())
				- Long.valueOf(c.getStartTime());

		Date date = new Date(duration);
		DateFormat formatter = new SimpleDateFormat("mm:ss");
		String durationFormatted = formatter.format(date);
		return durationFormatted;
	}

	private void reloadFragment() {
		// Remove Fragment
		fManager.beginTransaction()
				.remove(fManager.findFragmentById(R.id.frame_container))
				.commit();
		// Load Overview Fragment again
		OverviewFragment fragment = new OverviewFragment();
		fManager.beginTransaction().replace(R.id.frame_container, fragment)
				.commit();
	}

}
