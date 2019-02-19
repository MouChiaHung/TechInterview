package com.amo.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.amo.app.R;
import com.amo.app.base.BaseChildFragment;
import com.amo.app.entity.Card;
import com.amo.app.entity.CardList;
import com.amo.app.manager.CardManager;
import com.amo.app.ui.fragment.FirstHomeFragment;
import com.amo.app.ui.mvp.OurPresenter;
import com.amo.app.ui.mvp.OurView;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * adapter provides a binding from data set synchronized with database to views displayed within a RecyclerView
 */
public class CardAdapter extends BaseModelAdapter<Card> implements OurPresenter<OurView> {
    private BaseChildFragment mFragment;
    private LayoutInflater mInflater;
    private CardClickListener mCardClickListener;
    private MoreClickListener mMoreClickListener;
    private CardLoader mCardLoader;
    private final static int ITEM_TYPE_BASE = 10;
    private final static int ITEM_TYPE_UNKNOWN = ITEM_TYPE_BASE + 1;
    private final static int ITEM_TYPE_NORMAL = ITEM_TYPE_BASE + 2;
    private final static int ITEM_TYPE_UNDER_THE_CUT = ITEM_TYPE_BASE + 3;
    public final static String CARD_ID_UNDER_THE_CUT = "UNDER_THE_CUT";

    /*
    / **
     * result passed to our view on the OurView#onLoad()
     * /
    private final static int RESULT_BASE    = 20;
    public final static int RESULT_SUCCESS = RESULT_BASE + 1;
    public final static int RESULT_FAILURE = RESULT_BASE + 2;
    */

    /**
     * notification passed to our view on the OurView#onNotice()
     */
    public final static int PROGRESS_ON  = 0;
    public final static int PROGRESS_OFF = 1;
    public final static int HANDLER_GO_SUCCESS = 2;
    public final static int HANDLER_GO_FAILURE = 3;

    /**
     * start and end of a specific period
     */
    //public final static long DEFAULT_PERIOD_START = System.currentTimeMillis() - 14*24*60*60*1000;
    //public final static long DEFAULT_PERIOD_START = 0; //only Date works...
    //public final static long DEFAULT_PERIOD_END = System.currentTimeMillis();
    private final static long DEFAULT_PERIOD = (long) (7.0 * 24.0 * 3600.0 * 1000.0); //only Date works...
    private final static int DEFAULT_TIME_COST = 500; //500 ms for investigating how many cards to be read this time
    private final static int DEFAULT_TIME_WAIT = 2500; //2000 ms for waiting to start counting since an invocation

    private OurView mOurView; //FirstHomeFragment
    private Context mContext;
    private Thread mMVPThread;
    private final Object lock = new Lock();
    private Map<Long, Worker> mWorkers;
    private int mResult = BaseModelAdapter.RESULT_FAILURE;

    /**
     * earliest date of cards in a specific period
     */
    private long earliest_date = CardList.DEFAULT_LAST_DATE;
    private long start_date;
    private long end_date;
    
    /**
     * for inner class
     */
    private CardAdapter instance;
    private Map<String, Long> mapUserIdAndCardCount; //map recording the count of cards of each user
    private Map<String, Integer> mapUserIdAndCardCountRead = new HashMap<>(); //count of cards read from database of each user
    private int all_card_count = 0; //count of cards of all users in a specific period
    private int all_card_count_read = 0; // //count of cards read of all users from database in a specific period

    public CardAdapter(Context context, BaseChildFragment fragment) {
        super(context);
        mContext = context;
        mFragment = fragment;
        mInflater = LayoutInflater.from(context);
        instance = this;
    }

    /**
     * involves inflating a layout from XML and then returning it to the holder
     * when RecyclerView needs a new ViewHolder to represent item
     *
     * a new ViewHolder will be constructed with an inflated View which represents the item
     * by either creating a new View manually or inflating it from an XML layout file.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder view_holder = null;
        View item_view;
        switch (viewType) {
            case ITEM_TYPE_UNKNOWN:
                break;
            case ITEM_TYPE_NORMAL:
                item_view = mInflater.inflate(R.layout.item_first_home_card, parent, false);
                view_holder = new CardViewHolder(item_view);
                break;
            case ITEM_TYPE_UNDER_THE_CUT:
                item_view = mInflater.inflate(R.layout.item_first_home_more, parent, false);
                view_holder = new UnderTheCutViewHolder(item_view);
                break;
        }
        return view_holder;
    }

    /**
     * involves populating data into item through holder at the position (which is positioned by layout manager)
     * when RecyclerView displays of updates the UI with the data through the holder referring to the given position
     *
     * position is the index of one of items within the adapter's data set
     *
     * this method will synchronize data set with database database if the wanted position is beyond the length of data set
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Logger.d(">>> about the position:" + position);
        if (position >= getItemCount()) {
            Logger.d("... position(" + position + ")" + " >= size of data set(" + getItemCount()+  ")" + " and going to load");
            if (mFragment.isInternetConnectedOrConnecting()) {
                Logger.d("... going to onNeedLoad()");
                mCardLoader.onNeedLoad(getItemsOfDataSet());
            } else {
                ((FirstHomeFragment) mFragment).showSnackBar("沒網路耶...");
            }
        }
        switch (holder.getItemViewType()) {
            case ITEM_TYPE_UNKNOWN:
                break;
            case ITEM_TYPE_NORMAL:
                ((CardViewHolder) holder).bindData(getItemOfDataSet(position));
                break;
            case ITEM_TYPE_UNDER_THE_CUT:
                ((UnderTheCutViewHolder) holder).bindData(getItemOfDataSet(position));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        Card card = getItemOfDataSet(position);
        if (card == null) return ITEM_TYPE_UNKNOWN;
        if (card.getId().equals(CARD_ID_UNDER_THE_CUT)) return ITEM_TYPE_UNDER_THE_CUT;
        else return ITEM_TYPE_NORMAL;
    }

    public void setCardClickListener(CardClickListener listener) {
        mCardClickListener = listener;
    }

    public void setMoreClickListener(MoreClickListener listener) {
        mMoreClickListener = listener;
    }

    public void setCardLoadListener(CardLoader listener) {
        mCardLoader = listener;
    }

    @Override
    public void attach(OurView view) {
        Logger.d(">>>");
        if (view == null) {
            Logger.e("!!! view is null");
            return;
        }
        mOurView = view;
    }

    @Override
    public void detach(OurView view) {
        Logger.d(">>>");
        if (mOurView != view) {
            Logger.e("!!! mOurView doesn't refer to same one as view");
            return;
        }
        if (mOurView == null) {
            return;
        }
        mOurView = null;
    }

    /**
     * fethches and sorts cards based on mode which is time or rank
     */
    @Override
    public void load(int mode) {
        Logger.d(">>>");
        if (mMVPThread != null) {
            if (mMVPThread.isAlive()) {
                Logger.d("... previous loading task is still running but going to interrupt it");
                mMVPThread.interrupt();
            } else {
                mMVPThread = null;
            }
        }
        mMVPThread = new Thread(new LoadTask(mode));
        mMVPThread.start();
    }

    /**
     * fethches and sorts cards based on mode which is time or rank
     */
    @Override
    public void fetch(int mode) {
        Logger.d(">>>");
        if (mMVPThread != null) {
            if (mMVPThread.isAlive()) {
                Logger.d("... previous loading task is still running but going to interrupt it");
                mMVPThread.interrupt();
            } else {
                mMVPThread = null;
            }
        }
        mMVPThread = new Thread(new FetchTask(mode));
        mMVPThread.start();
    }

    @Override
    public void remove(int position) {

    }

    @Override
    public void recover(int position) {

    }

    @Override
    public void cancel(boolean cancel) {
        if (mWorkers == null) {
            Logger.e("!!! mWorkers is null");
            return;
        }
        if (!cancel) {
            Logger.e("!!! do nothing when cancel goes with false");
            return;
        }
        for (Map.Entry<Long, Worker> entry : mWorkers.entrySet()) {
            Worker worker = entry.getValue();
            if (worker.getThread() != null) {
                if (worker.getThread().isAlive()) {
                    Logger.d("... going to cancel on the worker and others will break:" + worker.getStamp());
                    worker.cancel();
                    break;
                } else {
                    Logger.d("... worker has completed its task:" + worker.getStamp());
                }
            } else {
                Logger.e("!!! work get thread returned null:" + worker.getStamp());
            }
        }
    }

    /**
     * makes a card used to create an item of under the cut type
     */
    private Card makeCardAsUnderTheCut() {
        Card card = new Card();
        card.setId(CardAdapter.CARD_ID_UNDER_THE_CUT);
        card.setTimeCreated((int)start_date);
        return card;
    }

    /**
     * synchronizes the data set of adapter all the time with database and notifies recycler view of changes
     */
    private void readCards(final BaseModelAdapter adapter, int mode) throws ClassNotFoundException {
        if (adapter == null) return;
        mOurView.onNotice(PROGRESS_ON, "Loading...");
        CardManager.getInstance(mContext).subscribeAllCards(new CardSubscriber() {
            @Override
            public void onClone(List cards) {
                Logger.d(">>>");
                mOurView.onNotice(PROGRESS_OFF, null);
                if (adapter instanceof CardAdapter) {
                    ((CardAdapter) adapter).setItemsAsDataSet(cards);
                } else {
                    Logger.d("!!! adapter is not card adapter");
                }
                mResult = RESULT_SUCCESS;
                if (mMVPThread != null && mMVPThread.isAlive()) {
                    Logger.d("... going to interrupt mvp thread");
                    mMVPThread.interrupt();
                }
            }

            @Override
            public void onError(String error) {
                Logger.e("!!! error:" + error);
                mResult = RESULT_FAILURE;
                if (mMVPThread != null && mMVPThread.isAlive()) {
                    Logger.d("... going to interrupt mvp thread");
                    mMVPThread.interrupt();
                }
            }
        }, mode);
    }

    public void clearCards() {
        removeItemsFromDataSet(BaseModelAdapter.REMOVE_POSITION_AT_FIRST, BaseModelAdapter.REMOVE_POSITION_AT_LAST);
    }

    /**
     * callback for triggering responses to UI that a card item within RecyclerView is clicked
     */
    public interface CardClickListener {
        void onCardClick(Card card, View item_view, int item_index);
    }

    /**
     * callback for triggering responses to UI that the under the cut item within RecyclerView is clicked
     */
    public interface MoreClickListener {
        void onMoreClick(Card card, View item_view);
    }

    /**
     * callback for triggering responses to UI that adapter needs to synchronize data set with database
     */
    public interface CardLoader {
        void onNeedLoad(List<Card> cards);
    }

    /**
     * callback for card manager in response to user and update the data set of adapter
     */
    class CardSubscriber implements CardManager.Subscriber {
        @Override
        public void onClone(List cards) {
        }

        @Override
        public void onError(String error) {
        }
    }

    private interface CountListener {
        void onCounting(Map<String, Long> parentIdsAndSiblingCounts);
    }

    /**
     * a ViewHolder describes an item view and metadata about its place within the RecyclerView
     */
    private class CardViewHolder extends RecyclerView.ViewHolder {
        private TextView mArticleTitleView, mArticleContentView;
        private ImageView mArticlePhotoView;
        private TextView mDateView;
        private TextView mRankView;

        private CardViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new OnItemViewClick());
            mArticleTitleView = itemView.findViewById(R.id.firstHomeCardArticleTitleTextView);
            mArticleContentView = itemView.findViewById(R.id.firstHomeCardArticleContentTextView);
            mArticlePhotoView = itemView.findViewById(R.id.firstHomeCardArticlePhotoImageView);
            mDateView = itemView.findViewById(R.id.firstHomeCardArticleDateTextView);
            mRankView = itemView.findViewById(R.id.firstHomeCardArticleRankTextView);
        }

        /**
         *  configures the individual RecyclerView.ViewHolder object and loads it with actual data that need to be displayed
         */
        private void bindData(Card card) {

            ViewCompat.setTransitionName(mArticleTitleView, String.valueOf(getAdapterPosition()) + "_tv");
            ViewCompat.setTransitionName(mArticleContentView, String.valueOf(getAdapterPosition()) + "_tv");
            ViewCompat.setTransitionName(mArticlePhotoView, String.valueOf(getAdapterPosition()) + "_image");
            ViewCompat.setTransitionName(mDateView, String.valueOf(getAdapterPosition()) + "_tv");

            mArticleTitleView.setText(card.getTitle());
            mArticleContentView.setText(card.getDescription());

            if (card.getBannerUrl() != null ) {
                String article_photo = card.getBannerUrl();
                mArticlePhotoView.setVisibility(View.VISIBLE);
                Glide.with(getContext())
                        .load(article_photo)
                        .fitCenter()
                        .override(getSize(getContext()).x, getSize(getContext()).y)
                        //.override(300, 300)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .error(R.drawable.ic_stub)
                        .into(mArticlePhotoView);
            } else {
                mArticlePhotoView.setVisibility(View.GONE);
                ((ViewGroup)itemView).removeView(mArticlePhotoView);
            }

            Date now = new Date();
            //Logger.e("... now:" + now.getTime());
            //Logger.e("... card time:" + card.getTimeCreated());
            //Logger.e("... card time*1000:" + ((long)card.getTimeCreated())*1000);
            long interval_millisec  = now.getTime() - (((long)card.getTimeCreated())*1000);
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
            mRankView.setText("rank " + card.getRank());
            mDateView.setText(buffer.toString());
        }

        private Point getSize(Context context) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size;
        }

        class OnItemViewClick implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                if(getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Logger.d(">>> click position:" + getAdapterPosition()); //gets item position
                } else {
                    Logger.e("!!! no position");
                }
                if(mCardClickListener != null) {
                    Card card_clicked = getItemOfDataSet(getAdapterPosition());
                    mCardClickListener.onCardClick(card_clicked, v, getAdapterPosition());
                }
            }
        }
    }

    /**
     * a ViewHolder describes an item view and metadata about its place within the RecyclerView
     */
    private class UnderTheCutViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private ImageView mImageView;

        private UnderTheCutViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new OnItemViewClick());
            mTextView = itemView.findViewById(R.id.firstHomeUnderTheCutTextView);
            mImageView = itemView.findViewById(R.id.firstHomeUnderTheCutImageView);
        }

        /**
         *  configures the individual RecyclerView.ViewHolder object and loads it with actual data that need to be displayed
         */
        @SuppressLint("SetTextI18n")
        private void bindData(Card card) {
            ViewCompat.setTransitionName(mTextView, String.valueOf(getAdapterPosition()) + "_tv");
            ViewCompat.setTransitionName(mImageView, String.valueOf(getAdapterPosition()) + "_image");
            mTextView.setVisibility(View.VISIBLE);
            //Logger.e("... interval_day   time:" + interval_day);
            long interval_millisec  = new Date().getTime() - card.getTimeCreated()*1000;
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
            mTextView.setText("按一下看更多，目前爬到" + buffer.toString());
            mImageView.setVisibility(View.VISIBLE);
        }

        class OnItemViewClick implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                if(getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Logger.d(">>> click position:" + getAdapterPosition()); //gets item position
                } else {
                    Logger.e("!!! no position");
                }
                if(mMoreClickListener != null) {
                    Card card_clicked = getItemOfDataSet(getAdapterPosition());
                    mMoreClickListener.onMoreClick(card_clicked, v);
                }
            }
        }
    }

    private static final class Lock {}

    /**
     * reads card from database in the default period and display on UI
     */
    private class FetchTask implements Runnable {
        private int mode;

        public FetchTask(int mode) {
            this.mode = mode;
        }

        @Override
        public void run() {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                try {
                                    readCards(instance, mode);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 0);
                    }
                }).start();
                Logger.d("... going to wait");
                synchronized (lock) { //becomes the owner of the object's monitor
                    lock.wait(); //relinquishes the ownership of the object's monitor
                }
            } catch (InterruptedException e) {
                Logger.d("... got InterruptedException and proceed");
                if (mOurView != null) {
                    Logger.d("... going to call onFetch(" + mResult + ", " + all_card_count +") on the our view (FirstHomeFragment)");
                    mOurView.onFetch(mResult, all_card_count_read);
                }
            }
            Logger.d("... task done and now count of items:" + getItemCount());
        }
    }

    /**
     * reads card from database in the default period and display on UI
     */
    private class LoadTask implements Runnable {
        private int mode;

        public LoadTask(int mode) {
            this.mode = mode;
        }

        @Override
        public void run() {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                try {
                                    readCards(instance, mode);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 0);
                    }
                }).start();
                Logger.d("... going to wait");
                synchronized (lock) { //becomes the owner of the object's monitor
                    lock.wait(); //relinquishes the ownership of the object's monitor
                }
            } catch (InterruptedException e) {
                Logger.d("... got InterruptedException and proceed");
                if (mOurView != null) {
                    Logger.d("... going to call onLoad(" + mResult + ", " + all_card_count +") on the our view (FirstHomeFragment)");
                    mOurView.onLoad(mResult, all_card_count_read);
                }
            }
            Logger.d("... task done and now count of items:" + getItemCount());
        }
    }
    
    private class Worker {
        Thread thread;
        long stamp;

        private Worker(Thread thread, long time_stamp) {
            this.thread = thread;
            this.stamp = time_stamp;
        }

        private long getStamp() {
            return stamp;
        }

        private Thread getThread() {
            return thread;
        }

        private void start() {
            if (thread != null) {
                Logger.d("... going to worker start:" + stamp);
                thread.start();
            }
        }

        private void cancel() {
        }
    }
}
