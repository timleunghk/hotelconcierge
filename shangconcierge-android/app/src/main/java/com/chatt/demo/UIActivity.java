package com.chatt.demo;

/**
 * Created by timothy.leung on 29/09/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.opentok.android.AudioDeviceManager;
import com.opentok.android.BaseAudioDevice;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Stream.StreamVideoType;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import com.chatt.demo.ClearNotificationService.ClearBinder;

import com.chatt.demo.ui.AudioLevelView;
import com.chatt.demo.ui.fragments.PublisherControlFragment;
import com.chatt.demo.ui.fragments.PublisherStatusFragment;
import com.chatt.demo.ui.fragments.SubscriberControlFragment;
import com.chatt.demo.ui.fragments.SubscriberQualityFragment;


import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.chatt.demo.CustomAudioDevice;
import android.media.AudioManager;

public class UIActivity extends Activity implements
        Session.SessionListener,
        Session.ArchiveListener,
        Session.StreamPropertiesListener,
        Session.ConnectionListener,
        Publisher.PublisherListener,
        Session.SignalListener,
        Subscriber.VideoListener, Subscriber.SubscriberListener,
        SubscriberControlFragment.SubscriberCallbacks,
        PublisherControlFragment.PublisherCallbacks {

    private static final String LOGTAG = "UIActivity";
    private static final int ANIMATION_DURATION = 3000;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private ArrayList<Stream> mStreams = new ArrayList<Stream>();
    private Handler mHandler = new Handler();

    private boolean mSubscriberAudioOnly = false;
    private boolean archiving = false;
    private boolean resumeHasRun = false;

    // View related variables
    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout mSubscriberViewContainer;
    private RelativeLayout mSubscriberAudioOnlyView;

    // Fragments
    private SubscriberControlFragment mSubscriberFragment;
    private PublisherControlFragment mPublisherFragment;
    private PublisherStatusFragment mPublisherStatusFragment;
    private SubscriberQualityFragment mSubscriberQualityFragment;
    private FragmentTransaction mFragmentTransaction;


    //private NotificationManager mNotificationManager;
    // Spinning wheel for loading subscriber view
    private ProgressBar mLoadingSub;

    private AudioLevelView mAudioLevelView;


    private SubscriberQualityFragment.CongestionLevel congestion = SubscriberQualityFragment.CongestionLevel.Low;

    private boolean mIsBound = false;
    private NotificationCompat.Builder mNotifyBuilder;
    private NotificationManager mNotificationManager;
    private ServiceConnection mConnection;

    private String key;

    public static String CALL = "Calling";
    public static String MISS = "Missed";
    public static String TALK = "In Service";
    public static String END = "Completed";
    public static String MSG = "TextMsg";
    public String sessionID;
    public String publisherToken,subscriberToken;
    public String sInternalID, sPlayMusic,sMyName;


    private CustomAudioDevice customAudioDevice;
    private AudioManager audioManager;


    private String sUserType,sMode;

    private Context context;

    private MediaPlayer mediaPlayer;

    private Chat mChat;

    static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);

        key=getString(R.string.opentok_apikey);
        sessionID = getIntent().getStringExtra("SESSIONID");
        publisherToken =  getIntent().getStringExtra("PUBTOKEN");
        subscriberToken = getIntent().getStringExtra("SUBTOKEN");
        sInternalID = getIntent().getStringExtra("OBJECTID");
        sPlayMusic = getIntent().getStringExtra("PLAYMUSIC");

        sMyName =  getIntent().getStringExtra("MYNAME");

        sMode = getIntent().getStringExtra("MODE");




        Log.d(LOGTAG, "key:" + key + ",sessionID:" + sessionID + ",publisherToken:" + publisherToken + ",subscriberToken:" + subscriberToken + ",sInternalID:" + sInternalID+",ReceiverName:" + sMyName);

        // Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        context = this;
        loadInterface();

        if (savedInstanceState == null) {
            mFragmentTransaction = getFragmentManager().beginTransaction();
            initSubscriberFragment();
            initPublisherFragment();
            initPublisherStatusFragment();
            initSubscriberQualityFragment();
            mFragmentTransaction.commitAllowingStateLoss();
        }

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        CustomAudioDevice customAudioDevice = new CustomAudioDevice(this);
        AudioDeviceManager.setAudioDevice(customAudioDevice);
        customAudioDevice.setOutputMode(BaseAudioDevice.OutputMode.SpeakerPhone);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(context, R.raw.pleasewait);
        mediaPlayer.setLooping(true);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);



        if (sPlayMusic.equalsIgnoreCase("YES")) {
            Log.d(LOGTAG,"Activate PlaySound -- ON");
            PlayMusic();

        }
        else
        {
            Log.d(LOGTAG, "Activate PlaySound -- OFF");

        }

        sessionConnect();

        if (sMode==MSG)
        {
            Log.d(LOGTAG,"EndCall triggered");
            onEndCall();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                if (mSubscriber != null) {
                    onViewClick.onClick(null);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Remove publisher & subscriber views because we want to reuse them
        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());

            if (mSubscriberFragment != null) {
                getFragmentManager().beginTransaction()
                        .remove(mSubscriberFragment).commit();

                initSubscriberFragment();
                if (mSubscriberQualityFragment != null) {
                    getFragmentManager().beginTransaction()
                            .remove(mSubscriberQualityFragment).commit();
                    initSubscriberQualityFragment();
                }
            }
        }
        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());

            if (mPublisherFragment != null) {
                getFragmentManager().beginTransaction()
                        .remove(mPublisherFragment).commit();

                initPublisherFragment();
            }

            if (mPublisherStatusFragment != null) {
                getFragmentManager().beginTransaction()
                        .remove(mPublisherStatusFragment).commit();

                initPublisherStatusFragment();
            }
        }

        loadInterface();
    }

    public void loadInterface() {


        Log.d(LOGTAG,"Start Loading LoadInterface");

        setContentView(R.layout.layout_ui_activity);
        mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);
        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherView);
        mSubscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberView);
        mSubscriberAudioOnlyView = (RelativeLayout) findViewById(R.id.audioOnlyView);
        mAudioLevelView = (AudioLevelView) findViewById(R.id.subscribermeter);
        mAudioLevelView.setIcons(BitmapFactory.decodeResource(getResources(),
                R.drawable.headset));
        Log.d(LOGTAG, "mAudioLevelView end");
        // Attach running video views
        if (mPublisher != null) {
            Log.d(LOGTAG, "attachPublisherView init  at LoadInterface");
            attachPublisherView(mPublisher);
            Log.d(LOGTAG, "attachPublisherView init  at LoadInterface.....Done");
        }

        // show subscriber status
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSubscriber != null) {
                    Log.d(LOGTAG, "mSubscriber is not null, attachSubscriberView start");
                    attachSubscriberView(mSubscriber);
                    Log.d(LOGTAG, "mSubscriber is not null, attachSubscriberView end");
                    if (mSubscriberAudioOnly) {
                        Log.d(LOGTAG, "LoadInterface mSubscriberAudioOnly=true");
                        mSubscriber.getView().setVisibility(View.GONE);
                        setAudioOnlyView(true);
                        congestion = SubscriberQualityFragment.CongestionLevel.High;
                    }
                }
            }
        }, 0);
        Log.d(LOGTAG, "loadFragments start");
        loadFragments();
        Log.d(LOGTAG, "loadFragments end");

        Log.d(LOGTAG,"Start Loading LoadInterface End");
    }

    public void loadFragments() {
        // show subscriber status
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

               if (mSubscriber != null) {
                    mSubscriberFragment.showSubscriberWidget(true);
                    mSubscriberFragment.initSubscriberUI();

                    if (congestion != SubscriberQualityFragment.CongestionLevel.Low) {
                        mSubscriberQualityFragment.setCongestion(congestion);
                        mSubscriberQualityFragment.showSubscriberWidget(true);
                    }
                }
                else {
                   Log.d(LOGTAG, "mSubscriber is null, skipped");
               }

            }
        }, 0);

        // show publisher status
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPublisher != null) {
                    mPublisherFragment.showPublisherWidget(true);
                    mPublisherFragment.initPublisherUI();

                    if (archiving) {
                        mPublisherStatusFragment.updateArchivingUI(true);
                        setPubViewMargins();
                    }
                }

            }
        }, 0);

    }

    public void initSubscriberFragment() {
        mSubscriberFragment = new SubscriberControlFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_sub_container, mSubscriberFragment).commit();
    }

    public void initPublisherFragment() {
        mPublisherFragment = new PublisherControlFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_pub_container, mPublisherFragment).commit();
    }

    public void initPublisherStatusFragment() {
        mPublisherStatusFragment = new PublisherStatusFragment();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_pub_status_container,
                        mPublisherStatusFragment).commit();
    }

    public void initSubscriberQualityFragment() {
        mSubscriberQualityFragment = new SubscriberQualityFragment();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_sub_quality_container,
                        mSubscriberQualityFragment).commit();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSession != null) {
            mSession.onPause();

            if (mSubscriber != null) {
                mSubscriberViewContainer.removeView(mSubscriber.getView());
            }
        }

        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(this.getTitle())
                .setContentText(getResources().getString(R.string.notification))
                .setSmallIcon(R.drawable.ic_launcher).setOngoing(true);

        Intent notificationIntent = new Intent(this, UIActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        mNotifyBuilder.setContentIntent(intent);
        if (mConnection == null) {
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName className, IBinder binder) {
                    ((ClearBinder) binder).service.startService(new Intent(UIActivity.this, ClearNotificationService.class));
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    mNotificationManager.notify(ClearNotificationService.NOTIFICATION_ID, mNotifyBuilder.build());
                }

                @Override
                public void onServiceDisconnected(ComponentName className) {
                    mConnection = null;
                }

            };
        }

        if (!mIsBound) {
            Log.d(LOGTAG, "mISBOUND GOT CALLED");
            bindService(new Intent(UIActivity.this,
                            ClearNotificationService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
            mIsBound = true;
            startService(notificationIntent);

        }
        StopMusic();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }

        if (!resumeHasRun) {
            resumeHasRun = true;
            return;
        } else {
            if (mSession != null) {
                mSession.onResume();
            }
        }

        mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);

        reloadInterface();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }
        if (isFinishing()) {
            mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
            if (mSession != null) {
                mSession.disconnect();
            }
        }
        active = false;
    }

    @Override
    public void onDestroy() {
        mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
        if (mIsBound) {
            unbindService(mConnection);
            mIsBound = false;
        }

        if (mSession != null) {
            mSession.disconnect();
        }
        Chat mChat = new Chat();
        mChat.DoAvailable(sMyName);
        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mSession != null) {
            mSession.disconnect();
        }

        super.onBackPressed();
    }

    public void reloadInterface() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSubscriber != null) {
                    attachSubscriberView(mSubscriber);
                    if (mSubscriberAudioOnly) {
                        mSubscriber.getView().setVisibility(View.GONE);
                        setAudioOnlyView(true);
                        congestion = SubscriberQualityFragment.CongestionLevel.High;
                    }
                }
            }
        }, 500);

        loadFragments();
    }

    private void sessionConnect() {

        if (mSession == null)
        {
            mSession = new Session(this, this.key,
                    this.sessionID);
            mSession.setSessionListener(this);
            mSession.setArchiveListener(this);
            mSession.setSignalListener(this);

            mSession.setStreamPropertiesListener(this);
            mSession.connect(this.publisherToken);
        }

    }

    private void attachPublisherView(Publisher publisher) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mPublisherViewContainer.addView(publisher.getView(), layoutParams);
        mPublisherViewContainer.setDrawingCacheEnabled(true);
        publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        publisher.getView().setOnClickListener(onViewClick);
    }

    @Override
    public void onMuteSubscriber() {
        if (mSubscriber != null) {
            mSubscriber.setSubscribeToAudio(!mSubscriber.getSubscribeToAudio());
        }
    }



    @Override
    public void onMutePublisher() {
        if (mPublisher != null) {
            mPublisher.setPublishAudio(!mPublisher.getPublishAudio());
        }
    }

    @Override
    public void onSwapCamera() {
        if (mPublisher != null) {
            mPublisher.swapCamera();
        }
    }

    @Override
    public void onEndCall() {
        if (mSession != null) {
            mSession.disconnect();
        }

         Log.i(LOGTAG, "Disconnected to the session....update END flag with sInternalID:" + sInternalID + "... Start");
         UpdateSessionID(sInternalID, UIActivity.END);
         Log.i(LOGTAG, "Disconnected to the session....update END flag with sInternalID:" + sInternalID + "... End");

        mChat = new Chat();
        mChat.DoAvailable(sMyName);

        finish();
    }

    private void attachSubscriberView(Subscriber subscriber) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mSubscriberViewContainer.removeView(mSubscriber.getView());
        mSubscriberViewContainer.addView(subscriber.getView(), layoutParams);
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        subscriber.getView().setOnClickListener(onViewClick);
    }

    private void subscribeToStream(Stream stream) {
        mSubscriber = new Subscriber(this, stream);
        mSubscriber.setSubscriberListener(this);
        mSubscriber.setVideoListener(this);
        mSession.subscribe(mSubscriber);

        if (mSubscriber.getSubscribeToVideo()) {
            // start loading spinning
            mLoadingSub.setVisibility(View.VISIBLE);
        }
    }

    private void unsubscriberFromStream(Stream stream) {
        mStreams.remove(stream);
        if (mSubscriber.getStream().equals(stream)) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber = null;
            if (!mStreams.isEmpty()) {
                subscribeToStream(mStreams.get(0));
            }
        }
    }

    private void setAudioOnlyView(boolean audioOnlyEnabled) {
        mSubscriberAudioOnly = audioOnlyEnabled;

        if (audioOnlyEnabled) {
            mSubscriber.getView().setVisibility(View.GONE);
            mSubscriberAudioOnlyView.setVisibility(View.VISIBLE);
            mSubscriberAudioOnlyView.setOnClickListener(onViewClick);

            // Audio only text for subscriber
            TextView subStatusText = (TextView) findViewById(R.id.subscriberName);
            subStatusText.setText(R.string.audioOnly);
            AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
            aa.setDuration(ANIMATION_DURATION);
            subStatusText.startAnimation(aa);


            mSubscriber
                    .setAudioLevelListener(new SubscriberKit.AudioLevelListener() {
                        @Override
                        public void onAudioLevelUpdated(
                                SubscriberKit subscriber, float audioLevel) {
                            mAudioLevelView.setMeterValue(audioLevel);
                        }
                    });
        } else {
            if (!mSubscriberAudioOnly) {
                mSubscriber.getView().setVisibility(View.VISIBLE);
                mSubscriberAudioOnlyView.setVisibility(View.GONE);

                mSubscriber.setAudioLevelListener(null);
            }
        }
    }

    private OnClickListener onViewClick = new OnClickListener() {
        @Override
        public void onClick(View v)
        {
            boolean visible = false;
            Log.d(LOGTAG,"OnClickListener triggered");
            if (mPublisher != null) {
                // check visibility of bars
                if (!mPublisherFragment.isMPublisherWidgetVisible()) {
                    visible = true;
                }
                mPublisherFragment.publisherClick();
                if (archiving) {
                    mPublisherStatusFragment.publisherClick();
                }
                setPubViewMargins();
                if (mSubscriber != null) {
                    Log.d(LOGTAG,"OnClickListener triggered..showSubscriberWidget="+visible);
                    mSubscriberFragment.showSubscriberWidget(visible);
                    mSubscriberFragment.initSubscriberUI();
                }
            }
        }
    };

    public Publisher getmPublisher() {
        return mPublisher;
    }

    public Subscriber getmSubscriber() {
        return mSubscriber;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    @Override
    public void onConnected(Session session) {
        Log.i(LOGTAG, "Connected to the session.");
        if (mPublisher == null) {
            mPublisher = new Publisher(this, "Publisher");
            mPublisher.setPublisherListener(this);
 /*           Log.d(LOGTAG, "attachPublisherView init  at onConnected Event (INVISIBLE)");
            attachPublisherView(mPublisher);
            Log.d(LOGTAG, "attachPublisherView init  at onConnected Event.....(INVISIBLE) Done");*/
            mSession.publish(mPublisher);

            Chat mChat = new Chat();
            mChat.DoNotAvailable(sMyName);

        }
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOGTAG, "Disconnected to the session.");





        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getRenderer()
                    .getView());
        }

        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getRenderer()
                    .getView());
        }

        mPublisher = null;
        mSubscriber = null;
        mStreams.clear();

        mSession.disconnect();
        mSession = null;

        mChat = new Chat();
        mChat.DoAvailable(sMyName);

        StopMusic();
    }



    @Override
    public void onStreamReceived(Session session, Stream stream) {

        mStreams.add(stream);
        if (mSubscriber == null) {
            subscribeToStream(stream);
           UpdateSessionID(sInternalID, TALK);
  /*           Log.d(LOGTAG, "attachPublisherView init  at onStreamReceived Event (INVISIBLE)");
            attachPublisherView(mPublisher);
            Log.d(LOGTAG, "attachPublisherView init  at onStreamReceived Event.....(INVISIBLE) Done");*/
        }
        mNotificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);
        StopMusic();
    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {

        Log.d(LOGTAG, "On Stream Dropped listener is triggered");
        mStreams.remove(stream);

        if (mSubscriber != null
                && mSubscriber.getStream().getStreamId()
                .equals(stream.getStreamId())) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber = null;



            findViewById(R.id.avatar).setVisibility(View.GONE);
            session.disconnect();
            mSubscriberAudioOnly = false;
            finish();
            StopMusic();
        }



    }

    @Override
    public void onStreamCreated(PublisherKit publisher, Stream stream) {

        mStreams.add(stream);
        mPublisherFragment.showPublisherWidget(true);
        mPublisherFragment.initPublisherUI();
        mPublisherStatusFragment.showPubStatusWidget(true);
        mPublisherStatusFragment.initPubStatusUI();


    }

    @Override
    public void onStreamDestroyed(PublisherKit publisher, Stream stream) {

        Log.d(LOGTAG, "On Stream Destroy listener is triggered");
        if (mSubscriber != null) {
            unsubscriberFromStream(stream);
        }
    }





    @Override
    public void onError(Session session, OpentokError exception) {
        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        UpdateSessionID(sInternalID, MISS);
    }

    public void setPubViewMargins() {
        RelativeLayout.LayoutParams pubLayoutParams = (LayoutParams) mPublisherViewContainer
                .getLayoutParams();
        int bottomMargin = 0;
        boolean controlBarVisible = mPublisherFragment
                .isMPublisherWidgetVisible();
        boolean statusBarVisible = mPublisherStatusFragment
                .isMPubStatusWidgetVisible();
        RelativeLayout.LayoutParams pubControlLayoutParams = (LayoutParams) mPublisherFragment
                .getMPublisherContainer().getLayoutParams();
        RelativeLayout.LayoutParams pubStatusLayoutParams = (LayoutParams) mPublisherStatusFragment
                .getMPubStatusContainer().getLayoutParams();

        // setting margins for publisher view on portrait orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (statusBarVisible && archiving) {
                // height of publisher control bar + height of publisher status
                // bar + 20 px
                bottomMargin = pubControlLayoutParams.height
                        + pubStatusLayoutParams.height + dpToPx(20);
            } else {
                if (controlBarVisible) {
                    // height of publisher control bar + 20 px
                    bottomMargin = pubControlLayoutParams.height + dpToPx(20);
                } else {
                    bottomMargin = dpToPx(20);
                }
            }
        }

        // setting margins for publisher view on landscape orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (statusBarVisible && archiving) {
                bottomMargin = pubStatusLayoutParams.height + dpToPx(20);
            } else {
                bottomMargin = dpToPx(20);
            }
        }

        pubLayoutParams.bottomMargin = bottomMargin;
        pubLayoutParams.leftMargin = dpToPx(20);

        mPublisherViewContainer.setLayoutParams(pubLayoutParams);

        if (mSubscriber != null) {
            if (mSubscriberAudioOnly) {
                RelativeLayout.LayoutParams subLayoutParams = (LayoutParams) mSubscriberAudioOnlyView
                        .getLayoutParams();
                int subBottomMargin = 0;
                subBottomMargin = pubLayoutParams.bottomMargin;
                subLayoutParams.bottomMargin = subBottomMargin;
                mSubscriberAudioOnlyView.setLayoutParams(subLayoutParams);
            }

            setSubQualityMargins();
        }
    }

    public void setSubQualityMargins() {
        RelativeLayout.LayoutParams subQualityLayoutParams = (LayoutParams) mSubscriberQualityFragment
                .getSubQualityContainer().getLayoutParams();
        boolean pubControlBarVisible = mPublisherFragment
                .isMPublisherWidgetVisible();
        boolean pubStatusBarVisible = mPublisherStatusFragment
                .isMPubStatusWidgetVisible();
        RelativeLayout.LayoutParams pubControlLayoutParams = (LayoutParams) mPublisherFragment
                .getMPublisherContainer().getLayoutParams();
        RelativeLayout.LayoutParams pubStatusLayoutParams = (LayoutParams) mPublisherStatusFragment
                .getMPubStatusContainer().getLayoutParams();
        RelativeLayout.LayoutParams audioMeterLayoutParams = (LayoutParams) mAudioLevelView.getLayoutParams();

        int bottomMargin = 0;

        // control pub fragment
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (pubControlBarVisible) {
                bottomMargin = pubControlLayoutParams.height + dpToPx(10);
            }
            if (pubStatusBarVisible && archiving) {
                bottomMargin = pubStatusLayoutParams.height + dpToPx(10);
            }
            if (bottomMargin == 0) {
                bottomMargin = dpToPx(10);
            }
            subQualityLayoutParams.rightMargin = dpToPx(10);
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!pubControlBarVisible) {
                subQualityLayoutParams.rightMargin = dpToPx(10);
                bottomMargin = dpToPx(10);
                audioMeterLayoutParams.rightMargin = 0;
                mAudioLevelView.setLayoutParams(audioMeterLayoutParams);

            } else {
                subQualityLayoutParams.rightMargin = pubControlLayoutParams.width;
                bottomMargin = dpToPx(10);
                audioMeterLayoutParams.rightMargin = pubControlLayoutParams.width;
            }
            if (pubStatusBarVisible && archiving) {
                bottomMargin = pubStatusLayoutParams.height + dpToPx(10);
            }
            mAudioLevelView.setLayoutParams(audioMeterLayoutParams);
        }

        subQualityLayoutParams.bottomMargin = bottomMargin;

        mSubscriberQualityFragment.getSubQualityContainer().setLayoutParams(
                subQualityLayoutParams);

    }


    @Override
    public void onError(PublisherKit publisher, OpentokError exception) {
        Log.i(LOGTAG, "Publisher exception: " + exception.getMessage());
    }

    @Override
    public void onConnected(SubscriberKit subscriber) {
        mLoadingSub.setVisibility(View.GONE);
        mSubscriberFragment.showSubscriberWidget(true);
        mSubscriberFragment.initSubscriberUI();
    }

    @Override
    public void onDisconnected(SubscriberKit subscriber) {
        Log.i(LOGTAG, "Subscriber disconnected.");
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriber) {
        Log.i(LOGTAG, "First frame received");

        // stop loading spinning
        mLoadingSub.setVisibility(View.GONE);


        attachSubscriberView(mSubscriber);

      //  mPublisherViewContainer.setVisibility(View.VISIBLE);




    }

    @Override
    public void onError(SubscriberKit subscriber, OpentokError exception) {
        Log.i(LOGTAG, "Subscriber exception: " + exception.getMessage());
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriber, String reason) {
        Log.i(LOGTAG, "Video disabled:" + reason);
        if (mSubscriber == subscriber) {
            setAudioOnlyView(true);
        }

        if (reason.equals("quality")) {
            mSubscriberQualityFragment.setCongestion(SubscriberQualityFragment.CongestionLevel.High);
            congestion = SubscriberQualityFragment.CongestionLevel.High;
            setSubQualityMargins();
            mSubscriberQualityFragment.showSubscriberWidget(true);
        }
    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriber, String reason) {
        Log.i(LOGTAG, "Video enabled:" + reason);
        if (mSubscriber == subscriber) {
            setAudioOnlyView(false);
        }
        if (reason.equals("quality")) {
            mSubscriberQualityFragment.setCongestion(SubscriberQualityFragment.CongestionLevel.Low);
            congestion = SubscriberQualityFragment.CongestionLevel.Low;
            Log.d(LOGTAG,"onVideoEnabled triggered, showSubscriberWidget=false");
            mSubscriberQualityFragment.showSubscriberWidget(false);
        }
    }

    @Override
    public void onStreamHasAudioChanged(Session session, Stream stream,
                                        boolean audioEnabled) {
        Log.i(LOGTAG, "Stream audio changed");
    }

    @Override
    public void onStreamHasVideoChanged(Session session, Stream stream,
                                        boolean videoEnabled) {
        Log.i(LOGTAG, "Stream video changed");
    }

    @Override
    public void onStreamVideoDimensionsChanged(Session session, Stream stream,
                                               int width, int height) {
        Log.i(LOGTAG, "Stream video dimensions changed");
    }

    @Override
    public void onArchiveStarted(Session session, String id, String name) {
        Log.i(LOGTAG, "Archiving starts");
        mPublisherFragment.showPublisherWidget(false);

        archiving = true;
        mPublisherStatusFragment.updateArchivingUI(true);
        mPublisherFragment.showPublisherWidget(true);
        mPublisherFragment.initPublisherUI();
        setPubViewMargins();

        if (mSubscriber != null) {
            mSubscriberFragment.showSubscriberWidget(true);
        }
    }

    @Override
    public void onArchiveStopped(Session session, String id) {
        Log.i(LOGTAG, "Archiving stops");
        archiving = false;

        mPublisherStatusFragment.updateArchivingUI(false);
        setPubViewMargins();

        if (mSubscriber != null) {
            setSubQualityMargins();
        }
    }

    /**
     * Converts dp to real pixels, according to the screen density.
     *
     * @param dp A number of density-independent pixels.
     * @return The equivalent number of real pixels.
     */
    public int dpToPx(int dp) {
        double screenDensity = getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriber) {
        Log.i(LOGTAG, "Video may be disabled soon due to network quality degradation. Add UI handling here.");
        mSubscriberQualityFragment.setCongestion(SubscriberQualityFragment.CongestionLevel.Mid);
        congestion = SubscriberQualityFragment.CongestionLevel.Mid;
        setSubQualityMargins();
        mSubscriberQualityFragment.showSubscriberWidget(true);
    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriber) {
        Log.i(LOGTAG, "Video may no longer be disabled as stream quality improved. Add UI handling here.");
        mSubscriberQualityFragment.setCongestion(SubscriberQualityFragment.CongestionLevel.Low);
        congestion = SubscriberQualityFragment.CongestionLevel.Low;
        Log.d(LOGTAG,"onVideoDisableWarningLifted triggered, showSubscriberWidget=false");
        mSubscriberQualityFragment.showSubscriberWidget(false);
    }

    @Override
    public void onStreamVideoTypeChanged(Session session, Stream stream,
                                         StreamVideoType videoType) {
        Log.i(LOGTAG, "Stream video type changed");
    }


    public void UpdateSessionID(String sInternalID,String status)
    {

        ParseObject po = ParseObject.createWithoutData("CallSections", sInternalID);
        Log.d(LOGTAG, "status to be saved:" + status + " at InternalID:" + sInternalID);
        po.put("Status",status);
        po.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Saved successfully.

                } else {
                    Log.d(LOGTAG, "Cannot update status in sessionid ,cannot pickup call");
                }
            }
        });
    }

    private void PlayMusic()
    {

        try {
            if(mediaPlayer != null) {
                mediaPlayer.stop();
            }
            mediaPlayer.prepare();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    mediaPlayer.start();

                }
            });

        } catch(Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void StopMusic()
    {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }



    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {

        AlertDialog dialog;

        if (connection != null)
        {
            if (connection != null)
            {
                Log.d(LOGTAG,"OnSignalReceived item: type:"+type+",data:"+data+",connection info:" + connection.getConnectionId());

                if (type.equals("showcamerarequest"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder((UIActivity) this.context);

                    builder.setTitle(this.getResources().getString(R.string.app_name));
                    builder.setMessage(this.getResources().getString(R.string.open_camera_request));

                    builder.setPositiveButton(this.getResources().getString(R.string.open_camera_request_ok), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            sendReplyOpenCameraRequest("OK");
                            Log.d(LOGTAG, "Detech video start");
                            DetachVideo();
                            Log.d(LOGTAG, "Detech video end");
                            Log.d(LOGTAG, "Attach video start");
                            attachVideoWithPublisher();
                            Log.d(LOGTAG, "Attach video end");
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton(this.getResources().getString(R.string.open_camera_request_deny), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            sendReplyOpenCameraRequest("DENY");
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else if (type.equals("closecameranotice"))
                {
                    DetachVideo();
                    attachVideo();

                    AlertDialog.Builder builder = new AlertDialog.Builder((UIActivity) this.context);
                    builder.setTitle(R.string.app_name);
                    builder.setMessage(this.getResources().getString(R.string.open_camera_request_done));
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", dismissListener);
                    dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }




    @Override
    public void onConnectionCreated(Session session, Connection connection) {

    }

    @Override
    public void onConnectionDestroyed(Session session, Connection connection) {

    }

    public void sendReplyOpenCameraRequest(String reply) {


        JSONObject json = new JSONObject();
        try {
            Log.e(LOGTAG, "sendReplyOpenCameraRequest reply:" + reply);

           json.put("streamid", mPublisher.getStream().getStreamId());
           json.put("name", mPublisher.getName());
           json.put("reply", reply);
           this.mSession.sendSignal("opencamera", json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private DialogInterface.OnClickListener dismissListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
        }
    };

    private void DetachVideo()
    {
        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getRenderer()
                    .getView());
        }
        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getRenderer()
                    .getView());
        }
    }

    private void attachVideoWithPublisher()
    {
        attachPublisherView(mPublisher);
        attachSubscriberView(mSubscriber);
    }

    private void attachVideo()
    {

        attachSubscriberView(mSubscriber);
    }
}

