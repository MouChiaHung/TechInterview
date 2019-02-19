package com.amo.app.base;

import android.app.Activity;

import com.orhanobut.logger.Logger;

import me.yokeyword.fragmentation.SupportFragment;

public class BaseMainFragment extends SupportFragment {
    private OnBackBaseListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnBackBaseListener) {
           mListener = (OnBackBaseListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + "didn't implement OnBackToFirstMainFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * called by SupportActivityDelegate.onBackPressed() that asks parent fragment manager(belonging to activity)
     * to find the top of its active fragment and call that fragment's override onBackPressedSupport() to deal with back pressed event
     * In my arch. there're only three fragments "FirstFragment", "SecondFragment"
     * and "ThirdFragment" managed by parent fragment manager(belonging to activity)
     */
    @Override
    public boolean onBackPressedSupport() {
        Logger.d(">>>");
        //super.onBackPressedSupport();
        if (getChildFragmentManager().getBackStackEntryCount() > 1) {
            Logger.d("... going to popChild() because getChildFragmentManager().getBackStackEntryCount()>1:" + getChildFragmentManager().getBackStackEntryCount());
            popChild(); //firstly removes top fragment and commit (for not stored in back stack), secondly runs getChildFragmentManager().popBackStack() (for stored in back stack)
        } else {
            Logger.d("... going to custom callback because getChildFragmentManager().getBackStackEntryCount()<=1:" + getChildFragmentManager().getBackStackEntryCount());
            /*
            if (this instanceof FirstFragment) {
                //Re-creating of EnterActivity leads to re-login error
                //Logger.d("... going to _mActivity.finish()");
                //_mActivity.finish();
            }
            */
            if (mListener != null) {
                Logger.d("... going to custom callback in response to user");
                mListener.onBackToFirstMainFragment();
            } else {
                Logger.e("!!! custom callback is null");
            }
        }
        Logger.d("... return true");
        return true;
    }

    public interface OnBackBaseListener {
        void onBackToFirstMainFragment();
    }
}
