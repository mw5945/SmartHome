package edu.uestc.smarthome;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AnalogClock;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.view.GestureDetector;

public class SmartHome extends Activity implements Runnable {
	TextView temp ; 
	TextView light ;
	TextView temp2 ; 
	TextView light2 ;
   //����ģ��
	UDPSender us;
	DatagramSocket dSocket;
	UDPReceiver URTh;
	private static final int PORT = 55555;
	//ʱ��ģ��
	private Calendar myCalendar;
	private TextView CurrentDate;
	//�������
	private Thread mThread;
	private long ReqFre = 500;
	private boolean ThLife;
	//��ť��Ӧ
	ImageButton temp_bt,light_bt,beeper_bt,wave_bt;
	boolean fan_state,light_state,beeper_state_1,beeper_state_2,wave_state_tp;
	//��������ģ��
	PopupWindow mPopWindowFan,mPopWindowLight;
	private View mMenuView;
	ImageButton PopWndFanBT,PopWndLightBT,BeeperBT;
	ImageButton PopWndFanBT2,PopWndLightBT2,BeeperBT2;
	//����Ч��
	private Animation slideLeftIn;  
    private Animation slideLeftOut;  
    private Animation slideRightIn;  
    private Animation slideRightOut;
	private ViewFlipper flipper;  
	private GestureDetector detector;  
	//��ǰҳ��
	private int CurrentPage;
	//������״̬
	private boolean bInfrared;
	private Object mLock;
	private boolean bScan;
	private ImageView State,State2;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //A1 �¶ȡ����ն�
        temp  = (TextView)findViewById(R.id.tvTempNum);
        light = (TextView)findViewById(R.id.tvLightNum);
        //A2 �¶ȡ����ն�
        temp2  = (TextView)findViewById(R.id.tvTempNum2);
        light2 = (TextView)findViewById(R.id.tvLightNum2);
        State = (ImageView)findViewById(R.id.WarningState);
        State2 = (ImageView)findViewById(R.id.WarningState2);
        //��������
        myCalendar=Calendar.getInstance();
        int month=myCalendar.get(Calendar.MONTH)+1;
        String date= myCalendar.get(Calendar.YEAR)+"-"+month+"-"+myCalendar.get(Calendar.DAY_OF_MONTH);
        CurrentDate = (TextView)findViewById(R.id.tvCurrentDate);
        CurrentDate.setText(date);
        CurrentDate = (TextView)findViewById(R.id.tvCurrentDate2);
        CurrentDate.setText(date);
       
        //�󶨰�ť����
        temp_bt = (ImageButton)findViewById(R.id.temp_control);
        temp_bt.setOnClickListener(ButtonListener);
        fan_state = true;

        light_bt=(ImageButton)findViewById(R.id.bright_control);
        light_bt.setOnClickListener(ButtonListener);
        light_state = false;
        
        beeper_bt=(ImageButton)findViewById(R.id.beeper_control);
        beeper_bt.setOnClickListener(ButtonListener);
        beeper_state_1 = false;
        beeper_state_2 = false;
      
        wave_bt =(ImageButton)findViewById(R.id.wave_control) ;
        wave_bt.setOnClickListener(ButtonListener);
       
        //��ʼ�� Socket
		try {
			dSocket = new DatagramSocket(PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//���� UDP Sender
		us = new UDPSender("192.168.43.22",55555,dSocket);
        //����UDP Receiver�����߳�
		URTh = new UDPReceiver(myHandler,dSocket);
		URTh.start();
		
		//ʱ��ģ���߳� - Ĭ��Ϊɨ�跢��Ϊ��
		mThread=new Thread(this);
		mThread.start();
		ThLife = true;
		bScan = true;
		
		//���õ���
		initPopupWindow();
		PopWndFanBT=(ImageButton)findViewById(R.id.IVtempcontrol);
		PopWndFanBT.setOnClickListener(ButtonListener);
		PopWndLightBT=(ImageButton)findViewById(R.id.IVbright);
		PopWndLightBT.setOnClickListener(ButtonListener);
		BeeperBT = (ImageButton)findViewById(R.id.IVbeeper);
		BeeperBT.setOnClickListener(ButtonListener);
		PopWndFanBT2=(ImageButton)findViewById(R.id.IVtempcontrol2);
		PopWndFanBT2.setOnClickListener(ButtonListener);
		PopWndLightBT2=(ImageButton)findViewById(R.id.IVbright2);
		PopWndLightBT2.setOnClickListener(ButtonListener);
		BeeperBT2 = (ImageButton)findViewById(R.id.IVbeeper2);
		BeeperBT2.setOnClickListener(ButtonListener);
		//����Ч��
		CurrentPage = 0;
		flipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		detector =  new GestureDetector(new GestureDetectorListener());
		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        //Ĭ�Ϻ���Ϊ�ر�
        bInfrared = false;
    }
    
    /**
     * HexString(ʮ��λ���Ʒ���ʾ��String)ת��Ϊbytes
     */
    public byte[] HexString2Bytes(String sString) 
    { 
    byte[] result = new byte[sString.length()/2]; 
    byte[] temp = sString.getBytes(); 
    
    for(int i=0; i<result.length; ++i ) 
    result[i] = uniteBytes(temp[i*2], temp[i*2+1]); 
    
    return result; 
    }

    private byte uniteBytes(byte src0, byte src1) {
         byte _b0 = Byte.decode("0x" + new String(new byte[] {src0})).byteValue();
         _b0 = (byte) (_b0 << 4);
         byte _b1 = Byte.decode("0x" + new String(new byte[] {src1})).byteValue();
         byte result = (byte) (_b0 | _b1);
         return result;
    } 
    
    
    /**
     * bytesת����int
     */
    public  static int bytesToInt(byte[] bytes) {
    	if(bytes.length >4) return 0;//���ݹ���
    	int addr = bytes[0] & 0xFF;
        if(bytes.length>=2)addr |= ((bytes[1] << 8) & 0xFF00);
        if(bytes.length>=3)addr |= ((bytes[2] << 16) & 0xFF0000);
        if(bytes.length==4)addr |= ((bytes[3] << 24) & 0xFF000000);
        return addr;
    }
    
    /**
     * bytesת����ʮ�������ַ���
     */
    public String byte2HexStr(byte[] b) {
        String hs="";
        String stmp="";
        for (int n=0;n<b.length;n++) {
            stmp=(Integer.toHexString(b[n] & 0XFF));
            if (stmp.length()==1) hs=hs+"0"+stmp;
            else hs=hs+stmp;
        }
        return hs.toUpperCase();
    }
    
    /**
     * ����bytes���ݶζ�ȡ����
     */
    public byte[] bytesDataTrim(byte[] b,int pos){
    	byte[] Data = new byte[2];
    	System.arraycopy(b, pos, Data, 0, 2);
    	return Data;    	
    }


    /*
     * DS18B20�¶�ת������
     * by MQui : mw5945@gmail.com
     * 2011.11.23 
     */
    public String temp_bytes2double(byte[] bytes){
    	byte byteL = bytes[0];
    	byte byteH = bytes[1];
    	DecimalFormat DF =new DecimalFormat("#0.000");
    	int idata;
    	String Strdata;
    	byte pos = (byte) (byteH & 0xF0);//��֤������
    	if(pos == 0){
    		idata = byteL & 0xFF;
            idata |= ((byteH << 8) & 0x0F00);
    	}else {
    		/*
    		 * ��֪һ�������Ĳ��룬����ת��Ϊʮ�����������裺
      		 * 1���ȶԸ�λȡ����
      		 * 2������ת��Ϊʮ��������
      		 * 3�����ϸ��ţ��ټ�ȥ1�� 
    		 */
    		idata = ~byteL & 0xFF;
    		idata |= ((~byteH << 8) & 0x0F00);
    		idata = (-idata)-1;
    	}
    	
    	Strdata = DF.format(idata*0.0625);
    	
    	return Strdata;
    }
    /*
     * ���նȻ��㺯��
     */
    public String light_bytes2double(byte[] bytes){
        byte byteL = bytes[0];
        byte byteH = bytes[1];
        DecimalFormat DF =new DecimalFormat("#0.00");
        int idata;
        double data;
        String Strdata;
        
           idata = byteL;
           idata |= ((byteH<<8) & 0xFF00) ;
           idata = idata>>2;
           data = idata/88;
           
           Strdata = DF.format(data);     
        
        return Strdata;
       }
    
    private OnClickListener ButtonListener =new OnClickListener(){
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			//���ȿ���
			if(v == temp_bt){
				if(fan_state == false){
					us.SetMsg(ENABLE_FAN_A1);
					us.send();
					PopWndFanBT.setImageResource(R.drawable.temp_control_info_on_selector);
					PopWndFanBT2.setImageResource(R.drawable.temp_control_info_on_selector2);
					fan_state=true;
				}
				else {
					us.SetMsg(DISABLE_FAN_A1);
					us.send();
					fan_state=false;
					PopWndFanBT.setImageResource(R.drawable.temp_control_info_off_selector);
					PopWndFanBT2.setImageResource(R.drawable.temp_control_info_off_selector2);
				}
			}
			
			//�ƿ���
			if(v == light_bt){
				if(light_state == false){
					us.SetMsg(ENABLE_LIGHT_A2);
					us.send();
					light_state=true;
					PopWndLightBT.setImageResource(R.drawable.bright_info_on_selector);
					PopWndLightBT2.setImageResource(R.drawable.bright_info_on_selector2);
				}
				else {
					us.SetMsg(DISABLE_LIGHT_A2);
					us.send();
					light_state=false;
					PopWndLightBT.setImageResource(R.drawable.bright_info_off_selector);
					PopWndLightBT2.setImageResource(R.drawable.bright_info_off_selector2);
				}
			}
			
			//����������
			if(v == beeper_bt){
				switch(CurrentPage){

				case 0://������A1����
					if(beeper_state_1 == false){
						us.SetMsg(ENABLE_BUZZER_A1);
						us.send();
						beeper_state_1=true;
						BeeperBT.setImageResource(R.drawable.beeper_info_on_selector);
					}else{ 
						us.SetMsg(DISABLE_BUZZER_A1);
						us.send();
						beeper_state_1=false;
						BeeperBT.setImageResource(R.drawable.beeper_info_off_selector);
						}
					break;

				case 1://������A2����
					if(beeper_state_2 == false){
						us.SetMsg(ENABLE_BUZZER_A2);
						beeper_state_2=true;
						us.send();
						BeeperBT2.setImageResource(R.drawable.beeper_info_on_selector2);
					}else{ 
						us.SetMsg(DISABLE_BUZZER_A2);
						beeper_state_2=false;
						us.send();
						BeeperBT2.setImageResource(R.drawable.beeper_info_off_selector2);}
					break;
				}
			}
			
			//����ͼ
			if(v == wave_bt){
				
				ThLife = false;
				mThread.stop();
				URTh.setLife(false);
				URTh.stop();
				try{
					dSocket.close();
			    }
				catch (Exception e) {
					e.printStackTrace();
			    }
				
				Intent RealtimeChart = new Intent(getApplicationContext(),RealtimeChart.class);
				startActivity(RealtimeChart);
			}
			
			//�������ô���
			if(v == PopWndFanBT || v == PopWndFanBT2){
				ShowPopWindow(PopWndFanBT);
			}
			//�������ô���
			if(v == PopWndLightBT || v == PopWndLightBT2){
				//ShowPopWindow(PopWndLightBT);
			}
			
			if(v == BeeperBT || v == BeeperBT2){
				bAlert = false;
    			bAlert2 = false;
    			us.SetMsg(DISABLE_ALERT_A3);
    			us.send();
			}
			//ʹ�ܿ���
			
		}
 
    };
    
    /* ͨ��Handler������UDP Rceiver�߳���Ϣ���������ݵ���Ϣ*/
    private Handler myHandler = new Handler()
    {
    	@Override
    	synchronized public void handleMessage(Message msg) {
    		// TODO Auto-generated method stub
				//��ȡ��Ϣ
	    		byte[] msg_data = new byte[msg.getData().getByteArray("cmd").length];
	    		msg_data = msg.getData().getByteArray("cmd");
	    		/**
	    		 * ��ȡ��ַ,�����ĵ����壬�յ������ݸ�ʽ
	    		 * Ϊ��5-12λbyte�ֽڶ���Ϊ��ַ��ֻ��ȡ��5λ
	    		 */
	    		byte cmd_type = msg_data[1];//����ID
	    		byte cmd_addr = msg_data[6];//��ַ��Ϣ
	    		byte[] cmd_data = new byte[2];
	    		System.arraycopy(msg_data, 2, cmd_data, 0, 2);
	    		ImageButton ImBt;
	    		if(cmd_addr == 0x11)//A1�ڵ���Ϣ
	    			switch(cmd_type){
	    				case LOCATION_REFNODE_TEMPRATURE://�¶�ID
	    					temp.setText(temp_bytes2double(cmd_data)+"��");
	    					//System.out.println(cmd_data);
	    					break;
	    				case LOCATION_REFNODE_LUMINOSITY://���ն�ID
	    					light.setText(light_bytes2double(cmd_data)+"LM");
	    					//System.out.println(cmd_data);
	    					break;
	    				case (byte) LOCATION_REFNODE_BUZZER://A1����������ID
	    					break;
	    				case (byte) LOCATION_REFNODE_GHG://A1�ɻɹ�ID
	    					if(bytesToInt(cmd_data)!= 0)
	    						{Door_1_Opened = true;
	    						State.setImageResource(R.drawable.stateon);}
	    					else {Door_1_Opened = false;
	    						State.setImageResource(R.drawable.stateoff);
	    						}
	    					break;
	    				case (byte) LOCATION_REFNOE_FAN://����״̬�ı�
	    					break;
	    				default:
	    					//error
	    					break;}
	    		
	    		if(cmd_addr == 0x22)//A2�ڵ���Ϣ
	    			switch(cmd_type){
	    				case LOCATION_REFNODE_TEMPRATURE://�¶�ID
	    					temp2.setText(temp_bytes2double(cmd_data)+"��");
	    					//System.out.println(cmd_data);
	    					break;
	    				case LOCATION_REFNODE_LUMINOSITY://���ն�ID
	    					light2.setText(light_bytes2double(cmd_data)+"LM");
	    					//System.out.println(cmd_data);
	    					break;
	    				case (byte) LOCATION_REFNODE_BUZZER://A2����������ID
	    					break;
	    				case (byte) LOCATION_REFNODE_GHG://A2�ɻɹ�ID
	    					if(bytesToInt(cmd_data)!= 0)
	    						{Door_2_Opened = true;
	    						State2.setImageResource(R.drawable.stateon);}
	    					else {
	    						Door_2_Opened = false;
	    						State2.setImageResource(R.drawable.stateoff);
	    						}
	    					break;
	    				case (byte) LOCATION_REFNOE_LIGHT://̨��״̬�ı�
	    					break;
	    				default:
	    					break;
	    		}
	    		
	    		if(cmd_addr == 0x33){//A3�ڵ���Ϣ
	    			System.out.println(msg_data);
	    			switch(cmd_type){
	    				case LOCATION_REFNODE_INFRARED:
	    					if(bytesToInt(cmd_data)!=0){
	    						//�յ����ⱻ����
	    						bAlert = true;
	    						bAlert = true;
	    						us.SetMsg(ENABLE_ALERT_A3);
	    						us.send();
	    						System.out.println(bytesToInt(cmd_data));}
	    					break;
	    				default:
	    					break;
	    			}
	    		}
	    		
    		super.handleMessage(msg);
    	}
    };
	    
	//��������ģ���ʼ��
	public void initPopupWindow(){
		Context mContext = SmartHome.this;
		LayoutInflater mInflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		
		//�¶��õ���
		mMenuView = mInflater.inflate(R.layout.menu_temp, null);
		mPopWindowFan = new PopupWindow(mMenuView,
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		mPopWindowFan.setFocusable(true);	// ���û�л�ý���menu�˵��еĿؼ��¼��޷���Ӧ
		mPopWindowFan.setBackgroundDrawable(dw);
		mPopWindowFan.setAnimationStyle(android.R.style.Animation_Toast);
		
		//���õ���
		mMenuView = mInflater.inflate(R.layout.menu_light, null);
		mPopWindowLight = new PopupWindow(mMenuView,
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		mPopWindowLight.setFocusable(true);
		mPopWindowLight.setBackgroundDrawable(dw);
		mPopWindowLight.setAnimationStyle(android.R.style.Animation_Toast);  
	
	}
	
	public void ShowPopWindow(View v){
		
		if(v == PopWndFanBT){
			if(mPopWindowFan.isShowing())
				mPopWindowFan.dismiss();
			else {
				mPopWindowFan.showAtLocation(findViewById(R.id.root),Gravity.CENTER, 0, 0);
			}
		}
		if(v == PopWndLightBT){
			if(mPopWindowLight.isShowing())
				mPopWindowLight.dismiss();
			else {
				mPopWindowLight.showAtLocation(findViewById(R.id.root),Gravity.CENTER, 0, 0);
			}
		}
	
	}
	private boolean Door_1_Opened = false;
	int timeout = -1;
	private boolean Door_2_Opened = false;
	int timeout2 = -1;
	private boolean bAlert = false;
	private int timeout3 = -1;
	private boolean bAlert2 = false;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			do
			{	//2���ڵ��¶ȡ����Ȳ�ѯ
				if(bScan==true){
				for(int StateReq=0;StateReq<4;StateReq++){
					us.StateRequestCmd(StateReq);
					us.send();
					Thread.sleep(ReqFre);}	
				}

				//����A1������5����
				if(Door_1_Opened == true) {
					Door_1_Opened = false;
					timeout = 5;
					us.SetMsg(ENABLE_BUZZER_A1);
					us.send();
					Thread.sleep(ReqFre);
					}
				
				if(timeout > 0){timeout--;Thread.sleep(ReqFre);}
				if(timeout == 0 ){
					timeout = -1;
					us.SetMsg(DISABLE_BUZZER_A1);
					us.send();
					Thread.sleep(ReqFre);}
				
				//����A2������5����
				if(Door_2_Opened == true) {
					Door_2_Opened = false;
					timeout2 = 5;
					us.SetMsg(ENABLE_BUZZER_A2);
					us.send();
					Thread.sleep(ReqFre);
					}
				
				if(timeout2 > 0){timeout2--;Thread.sleep(ReqFre);}
				if(timeout2 == 0){
					timeout2 = -1;
					us.SetMsg(DISABLE_BUZZER_A2);
					us.send();
					Thread.sleep(ReqFre);}
				
			}
			while(Thread.interrupted()==false && (ThLife == true));/* ��ϵͳ�����ж���Ϣʱֹͣ��ѭ��*/
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return detector.onTouchEvent(event);
    }
    
    private class GestureDetectorListener extends SimpleOnGestureListener {
    	private static final int SWIPE_MIN_DISTANCE = 120;
    	private static final int SWIPE_MAX_OFF_PATH = 250;
    	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    	
    	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && CurrentPage == 0) {
				CurrentPage =1;
				flipper.setInAnimation(slideLeftIn);  
                flipper.setOutAnimation(slideLeftOut);
				flipper.showNext();

			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY && CurrentPage == 1) {
				CurrentPage =0;
				flipper.setInAnimation(slideRightIn);  
                flipper.setOutAnimation(slideRightOut);
				flipper.showPrevious();
			}
		} catch (Exception e) {
			// nothing
		}
		return false;}

    }
    
      public boolean onCreateOptionsMenu(Menu menu){

    	  	if(bScan)
    	  		menu.add(Menu.NONE, 1, 1, "�ر�ɨ��").setIcon(android.R.drawable.star_big_on);
    	  	else
    	  		menu.add(Menu.NONE, 1, 1, "����ɨ��").setIcon(android.R.drawable.star_big_off);
   	  		 	
    	  	if(bInfrared)
   	  		 menu.add(Menu.NONE, 2, 2, "�رպ�����").setIcon(android.R.drawable.star_big_on);
    	  	else
   	  		 menu.add(Menu.NONE, 2, 2, "����������").setIcon(android.R.drawable.star_big_off);
    	  	
      	  	 menu.add(Menu.NONE, 3, 3, "�رվ���").setIcon(android.R.drawable.ic_lock_silent_mode_off);
    	  	
	    	return true;
	    }
	    
	    public boolean onOptionsItemSelected(MenuItem item){
	    	switch (item.getItemId()){
	    	
	    	case 1:
	    		if(bScan){
	    			Toast.makeText(this, "�ر�ɨ��", Toast.LENGTH_LONG).show();
	    			bScan = false;}
	    		else{
	    			Toast.makeText(this, "����ɨ��", Toast.LENGTH_LONG).show();
	    			bScan = true;
	    			}
	    		break;
	    	
	    	case 2:
	    		if(bInfrared){
	    			Toast.makeText(this, "�������ѹر�", Toast.LENGTH_LONG).show();
	    			bInfrared = false;
	    			us.SetMsg(DISABLE_INFRARED_A3);
	    			us.send();}
	    		else{
	    			Toast.makeText(this, "������������", Toast.LENGTH_LONG).show();
	    			bInfrared = true;
	    			us.SetMsg(ENABLE_INFRARED_A3);
	    			us.send();
	    		}
	    		break;
	    	
	    	case 3:
	    			Toast.makeText(this, "�����ѹر�", Toast.LENGTH_LONG).show();
	    			bAlert = false;
	    			bAlert2 = false;
	    			us.SetMsg(DISABLE_ALERT_A3);
	    			us.send();
	    			break;
	    	}
	    			
	    	return false;
	    	
	    }
	    
	    @Override   
	    public boolean onPrepareOptionsMenu(Menu menu){
	    	super.onPrepareOptionsMenu(menu);
	    	
	    	MenuItem menuItem = menu.findItem(1);
	    	if(!bScan)
	    		menuItem.setTitle("����ɨ��").setIcon(android.R.drawable.star_big_off);
	    	else
	    		menuItem.setTitle("�ر�ɨ��").setIcon(android.R.drawable.star_big_on);
	    	
	    	menuItem = menu.findItem(2);
	    	if(bInfrared)
	    		menuItem.setTitle("�رպ�����").setIcon(android.R.drawable.star_big_on);
	    	else
	    		menuItem.setTitle("����������").setIcon(android.R.drawable.star_big_off);
	    	return true;   
	    	
	    }
    
    protected void onStop(){
    	super.onStop();
    	//�ر��߳�
    	ThLife = false;
    	mThread.stop();
    	URTh.setLife(false);
    	URTh.stop();
    	//�ر�socket
		try{
			dSocket.close();
	    }
		catch (Exception e) {
			e.printStackTrace();
	    }
    }
    
    private static final int TAS_RD_REED_EN        =    0x0070;  //read reed enable 
    private static final int TAS_RD_INFRARED_EN    =    0x0071;  //read infrared enable 
    private static final int TAS_RD_FAN            =    0x0072;  //read fan state
    private static final int TAS_RD_LIGHT          =    0x0073;  //read light
    private static final int TAS_RD_ALARM          =   	0x0074;  //read alarm 
    private static final int TAS_RD_BUZZER		   =   	0x0075; //read buzzer	
    
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
    private byte[] ENABLE_FAN_A1	 ={(byte) 0xFF,(byte) 0x82,0x01,0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11,(byte)0xAA};
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