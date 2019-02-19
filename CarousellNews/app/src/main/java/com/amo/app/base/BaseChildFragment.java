package com.amo.app.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import me.yokeyword.fragmentation.SupportFragment;

public class BaseChildFragment extends SupportFragment {
    private ProgressDialog mProgressDialog;
    private Snackbar mSnackbar;

    public void showProgress(String msg) {
        hideProgress();
        mProgressDialog =  new ProgressDialog(_mActivity);
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void hideKeyboard() {
        View view = _mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) _mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showSnackBar(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public boolean isInternetConnectedOrConnecting() {
        ConnectivityManager cm = (ConnectivityManager) _mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        return network != null && network.isConnectedOrConnecting();
    }
}
