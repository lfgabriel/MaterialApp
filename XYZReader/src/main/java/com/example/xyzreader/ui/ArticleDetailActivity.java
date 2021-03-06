package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.ArticleTextFragment;
import com.example.xyzreader.util.AppBarStateChangeListener;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    public static final String ARG_ITEM_POSITION = "item_position";
    private static final String TAG = "ArticleDetailActivity";
    private TextView mTxtTitle, mTxtAuthor, mTxtDate;
    private ImageView mImageViewPhoto;
    private ViewPager mPager;
    private ArticleDetailsPagerAdapter mPagerAdapter;
    private Toolbar mToolbar;
    private SimpleTextSwitcher mSimpleTextSwitcherToolbarTitle;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private FrameLayout mFrameLayoutCollapsingToolbar;
    private AppBarLayout mAppBarLayout;
    private FloatingActionButton mFabShare;

    private Cursor mCursor;
    private int mPosition = 0;

    private boolean mMenuVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_detail);
        loadViews();

        // set toolbar
        setSupportActionBar(mToolbar);

        // don't show title on toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mPosition = getIntent().getExtras().getInt(ARG_ITEM_POSITION, 0);
            }
        }

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }

        });

        // app bar animations
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case COLLAPSED:
                        showToolbarContents();
                        break;
                    case EXPANDED:
                        hideToolbarContents();
                        break;
                }
            }
        });

        mTxtTitle.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                "Lora-Bold.ttf"));
        mTxtAuthor.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                "Lora-Regular.ttf"));
        mTxtDate.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                "Lora-Regular.ttf"));

        mFabShare.setOnClickListener(this);
        mFrameLayoutCollapsingToolbar.setOnClickListener(this);
    }

    private void loadViews() {
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mFrameLayoutCollapsingToolbar = (FrameLayout) findViewById(R.id.frame_layout_collapsing_toolbar);
        mTxtTitle = (TextView) findViewById(R.id.text_view_title);
        mTxtAuthor = (TextView) findViewById(R.id.text_view_author);
        mTxtDate = (TextView) findViewById(R.id.text_view_date);
        mImageViewPhoto = (ImageView) findViewById(R.id.image_view_photo);
        mPager = (ViewPager) findViewById(R.id.pager);
        mToolbar = (Toolbar) findViewById(R.id.up_container);
        mSimpleTextSwitcherToolbarTitle = (SimpleTextSwitcher) findViewById(R.id.simple_text_switcher_toolbar_title);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mFabShare = (FloatingActionButton) findViewById(R.id.fab_share);
    }

    private void setTitleData(int position) {
        mCursor.moveToPosition(position);
        Picasso.with(this)
                .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                .into(mImageViewPhoto);

        mCollapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
        mSimpleTextSwitcherToolbarTitle.setSwitcherText(mCursor.getString(ArticleLoader.Query.TITLE));

        mTxtTitle.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        mTxtAuthor.setText(mCursor.getString(ArticleLoader.Query.AUTHOR));
        mTxtDate.setText(DateUtils.getRelativeTimeSpanString(
                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString());
    }

    private void hideToolbarContents() {
        mMenuVisible = false;
        invalidateOptionsMenu();

        mFabShare.show();


        mSimpleTextSwitcherToolbarTitle.animate()
                .alpha(0.0f)
                .setDuration(300)
                .start();
    }

    private void showToolbarContents() {
        mMenuVisible = true;
        invalidateOptionsMenu();

        mFabShare.hide();


        mSimpleTextSwitcherToolbarTitle.animate()
                .alpha(1.0f)
                .setDuration(300)
                .start();

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        setTitleData(mPosition);

        // set pager
        mPagerAdapter = new ArticleDetailsPagerAdapter(getSupportFragmentManager(), cursor);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));
        mPager.setCurrentItem(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        mCursor.close();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.fab_share:
                onShare();
                break;
        }
    }

    private void onShare() {
        mCursor.moveToPosition(mPager.getCurrentItem());
        String title = mCursor.getString(ArticleLoader.Query.TITLE);
        String text = mCursor.getString(ArticleLoader.Query.BODY);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TITLE, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_using)));
        } else {
            Log.e(TAG, "No Intent available to handle action");
            Toast.makeText(this, R.string.no_app_available_to_share_content, Toast.LENGTH_LONG).show();
        }
    }

    private static class ArticleDetailsPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {
        private WeakReference<Cursor> mCursorWeakReference;

        public ArticleDetailsPagerAdapter(android.support.v4.app.FragmentManager fm, Cursor cursor) {
            super(fm);
            mCursorWeakReference = new WeakReference<Cursor>(cursor);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            mCursorWeakReference.get().moveToPosition(position);
            return ArticleTextFragment.newInstance(mCursorWeakReference.get().getString(ArticleLoader.Query.BODY));
        }

        @Override
        public int getCount() {
            return mCursorWeakReference.get().getCount();
        }
    }
}
