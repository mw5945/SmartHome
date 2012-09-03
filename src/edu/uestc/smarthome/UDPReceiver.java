package edu.uestc.smarthome;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UDPReceiver extends Thread {
	
	private boolean life = true;
	private Handler handler;
	DatagramSocket dSocket;
	protected static final int msg_Receive_Key = 0x4321; 
	
	public UDPReceiver(Handler handler,DatagramSocket dSocket){
		this.handler = handler;
		this.dSocket = dSocket;
	}
	
	/** 
     * @return the life 
     */  
    public boolean isLife() {  
        return life;  
	}
    
    /** 
     * @param life 
     *            the life to set 
     */  
    public void setLife(boolean life) {  
        this.life = life;  
    }

        @Override
	public void run() {
		// TODO Auto-generated method stub
         	//׼�����ܿռ�
        	byte [] recv_buf = new byte [1024];
             DatagramPacket dPacket = new DatagramPacket(recv_buf, recv_buf.length);
                while (life) {//һֱ��ѯ��ֱ���ر�
                	 //�������� 
                	try {
                         dSocket.receive(dPacket); 
                         if(dPacket.getLength()>0){
                        	 byte [] data = new byte[dPacket.getLength()];
                        	 System.arraycopy(dPacket.getData(), 0, data, 0, data.length);
                        	 Message msg = handler.obtainMessage();
                        	 Bundle b = new Bundle();
                        	 b.putByteArray("cmd", data);
                        	 msg.setData(b);
                        	 msg.what = msg_Receive_Key;
                        	 handler.sendMessage(msg);}
                     }catch (IOException e) {  
                    	 //��ʱ
                         e.printStackTrace();  
                     }
                     //ÿ���ѯ5��
                     try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }  //end of while 
         }
        
}
