package com.amo.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amo.app.R;
import com.amo.app.base.BaseMainFragment;
import com.amo.app.ui.fragment.FirstHomeFragment;
import com.orhanobut.logger.Logger;

public class FirstFragment extends BaseMainFragment{
    private View mView;

    public FirstFragment() {
        Logger.d(">>>");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(">>>");
        //super.onCreateView(inflater, container, savedInstanceState);
        /**
         * exception thrown if attachToRoot is true
         */
        mView = inflater.inflate(R.layout.app_first_main, container, false);
        return mView;
    }

    /**
     * called when VisibleDelegate#onActivityCreated() if VisibleDelegate#mIsFirstVisible is true
     */
    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        Logger.d(">>>");
        super.onLazyInitView(savedInstanceState);
        /**
         * instantiates child fragment manager belonging to first main fragment
         */
        Logger.d("... going to loadRootFragment() for child fragment home");
        loadRootFragment(R.id.fl_first_container, FirstHomeFragment.newInstance());
        //Logger.e("!!! amo test: going to SupportHelper.getActiveFragment(getFragmentManager()) that includes child fragment manager");
        //SupportHelper.getActiveFragment(getFragmentManager());
    }

    @Override
    public void onPause() {
        Logger.d(">>>");
        super.onPause();
        //Logger.e("!!! amo test 1Pause: going to SupportHelper.getActiveFragment(getFragmentManager()) that includes child fragment manager");
        //SupportHelper.getActiveFragment(getFragmentManager());
    }

    @Override
    public void onResume() {
        //Logger.d(">>>");
        super.onResume();
        //Logger.e("!!! amo test 1Resume: going to SupportHelper.getActiveFragment(getFragmentManager()) that includes child fragment manager");
        //SupportHelper.getActiveFragment(getFragmentManager());
    }

    @Override
    public void onDestroyView() {
        Logger.d(">>>");
        super.onDestroyView();
        //Logger.e("!!! amo test 1DestroyView: going to SupportHelper.getActiveFragment(getFragmentManager()) that includes child fragment manager");
        //SupportHelper.getActiveFragment(getFragmentManager());
    }
}
