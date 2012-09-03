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
 * ���캯����
 * 		(1)����ip & port;���õ�ַ����(��ѡ);��������ID��������;����send����
 * 		(2)����ip & port & msg�� ����send����	
 */
public class UDPSender {
	
	private DatagramSocket dSocket;
	private String dest_ip; //IPv4��ַ
	private int SERVER_PORT; //���Ͷ˿�
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
	 * ������Ϣ
	 */
	public void SetMsg(byte[] msg){
		this.msg = msg;
	}
	/*
	 * �����µ�ַ
	 */
	public void SetAddr(byte[] addr){
		this.addr = addr;
	}
	/**
	 * �����ַλ��������
	 * @param cmd
	 * @param addr
	 * @return
	 */
	public byte[] ChangeAddr(byte[] cmd, byte[] addr){
		System.arraycopy(addr, 0, cmd, 3, 2);
		return cmd;
	}
	
	/*
	 * ����UDP���ݱ�
	 */
	public synchronized boolean send(){
        //���ݱ�����
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
        //���ݱ�����
        try {  
            dSocket.send(dPacket);
        } catch (IOException e) {
            e.printStackTrace();
        	return false;
        }
        //Socket�ر�
        //dSocket.close();
		return true;	
	}
	
	public void StateRequestCmd(int ID){
		//byte [] CMD_INFO = null;
		switch(ID){
		case 0:
		case STATE_REFNODE_TEMPRATURE_A1://��ѯA1�¶�
			SetMsg(REQUEST_TEMPATURE_A1);
			break;
		case 1:
		case STATE_REFNODE_LUMINOSITY_A1://��ѯA1����
			SetMsg(REQUEST_LUMINOSITY_A1);
			break;
		case 2:
		case STATE_REFNODE_TEMPRATURE_A2://��ѯA2�¶�
			SetMsg(REQUEST_TEMPATURE_A2);
			break;
		case 3:
		case STATE_REFNODE_LUMINOSITY_A2://��ѯA2����
			SetMsg(REQUEST_LUMINOSITY_A2);
			break;
		case 4:
		case STATE_REFNODE_GHG_A1://��ѯA1�ɻɹ�״̬
			SetMsg(REQUEST_GHG_A1);
			break;
		case 5:
		case STATE_REFNODE_GHG_A2://��ѯA2�ɻɹ�״̬
			SetMsg(REQUEST_GHG_A2);
			break;
		default:
				break;
		}
	}
	
	//��ȡ�¶�
	private static final int LOCATION_REFNODE_TEMPRATURE    =   0x10;		//�¶�ID
	private static final int STATE_REFNODE_TEMPRATURE_A1    =   0x1001;		//A1�¶�
	private static final int STATE_REFNODE_TEMPRATURE_A2    =   0x1002;		//A2�¶�
	private byte[] REQUEST_TEMPATURE_A1 ={(byte) 0xFF,0x10,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
	private byte[] REQUEST_TEMPATURE_A2 ={(byte) 0xFF,0x10,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
	
	//��ȡ���ն�
	private static final int LOCATION_REFNODE_LUMINOSITY	=	0x11;		//���ն�ID
	private static final int STATE_REFNODE_LUMINOSITY_A1    =   0x1101;		//A1������
	private static final int STATE_REFNODE_LUMINOSITY_A2    =   0x1102;		//A2������
    private byte[] REQUEST_LUMINOSITY_A1 ={(byte) 0xFF,0x11,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
	private byte[] REQUEST_LUMINOSITY_A2 ={(byte) 0xFF,0x11,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
	
	//��ȡ�ɻɹ�״̬
    private static final int LOCATION_REFNODE_GHG	=   0x12;		//�ɻɹ�ID
	private static final int STATE_REFNODE_GHG_A1    =   0x1201;		//A1�¶�
	private static final int STATE_REFNODE_GHG_A2    =   0x1202;		//A2�¶�
    private byte[] REQUEST_GHG_A1 		 ={(byte) 0xFF,0x12,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] REQUEST_GHG_A2  		 ={(byte) 0xFF,0x12,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
   
    //��ȡ����״̬
    private static final int LOCATION_REFNODE_INFRARED		=   0x13;	//�����Ӧ��ID
    private byte[] REQUEST_INFRARED_A3 	 ={(byte) 0xFF,0x13,0x00,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    
    //���øɻɹ�ʹ��
    private static final int LOCATION_ABLE_GHG	=   0x80;		//ʹ�ܸɻɹ�ID
    private byte[] ENABLE_GHG_A1 		 ={(byte) 0xFF,(byte) 0x80,0x01,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] DISABLE_GHG_A1  		 ={(byte) 0xFF,(byte) 0x80,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] ENABLE_GHG_A2 		 ={(byte) 0xFF,(byte) 0x80,0x01,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    private byte[] DISABLE_GHG_A2  		 ={(byte) 0xFF,(byte) 0x80,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    
    //���ú���ʹ��
    private static final int LOCATION_ABLE_INFRARED	=   0x81;		//ʹ�ܺ����Ӧ��ID
    private byte[] ENABLE_INFRARED_A3 	 ={(byte) 0xFF,(byte) 0x81,0x01,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    private byte[] DISABLE_INFRARED_A3	 ={(byte) 0xFF,(byte) 0x81,0x00,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    
    //���÷���
    private static final int LOCATION_REFNOE_FAN	=   0x82;		//����ID
    private byte[] ENABLE_FAN_A1 		 ={(byte) 0xFF,(byte) 0x82,0x01,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] DISABLE_FAN_A1		 ={(byte) 0xFF,(byte) 0x82,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    
    //����̨��
    private static final int LOCATION_REFNOE_LIGHT	=   0x83;		//̨��ID
    private byte[] ENABLE_LIGHT_A2 		 ={(byte) 0xFF,(byte) 0x83,0x01,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    private byte[] DISABLE_LIGHT_A2		 ={(byte) 0xFF,(byte) 0x83,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    
    //���þ���
    private static final int LOCATION_REFNOE_ALERT	=   0x84;		//����ID
    private byte[] ENABLE_ALERT_A3 		 ={(byte) 0xFF,(byte) 0x84,0x01,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    private byte[] DISABLE_ALERT_A3		 ={(byte) 0xFF,(byte) 0x84,0x00,0x00,0x33,0x33,0x33,0x33,0x33,0x33,0x33,0x33,(byte)0xAA};
    
    //���÷�����
    private static final int LOCATION_REFNODE_BUZZER =	0x85;	//����������ID
    private byte[] ENABLE_BUZZER_A1 	 ={(byte) 0xFF,(byte) 0x85,0x01,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] DISABLE_BUZZER_A1 	 ={(byte) 0xFF,(byte) 0x85,0x00,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
    private byte[] ENABLE_BUZZER_A2 	 ={(byte) 0xFF,(byte) 0x85,0x01,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};
    private byte[] DISABLE_BUZZER_A2	 ={(byte) 0xFF,(byte) 0x85,0x00,0x00,0x22,0x22,0x22,0x22,0x22,0x22,0x22,0x22,(byte)0xAA};  
}
