package edu.uestc.smarthome;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.util.Log;
/*
 * UDPSneder by MQui
 * 构造函数：
 * 		(1)设置ip & port;调用地址设置(可选);调用命令ID设置命令;调用send发送
 * 		(2)设置ip & port & msg； 调用send发送	
 */
public class UDPSender {
	
	private DatagramSocket dSocket;
	private String dest_ip; //IPv4地址
	private int SERVER_PORT; //发送端口
	private byte[] addr = null;
	private byte[] msg = null;
	DatagramPacket dPacket;
	
	public UDPSender() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	public UDPSender(String dest_ip, int SERVER_PORT,DatagramSocket dSocket){
		super();
		this.dSocket = dSocket;
		this.dest_ip = dest_ip;
		this.SERVER_PORT = SERVER_PORT;
	}
	
	/*
	 * 设置消息
	 */
	public void SetMsg(byte[] msg){
		this.msg = msg;
	}
	/*
	 * 设置新地址
	 */
	public void SetAddr(byte[] addr){
		this.addr = addr;
	}
	/**
	 * 命令地址位更改设置
	 * @param cmd
	 * @param addr
	 * @return
	 */
	public byte[] ChangeAddr(byte[] cmd, byte[] addr){
		System.arraycopy(addr, 0, cmd, 3, 2);
		return cmd;
	}
	
	/*
	 * 发送UDP数据报
	 */
	public synchronized boolean send(){
        //数据报生成
		dPacket = new DatagramPacket(msg,msg.length);
        try {
        	dPacket.setLength(msg.length);
			dPacket.setAddress(InetAddress.getByName(dest_ip));
			dPacket.setPort(SERVER_PORT);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
        //数据报发送
        try {  
            dSocket.send(dPacket);
        } catch (IOException e) {
            e.printStackTrace();
        	return false;
        }
        //Socket关闭
        //dSocket.close();
		return true;	
	}
	
	public void StateRequestCmd(int ID){
		//byte [] CMD_INFO = null;
		switch(ID){
		case 0:
		case STATE_REFNODE_TEMPRATURE_A1://查询A1温度
			SetMsg(REQUEST_TEMPATURE_A1);
			break;
		case 1:
		case STATE_REFNODE_LUMINOSITY_A1://查询A1亮度
			SetMsg(REQUEST_LUMINOSITY_A1);
			break;
		case 2:
		case STATE_REFNODE_TEMPRATURE_A2://查询A2温度
			SetMsg(REQUEST_TEMPATURE_A2);
			break;
		case 3:
		case STATE_REFNODE_LUMINOSITY_A2://查询A2亮度
			SetMsg(REQUEST_LUMINOSITY_A2);
			break;
		case 4:
		case STATE_REFNODE_GHG_A1://查询A1干簧管状态
			SetMsg(REQUEST_GHG_A1);
			break;
		case 5:
		case STATE_REFNODE_GHG_A2://查询A2干簧管状态
			SetMsg(REQUEST_GHG_A2);
			break;
		default:
				break;
		}
	}
	
	//读取温度
	private static final int LOCATION_REFNODE_TEMPRATURE    =   0x10;		//温度ID
	private static final int STATE_REFNODE_TEMPRATURE_A1    =   0x1001;		//A1温度
	private static final int STATE_REFNODE_TEMPRATURE_A2    =   0x1002;		//A2温度
	private byte[] REQUEST_TEMPATURE_A1 ={(byte) 0xFF,0x10,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
	private byte[] REQUEST_TEMPATURE_A2 ={(byte) 0xFF,0x10,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
	
	//读取光照度
	private static final int LOCATION_REFNODE_LUMINOSITY	=	0x11;		//光照度ID
	private static final int STATE_REFNODE_LUMINOSITY_A1    =   0x1101;		//A1光亮度
	private static final int STATE_REFNODE_LUMINOSITY_A2    =   0x1102;		//A2光亮度
    private byte[] REQUEST_LUMINOSITY_A1 ={(byte) 0xFF,0x11,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
	private byte[] REQUEST_LUMINOSITY_A2 ={(byte) 0xFF,0x11,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
	
	//读取干簧管状态
    private static final int LOCATION_REFNODE_GHG	=   0x12;		//干簧管ID
	private static final int STATE_REFNODE_GHG_A1    =   0x1201;		//A1温度
	private static final int STATE_REFNODE_GHG_A2    =   0x1202;		//A2温度
    private byte[] REQUEST_GHG_A1 		 ={(byte) 0xFF,0x12,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] REQUEST_GHG_A2  		 ={(byte) 0xFF,0x12,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
   
    //读取红外状态
    private static final int LOCATION_REFNODE_INFRARED		=   0x13;	//红外感应器ID
    private byte[] REQUEST_INFRARED_A3 	 ={(byte) 0xFF,0x13,0x00,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    
    //设置干簧管使能
    private static final int LOCATION_ABLE_GHG	=   0x80;		//使能干簧管ID
    private byte[] ENABLE_GHG_A1 		 ={(byte) 0xFF,(byte) 0x80,0x01,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] DISABLE_GHG_A1  		 ={(byte) 0xFF,(byte) 0x80,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] ENABLE_GHG_A2 		 ={(byte) 0xFF,(byte) 0x80,0x01,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    private byte[] DISABLE_GHG_A2  		 ={(byte) 0xFF,(byte) 0x80,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    
    //设置红外使能
    private static final int LOCATION_ABLE_INFRARED	=   0x81;		//使能红外感应器ID
    private byte[] ENABLE_INFRARED_A3 	 ={(byte) 0xFF,(byte) 0x81,0x01,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    private byte[] DISABLE_INFRARED_A3	 ={(byte) 0xFF,(byte) 0x81,0x00,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    
    //设置风扇
    private static final int LOCATION_REFNOE_FAN	=   0x82;		//风扇ID
    private byte[] ENABLE_FAN_A1 		 ={(byte) 0xFF,(byte) 0x82,0x01,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] DISABLE_FAN_A1		 ={(byte) 0xFF,(byte) 0x82,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    
    //设置台灯
    private static final int LOCATION_REFNOE_LIGHT	=   0x83;		//台灯ID
    private byte[] ENABLE_LIGHT_A2 		 ={(byte) 0xFF,(byte) 0x83,0x01,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    private byte[] DISABLE_LIGHT_A2		 ={(byte) 0xFF,(byte) 0x83,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    
    //设置警报
    private static final int LOCATION_REFNOE_ALERT	=   0x84;		//警报ID
    private byte[] ENABLE_ALERT_A3 		 ={(byte) 0xFF,(byte) 0x84,0x01,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    private byte[] DISABLE_ALERT_A3		 ={(byte) 0xFF,(byte) 0x84,0x00,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    
    //设置蜂鸣器
    private static final int LOCATION_REFNODE_BUZZER =	0x85;	//蜂鸣器控制ID
    private byte[] ENABLE_BUZZER_A1 	 ={(byte) 0xFF,(byte) 0x85,0x01,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] DISABLE_BUZZER_A1 	 ={(byte) 0xFF,(byte) 0x85,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] ENABLE_BUZZER_A2 	 ={(byte) 0xFF,(byte) 0x85,0x01,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    private byte[] DISABLE_BUZZER_A2	 ={(byte) 0xFF,(byte) 0x85,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};  
}
