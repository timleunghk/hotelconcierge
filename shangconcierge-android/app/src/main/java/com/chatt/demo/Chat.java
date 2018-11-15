package com.chatt.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.format.DateUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.net.Uri;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.Conversation;
import com.chatt.demo.utils.Const;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.location.LocationProvider;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

/**
 * The Class Chat is the Activity class that holds main chat screen. It shows
 * all the conversation messages between two users and also allows the user to
 * send and receive messages.
 */

public class Chat extends CustomActivity implements LocationListener {

	/** The Conversation list. */
	private ArrayList<Conversation> convList;

	/** The chat adapter. */
	private ChatAdapter adp;

	/** The Editext to compose the message. */
	private EditText txt;


	/** The date of last message in conversation. */
	private Date lastMsgDate;

	/** Flag to hold if the activity is running or not. */
	private boolean isRunning;

	/** The handler. */
	private static Handler handler;

	private static String TAG = "Log_Chat";

	private String UPLOAD_MEDIA_FILE = "upload_media_file_message";

	private String MSG_TYPE_TEXT = "text";
	private String MSG_TYPE_AUDIO = "audio";
	private String MSG_TYPE_IMAGE = "image";
	private String MSG_TYPE_VIDEO = "video";
	private String MSG_TYPE_POSITION = "position";


	public String sessionID;
	public String publisherToken, subscriberToken;

	private String senderName, receiverName,receiverFullName;


	public static String CALL = "Calling";
	public static String MISS = "Missed";
	public static String TALK = "In Service";
	public static String END = "Completed";


	private static final int SELECT_PICTURE = 1;

	private ProgressDialog mConnectingDialog;

	private String sUserType;

	private String selectedImagePath;
	private double mLat, mLong;

	public final String LM_GPS = LocationManager.GPS_PROVIDER;
	public final String LM_NETWORK = LocationManager.NETWORK_PROVIDER;
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;

	private Location lastKnownLocation;

	private Context mContext;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		String tmpSenderName, tmpRcvName,tmpreceiverFullName,tmpStatus,busymsg;
		Intent mIntent;
		AlertDialog dialog;
		mIntent = getIntent();

		tmpSenderName = mIntent.getStringExtra("sendername");
		tmpRcvName = mIntent.getStringExtra("receivername");
		tmpreceiverFullName = mIntent.getStringExtra("receiverfullname");
		tmpStatus = mIntent.getStringExtra("status");

		Log.d(TAG, "Generate Call start: Get Intent from CallService class sender:" + tmpSenderName + ", receiver: " + tmpRcvName);



		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);

		convList = new ArrayList<Conversation>();
		ListView list = (ListView) findViewById(R.id.list);


		adp = new ChatAdapter();
		list.setAdapter(adp);
		list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.setStackFromBottom(true);

		txt = (EditText) findViewById(R.id.txt);
		txt.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);

		setTouchNClick(R.id.btnSend);
		setTouchNClick(R.id.btnVideoCall);
		setTouchNClick(R.id.menu_make_call);
		setTouchNClick(R.id.menu_map);


		handler = new Handler();

		mContext = this;

		senderName = tmpSenderName;
		receiverName = tmpRcvName;
		receiverFullName = tmpreceiverFullName;
		//getActionBar().setTitle(receiverName);
		getActionBar().setTitle(tmpreceiverFullName);

		isRunning = true;
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


		mLocationManager.requestLocationUpdates(LM_GPS, 500, 1, this);
		mLocationManager.requestLocationUpdates(LM_NETWORK, 500, 1, this);

		lastKnownLocation = mLocationManager.getLastKnownLocation(LM_GPS);

		openGPS();

		busymsg = getResources().getString(R.string.busy_call);
		busymsg = busymsg.replace("XXCaller", receiverFullName);

		if (tmpStatus.equalsIgnoreCase("offline"))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.app_name);
			builder.setMessage(busymsg);
			builder.setCancelable(false);
			builder.setPositiveButton("OK", dismissListener);
			dialog = builder.create();
			dialog.show();
		}

	}

	public void openGPS()
	{

		boolean gps = mLocationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean network = mLocationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (gps || network)
		{
			return;
		}
		else
		{
			Intent gpsOptionsIntent = new Intent(
					android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(gpsOptionsIntent);
		}

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		//Setup Menu

		super.onCreateOptionsMenu(menu);
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.call_menu, menu);
		return true;

	}


	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
		isRunning = true;
		loadConversationList();
		updateUserStatus(true);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		updateUserStatus(false);
		isRunning = false;
	}

	/* (non-Javadoc)
	 * @see com.socialshare.custom.CustomFragment#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.btnSend)
		{
			sendMessage();
		}
		if (v.getId() == R.id.btnVideoCall)
		{
			sendMediaFiles();
		}
		if (v.getId() == R.id.menu_make_call)
		{
			videoCall();
		}
		if (v.getId() == R.id.menu_map)
		{
			sendMap();
		}
	}

	private void sendMap()
	{

		String strMsg;

		strMsg = "{\"Long\":\"" + mLong + "\",\"Lat\":\"" + mLat + "\"}";
		Log.d(TAG,"GPS Location: Long: " + mLong + ", lat:" +mLat + ", strMsg:" + strMsg);

		ParseObject po = new ParseObject("concierge"); //Parse Web > Core > class name (Installation/Role/Session/User/chat/concierge)
		po.put("msgtype", MSG_TYPE_POSITION);
		po.put("sender",   senderName);
		po.put("receiver", receiverName);
		po.put("message", strMsg);
		po.put("msgread", Conversation.STATUS_UNREAD);
		po.saveEventually(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				//TODO: MessageBox -- Your location has been sent to staff
				Log.d(TAG, "GPS Location: Long: " + mLong + ", lat:" + mLat + " ---- submitted");
			}
		});


	}




	private void videoCall()
	{
		int recNo;
		boolean bIsUsing;
		recNo = 0;

		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", receiverName);


		query.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> li, ParseException e) {
				String callmsg, busymsg;
				AlertDialog dialog;
				boolean bIsUsing;

				callmsg = getResources().getString(R.string.call_start);
				//callmsg = callmsg.replace("XXUser", receiverName);

				callmsg = callmsg.replace("XXUser", receiverFullName);

				busymsg = getResources().getString(R.string.busy_call);
				//busymsg = busymsg.replace("XXCaller", receiverName);
				busymsg = busymsg.replace("XXCaller", receiverFullName);

				Log.d(TAG, "video call (done):" + receiverName + ",size:" + li.size());
				if (li != null && li.size() > 0) {
					for (int i = li.size() - 1; i >= 0; i--) {
						ParseObject po = li.get(i);
						bIsUsing = po.getBoolean("IsUsing");
						if (bIsUsing) {
							Log.d(TAG, "bIsUsing is true");
							AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
							builder.setTitle(R.string.app_name);
							builder.setMessage(busymsg);
							builder.setCancelable(false);
							builder.setPositiveButton("OK", dismissListener);
							dialog = builder.create();
							dialog.show();

						} else {
							Log.d(TAG, "bIsUsing is false");
							mConnectingDialog = new ProgressDialog(mContext);
							mConnectingDialog.setTitle(getResources().getString(R.string.app_name));
							mConnectingDialog.setMessage(callmsg);

							mConnectingDialog.setCancelable(false);
							mConnectingDialog.setIndeterminate(true);
							mConnectingDialog.show();

							GetSessionID();
						}
					}
				}
			}
		});
	}




	private DialogInterface.OnClickListener dismissListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			dialog.dismiss();
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	/**
	 * Call this method to Send message to opponent. It does nothing if the text
	 * is empty otherwise it creates a Parse object for Chat message and send it
	 * to Parse server.
	 */
	private void sendMessage()
	{
		if (txt.length() == 0)
			return;
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

		String s = txt.getText().toString();
		final Conversation c = new Conversation(MSG_TYPE_TEXT,s,null,new Date(),UserList.user.getUsername(),Conversation.STATUS_UNREAD);
		c.setStatus(Conversation.STATUS_SENDING);

		convList.add(c);
		adp.notifyDataSetChanged();
		txt.setText(null);


		ParseObject po = new ParseObject("concierge"); //Parse Web > Core > class name (Installation/Role/Session/User/chat/concierge)
		po.put("msgtype", MSG_TYPE_TEXT);
		po.put("sender",   senderName);
		po.put("receiver", receiverName);
		po.put("message", s);
		po.put("msgread",Conversation.STATUS_UNREAD);
		po.saveEventually(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null)
					c.setStatus(Conversation.STATUS_SENT);
				else
					c.setStatus(Conversation.STATUS_FAILED);
				adp.notifyDataSetChanged();
			}
		});

	}

	private void sendMediaFiles()
	{

		Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		Bitmap bitmap;

		Uri selectedImageURI;
		InputStream inStream;
		ByteArrayOutputStream outStream;

		BitmapFactory.Options bmpOpt;

		inStream = null;
		bitmap=null;

		int iScale;

		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {

				selectedImageURI = data.getData();

				try
				{
					inStream = getContentResolver().openInputStream(selectedImageURI);

					if (inStream!=null)
					{

						bmpOpt = new BitmapFactory.Options();
						bmpOpt.inJustDecodeBounds = false;
						bmpOpt.inPreferredConfig = Bitmap.Config.ARGB_8888;
						bmpOpt.inSampleSize = 5;




						bitmap = BitmapFactory.decodeStream(inStream, null, bmpOpt);
						Log.d(TAG,"Width:" + bitmap.getWidth()+";Height:"+ bitmap.getHeight());
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);

						outStream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
						byte[] image = outStream.toByteArray();
						String s = txt.getText().toString();
						ParseFile uploadFile = new ParseFile("attachment.png",image); //change user_chat2.png to sender_receiver_yyyymmdd_hhmmss.png
						uploadFile.saveInBackground();

						ParseObject po = new ParseObject("concierge"); //Parse Web > Core > class name (Installation/Role/Session/User/chat/concierge)
						po.put("msgtype",MSG_TYPE_IMAGE);
						po.put("receiver",  receiverName);
						po.put("sender",senderName );
						po.put("mediafile", uploadFile);
						po.put("message", UPLOAD_MEDIA_FILE);
						po.put("msgread", Conversation.STATUS_UNREAD);
						po.saveInBackground();



						final Conversation c = new Conversation(MSG_TYPE_IMAGE,s,uploadFile,new Date(),UserList.user.getUsername(),Conversation.STATUS_UNREAD);

						c.setStatus(Conversation.STATUS_SENDING);
						Log.d(TAG, "onActivityResult (image) convList add start");
						convList.add(c);
						adp.notifyDataSetChanged();
						txt.setText(null);
						Log.d(TAG, "onActivityResult (image) convList add end");

						po.saveEventually(new SaveCallback() {

							@Override
							public void done(ParseException e) {
								if (e == null)
									c.setStatus(Conversation.STATUS_SENT);
								else
									c.setStatus(Conversation.STATUS_FAILED);
								adp.notifyDataSetChanged();
							}
						});


					}

				}
				catch (FileNotFoundException e)
				{
					Log.d(TAG,"File not found");
				}


			}


		}
	}




	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		//Log.d(TAG,"inSampleSize:" + inSampleSize);

		return inSampleSize;

	}

	/**
	 * Load the conversation list from Parse server and save the date of last
	 * message that will be used to load only recent new messages
	 */

	private void loadConversationList()
	{
		ParseQuery<ParseObject> q = ParseQuery.getQuery("concierge");

		Log.d(TAG,"UserList user getUserName=" + UserList.user.getUsername());

		if (convList.size() == 0)
		{
			Log.d(TAG,"ConvList size=0, do this whereContaedIn cause");
			ArrayList<String> al = new ArrayList<String>();


			al.add(senderName);
		//	al.add(UserList.user.getUsername());
			al.add(receiverName);
			q.whereContainedIn("sender", al);
			q.whereContainedIn("receiver", al);
			q.whereNotEqualTo("msgtype","position");


		}
		else
		{
			// load only newly received message..
			Log.d(TAG,"ConvList size> 0, Search sender and receiver only");
			if (lastMsgDate != null)
				q.whereGreaterThan("createdAt", lastMsgDate);
			q.whereEqualTo("sender", receiverName);
			q.whereEqualTo("receiver", UserList.user.getUsername());
		}
/*		{
			Log.d(TAG,"ConvList size>0, do this");
			// load only newly received message..
			if (lastMsgDate != null)
			{
				//q.whereGreaterThan("createdAt", lastMsgDate);
				if (lastMsgDate != null)
					q.whereGreaterThan("createdAt", lastMsgDate);
				q.whereEqualTo("sender", receiverName);//senderName);
				q.whereEqualTo("receiver", senderName);//receiverName);

				Log.d(TAG, "lastMsgDate:" + lastMsgDate + ",sendername:" + senderName + ", receivername:" + receiverName);
			}
		}*/


		q.orderByDescending("createdAt");
		q.setLimit(10);
		q.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> li, ParseException e) {
				Conversation c;

				String sMsgType;

				String sMsg;
				Date createDateTime;
				String sSendername;
				String sInternalID;
				boolean isRead;

				if (li != null && li.size() > 0) {
					for (int i = li.size() - 1; i >= 0; i--) {
						ParseObject po = li.get(i);
						ParseFile fileObject;
						sInternalID = po.getObjectId();
						sMsgType = po.getString("msgtype");
						sMsg = po.getString("message");
						createDateTime = po.getCreatedAt();
						sSendername = po.getString("sender");
						fileObject = (ParseFile) po.get("mediafile");
						isRead = po.getBoolean("msgread");

						Log.d(TAG, "Loaded msgType:" + sMsgType);
						Log.d(TAG, "lastMsgDate:" + lastMsgDate + ",createDateTime:" + createDateTime);

						c = new Conversation(sMsgType, sMsg, fileObject, createDateTime, sSendername, isRead);

						if (!isRead) {
							Log.d(TAG, "Message unread!");
							UpdateMsgRead(sInternalID);
						}

						Log.d(TAG,"loadConversationList: add c into convList start");
						convList.add(c);
						Log.d(TAG, "loadConversationList  add c into convList end");
						if (lastMsgDate == null
								|| lastMsgDate.before(c.getDate()))
							lastMsgDate = c.getDate();
						adp.notifyDataSetChanged();
					}
					Log.d(TAG,"loadConversationList done add convList end");
				}
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						if (isRunning)
							loadConversationList();
					}
				}, 1000);
			}
		});
		System.gc();
	}




	/**
	 * The Class ChatAdapter is the adapter class for Chat ListView. This
	 * adapter shows the Sent or Receieved Chat message in each list item.
	 */
	private class ChatAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return convList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Conversation getItem(int arg0)
		{
			return convList.get(arg0);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0)
		{
			return arg0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int pos, View v, ViewGroup arg2)
		{

			int imgHeight,imgWidth;
			CharSequence sDateTime;
			Conversation c = getItem(pos);
			byte[] decodeData;
			BitmapFactory.Options opt;
			Bitmap bmpView;

			Log.d(TAG,"getView triggered, pos:" + pos + "msgtype: "+c.getMsgType());
			if (c.isSent())
				v = getLayoutInflater().inflate(R.layout.chat_item_sent, null);
			else
				v = getLayoutInflater().inflate(R.layout.chat_item_rcv, null);

			LinearLayout layout = (LinearLayout) v.findViewById(R.id.v1);
			TextView lbl = (TextView) v.findViewById(R.id.lbl2);



			if (c.getMsgType().contains(MSG_TYPE_IMAGE))
			{

				ParseFile parseFile;


				//parsefile decode to stream > bitmap
				try
				{
					Log.d(TAG,"Before getparsefile");
					parseFile = c.getparseFile();
					Log.d(TAG,"After getParseFile");

					opt = new BitmapFactory.Options();

					opt.inJustDecodeBounds = false;
					opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
					opt.inSampleSize = 1;

					decodeData = parseFile.getData();
					Log.d(TAG, "decodeData.length:"+decodeData.length);

					//bmpView = BitmapFactory.decodeByteArray(decodeData, 0, decodeData.length);

					bmpView = BitmapFactory.decodeByteArray(decodeData, 0, decodeData.length,opt);


					if (bmpView!=null)
						Log.d(TAG, "bmpView is not null");
					else
						Log.d(TAG, "bmpView is null");

					if (bmpView!=null)
					{
						Drawable mDrawable=new BitmapDrawable(bmpView);
						imgHeight = (460 * bmpView.getHeight())/bmpView.getWidth();
						imgWidth = 460; //bitmap.getWidth()/10;
						layout.getLayoutParams().height = imgHeight;
						layout.getLayoutParams().width = imgWidth;
						layout.setBackground(mDrawable);
						Log.d(TAG, "Drawable....end");
					}

					decodeData = null;

				}
				catch (ParseException e)
				{
					Log.d(TAG, "Parse File Error with "+ e.getMessage());
				}

				lbl.setText("");
				parseFile = null;
			}
			else
			{
				lbl.setText(c.getMsg()); //message content
				Linkify.addLinks(lbl,Linkify.WEB_URLS);
			}

			sDateTime = DateUtils.getRelativeDateTimeString(Chat.this, c
										.getDate().getTime(), DateUtils.SECOND_IN_MILLIS,
								DateUtils.DAY_IN_MILLIS, 0);



			lbl = (TextView) v.findViewById(R.id.lbl3);
			if (c.isSent())
			{
				if (c.getStatus() == Conversation.STATUS_SENT)
					lbl.setText("Delivered, " + sDateTime);
				else if (c.getStatus() == Conversation.STATUS_SENDING)
					lbl.setText("Sending...");
				else
					lbl.setText("Failed");
			}
			else
				lbl.setText("");


			c = null;


			return v;
		}

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			finish();
		}
		if (item.getItemId() == R.id.menu_make_call)
		{
			videoCall();

		}
		if (item.getItemId() == R.id.menu_map)
		{
			sendMap();

		}
		return super.onOptionsItemSelected(item);
	}


	private void updateUserStatus(boolean online)
	{
		UserList.user.put("online", online);
		UserList.user.saveEventually();
	}

	private void UpdateMsgRead(String sInternalID)
	{

		ParseObject po = ParseObject.createWithoutData("concierge", sInternalID);
		po.put("msgread",Conversation.STATUS_READ);



		po.saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				if (e == null) {
					// Saved successfully.
					Log.d(TAG, "Update session id done, start call here");
				} else {
					Log.d(TAG, "Cannot update session id,cannot pickup call");
				}
			}
		});
	}


	void GetSessionID()
	{

		//TODO: Yes/No

		final HashMap<String, Object> params = new HashMap<String, Object>();
		// params.put("movie", "The Matrix");
		ParseCloud.callFunctionInBackground("opentokNewSession", params, new FunctionCallback<String>() {
			public void done(String tmpSessionID, ParseException e) {
				if (e == null) {
					// ratings is 4.5

					//TODO: Message Box

					Log.d(TAG, "Get Session ID:" + tmpSessionID);
					sessionID = tmpSessionID;
					GetPublisherToken();
				} else {
					Log.d(TAG, "Get Session ID Error Found:" + e.getMessage());
					sessionID = "";
				}
			}
		});

	}


	void GetSubscriberToken()
	{
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("sessionId", sessionID);
		ParseCloud.callFunctionInBackground("opentokGenerateToken", params, new FunctionCallback<String>() {
			public void done(String tmpToken, ParseException e) {
				String sToken = "";
				if (e == null) {
					Log.d(TAG, "Get Subscriber Token:" + tmpToken);
					subscriberToken = tmpToken;
					GenerateCall();
				} else {
					Log.d(TAG, "Get Token: Error Found:" + e.getMessage());
					subscriberToken = "";
				}

			}
		});
	}

	void GetPublisherToken() {
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("sessionId", sessionID);
		//  params.put("options",Opentok.ROLE.PUBLISHER);
		ParseCloud.callFunctionInBackground("opentokGenerateToken", params, new FunctionCallback<String>() {
			public void done(String tmpToken, ParseException e) {
				String sToken = "";
				if (e == null) {
					Log.d(TAG, "Get Publisher Token:" + tmpToken);
					publisherToken = tmpToken;
					GetSubscriberToken();
				} else {
					Log.d(TAG, "Get Token: Error Found:" + e.getMessage());
					publisherToken = "";
				}

			}
		});
	}

	private void GenerateCall()
	{


		ParseObject po = new ParseObject("CallSections"); //Parse Web > Core > class name (Installation/Role/Session/User/chat/concierge)

		Log.d(TAG,"Generate Call start: sessionID:"+ sessionID + ", CallerID:"+senderName + ", ReceiverID:" + receiverName + ", PublisherToken:" + publisherToken + ",SubscriberToken:"+subscriberToken);


		po.put("SessionID", sessionID);
		po.put("CallerID",senderName); //tim
		po.put("ReceiverID", receiverName); //ksl
		po.put("PublisherToken",publisherToken);
		po.put("SubscriberToken", subscriberToken);
		po.put("Status", CALL);



		po.saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				if (e == null) {
					DoNotAvailable(senderName);
					//getInternalID(sessionID);
				}
			}
		});
	}

	public void DoNotAvailable(String username)
	{
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", username);
		query.setLimit(1);

		query.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> objects, ParseException e)
			{
				if (objects.size()>0)
				{
					for (ParseObject parseObject:objects)
					{
						parseObject.put("IsUsing",true);
						parseObject.saveInBackground(new SaveCallback() {
							public void done(ParseException e) {
								if (e == null) {
									getInternalID(sessionID);// Saved successfully
								}
							}
						});
					}
				}

			}
		});


	}

	public void DoAvailable(String username) //When user click End Call or Call Terminated
	{
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", username);
		query.setLimit(1);

		Log.d(TAG,"DoAvailable:" + username + " triggered");

		query.findInBackground(new FindCallback<ParseUser>() {
			public void done(List<ParseUser> objects, ParseException e) { //'int java.util.List.size()' on a null object reference
				if (objects.size() > 0) {
					for (ParseObject parseObject : objects) {
						parseObject.put("IsUsing", false);
						parseObject.saveInBackground(new SaveCallback() {
							public void done(ParseException e) {
								if (e == null) {
									Log.d(TAG,"DoAvailable save completed");
								}
							}
						});
					}
				}

			}
		});
	}





	private void getInternalID(String sessionID)
	{

		Log.d(TAG, "handleIncomingCall run");


		ParseQuery<ParseObject> q = ParseQuery.getQuery("CallSections");
		q.whereEqualTo("Status", UIActivity.CALL);
		q.whereEqualTo("SessionID", sessionID);
		q.setLimit(1);

		q.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> li, ParseException e) {
				String internalID;
				String caller;
				String publisherToken, subscriberToken, sessionID;


				if (li != null && li.size() > 0) {
					for (int i = li.size() - 1; i >= 0; i--) {
						ParseObject po = li.get(i);
						internalID = po.getObjectId();//.getString("objectId");
						Call(internalID);
						}
				}
			}
		});
	}


	private void Call(String sInternalID) //For call
	{
		String sSessionID,sPublisherToken,sSubscriberToken;

		sSessionID = sessionID;
		sPublisherToken = publisherToken;
		sSubscriberToken = subscriberToken;


		Call(sSessionID, sPublisherToken,sSubscriberToken,sInternalID);
	}

	private void Call(String sSessionID,String sPublisherToken,String sSubscriberToken,String sInternalID) //For receiving call
	{

		mConnectingDialog.dismiss();
		mConnectingDialog = null;


		Intent intent = new Intent(Chat.this, UIActivity.class);
		intent.putExtra("SESSIONID", sSessionID);
		intent.putExtra("PUBTOKEN", sPublisherToken);
		intent.putExtra("SUBTOKEN", sSubscriberToken);
		intent.putExtra("OBJECTID", sInternalID);
		intent.putExtra("MYNAME", senderName);
		intent.putExtra("PLAYMUSIC", "Yes");
		startActivity(intent);


	}


	public void onLocationChanged(Location location)
	{
		mLat=location.getLatitude();
		mLong = location.getLongitude();
		Log.d(TAG, "GPS Signal Detected -- long:" + mLong + ", mLat:" + mLat);
	}
	public void onProviderDisabled(String provider) {
	}
	public void onProviderEnabled(String provider) {
	}
	public void onStatusChanged(String provider, int status, Bundle extras) {}




}
