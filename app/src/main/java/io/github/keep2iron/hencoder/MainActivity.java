package io.github.keep2iron.hencoder;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            String[] title = new String[]{"即刻", "薄荷", "小米", "Flipboard"};
            int[] layoutRes = new int[]{R.layout.frag_jike, R.layout.frag_bohe,R.layout.frag_xiaomi};

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }

            @Override
            public Fragment getItem(int position) {
                return ViewFragment.getInstance(layoutRes[position]);
            }

            @Override
            public int getCount() {
                return layoutRes.length;
            }
        };
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
    }
}
