package com.chatt.demo.model;

import android.graphics.Bitmap;
import android.media.Image;

import java.util.Date;

import com.chatt.demo.UserList;
import com.parse.ParseFile;

/**
 * The Class Conversation is a Java Bean class that represents a single chat
 * conversation message.
 */
public class Conversation
{

	/** The Constant STATUS_SENDING. */
	public static final int STATUS_SENDING = 0;

	/** The Constant STATUS_SENT. */
	public static final int STATUS_SENT = 1;

	/** The Constant STATUS_FAILED. */
	public static final int STATUS_FAILED = 2;

	public static final boolean STATUS_READ = true;

	public static final boolean STATUS_UNREAD = false;

	/** The msg. */
	private String msg;

	/** The status. */
	private int status = STATUS_SENT;

	/** The date. */
	private Date date;

	/** The sender. */
	private String sender;

	private String msgType;
	//private Bitmap bmp;
	private ParseFile parseFile;

	private boolean msgread;

	public Conversation(String msg, Date date, String sender)
	{
		this.msg = msg;
		this.date = date;
		this.sender = sender;
	}

	/**
	 * Instantiates a new conversation.
	 */
	//public Conversation(String msgType,String msg,Bitmap bmp,Date date,String sender)
	public Conversation(String msgType,String msg,ParseFile parseFile,Date date,String sender,boolean msgread)
	{
		this.msgType = msgType;
		this.msg = msg;
		this.date = date;
		this.sender = sender;
		this.parseFile = parseFile;
		this.msgread = msgread;

	}

	/**
	 * Gets the msg.
	 * 
	 * @return the msg
	 */

	public String getMsgType()
	{
		return msgType;
	}
	public void setMsgType(String msgType)
	{
		this.msgType = msgType;
	}

	public ParseFile getparseFile(){
		return parseFile;
	}

	public void setparseFile(ParseFile parseFile)
	{

		this.parseFile = parseFile;

	}

	public boolean getMsgRead()
	{
		return msgread;
	}

	public void setMsgRead(boolean msgread)
	{
		this.msgread = msgread;
	}


	public String getMsg()
	{
		return msg;
	}

	/**
	 * Sets the msg.
	 * 
	 * @param msg
	 *            the new msg
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	/**
	 * Checks if is sent.
	 * 
	 * @return true, if is sent
	 */
	public boolean isSent()
	{
		return UserList.user.getUsername().equals(sender);
	}

	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * Sets the date.
	 * 
	 * @param date
	 *            the new date
	 */
	public void setDate(Date date)
	{
		this.date = date;
	}

	/**
	 * Gets the sender.
	 * 
	 * @return the sender
	 */
	public String getSender()
	{
		return sender;
	}

	/**
	 * Sets the sender.
	 * 
	 * @param sender
	 *            the new sender
	 */
	public void setSender(String sender)
	{
		this.sender = sender;
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(int status)
	{
		this.status = status;
	}

}
