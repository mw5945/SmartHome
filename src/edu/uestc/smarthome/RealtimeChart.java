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
	XYSeries series; //曲线
	XYMultipleSeriesDataset dataset;//曲线数据
	XYMultipleSeriesRenderer renderer;//渲染
	GraphicalView chart;
	List<double[]> x = new ArrayList<double[]>(); //X轴数据数组
    List<double[]> y = new ArrayList<double[]>(); //Y轴数据数组
    boolean bRoom;
    int roomNum;
    private double XarrayT;
	private double XarrayL;
    //网络模块
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
	        String[] titles = new String[] { "温度", "光亮度"}; 
	        //初始化温度数据
	        x.add(new double[] {} ); 
	        y.add(new double[] {}); 
	        //初始化光照数据
	        x.add(new double[] {} ); 
	        y.add(new double[] {}); 
	        //初始化曲线数组
	        dataset = buildDataset(titles, x, y); 
	        //初始化Y轴
	        XarrayT = 0;
	        XarrayL = 0;
	        //设置绘制曲线样式
	        int[] colors = new int[] { Color.BLUE, Color.GREEN}; 
	        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.DIAMOND}; 
	        renderer = buildRenderer(colors, styles, true); 
	        //设置曲线图信息
	        setChartSettings(renderer, "温度/光亮度-实时曲线图 【客厅】", "时间", "℃/lm", 60, 0, -50, 150 , Color.WHITE, Color.WHITE); 
	        chart = ChartFactory.getLineChartView(this, dataset, renderer); 
	        setContentView(chart); 
	        
	        //初始化接受ROOM_1节点温度
	        bRoom = true;
	       
	        //初始化 Socket
			try {
				dSocket = new DatagramSocket(PORT);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//配置 UDP Sender
			us = new UDPSender("192.168.43.22",55555,dSocket);
			
	        //配置UDP Receiver监听线程
			URTh = new UDPReceiver(myHandler,dSocket);
			URTh.start();
			
			//配置发送线程
			ThLife = true;
			mThread = new Thread(this);
			mThread.start();

	        
	    } 

	    protected XYMultipleSeriesDataset buildDataset(String[] titles, 
	                                                   List<double[]> xValues, 
	                                                   List<double[]> yValues) 
	    { 
	    	//曲线初始化
	        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset(); 
	        
	        int length = titles.length;                  //有几条线 
	        for (int i = 0; i < length; i++) 
	        { 
	            TimeSeries series = new TimeSeries(titles[i]);    //根据每条线的名称创建 
	              double[] xV = (double[]) xValues.get(i);                 //获取第i条线的数据 
	              double[] yV = (double[]) yValues.get(i); 
	              int seriesLength = xV.length;                 //有几个点 

	              for (int k = 0; k < seriesLength; k++)        //每条线里有几个点 
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
	     * DS18B20温度转换函数
	     * by MQui : mw5945@gmail.com
	     * 2011.11.23 
	     */
	    public double temp_bytes2double(byte[] bytes){
	    	byte byteL = bytes[0];
	    	byte byteH = bytes[1];
	    	int idata;
	    	byte pos = (byte) (byteH & 0xF0);//验证正负数
	    	if(pos == 0){
	    		idata = byteL & 0xFF;
	            idata |= ((byteH << 8) & 0x0F00);
	    	}else {
	    		/*
	    		 * 已知一个负数的补码，将其转换为十进制数，步骤：
	      		 * 1、先对各位取反；
	      		 * 2、将其转换为十进制数；
	      		 * 3、加上负号，再减去1。 
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
	     * 光照度转换公式
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
	    
	    /* 通过Handler来接收UDP Rceiver线程消息进程所传递的信息*/
	    private Handler myHandler = new Handler()
	    {
			@Override
	    	synchronized public void handleMessage(Message msg) {
	    		// TODO Auto-generated method stub
					//获取消息
		    		byte[] msg_data = new byte[msg.getData().getByteArray("cmd").length];
		    		msg_data = msg.getData().getByteArray("cmd");
		    		/**
		    		 * 获取地址,根据文档定义，收到的数据格式
		    		 * 为第5-12位byte字节定义为地址，只截取第5位
		    		 */
		    		byte cmd_type = msg_data[1];//命令ID
		    		byte cmd_addr = msg_data[6];//地址信息
		    		byte[] cmd_data = new byte[2];
		    		System.arraycopy(msg_data, 2, cmd_data, 0, 2);
		    		
		    		if(cmd_addr == 0x11)//A1节点消息
		    			switch(cmd_type){
		    				case LOCATION_REFNODE_TEMPRATURE://温度ID
		    					dataset.getSeriesAt(0).add(XarrayT,temp_bytes2double(cmd_data) );
		    					XarrayT++;
		    			    	chart.repaint();
		    					System.out.println(cmd_data);
		    					break;
		    				case LOCATION_REFNODE_LUMINOSITY://光照度ID
		    					dataset.getSeriesAt(1).add(XarrayL,light_bytes2double(cmd_data) );
		    					XarrayL++;
		    			    	chart.repaint();
		    					System.out.println(cmd_data);
		    					break;
		    				default:
		    					//error
		    					break;
		    				}
		    		
		    		if(cmd_addr == 0x22)//A2节点消息
		    			switch(cmd_type){
		    				case LOCATION_REFNODE_TEMPRATURE://温度ID
		    					dataset.getSeriesAt(0).add(XarrayT,temp_bytes2double(cmd_data) );
		    					XarrayT++;
		    			    	chart.repaint();
		    					System.out.println(cmd_data);
		    					break;
		    				case LOCATION_REFNODE_LUMINOSITY://光照度ID
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
	    	 menu.add(Menu.NONE, Menu.FIRST + 1, 5, "客厅").setIcon(android.R.drawable.ic_menu_upload);
	    	 menu.add(Menu.NONE, Menu.FIRST + 2, 5, "卧室").setIcon(android.R.drawable.ic_menu_crop); 
	    	return true;
	    }
	    
	    public boolean onOptionsItemSelected(MenuItem item){
	    	switch (item.getItemId()){
	    	case Menu.FIRST+1:
	    		if(bRoom)Toast.makeText(this, "正在绘制<客厅数据曲线图>", Toast.LENGTH_LONG).show();
	    		else{
	    			bRoom = true;
	    			XarrayT =0;
	    			XarrayL=0;
	    			dataset.getSeriesAt(0).clear();
	    			dataset.getSeriesAt(1).clear();
	    			renderer.setChartTitle("温度/光亮度-实时曲线图 【客厅】");
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
	    			renderer.setChartTitle("温度/光亮度-实时曲线图 【卧室】");
	    			chart.repaint();
	    		}
	    		else Toast.makeText(this, "正在绘制<卧室-数据曲线图>", Toast.LENGTH_LONG).show();
	    		break;
	    	}
	    	return false;
	    	
	    }
	    
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			try {
				do
				{	//2个节点温度、亮度查询
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
				while(Thread.interrupted()==false || ThLife == true);/* 当系统发出中断信息时停止本循环*/
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} 
		//监听返回键
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			
			//按下键盘上返回按钮
			if(keyCode == KeyEvent.KEYCODE_BACK){
				//返回之前的activity
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
		//读取温度
		private static final int LOCATION_REFNODE_TEMPRATURE    =   0x10;		//温度ID
		private static final int STATE_REFNODE_TEMPRATURE_A1    =   0x1001;		//A1温度
		private static final int STATE_REFNODE_TEMPRATURE_A2    =   0x1002;		//A2温度
		
		//读取光照度
		private static final int LOCATION_REFNODE_LUMINOSITY	=	0x11;		//光照度ID
		private static final int STATE_REFNODE_LUMINOSITY_A1    =   0x1101;		//A1光亮度
		private static final int STATE_REFNODE_LUMINOSITY_A2    =   0x1102;		//A2光亮度
} 
	   
