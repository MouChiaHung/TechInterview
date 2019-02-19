package com.amo.app.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.amo.app.R;
import com.amo.app.adapter.BaseModelAdapter;
import com.amo.app.adapter.CardAdapter;
import com.amo.app.base.BaseChildFragment;
import com.amo.app.entity.Card;
import com.amo.app.manager.CardManager;
import com.amo.app.ui.mvp.OurModel;
import com.amo.app.ui.mvp.OurPresenter;
import com.amo.app.ui.mvp.OurView;
import com.orhanobut.logger.Logger;


import java.util.Date;
import java.util.List;

import me.yokeyword.eventbusactivityscope.EventBusActivityScope;

public class FirstHomeFragment extends BaseChildFragment implements SwipeRefreshLayout.OnRefreshListener, OurView {
    private final static int HANDLER_MSG_WHAT_BASE   = 10;
    private final static int HANDLER_MSG_WHAT_CANCEL = HANDLER_MSG_WHAT_BASE + 5;
    private final static int POP_RESULT_BASE   = 30;
    public  final static int POP_RESULT_CANCEL = POP_RESULT_BASE + 1;

    @SuppressLint("StaticFieldLeak")
    private static FirstHomeFragment instance;
    @SuppressLint("StaticFieldLeak")
    private Handler mHandler;
    private View mView;
    private FloatingActionButton mAddCardBtn;
    private RecyclerView mCardRCV;
    private SwipeRefreshLayout mRefresh;
    private OnPop mOnPop;
    private int mResultCode;

    private Toolbar mToolbar;

    private CardAdapter mCardAdapter;
    private CardGenerator mCardGeneratorAction; //FirstAddCardFragment acts as CardGenerator

    private int mSortMode = CardManager.SORT_MODE_TIME;

    /**
     * gets a singleton object about this class
     * should be called before other methods
     */
    public static FirstHomeFragment newInstance() {
        Bundle args = new Bundle();
        FirstHomeFragment fragment = new FirstHomeFragment();
        fragment.setArguments(args);
        instance = fragment;
        return fragment;
    }

    public void setOnPop(OnPop onPop) {
        this.mOnPop = onPop;
    }


    public CardAdapter getCardAdapter() {
        return mCardAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(">>>");
        mView = inflater.inflate(R.layout.app_first_child_home, container, false);
        //EventBusActivityScope.getDefault(_mActivity).register(this);
        initHandler();
        initView();
        initToolbar();
        initCardAdapter();
        if (mCardAdapter != null) mCardAdapter.attach(this);
        initCardRecyclerView();
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Logger.d(">>>");
        super.onActivityCreated(savedInstanceState);
        pullCards();
    }

    @Override
    public void onPause() {
        Logger.d(">>>");
        super.onPause();
        //Logger.e("!!! amo test 1HomePause: going to SupportHelper.getActiveFragment(getFragmentManager()) that includes child fragment manager");
        //SupportHelper.getActiveFragment(getFragmentManager());
    }

    @Override
    public void onResume() {
        //Logger.d(">>>");
        super.onResume();
        //pullCards();
        //Logger.e("!!! amo test 1HomeResume: going to SupportHelper.getActiveFragment(getFragmentManager()) that includes child fragment manager");
        //SupportHelper.getActiveFragment(getFragmentManager());
    }

    @Override
    public void onDestroyView() {
        Logger.d(">>>");
        super.onDestroyView();
        EventBusActivityScope.getDefault(_mActivity).unregister(this);
        if (mCardAdapter != null) mCardAdapter.detach(this);
        mCardRCV.clearOnScrollListeners();
        //Logger.e("!!! amo test 1HomeDestroyView: going to SupportHelper.getActiveFragment(getFragmentManager()) that includes child fragment manager");
        //SupportHelper.getActiveFragment(getFragmentManager());
    }

    @Override
    public void onRefresh() {
        Logger.d(">>>");
        pullCards();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mResultCode == POP_RESULT_CANCEL) {

                }
            }
        }).start();
        super.pop();
        if (mOnPop != null) mOnPop.onPop(mResultCode);
    }

    /**
     * consumes this back press event at TransactionDelegate#dispatchBackPressedEvent()
     * and let handler trigger pop() which has been overridden by this class
     * instead of ending in popChild() on the parent fragment "FirstFragment"
     */
    @Override
    public boolean onBackPressedSupport() {
        Message msg = new Message();
        msg.what = HANDLER_MSG_WHAT_CANCEL;
        mHandler.sendMessage(msg);
        return true;
    }

    private void initView() {
        mRefresh = mView.findViewById(R.id.homeRefresh);
        mRefresh.setColorSchemeResources(R.color.colorPrimary);
        mRefresh.setOnRefreshListener(this);
    }

    private void initHandler() {
        mHandler = new UIHandler();
    }

    @SuppressLint("ResourceAsColor")
    private void initToolbar() {
        mToolbar = mView.findViewById(R.id.homeToolbar);
        mToolbar.inflateMenu(R.menu.first_home_tool_bar);
        mToolbar.setTitleMarginStart(120); //in pixels
        mToolbar.setTitle(R.string.toolbar_name);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.options:
                        final PopupMenu popupMenu = new PopupMenu(_mActivity, mToolbar, GravityCompat.END);
                        popupMenu.inflate(R.menu.first_home_tool_bar_pop_menu);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.option_recent:
                                        mSortMode = CardManager.SORT_MODE_TIME;
                                        pullCards();
                                        popupMenu.dismiss();
                                        break;
                                    case R.id.option_popular:
                                        mSortMode = CardManager.SORT_MODE_RANK;
                                        pullCards();
                                        popupMenu.dismiss();
                                        break;
                                }
                                return true;
                            }
                        });
                        popupMenu.show();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        mToolbar.getMenu().findItem(R.id.options).setVisible(true);
    }

    private void pullCards() {
        Logger.d(">>>");
        if (mCardAdapter == null) return;
        mCardAdapter.clearCards();
        syncCardData(mSortMode);
    }

    private void moreCards() {
        Logger.d(">>>");
        if (mCardAdapter == null) return;
        moreCardData(mSortMode);
    }

    /**
     * just to synchronize the present data set of adapter with the display on UI
     * this method triggers CardAdapter#onBindViewHolder()
     * which leads the recycler view to carries out its updating on its own
     * therefore, don't call this method in CardAdapter#onBindViewHolder() for avoiding endless loops
     */
    private void bindCardData() {
        if (mCardAdapter == null) {
            Logger.e("!!! adapter is null");
            return;
        }
        mCardAdapter.notifyDataSetChanged();
    }

    /**
     * synchronizes the data set of adapter in a specific period (defined in presenter) with database and display on UI
     */
    private void syncCardData(int mode) {
        if (mCardAdapter == null) {
            Logger.e("!!! adapter is null");
            return;
        }
        mCardAdapter.load(mode);
    }

    /**
     * synchronizes the data set of adapter in a early period (defined in presenter) with database and display on UI
     */
    private void moreCardData(int mode) {
        if (mCardAdapter == null) {
            Logger.e("!!! adapter is null");
            return;
        }
        mCardAdapter.fetch(mode);
    }

    private void initCardAdapter() {
        mCardAdapter = new CardAdapter(_mActivity, this);
        mCardAdapter.setCardClickListener(new CardClickListener());
        mCardAdapter.setMoreClickListener(new MoreClickListener());
        mCardAdapter.setCardLoadListener(new CardLoader());
        readCardsFromMocks(mCardAdapter.getItemsOfDataSet());
    }

    private void initCardRecyclerView() {
        mCardRCV = mView.findViewById(R.id.homeRCV);
        //mLayoutManager = new LinearLayoutManager(_mActivity);
        LinearLayoutManager mLayoutManager = new CardsLayoutManager(_mActivity);
        mLayoutManager.setAutoMeasureEnabled(false);
        mCardRCV.setHasFixedSize(false);
        mCardRCV.setLayoutManager(mLayoutManager);
        mCardRCV.setAdapter(mCardAdapter);
        mCardRCV.addOnScrollListener(new OnRCVScroll());
        //mCardRCV.setNestedScrollingEnabled(false);
        //mCardRCV.addItemDecoration(new DividerItemDecoration(_mActivity, mLayoutManager.getOrientation()));
        //mCardRCV.getRecycledViewPool().setMaxRecycledViews(0, 0);
    }

    /**
     * for testing and fixing the issue
     * that RecyclerView.Adapter#onBindViewHolder() won't be invoked if RecyclerView.Adapter#getItemCount() return 0
     */
    private void readCardsFromMocks(List<Card> cards) {
        Card mock;
        String id;
        String title;
        String description;
        String banner;
        int time;
        int rank;
        for (int i = 0; i < 1; i++) {
            id           = "ID" + i;
            title        = "";
            description  = "";
            banner       = String.valueOf(R.drawable.ic_stub);
            time         = -1;
            rank         = 100 + i * 100;
            mock = new Card(id, title, description, banner, time, rank);
            //CardsDao.getInstance(_mActivity).add(mock);
            cards.add(mock);
        }
        //CardsDao.getInstance(_mActivity).find(0, cards);
    }

    public void showSnackBar(String msg) {
        showSnackBar(mAddCardBtn, msg);
    }

    /**
     * orientates recycler view along the edge of the item at specified posion
     */
    private void orientateRecyclerView(RecyclerView rcv, int position) {
        if (rcv == null) return;
        if (position < 0) return;
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rcv.getLayoutManager();
        if (linearLayoutManager == null) return;
        int first = linearLayoutManager.findFirstVisibleItemPosition();
        int last = linearLayoutManager.findLastVisibleItemPosition();
        if (position < first) {
            rcv.smoothScrollToPosition(first);
        } else if (position > last) {
            rcv.smoothScrollToPosition(last);
        } else {
            rcv.smoothScrollBy((rcv.getChildAt(position-first)).getLeft(), 0);
        }
    }

    /**
     * refers to a well-implemented generator representing an action of generating a card which is called by home fragment
     * <p>
     * since user creates a card on another fragment which acts as a generator
     * so we should pass generator fragment a reference to home fragment to call this method outside
     */
    public void enqueueAction(CardGenerator action) {
        mCardGeneratorAction = action;
    }

    /**
     * executes action done by main thread to have generator add a new card for this home fragment
     * <p>
     * since user creates a card on another fragment which acts as a generator
     * so we should pass generator fragment a reference to home fragment to call this method outside
     */
    public void executeAction() {
        Runnable mExecCommit = new Runnable() {
            @Override
            public void run() {
                mCardGeneratorAction.generateCards(mCardAdapter.getItemsOfDataSet());
            }
        };
        mHandler.post(mExecCommit);
    }

    /**
     * executes generator immediately to add a new card for this home fragment
     * <p>
     * since user creates a card on another fragment which acts as a generator
     * so we should pass generator fragment a reference to home fragment to call this method outside
     */
    public void generateCardImmediate(CardGenerator cardGenerator) {
        cardGenerator.generateCards(mCardAdapter.getItemsOfDataSet());
        bindCardData();
    }

    @Override
    public void feed(OurModel model, OurPresenter presenter) {

    }

    @Override
    public void onNotice(int result, String message) {
        if (result == CardAdapter.PROGRESS_ON && message != null) showProgress(message);
        else if (result == CardAdapter.PROGRESS_OFF) hideProgress();
    }

    /**
     * passed from CardAdapter, pojo means the count of card just read from database in the early period
     */
    @Override
    public void onFetch(int result, Object pojo) {
        switch (result) {
            case BaseModelAdapter.RESULT_SUCCESS:
                Logger.d("... got BaseModelAdapter.RESULT_SUCCESS and let RCV scroll to " + (mCardRCV.getAdapter().getItemCount()-((int)pojo)));
                if (mRefresh.isRefreshing()) mRefresh.setRefreshing(false);
                mCardRCV.smoothScrollToPosition(mCardRCV.getAdapter().getItemCount()-((int)pojo));
                break;
            case BaseModelAdapter.RESULT_FAILURE:
                Logger.e("!!! got BaseModelAdapter.RESULT_FAILURE");
                if (mRefresh.isRefreshing()) mRefresh.setRefreshing(false);
                break;
            default:
                break;
        }
    }

    /**
     * passed from CardAdapter, pojo is barely null
     */
    @Override
    public void onLoad(int result, Object pojo) {
        switch (result) {
            case BaseModelAdapter.RESULT_SUCCESS:
                Logger.d("... got BaseModelAdapter.RESULT_SUCCESS and let RCV scroll to 0");
                if (mRefresh.isRefreshing()) mRefresh.setRefreshing(false);
                mCardRCV.smoothScrollToPosition(BaseModelAdapter.POSITION_AT_FIRST);
                break;
            case BaseModelAdapter.RESULT_FAILURE:
                Logger.e("!!! got BaseModelAdapter.RESULT_FAILURE");
                if (mRefresh.isRefreshing()) mRefresh.setRefreshing(false);
                break;
            /*
            case CardAdapter.HANDLER_GO_SUCCESS:
                Message msg1 = new Message();
                msg1.what = HANDLER_MSG_WHAT_SYNC_CARD_SUCCESS;
                mHandler.sendMessage(msg1);
                break;
            case CardAdapter.HANDLER_GO_FAILURE:
                Message msg2 = new Message();
                msg2.what = HANDLER_MSG_WHAT_SYNC_CARD_FAILURE;
                mHandler.sendMessage(msg2);
                break;
            */
            default:
                break;
        }
    }

    @Override
    public void onRemove(int result, int position, Object model) {

    }

    @Override
    public void onRecover(int result, int position,Object model) {

    }

    /**
     * a generator implemented in order to add a new card for the recycler adapter of home fragment
     * home fragment calls generator when to add a new card into its adapter
     */
    interface CardGenerator {
        void generateCards(List<Card> cards);
    }

    /**
     * callback for popping this fragment from back back
     */
    public interface OnPop {
        void onPop(int resultCode);
    }

    /**
     * callback for card adapter to start card detail fragment
     */
    class CardClickListener implements CardAdapter.CardClickListener {
        @Override
        public void onCardClick(Card card, View item_view, int item_index) {
            Logger.d("... clicked card id:" + card.getId() + " and going to start card detail fragment");

            if (item_view.isClickable()) {
                FirstCardDetailFragment cardDetailFragment = FirstCardDetailFragment.newInstance(card);
                cardDetailFragment.setHomeFragment(instance);
                if (cardDetailFragment == null) return;
                cardDetailFragment.setOnPop(new CardDetailFragmentOnPop());
                start(cardDetailFragment);
            }
        }
    }

    /**
     * callback for card adapter to invoke it to load more cards in a specific period
     */
    class MoreClickListener implements CardAdapter.MoreClickListener {
        @Override
        public void onMoreClick(Card card, View item_view) {
            Logger.d("... clicked under the cut date:" + new Date(card.getTimeCreated()).toString());
            if (!mRefresh.isRefreshing()) mRefresh.setRefreshing(true);
            moreCards();
        }
    }

    /**
     * callback for card adapter
     */
    class CardLoader implements CardAdapter.CardLoader {
        @Override
        public void onNeedLoad(List<Card> cards) {
            Logger.d(">>> do nothing here");
        }
    }

    @SuppressLint("HandlerLeak")
    private class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MSG_WHAT_CANCEL:
                    mResultCode = POP_RESULT_CANCEL;
                    mCardRCV.smoothScrollToPosition(BaseModelAdapter.POSITION_AT_FIRST);
                    //mPostBtn.setClickable(true); //unblocks click
                    /**
                     * no pop() due to retain home fragmenat at the least
                     */
                    //pop();
                    break;
                default:

                    break;
            }
        }
    }

    class CardsLayoutManager extends LinearLayoutManager {
        private CardsLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            if (true) return;
            View child_of_rv = recycler.getViewForPosition(0);
            if (child_of_rv == null) {
                Logger.e("!!! child_of_rv is null");
                super.onMeasure(recycler, state, widthSpec, heightSpec);
                return;
            }
            measureChild(child_of_rv, widthSpec, heightSpec);
            Logger.d("... View.MeasureSpec.getSize(widthSpec):" + View.MeasureSpec.getSize(widthSpec));
            Logger.d("... View.MeasureSpec.getSize(heightSpec):" + View.MeasureSpec.getSize(heightSpec));
            Logger.d("... recycler.getViewForPosition(0).getMeasuredWidth():" + recycler.getViewForPosition(0).getMeasuredWidth());
            Logger.d("... recycler.getViewForPosition(0).getMeasuredHeight():" + recycler.getViewForPosition(0).getMeasuredHeight());
            int measuredWidth = View.MeasureSpec.getSize(widthSpec);
            //int measuredWidth = child_of_rv.getMeasuredWidth();
            //int measureHeight = View.MeasureSpec.getSize(heightSpec);
            int measureHeight = child_of_rv.getMeasuredHeight();
            setMeasuredDimension(measuredWidth, measureHeight);
        }
    }

    class OnRCVScroll extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                //Logger.d("... do nothing when not at scroll state idle");
                return;
            }
            int first_visible = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            int first_complete = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            int last_visible = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition();
            int last_complete = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            int destination = (first_visible == first_complete) ? first_visible : first_visible+1;
            int items_count = recyclerView.getAdapter().getItemCount();
            if (destination == RecyclerView.NO_POSITION) {
                Logger.e("!!! no position to scroll");
                return;
            }
            if (first_visible == first_complete) {
                Logger.d("... meet the first item of data set of adapter, last item position:"
                        + last_visible
                        + ", item count:" + items_count
                );
            } else if (last_complete == recyclerView.getAdapter().getItemCount()-1) {
                Logger.d("... meet the last item of data set of adapter, last item position:"
                        + last_visible
                        + ", item count:" + items_count
                );
            }
            //orientateRecyclerView(recyclerView, destination); //bug happens when
        }

        /**
         * this happens many times a second during a scroll, so be wary of the code placed here.
         */
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            /*
            if (dy < 0) {
                Logger.d("... RCV scrolling up:" + dy);
            } else if (dy > 0) {
                Logger.d("... RCV scrolling down:" + dy);
            }
            int visible_item_count = recyclerView.getChildCount();
            int items_count = recyclerView.getAdapter().getItemCount();
            int invisible_item_count = (items_count >= visible_item_count) ? (items_count - visible_item_count) : 0;
            if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() == invisible_item_count) {
                Logger.d("... meet the last item of data set of adapter, last item position:"
                        + ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition()
                        + ", item count:" + items_count
                        );
            } else if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() < invisible_item_count){
                Logger.d("... before the last item of data set of adapter, last item position:"
                        + ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition()
                        + ", item count:" + items_count
                        );
            } else if (((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > invisible_item_count){
                Logger.d("... after(data set shrunk or un-synchronized!) the last item of data set of adapter, last item position:"
                        + ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastVisibleItemPosition()
                        + ", item count:" + items_count
                );
            }
            */
        }
    }

    /**
     * callback for pop() on card detail fragment
     */
    class CardDetailFragmentOnPop implements FirstCardDetailFragment.OnPop {
        @Override
        public void onPop(int resultCode) {
            //pullCards();
            switch (resultCode) {
                case FirstCardDetailFragment.POP_RESULT_INIT_CARD_FAILURE:
                    showSnackBar("failed to load");
                    break;
                case FirstCardDetailFragment.POP_RESULT_CANCEL:
                    //showSnackBar("");
                    break;
            }
        }
    }

}
