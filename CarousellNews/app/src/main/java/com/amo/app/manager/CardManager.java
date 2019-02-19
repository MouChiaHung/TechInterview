package com.amo.app.manager;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amo.app.dao.CardsDao;
import com.amo.app.dao.HttpJsonParser;
import com.amo.app.entity.Card;
import com.amo.app.entity.CardList;
import com.amo.app.worker.ExecutorHelper;

import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CardManager {
    public final static int CARD_UPLOAD_PROCESS_DONE = 100;
    public final static int CARD_REMOVE_PROCESS_DONE = 100;
    public final static int ERROR_BASE = -10;
    public final static int UPLOAD_PHOTO_ERROR = ERROR_BASE - 1;
    public final static int REMOVE_PHOTO_ERROR = ERROR_BASE - 2;
    public final static int INDEX_NO_SUCH_THING = -1;

    public final static int SORT_MODE_TIME = 10;
    public final static int SORT_MODE_RANK = 11;

    private static CardManager mInstance;
    private Context mContext;

    private final Object lock = new Lock();

    private static final String KEY_ARRAY = "array";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_BANNER = "banner_url";
    private static final String KEY_TIME = "time_created";
    private static final String KEY_RANK = "rank";
    private String url = "https://storage.googleapis.com/carousell-interview-assets/android/carousell_news.json";

    /**
     * inner class access
     */
    private static int count_of_success_upload = 0;
    private static int count_of_delete_photo = 0;

    private CardManager(Context context) throws ClassNotFoundException {
        mContext = context;
        init();
    }

    /**
     * gets a singleton object managing all methods.
     * this method should be called before other methods.
     */
    public static CardManager getInstance(Context context) throws ClassNotFoundException {
        //Logger.d(">>>");
        if (mInstance == null ) {
            Logger.d("... going to create the singleton instance");
            mInstance = new CardManager(context);
        }
        count_of_success_upload = 0;
        count_of_delete_photo = 0;
        return mInstance;
    }

    private void init()  {

    }

    /**
     * attaches value event listener to obtain values of cards
     * to obtain value through attaching listener is asynchronous to this method
     * a subscriber implemented by UI class is called by listener for reacting simultaneously on UI when subscriber is triggered
     */
    public void subscribeAllCards(final Subscriber subscriber, int mode) {
        Logger.d(">>>");
        ReadTask task = new ReadTask();
        task.setMode(mode);
        task.execute(subscriber);
    }

    private static final class Lock {}

    private class ReadTask extends AsyncTask<Subscriber, Void, List<Card>> {
        JSONObject response;
        JSONArray response_json_array;
        String response_json_string;
        Subscriber[] subscriber;
        List<Card> cards;
        int mode = SORT_MODE_TIME;
        long now_millsec = (long)(new Date().getTime());
        int now_sec = (int)(now_millsec/1000);

        public void setMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
        }

        @Override
        protected List<Card> doInBackground(Subscriber... params) {
            Logger.d(">>> params:" + params.getClass().getSimpleName()
                    + " and now_sec:" + now_sec);
            /**
             * resets local SQL table
             */
            try {
                int rm_num = CardsDao.getInstance(mContext).remove(now_sec);
                Logger.d("... romoved count:" + rm_num
                );
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            subscriber = params;
            HttpJsonParser jsonParser = new HttpJsonParser();
            response_json_string = jsonParser.requestForJSONString(url,"GET",null);
            try {
                //response_json_array = new JSONArray(url);
                response_json_array = new JSONArray(response_json_string);
                cards = new ArrayList<>();
                //populates the cardJSONObj list from response
                Logger.d("... response_json_array.length():" + response_json_array.length());
                for (int i = 0; i<response_json_array.length();i++){
                    Card card = new Card();
                    JSONObject cardJSONObj = response_json_array.getJSONObject(i);
                    card.setId(cardJSONObj.getString(KEY_ID));
                    card.setTitle(cardJSONObj.getString(KEY_TITLE));
                    card.setDescription(cardJSONObj.getString(KEY_DESCRIPTION));
                    card.setBannerUrl(cardJSONObj.getString(KEY_BANNER));
                    card.setTimeCreated(cardJSONObj.getInt(KEY_TIME));
                    card.setRank(cardJSONObj.getInt(KEY_RANK));
                    cards.add(card);
                    CardsDao.getInstance(mContext).add(card);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Logger.e("!!! exception:" + e.getMessage());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Logger.e("!!! exception:" + e.getMessage());
            }
            return cards;
        }

        protected void onPostExecute(final List<Card> result) {
            if (result == null) {
                Logger.e("!!! result is null");
                subscriber[0].onError("cards is null");
                return;
            }
            switch (mode) {
                case SORT_MODE_TIME:
                    Logger.d("... sort with time");
                    result.clear();
                    try {
                        CardsDao.getInstance(mContext).find("time_created", now_sec, result);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        public void run() {
                            if (subscriber[0] != null) {
                                subscriber[0].onClone(result);
                            } else {
                                Logger.e("!!! subscriber[0] is null");
                            }
                        }
                    }, 0);
                    break;
                case SORT_MODE_RANK:
                    Logger.d("... sort with rank");
                    result.clear();
                    try {
                        CardsDao.getInstance(mContext).find("rank", 0, result);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        public void run() {
                            if (subscriber[0] != null) {
                                subscriber[0].onClone(result);
                            } else {
                                Logger.e("!!! subscriber[0] is null");
                            }
                        }
                    }, 0);
                    break;
                default:
                    Logger.d("... no sort");
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        public void run() {
                            if (subscriber[0] != null) {
                                subscriber[0].onClone(result);
                            } else {
                                Logger.e("!!! subscriber[0] is null");
                            }
                        }
                    }, 0);
                    break;
            }

        }
    }

    /**
     * callback implemented and used to notified other UI class(V of MVP) when ValueEventListener#onDataChange() is invoked
     * subscriber for reacting to event about value changes of database on UI
     */
    public interface Subscriber {
        void onClone(List cards);
        void onError(String error);
    }
}
