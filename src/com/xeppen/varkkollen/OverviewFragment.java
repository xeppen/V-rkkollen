package com.xeppen.varkkollen;

import java.util.ArrayList;
import java.util.List;

import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.xeppen.varkkollen.util.ContractionExpandableListAdapter;
import com.xeppen.varkkollen.model.Contraction;
import com.xeppen.varkkollen.sql.FeedReaderDbHelper;
import com.xeppen.varkkollen.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class OverviewFragment extends Fragment {

	// ContractionAdapter adapter;
	private ContractionExpandableListAdapter adapter;
	List<Contraction> contractions;

	// UI Elements
	ListView contractionList;

	// Defining database
	FeedReaderDbHelper dbHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_overview, container,
				false);

		contractionList = (ListView) rootView
				.findViewById(R.id.ContractionList);
		contractions = new ArrayList<Contraction>();

		dbHelper = new FeedReaderDbHelper(getActivity());
		fetchAllContractions();
		setCustomAdapter();
		return rootView;
	}

	public void fetchAllContractions() {
		contractions = dbHelper.getAllContractions();
	}

	public void setCustomAdapter() {
		// adapter = new ContractionAdapter(getActivity(), R.id.ContractionList,
		// contractions);
		adapter = new ContractionExpandableListAdapter(getActivity(), contractions, getActivity().getFragmentManager());
		AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(
				adapter);
		alphaInAnimationAdapter.setAbsListView(contractionList);
		alphaInAnimationAdapter.setInitialDelayMillis(500);
		contractionList.setAdapter(alphaInAnimationAdapter);

	}
}
