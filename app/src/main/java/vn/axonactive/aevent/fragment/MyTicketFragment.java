package vn.axonactive.aevent.fragment;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.ArrayList;
import java.util.List;

import jonathanfinerty.once.Once;
import vn.axonactive.aevent.R;

/**
 * Created by ltphuc on 2/13/2017.
 */

public class MyTicketFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private String showTicketTour = "showTicketTour";

    public MyTicketFragment() {

    }


    public static MyTicketFragment newInstance() {
        MyTicketFragment fragment = new MyTicketFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_my_ticket, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        boolean beenDone = Once.beenDone(Once.THIS_APP_INSTALL, showTicketTour);
        if (!beenDone) {
            initOnBoardingTutorial();
            Once.markDone(showTicketTour);
        }
        return view;

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(TicketFragment.newInstance("up-coming"), "UPCOMING");
        adapter.addFragment(TicketFragment.newInstance("passing"), "EXPIRED");
        viewPager.setAdapter(adapter);
    }
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    private void initOnBoardingTutorial() {

        final Drawable close = ContextCompat.getDrawable(getActivity(), R.drawable.ic_close);
        final Rect droidTarget = new Rect(0, 0, getScreenWidth(), getScreenHeight());

        TapTargetSequence sequence = new TapTargetSequence(getActivity())                                    //initializes and starts the onboarding process
                .targets(
                        TapTarget.forBounds(droidTarget, "View Tickets", "Tap on the event to view your ticket")
                        .icon(close)
                )

                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        // Yay
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget) {


                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Boo
                    }
                });

        sequence.start();
    }


        private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
