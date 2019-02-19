package com.amo.app;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentationMagician;
import android.os.Bundle;
import android.view.View;

import com.amo.app.base.BaseMainFragment;
import com.amo.app.ui.fragment.FirstFragment;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.util.List;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;
import me.yokeyword.fragmentation.SupportActivity;
import me.yokeyword.fragmentation.SupportFragment;

public class MainActivity extends SupportActivity implements BaseMainFragment.OnBackBaseListener {
    public static final int FIRST = 0;
    public static final int SECOND = 1;
    public static final int THIRD = 2;
    private SupportFragment[] mFragments = new SupportFragment[3];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.d(">>>");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_main);
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(1)         // (Optional) How many method line to show. Default 2
                .methodOffset(0)        // (Optional) Skips some method invokes in stack trace. Default 5
                .tag("APP")           // (Optional) Custom tag for each log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        if (findFragment(FirstFragment.class) == null) {
            Logger.d("... going to loadMultipleRootFragment()");
            mFragments[FIRST] = new FirstFragment();
            mFragments[SECOND] = null;
            mFragments[THIRD] = null;
            //loadMultipleRootFragment(R.id.fl_container, FIRST, mFragments[FIRST], mFragments[SECOND], mFragments[THIRD]);
            //mBottomBarFirst.setSelected(true);
            loadRootFragment(R.id.fl_container, mFragments[FIRST]);
        } else {
            mFragments[FIRST] = findFragment(FirstFragment.class);
            mFragments[SECOND] = null;
            mFragments[THIRD] = null;
        }

    }

    @Override
    public void onPause() {
        //Logger.d(">>>");
        super.onPause();
        //SupportHelper.getActiveFragment(getSupportFragmentManager());
    }

    @Override
    public void onResume() {
        //Logger.d(">>>");
        super.onResume();
        //SupportHelper.getActiveFragment(getSupportFragmentManager());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Logger.d(">>>");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        Logger.d(">>>");
        Logger.clearLogAdapters();

        /*
        try {
            CardManager.getInstance(this).unsubscribe();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            CommentManager.getInstance(this).unsubscribe();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        */

        super.onDestroy();
    }

    @Override
    public void onBackToFirstMainFragment() {
        Logger.d(">>>");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
}
