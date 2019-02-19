package com.amo.app.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orhanobut.logger.Logger;
import com.amo.app.R;
import com.amo.app.adapter.BaseModelAdapter;
import com.amo.app.base.BaseChildFragment;
import com.amo.app.entity.Card;
import com.amo.app.ui.mvp.OurModel;
import com.amo.app.ui.mvp.OurPresenter;
import com.amo.app.ui.mvp.OurView;

import java.util.Date;

public class FirstCardDetailFragment extends BaseChildFragment implements OurView {
    private final static String ARG_CARD_DETAIL = "ARG_CARD_DETAIL";

    private final static int HANDLER_MSG_WHAT_BASE                  = 10;
    private  final static int HANDLER_MSG_WHAT_INIT_CARD_SUCCESS     = HANDLER_MSG_WHAT_BASE + 3;
    private  final static int HANDLER_MSG_WHAT_INIT_CARD_FAILURE     = HANDLER_MSG_WHAT_BASE + 5;
    private  final static int HANDLER_MSG_WHAT_CANCEL                  = HANDLER_MSG_WHAT_BASE + 8;

    private final static int POP_RESULT_BASE                    = 20;
    public  final static int POP_RESULT_INIT_CARD_FAILURE       = POP_RESULT_BASE + 0;
    public  final static int POP_RESULT_INIT_COMMENT_FAILURE    = POP_RESULT_BASE + 1;
    public  final static int POP_RESULT_CANCEL                  = POP_RESULT_BASE + 3;

    public  final static int POP_RESULT_REMOVE_CARD_SUCCESS_COMMENT_SUCCESS = POP_RESULT_BASE + 6;
    public  final static int POP_RESULT_REMOVE_CARD_SUCCESS_COMMENT_FAILURE = POP_RESULT_BASE + 7;
    public  final static int POP_RESULT_REMOVE_CARD_FAILURE_COMMENT_NO_TRY  = POP_RESULT_BASE + 8;

    private FirstHomeFragment mFirstHomeFragment;
    private View mView;
    private ImageView mArticleImage;
    private TextView mArticleTitleView, mArticleContentView;
    private TextView mDateView;
    private TextView mRankView;
    private Button mSnackBtn;

    private Handler mHandler;
    /**
     * retrieves the card presented on this our view by invoking the presenter
     * to load a specified model from database and obtains the reference on the callback
     */
    private static Card mCard;

    private OnPop mOnPop;
    private int mResultCode = POP_RESULT_BASE; //used to pass a result to OnPop in response to user
    private static boolean is_debug = true;

    public static FirstCardDetailFragment newInstance(Card card) {
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        FirstCardDetailFragment fragment = new FirstCardDetailFragment();
        mCard = card;
        return fragment;
    }

    /**
     * once Fragment is returned from back stack, its View would be destroyed and recreated.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(">>>");
        mView = inflater.inflate(R.layout.app_first_child_detail, container, false);
        initHandler();
        initView();
        return mView;
    }

    /**
     *  If any property is belonged to View, do the state saving/restoring inside View
     *  through having implements on View#onSaveInstanceState() and View#onRestoreInstanceState().
     *  If any property is belonged to Fragment, do it inside Fragment
     *  through having implements on Fragment#onSaveInstanceState() and Fragment#onActivityCreated().
     */
    @Override
    public void onResume() {
        //Logger.d(">>>");
        super.onResume();
        if (getArguments().getParcelable(ARG_CARD_DETAIL) == null) Logger.e("!!! no card saved in argument of this fragment");
        //bindCardData();
        //bindCommentData();
    }

    @Override
    public void onAttach(Context context) {
        Logger.d(">>>");
        super.onAttach(context);
    }

    @Override
    public void onSupportVisible() {
        Logger.d(">>>");
        super.onSupportVisible();
    }

    @Override
    public void onSupportInvisible() {
        Logger.d(">>>");
        super.onSupportInvisible();
    }

    @Override
    public void onDestroyView() {
        Logger.d(">>>");
        super.onDestroyView();
    }

    /**
     * pops this child fragment by popping the last back stack
     * which's on the top of the mBackStack of child fragment manager and records the last operations of fragments
     *
     * first fragment invokes loadRootFragment() to create a child fragment manager and uses it to start child fragments including this
     * therefore, this fragment is managed by a child manger belonging to a parent manager which is managing the first fragment
     */
    @Override
    public void pop() {
        Logger.d(">>> mResultCode:" + mResultCode);
        super.pop();
        if (mOnPop != null) mOnPop.onPop(mResultCode);
    }

    @Override
    public boolean onBackPressedSupport() {
        Message message = new Message();
        message.what = HANDLER_MSG_WHAT_CANCEL;
        mHandler.sendMessage(message);
        /**
         * consumes this back press event at TransactionDelegate#dispatchBackPressedEvent()
         * and let handler trigger pop() which has been overridden by this class
         * instead of ending in popChild() on the parent fragment "FirstFragment"
         */
        return true;
    }

    public void setHomeFragment(FirstHomeFragment fragment) {
        this.mFirstHomeFragment = fragment;
    }

    public void setOnPop(OnPop onPop) {
        this.mOnPop = onPop;
    }

    @SuppressLint("HandlerLeak")
    private void initHandler() {
        mHandler = new UIHandler();
    }

    private void initView() {
        if (mView == null) {
            Logger.e("!!! mView is null");
            return;
        }
        mArticleImage = mView.findViewById(R.id.firstCardDetailImageView);
        mArticleTitleView = mView.findViewById(R.id.firstCardDetailArticleTitleTextView);
        mArticleContentView = mView.findViewById(R.id.firstCardDetailArticleContentTextView);
        mDateView = mView.findViewById(R.id.firstDetailCardArticleDateTextView);
        mRankView = mView.findViewById(R.id.firstDetailCardArticleRankTextView);
        mSnackBtn = mView.findViewById(R.id.firstCardDetailSnackBtn);
        bindCardData();
    }

    /**
     * synchronize some of data of this card with UI
     */
    private void bindCardData() {
        Logger.d(">>>");
        if (mCard == null) {
            Logger.e("!!! card is null");
            return;
        }
        if (mCard.getTitle() == null) {
            Logger.e("!!! title is null");
            return;
        }
        if (mCard.getDescription() == null) {
            Logger.e("!!! description is null");
            return;
        }
        if (mCard.getBannerUrl() == null) {
            Logger.e("!!! banner url is null");
        }

        Glide.with(_mActivity)
                .load(mCard.getBannerUrl())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .error(R.drawable.ic_stub)
                .into(mArticleImage);
        mArticleTitleView.setText(mCard.getTitle());
        mArticleContentView.setText(mCard.getDescription());

        Date now = new Date();
        //Logger.e("... now:" + now.getTime());
        //Logger.e("... card time:" + card.getTimeCreated());
        //Logger.e("... card time*1000:" + ((long)card.getTimeCreated())*1000);
        long interval_millisec  = now.getTime() - (((long)mCard.getTimeCreated())*1000);
        long interval_sec       = interval_millisec/1000;
        long interval_min       = interval_sec/(60);
        long interval_hr        = interval_min/(60);
        long interval_day       = interval_hr/(24);
        long interval_week      = interval_day/(7);
        long interval_month     = interval_week/(4);
        long interval_year      = interval_month/(12);

        StringBuffer buffer = new StringBuffer();
        do {
            if (interval_year >= 1) {
                buffer.append(interval_year);
                buffer.append(" years");
                buffer.append(" ago");
                break;
            }
            if (interval_month >= 1) {
                buffer.append(interval_month);
                buffer.append(" months");
                buffer.append(" ago");
                break;
            }
            if (interval_week >= 1) {
                buffer.append(interval_week);
                buffer.append(" weeks");
                buffer.append(" ago");
                break;
            }
            if (interval_day >= 1) {
                buffer.append(interval_day);
                buffer.append(" days");
                buffer.append(" ago");
                break;
            }
            if (interval_hr >= 1) {
                buffer.append(interval_hr);
                buffer.append(" hours");
                buffer.append(" ago");
                break;
            }
            if (interval_min >= 1) {
                buffer.append(interval_min);
                buffer.append(" mins");
                buffer.append(" ago");
                break;
            }
            buffer.append("just before");
            break;
        } while (false);
        mRankView.setText("rank " + mCard.getRank());
        mDateView.setText(buffer.toString());
    }



    public void showSnackBar(String msg) {
        showSnackBar(mSnackBtn, msg);
    }

    /**
     * callback of view as MVP design pattern holding jobs of view while presenter task is done
     */
    @Override
    public void feed(OurModel model, OurPresenter presenter) {
        
    }

    @Override
    public void onNotice(int result, String message) {

    }

    @Override
    public void onFetch(int result, Object pojo) {

    }

    @Override
    public void onLoad(int result, Object pojo) {

    }

    @Override
    public void onRemove(int result, int position, Object model) {

    }

    @Override
    public void onRecover(int result, int position, Object model) {

    }


    /**
     * callback for popping this fragment from back back
     */
    public interface OnPop {
        void onPop(int resultCode);
    }

    @SuppressLint("HandlerLeak")
    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MSG_WHAT_CANCEL:
                    mResultCode = POP_RESULT_CANCEL;
                    pop();
                    break;
                default:

                    break;
            }
        }
    }

    private static class InnerLogger {
        static void d(@Nullable Object object) {
            if (is_debug) com.orhanobut.logger.Logger.d(object);
        }

        static void e(@NonNull String message, @Nullable Object... args) {
            if (is_debug) com.orhanobut.logger.Logger.e(message, args);
        }
    }
}
