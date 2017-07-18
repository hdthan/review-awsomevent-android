package vn.axonactive.aevent_organizer.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import vn.axonactive.aevent_organizer.R;
import vn.axonactive.aevent_organizer.model.Statistic;
import vn.axonactive.aevent_organizer.util.GlobalBus;


public class DashBoardFragment extends Fragment {

    private TextView mTotal;
    private List<Entry> mEntries;
    private List<String> mLabels;

    private PieChart mPieChart;

    private boolean firstLaunch = true;

    private static final int[] COLOR_THEME = {
            Color.rgb(139, 195, 74), Color.rgb(189, 189, 189), Color.rgb(245, 199, 0),
            Color.rgb(106, 150, 31), Color.rgb(179, 100, 53)
    };

    public DashBoardFragment() {
    }


    public static DashBoardFragment newInstance() {
        DashBoardFragment fragment = new DashBoardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_event_dashboard, container, false);

        mTotal = (TextView) view.findViewById(R.id.total);

        mEntries = new ArrayList<>();

        mLabels = new ArrayList<>();
        mLabels.add("Checked");
        mLabels.add("Unchecked");

        mPieChart = (PieChart) view.findViewById(R.id.chart);

        return view;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private class IntegerFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return "" + (int) value;
        }
    }

    @Subscribe
    public void revStatisticDataFromActivity(Statistic statistic) {
        if (mEntries != null && mEntries.size() > 0) {
            mEntries.clear();
        }

        String total = "Total: " + statistic.getTotal();
        mTotal.setText(total);

        mEntries.add(new Entry(statistic.getChecked(), 0));
        mEntries.add(new Entry((statistic.getUnchecked()), 1));

        PieDataSet dataSet = new PieDataSet(mEntries, "");

        PieData data = new PieData(mLabels, dataSet);
        dataSet.setValueFormatter(new IntegerFormatter());
        data.setValueTextColor(Color.parseColor("#ffffff"));
        data.setValueTextSize(12f);
        dataSet.setColors(COLOR_THEME);
        mPieChart.setData(data);
        mPieChart.setDescription("");
        mPieChart.setDrawSliceText(false);

        if (firstLaunch) {
            mPieChart.animateY(2500);
            firstLaunch = false;
        } else {
            mPieChart.notifyDataSetChanged();
            mPieChart.invalidate();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        GlobalBus.getBus().unregister(this);
    }
}
