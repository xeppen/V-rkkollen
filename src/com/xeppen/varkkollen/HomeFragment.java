package com.xeppen.varkkollen;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.xeppen.varkkollen.model.Contraction;
import com.xeppen.varkkollen.sql.FeedReaderDbHelper;
import com.xeppen.varkkollen.R;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {
	// Define Chronometer
	Chronometer contractionTimeChronometer;

	// Define TextViews
	TextView timeSinceLastContraction;
	TextView durationLastContraction;
	TextView durationLastHour;
	TextView frequencyLastHour;
	TextView contractionsLastHour;
	TextView durationLastSixHours;
	TextView frequencyLastSixHours;
	TextView contractionsLastSixHours;

	// Define Button
	Button timerStartButton;

	// Define running boolean for Chronometer
	static boolean isChronometerRunning = false;

	// Define list of Contractions
	List<Contraction> conts = new ArrayList<Contraction>();

	// Defining database
	FeedReaderDbHelper dbHelper;

	// Global variables
	long startTimeInMilliSeconds = 0;
	long stopTimeInMilliSeconds = 0;

	GradientDrawable gdDefault;

	// Define button colors
	int redColor;
	int greenColor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_home, container, false);

		// Assign elements
		timeSinceLastContraction = (TextView) v
				.findViewById(R.id.timeSinceLastContraction);
		durationLastContraction = (TextView) v
				.findViewById(R.id.durationLastContraction);
		durationLastHour = (TextView) v.findViewById(R.id.durationLastHour);
		frequencyLastHour = (TextView) v.findViewById(R.id.frequencyLastHour);
		contractionsLastHour = (TextView) v
				.findViewById(R.id.contractionsLastHour);
		durationLastSixHours = (TextView) v
				.findViewById(R.id.durationLastSixHours);
		frequencyLastSixHours = (TextView) v
				.findViewById(R.id.frequencyLastSixHours);
		contractionsLastSixHours = (TextView) v
				.findViewById(R.id.contractionsLastSixHours);
		timerStartButton = (Button) v.findViewById(R.id.timerStartButton);
		contractionTimeChronometer = (Chronometer) v
				.findViewById(R.id.contractionTimeChronometer);
		dbHelper = new FeedReaderDbHelper(getActivity());
		gdDefault = new GradientDrawable();

		// Clear db
		//dbHelper.clearDB();

		// Set button start text
		timerStartButton.setText("Starta");

		addListeners();
		updateLatestBox();
		updateLastHourBox();
		updateLastSixHoursBox();

		return v;
	}

	private void addListeners() {

		// Start Button listener
		timerStartButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!isChronometerRunning) {
					// Change text
					timerStartButton.setText("Stoppa");
					// Save start time in ms
					startTimeInMilliSeconds = System.currentTimeMillis();

					// Set start time
					String curTime = getCurrentTime();

					// Start the timer
					contractionTimeChronometer.setBase(SystemClock
							.elapsedRealtime());
					contractionTimeChronometer.start();

					// Set boolean
					isChronometerRunning = true;
				} else if (isChronometerRunning) {
					// Change text
					timerStartButton.setText("Starta");

					// Set stop time
					String curTime = getCurrentTime();
					stopTimeInMilliSeconds = System.currentTimeMillis();

					// Stop the timer
					contractionTimeChronometer.stop();

					// Calculate values
					long milliseconds = SystemClock.elapsedRealtime()
							- contractionTimeChronometer.getBase();
					int seconds = (int) (milliseconds / 1000) % 60;
					int minutes = (int) ((milliseconds / (1000 * 60)) % 60);

					// Create contraction
					Contraction cont = new Contraction();
					cont.setStartTime(String.valueOf(startTimeInMilliSeconds));
					cont.setStopTime(String.valueOf(stopTimeInMilliSeconds));
					cont.setDurationMin(minutes);
					cont.setDurationSec(seconds);

					// Add contraction to DB
					dbHelper.AddContraction(cont);
					dbHelper.close();
					alert("New contraction added!");

					// Update last box
					updateLatestBox();

					// Update Last hour box
					updateLastHourBox();
					// Update six hour box
					updateLastSixHoursBox();

					// Set boolean
					isChronometerRunning = false;
				}

			}
		});
	}

	private void updateLatestBox() {
		List<Contraction> items = dbHelper.GetLatestCont();

		if (items != null) {

			Contraction lastCont = items.get(0);
			if (items.size() == 2) {
				Contraction secondLastCont = items.get(1);

				long timeDiff = Long.valueOf(lastCont.getStartTime())
						- Long.valueOf(secondLastCont.getStartTime());

				// Frequency
				int elapsedSeconds = (int) (timeDiff / 1000) % 60;
				int elapsedMinutes = (int) ((timeDiff / (1000 * 60)) % 60);

				String elapsedTime = String.format("%02d:%02d", elapsedMinutes,
						elapsedSeconds);
				timeSinceLastContraction.setText(elapsedTime);
			}

			// Duration of last contraction
			long duration = Long.valueOf(lastCont.getStopTime())
					- Long.valueOf(lastCont.getStartTime());

			Date date = new Date(duration);
			DateFormat formatter = new SimpleDateFormat("mm:ss");
			String durationFormatted = formatter.format(date);
			durationLastContraction.setText(durationFormatted);
		} else {
			timeSinceLastContraction.setText("00:00");
			durationLastContraction.setText("00:00");
		}
	}

	protected void updateLastHourBox() {
		List<Contraction> contractions = new ArrayList<Contraction>();

		contractions = dbHelper.getPreviousHoursContractions(1);
		int count = contractions.size();
		int totalDurationInSec = 0;
		int totalIntervallInSec = 0;

		for (int i = 0; i < count; i++) {
			Contraction currentContraction = contractions.get(i);
			totalDurationInSec += getContractionDurationInSec(currentContraction);
			if (i + 1 < count)
				totalIntervallInSec += getContractionIntervallInSec(
						currentContraction, contractions.get(i + 1));
		}
		String duration;
		String intervall;
		if (count != 0) {
			int averageDurationInSec = totalDurationInSec / count;
			int averageIntervallInSec = totalIntervallInSec / count;

			duration = String
					.format("%02d:%02d", (averageDurationInSec / 60) % 60,
							averageDurationInSec % 60);
			intervall = String.format("%02d:%02d",
					(averageIntervallInSec / 60) % 60,
					averageIntervallInSec % 60);
		} else {
			duration = "00:00";
			intervall = "00:00";
		}

		setLabelsHourBox(duration, intervall, count);

	}

	protected void updateLastSixHoursBox() {
		List<Contraction> contractions = new ArrayList<Contraction>();

		contractions = dbHelper.getPreviousHoursContractions(6);
		int count = contractions.size();
		int totalDurationInSec = 0;
		int totalIntervallInSec = 0;

		for (int i = 0; i < count; i++) {
			Contraction currentContraction = contractions.get(i);
			totalDurationInSec += getContractionDurationInSec(currentContraction);
			if (i + 1 < count)
				totalIntervallInSec += getContractionIntervallInSec(
						currentContraction, contractions.get(i + 1));

			Log.d("VŠrkkollen", i + ": duration: " + totalDurationInSec
					+ "s - intervall: " + totalIntervallInSec + "s");
		}
		String duration;
		String intervall;
		if (count != 0) {
			int averageDurationInSec = totalDurationInSec / count;
			int averageIntervallInSec = totalIntervallInSec / count;

			duration = String
					.format("%02d:%02d", (averageDurationInSec / 60) % 60,
							averageDurationInSec % 60);
			intervall = String.format("%02d:%02d",
					(averageIntervallInSec / 60) % 60,
					averageIntervallInSec % 60);
		} else {
			duration = "00:00";
			intervall = "00:00";
		}

		setLabelsSixHoursBox(duration, intervall, count);

	}

	private String getDuration(Contraction c) {
		long duration = Long.valueOf(c.getStopTime())
				- Long.valueOf(c.getStartTime());

		Date date = new Date(duration);
		DateFormat formatter = new SimpleDateFormat("mm:ss");
		String durationFormatted = formatter.format(date);
		return durationFormatted;
	}

	protected String getElapsedTimeSinceLastCont(Contraction lastCur) {
		Log.d("VŠrkkollen", "lastCur.getStopTime(): " + lastCur.getStopTime());
		long elapsedTimeSinceLastCont = System.currentTimeMillis()
				- Long.valueOf(lastCur.getStopTime());
		int elapsedSeconds = (int) (elapsedTimeSinceLastCont / 1000) % 60;
		int elapsedMinutes = (int) ((elapsedTimeSinceLastCont / (1000 * 60)) % 60);

		// Set time elapsed since last contraction
		String elapsedTime = String.format("%02d:%02d", elapsedMinutes,
				elapsedSeconds);
		return elapsedTime;
	}

	private void alert(String s) {
		Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
	}

	private String getCurrentTime() {
		Time now = new Time(Time.getCurrentTimezone());
		now.setToNow();
		String time = now.format("%k:%M:%S");
		return time;
	}

	private int getContractionDurationInSec(Contraction c) {
		return c.getDurationMin() * 60 + c.getDurationSec();
	}

	private long getContractionIntervallInSec(Contraction firstContraction,
			Contraction secondContraction) {
		long startTimeFirstContraction = Long.valueOf(firstContraction
				.getStartTime());
		long startTimeSecondContraction = Long.valueOf(secondContraction
				.getStartTime());

		long intervallInMilliseconds = startTimeSecondContraction
				- startTimeFirstContraction;
		long intervallInSeconds = intervallInMilliseconds / 1000;
		return intervallInSeconds;
	}

	void setLabelsHourBox(String duration, String intervall, int contractions) {
		durationLastHour.setText(duration);
		frequencyLastHour.setText(intervall);
		contractionsLastHour.setText(contractions + " vŠrkar");
	}

	void setLabelsSixHoursBox(String duration, String intervall,
			int contractions) {
		durationLastSixHours.setText(duration);
		frequencyLastSixHours.setText(intervall);
		contractionsLastSixHours.setText(contractions + " vŠrkar");
	}

	private long getMillisecondsFromTimeFormat(String myTimeString) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		Date date = null;
		try {
			date = sdf.parse(myTimeString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Calendar calendar = GregorianCalendar.getInstance(); // creates a new
																// calendar
																// instance
		calendar.setTime(date); // assigns calendar to given date
		return (long) calendar.get(Calendar.MILLISECOND);
	}
}
