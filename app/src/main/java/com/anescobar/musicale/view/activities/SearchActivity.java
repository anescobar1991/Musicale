package com.anescobar.musicale.view.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.anescobar.musicale.R;
import com.anescobar.musicale.view.fragments.SearchFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SearchActivity extends BaseActivity {
    private static final String SEARCH_FRAGMENT_TAG = "searchFragment";

    @InjectView(R.id.musicale_toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ButterKnife.inject(this);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        addFragmentToActivity(R.id.container, new SearchFragment(), SEARCH_FRAGMENT_TAG);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}