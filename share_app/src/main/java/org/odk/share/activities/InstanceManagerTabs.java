package org.odk.share.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.dto.Instance;
import org.odk.share.fragments.StatisticsFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class InstanceManagerTabs extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.viewpager) ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_manager_tabs);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTab();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                TextView title = view.findViewById(R.id.tvTabTitle);
                title.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                TextView title = view.findViewById(R.id.tvTabTitle);
                title.setVisibility(View.GONE);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupTab() {
        setupTabIcon("Statistics", 0, R.drawable.ic_stats, true);
        setupTabIcon("Sent", 1, R.drawable.ic_upload, false);
        setupTabIcon("Received", 2, R.drawable.ic_download, false);
        setupTabIcon("Review", 3, R.drawable.ic_assignment, false);
    }

    private void setupTabIcon(String title, int position, int resId, boolean visible) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView titleView = view.findViewById(R.id.tvTabTitle);
        ImageView iconView = view.findViewById(R.id.ivTabIcon);

        titleView.setText(title);
        if (visible) {
            titleView.setVisibility(View.VISIBLE);
        } else {
            titleView.setVisibility(View.GONE);
        }

        iconView.setImageResource(resId);
        tabLayout.getTabAt(position).setCustomView(view);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new StatisticsFragment(), "ONE");
        adapter.addFrag(new StatisticsFragment(), "TWO");
        adapter.addFrag(new StatisticsFragment(), "THREE");
        adapter.addFrag(new StatisticsFragment(), "FOUR");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
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

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
