package edu.uestc.smarthome;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;



public class RealtimeChart extends Activity implements Runnable{
	XYSeries series; //����
	XYMultipleSeriesDataset dataset;//��������
	XYMultipleSeriesRenderer renderer;//��Ⱦ
	GraphicalView chart;
	List<double[]> x = new ArrayList<double[]>(); //X����������
    List<double[]> y = new ArrayList<double[]>(); //Y����������
    boolean bRoom;
    int roomNum;
    private double XarrayT;
	private double XarrayL;
    //����ģ��
    UDPSender us;
    DatagramSocket dSocket;
	UDPReceiver URTh;
	private boolean ThLife;
	private static final int PORT = 55555;
	private Thread mThread;

    @Override 
	    public void onCreate(Bundle savedInstanceState) { 
	        super.onCreate(savedInstanceState); 
	        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
	        String[] titles = new String[] { "�¶�", "������"}; 
	        //��ʼ���¶�����
	        x.add(new double[] {} ); 
	        y.add(new double[] {}); 
	        //��ʼ����������
	        x.add(new double[] {} ); 
	        y.add(new double[] {}); 
	        //��ʼ����������
	        dataset = buildDataset(titles, x, y); 
	        //��ʼ��Y��
	        XarrayT = 0;
	        XarrayL = 0;
	        //���û���������ʽ
	        int[] colors = new int[] { Color.BLUE, Color.GREEN}; 
	        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND}; 
	        renderer = buildRenderer(colors, styles, true); 
	        //��������ͼ��Ϣ
	        setChartSettings(renderer, "�¶�/������-ʵʱ����ͼ ��������", "ʱ��", "��/lm", 60, 0, -50, 150 , Color.WHITE, Color.WHITE); 
	        chart = ChartFactory.getLineChartView(this, dataset, renderer); 
	        setContentView(chart); 
	        
	        //��ʼ������ROOM_1�ڵ��¶�
	        bRoom = true;
	       
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
			
			//���÷����߳�
			ThLife = true;
			mThread = new Thread(this);
			mThread.start();

	        
	    } 

	    protected XYMultipleSeriesDataset buildDataset(String[] titles, 
	                                                   List<double[]> xValues, 
	                                                   List<double[]> yValues) 
	    { 
	    	//���߳�ʼ��
	        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset(); 
	        
	        int length = titles.length;                  //�м����� 
	        for (int i = 0; i < length; i++) 
	        { 
	            TimeSeries series = new TimeSeries(titles[i]);    //����ÿ���ߵ����ƴ��� 
	              double[] xV = (double[]) xValues.get(i);                 //��ȡ��i���ߵ����� 
	              double[] yV = (double[]) yValues.get(i); 
	              int seriesLength = xV.length;                 //�м����� 

	              for (int k = 0; k < seriesLength; k++)        //ÿ�������м����� 
	         { 
	                series.add(xV[k], yV[k]); 
	            } 
	            dataset.addSeries(series); 
	        } 

	        return dataset; 
	    } 

	    protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles, boolean fill) 
	    { 
	        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(); 
	        int length = colors.length; 
	        for (int i = 0; i < length; i++) 
	        { 
	            XYSeriesRenderer r = new XYSeriesRenderer(); 
	            r.setColor(colors[i]); 
	            r.setPointStyle(styles[i]); 
	            r.setFillPoints(fill); 
	            renderer.addSeriesRenderer(r); 
	        } 
	        return renderer; 
	    } 

	    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, 
	                                String xTitle,String yTitle, double xMin, 
	                                double xMax, double yMin, double yMax, 
	                                int axesColor,int labelsColor) 
	    { 
	        renderer.setChartTitle(title); 
	        renderer.setXTitle(xTitle); 
	        renderer.setYTitle(yTitle); 
	        renderer.setXAxisMin(xMin); 
	        renderer.setXAxisMax(xMax); 
	        renderer.setYAxisMin(yMin); 
	        renderer.setYAxisMax(yMax); 
	        renderer.setAxesColor(axesColor); 
	        renderer.setLabelsColor(labelsColor); 
	        renderer.setZoomEnabled(false);
	    }
	    
	    /*
	     * DS18B20�¶�ת������
	     * by MQui : mw5945@gmail.com
	     * 2011.11.23 
	     */
	    public double temp_bytes2double(byte[] bytes){
	    	byte byteL = bytes[0];
	    	byte byteH = bytes[1];
	    	int idata;
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
	    	double data;
	    	data = idata*0.0625;
	    	return data;
	    }
	    /*
	     * ���ն�ת����ʽ
	     */
	    public double light_bytes2double(byte[] bytes){
	        byte byteL = bytes[0];
	        byte byteH = bytes[1];
	        int idata;
	        double data;
       
	        idata = byteL;
	        idata |= (byteH<<8) & 0xFF00 ;
	        idata = idata>>2;
	        data = idata/333;
	           
	        return data;
	       } 
	    
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
		    		
		    		if(cmd_addr == 0x11)//A1�ڵ���Ϣ
		    			switch(cmd_type){
		    				case LOCATION_REFNODE_TEMPRATURE://�¶�ID
		    					dataset.getSeriesAt(0).add(XarrayT,temp_bytes2double(cmd_data) );
		    					XarrayT++;
		    			    	chart.repaint();
		    					System.out.println(cmd_data);
		    					break;
		    				case LOCATION_REFNODE_LUMINOSITY://���ն�ID
		    					dataset.getSeriesAt(1).add(XarrayL,light_bytes2double(cmd_data) );
		    					XarrayL++;
		    			    	chart.repaint();
		    					System.out.println(cmd_data);
		    					break;
		    				default:
		    					//error
		    					break;
		    				}
		    		
		    		if(cmd_addr == 0x22)//A2�ڵ���Ϣ
		    			switch(cmd_type){
		    				case LOCATION_REFNODE_TEMPRATURE://�¶�ID
		    					dataset.getSeriesAt(0).add(XarrayT,temp_bytes2double(cmd_data) );
		    					XarrayT++;
		    			    	chart.repaint();
		    					System.out.println(cmd_data);
		    					break;
		    				case LOCATION_REFNODE_LUMINOSITY://���ն�ID
		    					dataset.getSeriesAt(1).add(XarrayL,light_bytes2double(cmd_data) );
		    					XarrayL++;
		    			    	chart.repaint();
		    					System.out.println(cmd_data);
		    					break;
		    				default:
		    					break;
		    		}
		    		

	    		super.handleMessage(msg);
	    	}
	    };
	    
	    public boolean onCreateOptionsMenu(Menu menu){
	    	 menu.add(Menu.NONE, Menu.FIRST + 1, 5, "����").setIcon(android.R.drawable.ic_menu_upload);
	    	 menu.add(Menu.NONE, Menu.FIRST + 2, 5, "����").setIcon(android.R.drawable.ic_menu_crop); 
	    	return true;
	    }
	    
	    public boolean onOptionsItemSelected(MenuItem item){
	    	switch (item.getItemId()){
	    	case Menu.FIRST+1:
	    		if(bRoom)Toast.makeText(this, "���ڻ���<������������ͼ>", Toast.LENGTH_LONG).show();
	    		else{
	    			bRoom = true;
	    			XarrayT =0;
	    			XarrayL=0;
	    			dataset.getSeriesAt(0).clear();
	    			dataset.getSeriesAt(1).clear();
	    			renderer.setChartTitle("�¶�/������-ʵʱ����ͼ ��������");
	    			chart.repaint();
	    		}
	    		break;
	    	case Menu.FIRST+2:
	    		if(bRoom){
	    			bRoom = false;
	    			XarrayT =0;
	    			XarrayL=0;
	    			dataset.getSeriesAt(0).clear();
	    			dataset.getSeriesAt(1).clear();
	    			renderer.setChartTitle("�¶�/������-ʵʱ����ͼ �����ҡ�");
	    			chart.repaint();
	    		}
	    		else Toast.makeText(this, "���ڻ���<����-��������ͼ>", Toast.LENGTH_LONG).show();
	    		break;
	    	}
	    	return false;
	    	
	    }
	    
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			try {
				do
				{	//2���ڵ��¶ȡ����Ȳ�ѯ
					if(bRoom)roomNum = 0;
						else roomNum = 2;
	
					us.StateRequestCmd(roomNum);
					us.send();
					Thread.sleep(600);
					roomNum++;
					us.StateRequestCmd(roomNum);
					us.send();
					Thread.sleep(600);
					
				}
				while(Thread.interrupted()==false || ThLife == true);/* ��ϵͳ�����ж���Ϣʱֹͣ��ѭ��*/
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} 
		//�������ؼ�
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			
			//���¼����Ϸ��ذ�ť
			if(keyCode == KeyEvent.KEYCODE_BACK){
				//����֮ǰ��activity
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
				finish();
				return true;
			}
			
			return super.onKeyDown(keyCode, event);
		}
		//��ȡ�¶�
		private static final int LOCATION_REFNODE_TEMPRATURE    =   0x10;		//�¶�ID
		private static final int STATE_REFNODE_TEMPRATURE_A1    =   0x1001;		//A1�¶�
		private static final int STATE_REFNODE_TEMPRATURE_A2    =   0x1002;		//A2�¶�
		
		//��ȡ���ն�
		private static final int LOCATION_REFNODE_LUMINOSITY	=	0x11;		//���ն�ID
		private static final int STATE_REFNODE_LUMINOSITY_A1    =   0x1101;		//A1������
		private static final int STATE_REFNODE_LUMINOSITY_A2    =   0x1102;		//A2������
} 
	   
