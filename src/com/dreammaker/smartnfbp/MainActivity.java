package com.dreammaker.smartnfbp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Gravity;
//import android.view.Menu;            //如使用菜单加入此三包
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class MainActivity extends Activity {

	private final static int REQUEST_CONNECT_DEVICE = 1;
	// 宏定义查询设备句柄
	private final static String TAG = "LEO";
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	// SPP服务UUID号

	private InputStream is; // 输入流，用来接收蓝牙数据
	private TextView text0; // 提示栏解句柄
	private TextView textstation; // 提示栏解句柄 车站
	private TextView textroad; // 提示栏解句柄 路径
	private EditText edit0; // 发送数据输入句柄
	private EditText TTStext; //TTS 接收 的语句
	private TextView dis; // 接收数据显示句柄
	private ScrollView sv; // 翻页句柄
	private String smsg = ""; // 显示用数据缓存
	private String fmsg = ""; // 保存用数据缓存
	private String SmartNFBPmessage = ""; // 暂存字符 用来记录路径
	private String SmartNFBPstation = ""; // 暂存字符 用来记录车站
	private int SmartNFBPstation_num = 0; // 暂存字符 用来记录车站数字
	private String SmartNFBP2wordmessage = "00"; // 暂存2个字符 用来查找表
	private String SmartNFBPstationStart = ""; // 来记录起始点 确定图片用
	private String SmartNFBPinput = ""; // 判断用字符
	private String SmartNFBPflag = ""; // 判断是否前进
	private int SmartNFBPENDflag=0;
	private OutputStream outStream = null;// 发送流
	
	private String SmartNFBPStationname[] = {"经天路","南大仙林校区","羊山公园","仙林中心","学则路","仙鹤门","金马路","马群","钟灵街","孝陵卫","下马坊","苜蓿园","明故宫","西安门","大行宫","新街口","上海路"};
			//{"Jing Tian Lu","Nan Da Xian Lin Xiao Qu","Yang Shan Gong Yuan","Xian Lin Zhong Xin","Xue Ze Lu","Xian He Men","Jin Ma Lu","Ma Qun","Zhong Ling Jie","Xiao Ling Wei","Xia Ma Fang","Mu Xu Yuan","Ming Gu Gong","Xi An Men","Da Xing Gong","Xin Jie Kou","Shang Hai Lu"};
		//{"经天路","南大仙林校区","羊山公园","仙林中心","学则路","仙鹤门","金马路","马群","钟灵街","孝陵卫","下马坊","苜蓿园","明故宫","西安门","大行宫","新街口","上海路"};
	
	
	private Button mButtonON, mButtonONR, mButtonONW, mButtonOND, mButtonOFF;

	////////  振动 //////////
	Vibrator vibrator;
	//////// 震动结束 ///////
	/////// TTS 语音 ///////
	TextToSpeech tts;
	/////  TTS 相关 按钮 ///////
	Button speech;
	Button record;

	//////  TTS 结束 ///////

	public String filename = ""; // 用来保存存储的文件名
	BluetoothDevice _device = null; // 蓝牙设备
	BluetoothSocket _socket = null; // 蓝牙通信socket
	boolean _discoveryFinished = false;
	boolean bRun = true;
	boolean bThread = false;

	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();

	// 获取本地蓝牙适配器，即蓝牙设备

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main); // 设置画面为主画面 main.xml

		text0 = (TextView) findViewById(R.id.Text0); // 得到提示栏句柄
		textstation = (TextView) findViewById(R.id.TextStation); // 得到提示栏车站句柄
		textroad = (TextView) findViewById(R.id.TextRoad); // 得到提示栏车站句柄
		edit0 = (EditText) findViewById(R.id.Edit0); // 得到输入框句柄
		sv = (ScrollView) findViewById(R.id.ScrollView01); // 得到翻页句柄
		dis = (TextView) findViewById(R.id.in); // 得到数据显示句柄
		
		
		//////////////       振动           //////////////////////
		// 获取系统的 Vibrator 服务
		vibrator = (Vibrator) getSystemService(
			Service.VIBRATOR_SERVICE);
		//////////////  震动代码结束     /////////////////////////
		
		///////// TTS 开始 初始化 /////////////
		// 初始化TextToSpeech对象
				tts = new TextToSpeech(this, new OnInitListener()
				{
					@Override
					public void onInit(int status)
					{
						// 如果装载TTS引擎成功
						if (status == TextToSpeech.SUCCESS)
						{
							// 设置使用飞讯语音合成
							int result = tts.setLanguage(Locale.CHINA);
							// 如果不支持所设置的语言
							if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
								&& result != TextToSpeech.LANG_AVAILABLE)
							{
								Toast.makeText(MainActivity.this
									, "TTS暂时不支持这种语言的朗读。", Toast.LENGTH_LONG)
									.show();
							}
							else{
								// 执行朗读
								tts.speak("欢迎使用地铁导航系统，请等待蓝牙连接",TextToSpeech.QUEUE_ADD, null);//欢迎使用地铁导航系统，请等待蓝牙连接
							}
						}
					}
				});
				TTStext = (EditText) findViewById(R.id.Edit0);
				speech = (Button) findViewById(R.id.button_speech);
				record = (Button) findViewById(R.id.button_record);
				speech.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View arg0)
					{
						// 执行朗读
						tts.speak(TTStext.getText().toString(),
							TextToSpeech.QUEUE_ADD, null);
					}
				});
				record.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View arg0)
					{
						// 将朗读文本的音频记录到指定文件
						tts.synthesizeToFile(TTStext.getText().toString()
							, null, "/mnt/sdcard/sound.wav");
						Toast.makeText(MainActivity.this, "声音记录成功！"
							, Toast.LENGTH_LONG).show();
					}
				});
		///////////  TTS  END //////////////

		// ////////
		// 按钮的控制代码：
		// on 按钮 发出指令ON! 打开系统
		mButtonON = (Button) findViewById(R.id.buttonON);
		mButtonON.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_socket != null) {
					String message;// 定义文字
					byte[] msgBuffer;// 定义文字数组
					// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
					try {
						outStream = _socket.getOutputStream();
					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Output stream creation failed.",
								e);// 返回错误信息
					}
					message = "ON!"; // 定义文字
					msgBuffer = message.getBytes(); // 得到编码
					try {
						outStream.write(msgBuffer);

					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Exception during write.", e);
					}
				} else {
					Toast.makeText(getApplication(), "请连接设备!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		// off 按钮 发出指令OFF! 关闭系统
		mButtonOFF = (Button) findViewById(R.id.buttonOFF);
		mButtonOFF.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_socket != null) {
					String message;// 定义文字
					byte[] msgBuffer;// 定义文字数组
					// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
					try {
						outStream = _socket.getOutputStream();
					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Output stream creation failed.",
								e);// 返回错误信息
					}
					message = "OFF!"; // 定义文字
					msgBuffer = message.getBytes(); // 得到编码
					try {
						outStream.write(msgBuffer);

					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Exception during write.", e);
					}
				} else {
					Toast.makeText(getApplication(), "请连接设备!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		// ONR 按钮 发出指令ONR 功能为读取RFID
		mButtonONR = (Button) findViewById(R.id.buttonONR);
		mButtonONR.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_socket != null) {
					String message;// 定义文字
					byte[] msgBuffer;// 定义文字数组
					// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
					try {
						outStream = _socket.getOutputStream();
					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Output stream creation failed.",
								e);// 返回错误信息
					}
					message = "ONR"; // 定义文字
					msgBuffer = message.getBytes(); // 得到编码
					try {
						outStream.write(msgBuffer);

					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Exception during write.", e);
					}
				} else {
					Toast.makeText(getApplication(), "请连接设备!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		// ONW 按钮 发出指令ONW 功能为写入字符
		mButtonONW = (Button) findViewById(R.id.buttonONW);
		mButtonONW.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_socket != null) {
					String message;// 定义文字
					byte[] msgBuffer;// 定义文字数组
					// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
					try {
						outStream = _socket.getOutputStream();
					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Output stream creation failed.",
								e);// 返回错误信息
					}
					message = "ONW"; // 定义文字
					msgBuffer = message.getBytes(); // 得到编码
					try {
						outStream.write(msgBuffer);

					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Exception during write.", e);
					}
				} else {
					Toast.makeText(getApplication(), "请连接设备!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		// OND 按钮 发出指令OND 功能为探测距离
		mButtonOND = (Button) findViewById(R.id.buttonOND);
		mButtonOND.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (_socket != null) {
					String message;// 定义文字
					byte[] msgBuffer;// 定义文字数组
					// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
					try {
						outStream = _socket.getOutputStream();
					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Output stream creation failed.",
								e);// 返回错误信息
					}
					message = "OND"; // 定义文字
					msgBuffer = message.getBytes(); // 得到编码
					try {
						outStream.write(msgBuffer);

					} catch (IOException e) {
						Log.e(TAG, "ON RESUME: Exception during write.", e);
					}
				} else {
					Toast.makeText(getApplication(), "请连接设备!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		// /////////
		// 如果打开本地蓝牙设备不成功，提示信息，结束程序
		if (_bluetooth == null) {
			Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		// 设置设备可以被搜索
		new Thread() {
			public void run() {
				if (_bluetooth.isEnabled() == false) {
					_bluetooth.enable();
				}
			}
		}.start();
	}

	// 发送按键响应
	public void onSendButtonClicked(View v) {
		if (_socket != null) {
			int i = 0;
			int n = 0;
			try {
				OutputStream os = _socket.getOutputStream(); // 蓝牙连接输出流
				byte[] bos = edit0.getText().toString().getBytes();
				for (i = 0; i < bos.length; i++) {
					if (bos[i] == 0x0a)
						n++;
				}
				byte[] bos_new = new byte[bos.length + n];
				n = 0;
				for (i = 0; i < bos.length; i++) { // 手机中换行为0a,将其改为0d 0a后再发送
					if (bos[i] == 0x0a) {
						bos_new[n] = 0x0d;
						n++;
						bos_new[n] = 0x0a;
					} else {
						bos_new[n] = bos[i];
					}
					n++;
				}
				os.write(bos_new);
			} catch (IOException e) {
			}
		} else {
			Toast.makeText(getApplication(), "请连接设备!", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// 接收活动结果，响应startActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE: // 连接结果，由DeviceListActivity设置返回
			// 响应返回结果
			if (resultCode == Activity.RESULT_OK) { // 连接成功，由DeviceListActivity设置返回
				// MAC地址，由DeviceListActivity设置返回
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// 得到蓝牙设备句柄
				_device = _bluetooth.getRemoteDevice(address);

				// 用服务号得到socket
				try {
					_socket = _device.createRfcommSocketToServiceRecord(UUID
							.fromString(MY_UUID));
				} catch (IOException e) {
					Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
					// 执行朗读
					tts.speak("蓝牙连接失败",
						TextToSpeech.QUEUE_ADD, null);
				}
				// 连接socket
				Button btn = (Button) findViewById(R.id.Button03);
				try {
					_socket.connect();
					Toast.makeText(this, "连接" + _device.getName() + "成功！",
							Toast.LENGTH_SHORT).show();
					// 执行朗读
					tts.speak("蓝牙连接" + _device.getName() + "成功",
						TextToSpeech.QUEUE_ADD, null);
					btn.setText("Break");
				} catch (IOException e) {
					try {
						Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT)
								.show();
						// 执行朗读
						tts.speak("蓝牙连接失败",
							TextToSpeech.QUEUE_ADD, null);
						_socket.close();
						_socket = null;
					} catch (IOException ee) {
						Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT)
								.show();
						// 执行朗读
						tts.speak("蓝牙连接失败",
							TextToSpeech.QUEUE_ADD, null);
					}
					return;
				}
				// 打开接收线程
				try {
					is = _socket.getInputStream(); // 得到蓝牙数据输入流
				} catch (IOException e) {
					Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (bThread == false) {
					ReadThread.start();
					bThread = true;
				} else {
					bRun = true;
				}
			}
			break;
		default:
			break;
		}
	}
	// 接收数据线程
	Thread ReadThread = new Thread() {

		public void run() {
			int num = 0;
			byte[] buffer = new byte[1024];
			byte[] buffer_new = new byte[1024];
			int i = 0;
			int n = 0;
			bRun = true;
			// 接收线程
			while (true) {
				try {
					while (is.available() == 0) {
						while (bRun == false) {
						}
					}
					// 延时程序
					try {
						Thread.currentThread();
						Thread.sleep(500); // 延时时间
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 完成延时
					while (true) {
						num = is.read(buffer); // 读入数据
						n = 0;

						String s0 = new String(buffer, 0, num);
						fmsg += s0; // 保存收到数据
						for (i = 0; i < num; i++) {
							if ((buffer[i] == 0x0d) && (buffer[i + 1] == 0x0a)) {
								buffer_new[n] = 0x0a;
								i++;
							} else {
								buffer_new[n] = buffer[i];
							}
							n++;
						}
						String s = new String(buffer_new, 0, n);

						smsg += s; // 写入接收缓存
						if ((s.subSequence(0, 2).equals("A#")|s.subSequence(0, 2).equals("B#")|s.subSequence(0, 2).equals("C#")|s.subSequence(0, 2).equals("D#")|s.subSequence(0, 2).equals("E#")|s.subSequence(0, 2).equals("F#")|s.subSequence(0, 2).equals("G#")|s.subSequence(0, 2).equals("H#")) && s.length() < 18) {//判断是不是关键字
							if (SmartNFBPinput.equals(s)) {	//与上次字符结果一致，则不再处理
								SmartNFBPflag = "0";	//标识符置为0
							} else {
								SmartNFBPmessage += s.substring(0, 1); // 把最后字符串拷贝出来【相对位置的编程】
								SmartNFBP2wordmessage=SmartNFBP2wordmessage.substring(1, 2)+s.substring(0, 1);//组合两个字符 为了查表
								SmartNFBPstation=s.substring(6, 8);
								SmartNFBPstation_num= Integer.parseInt(SmartNFBPstation.substring(0, 1))*10+Integer.parseInt(SmartNFBPstation.substring(1, 2));//((SmartNFBPstation.substring(0, 1).compareTo("0"))-48)*10+((SmartNFBPstation.substring(1, 2).compareTo("0"))-48);// 车站字符转数字
								SmartNFBPflag = "1";
								SmartNFBPinput = s;	//用来记录上一针数据
							}
						}else if(s.subSequence(0, 2).equals("V!")){
							// 控制手机震动2秒
							vibrator.vibrate(2000);
							//  加振动防护执行机制 防止同时运行报错！
							//  图片需要 G H 更改
							// 执行朗读
							/*
							tts.speak("注意安全！",
								TextToSpeech.QUEUE_ADD, null);
							*/
							//	 播送完毕
							SmartNFBPflag = "0";
							continue;
						}
						else if(s.subSequence(0, 2).equals("H!")){
							// 控制手机震动1秒
							vibrator.vibrate(1000);
							// 执行朗读
							tts.speak("请求帮助!",
								TextToSpeech.QUEUE_ADD, null);							
							//	 播送完毕
							SmartNFBPflag = "0";
							continue;
						}
						else if(s.subSequence(0, 2).equals("Y!")){
							// 控制手机震动1秒
							vibrator.vibrate(1000);
							// 执行朗读
							tts.speak("确认!",
								TextToSpeech.QUEUE_ADD, null);							
							//	 播送完毕
							SmartNFBPflag = "0";
							continue;
						}
						else if(s.subSequence(0, 2).equals("N!")){
							// 控制手机震动1秒
							vibrator.vibrate(1000);
							// 执行朗读
							tts.speak("取消!",
								TextToSpeech.QUEUE_ADD, null);							
							//	 播送完毕
							SmartNFBPflag = "0";
							continue;
						}
						else {
							SmartNFBPflag = "0";
						}
						
						if (is.available() == 0)
							break; // 短时间没有数据才跳出进行显示
					}
					// 发送显示消息，进行显示刷新
					handler.sendMessage(handler.obtainMessage());
					// //

				} catch (IOException e) {
				}
			}
		}
	};

	
	
	//  在出站点都不能正常显示 Thoast 图片 估计是因为延时！！！ 这个程序不是很先进，所以需要优化！ 加油加油！
	// 消息处理队列
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@SuppressWarnings("deprecation")
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (SmartNFBPflag.equals("1")) {
				// 进站 部分  A & B
				if (SmartNFBP2wordmessage.equals("0A")) {
					// A
					try {
						SmartNFBPstationStart="A"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//欢迎来到XXX地铁站，这里是A进出口，前方有扶梯，请您注意安全//
						tts.speak("欢迎来到 "+SmartNFBPstation+"地铁站,这里是A进出口，前方有扶梯，请您注意安全。",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.a);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.b);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				} else if (SmartNFBP2wordmessage.equals("0B")) {
					// B
					try {
						SmartNFBPstationStart="B"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//欢迎来到XXX地铁站，这里是B进出口，前方有扶梯，请您注意安全//
						tts.speak("欢迎来到"+SmartNFBPstation+"地铁站，前方有扶梯，请您注意安全",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.a);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.b);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				} 
				
				//开始进入车站拐点C
				else if (SmartNFBP2wordmessage.equals("AC")) {
					// A->C
					try {
						SmartNFBPstationStart="A"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//这里是拐点C，如需从B进出口出站请直行，如需进站乘车请左转//
						tts.speak("这里是拐点C，如需从B进出口出站请直行，如需进站乘车请左转",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.ac);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bc);	//更改图片资源
						}	
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				} 
				//开始进入车站拐点C
				else if (SmartNFBP2wordmessage.equals("BC")) {
					// B->C
					try {
						SmartNFBPstationStart="B"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//这里是拐点C，如需从A进出口出站请直行，如需进站乘车请右转//
						tts.speak("这里是拐点C，如需从A进出口出站请直行，如需进站乘车请右转",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.ac);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bc);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				//开始进入车站闸机 D
				else if (SmartNFBP2wordmessage.equals("CD")) {
					// C->D
					try {
						//SmartNFBPstationStart="B"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//这里是闸机口，请出示盲人证，听到闸机提示音后请直行，过闸机时请小心//
						tts.speak("这里是闸机口，请出示盲人证，听到闸机提示音后请直行，过闸机时请小心",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.acd);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bcd);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.defg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.defh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				} 
				//开始进入车站内部 E
				else if (SmartNFBP2wordmessage.equals("DE")) {
					// D->E
					try {
						//SmartNFBPstationStart="B"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//您已进入地铁站，盲道前方有楼梯，请注意安全//
						tts.speak("您已进入地铁站，盲道前方有楼梯，请注意安全",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.acde);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bcde);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.efg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.efh);	//更改图片资源
						}					
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				} 
				//开始进入车站 乘车区  F
				else if (SmartNFBP2wordmessage.equals("EF")) {
					// E->F
					try {
						//SmartNFBPstationStart="B"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//欢迎来到乘车等候区拐点F，若要乘坐开向XXXXSmartNFBPStationname[SmartNFBPstation_num-1]方向的列车请左转，若要乘坐开向XXXXSmartNFBPStationname[SmartNFBPstation_num+1]方向的列车请右转//
						tts.speak("欢迎来到乘车等候区拐点F，若要乘坐开向 "+SmartNFBPStationname[SmartNFBPstation_num-1]+"方向的列车请左转，若要乘坐开向 "+SmartNFBPStationname[SmartNFBPstation_num+1]+"方向的列车请右转",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.acdef);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bcdef);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.efg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.efh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				} 
				//开始 乘车区上车   G
				else if (SmartNFBP2wordmessage.equals("FG")) {
					// F->G
					try {
						//SmartNFBPstationStart="B"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//您已达到去往XXXXSmartNFBPStationname[SmartNFBPstation_num-1]站的地铁等车处，请等候列车，上车时请小心脚下//
						tts.speak("您已达到去往  "+SmartNFBPStationname[SmartNFBPstation_num-1]+" 站的地铁等车处，请等候列车，上车时请小心脚下",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.acdefg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bcdefg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.g);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.fgh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				//开始 乘车区上车  H
				else if (SmartNFBP2wordmessage.equals("FH")) {
					// F->H
					try {
						//SmartNFBPstationStart="B"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//您已达到去往XXXXSmartNFBPStationname[SmartNFBPstation_num+1]站的地铁等车处，请等候列车，上车时请小心脚下//
						tts.speak("您已达到去往"+SmartNFBPStationname[SmartNFBPstation_num+1]+" 站的地铁等车处，请等候列车，上车时请小心脚下",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.acdefh);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bcdefh);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.fgh);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.h);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				////////////////////////////////////////////////////////////////////////
				//  坐车过程//////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////
				
				//下车 G or H
				else if (SmartNFBP2wordmessage.equals("GG")) {
					// G->G
					try {
						SmartNFBPstationStart="G"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//您已下车，欢迎来到XXXX地铁站，请直行//
						tts.speak("您已下车，欢迎来到 "+SmartNFBPStationname[SmartNFBPstation_num]+" 地铁站，请直行",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.g);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.g);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.g);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.h);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				else if (SmartNFBP2wordmessage.equals("HH")) {
					// H->H
					try {
						SmartNFBPstationStart="H"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//您已下车，欢迎来到XXXX地铁站，请直行//
						tts.speak("您已下车，欢迎来到 "+SmartNFBPStationname[SmartNFBPstation_num]+" 地铁站，请直行",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.h);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.h);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.g);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.h);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				// 在乘车大厅 F
				else if (SmartNFBP2wordmessage.equals("GF")) {
					// G->F
					try {
						SmartNFBPstationStart="G"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//您已到达乘车处拐点F，如需乘坐反方向行驶的列车，请直行，如需出站请右转，有楼梯请小心//
						tts.speak("您已到达乘车处拐点F，如需乘坐反方向行驶的列车，请直行，如需出站请右转，有楼梯请小心",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.acdef);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bcdef);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.fg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.fh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				else if (SmartNFBP2wordmessage.equals("HF")) {
					// H->F
					try {
						SmartNFBPstationStart="H"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//您已到达乘车处拐点F，如需乘坐反方向行驶的列车，请直行，如需出站请左转，有楼梯请小心//
						tts.speak("您已到达乘车处拐点F，如需乘坐反方向行驶的列车，请直行，如需出站请左转，有楼梯请小心 ",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.acdef);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bcdef);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.fg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.fh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				//站内闸机口 E
				else if (SmartNFBP2wordmessage.equals("FE")) {
					// F->E
					try {
						//SmartNFBPstationStart="G"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//这里是闸机口，请出示盲人证，听到闸机提示音后请直行，过闸机时请小心//
						tts.speak("这里是闸机口，请出示盲人证，听到闸机提示音后请直行，过闸机时请小心",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.acde);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bcde);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.efg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.efh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				//离开地铁站内部
				else if (SmartNFBP2wordmessage.equals("ED")) {
					// E->D
					try {
						//SmartNFBPstationStart="G"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//您已离开地铁站，前方是出站拐点，请注意安全//
						tts.speak("您已离开地铁站，前方是出站拐点，请注意安全",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.acd);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bcd);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.defg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.defh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				//去出站拐点 C
				else if (SmartNFBP2wordmessage.equals("DC")) {
					// D->C
					try {
						//SmartNFBPstationStart="G"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//这里是拐点C，如需从A进出口出站请右转，如需从A进出口出站请左转，前进方向有扶梯，请注意安全//
						tts.speak("这里是拐点C，如需从A进出口出站请右转，如需从A进出口出站请左转，前进方向有扶梯，请注意安全",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.ac);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.bc);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.cdefg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.cdefh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				
				// 出站  A & B
				else if (SmartNFBP2wordmessage.equals("CA")) {
					// C->A
					try {
						//SmartNFBPstationStart="G"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//这里是XXX地铁站B进出口，您已完成导航，欢迎再次光临//
						tts.speak("这里是出站口A，您已完成导航，欢迎再次光临",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.a);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.b);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.acdefg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.acdefh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
						SmartNFBPENDflag=1;	//标志完成导航任务
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
				else if (SmartNFBP2wordmessage.equals("CB")) {
					// C->B
					try {
						//SmartNFBPstationStart="G"; //记录起始点 来方便确定图片
						SmartNFBPstation=SmartNFBPStationname[SmartNFBPstation_num];//用数字来遍历车站名称
						text0.setText(SmartNFBPmessage);//显示路径
						textroad.setText(SmartNFBP2wordmessage.substring(0, 1)+"->"+SmartNFBP2wordmessage.substring(1, 2));	//显示当前的相对位置符号
						textstation.setText(SmartNFBPstation);//显示车站
						// 执行朗读
						//中文文本：
						//这里是XXX地铁站A进出口，您已完成导航，欢迎再次光临//
						tts.speak("这里是出站口B，，您已完成导航，欢迎再次光临",
							TextToSpeech.QUEUE_ADD, null);
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(1500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// 完成延时
						// 图片 Toast 提示
						Toast pictoast=Toast.makeText(MainActivity.this, "Point A",
								Toast.LENGTH_LONG);
						pictoast.setGravity(Gravity.CENTER,0,0);//设置所在位置
						View pictoastView=pictoast.getView();
						//创建一个 ImageView 
						ImageView image= new ImageView(MainActivity.this);
						if(SmartNFBPstationStart.substring(0, 1).equals("A"))
						{
							image.setImageResource(R.drawable.a);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("B")){
							image.setImageResource(R.drawable.b);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("G")){
							image.setImageResource(R.drawable.bcdefg);	//更改图片资源
						}
						else if(SmartNFBPstationStart.substring(0, 1).equals("H")){
							image.setImageResource(R.drawable.bcdefh);	//更改图片资源
						}
						//
						LinearLayout pic= new LinearLayout(MainActivity.this);
						pic.setHorizontalGravity(Gravity.CENTER);
						pic.addView(image);
						pic.addView(pictoastView);
						pictoast.setView(pic);
						pictoast.show();
						// 图片 Toast 提示结束
						SmartNFBPENDflag=1;	//标志完成导航任务
					} catch (Exception e) {
						e.printStackTrace();
					} //收集错误
				}
			}
				
			//完成导航提示
			if(SmartNFBPENDflag==1){
				// 延时程序
				try {
					Thread.currentThread();
					Thread.sleep(500); // 延时时间
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 完成延时
				// 延时程序
				try {
					Thread.currentThread();
					Thread.sleep(5000); // 延时时间 5s
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 完成延时
				tts.speak("本次导航结束！",
						TextToSpeech.QUEUE_ADD, null);
				SmartNFBPmessage = "";
				SmartNFBP2wordmessage="00";
				SmartNFBPstation="";
				SmartNFBPstation_num=0;
				SmartNFBPstationStart="";
				SmartNFBPinput = "";
				SmartNFBPENDflag=0;
				//完成导航后 重新标志抬头
				text0.setText(SmartNFBPmessage+"Finish");//显示路径
				textroad.setText("Road");	//显示当前的相对位置符号
				textstation.setText("Station");//显示车站
				
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			dis.setText(smsg); // 显示数据
			sv.scrollTo(0, dis.getMeasuredHeight()); // 跳至数据最后一页
		}
	};

	// 关闭程序掉用处理部分
	@SuppressLint("HandlerLeak")
	public void onDestroy() {
		super.onDestroy();
		if (_socket != null) // 关闭连接socket
			try {
				_socket.close();
			} catch (IOException e) {
			}
		// 关闭TextToSpeech对象
				if (tts != null)
				{
					tts.shutdown();
				}
		_bluetooth.disable(); // 关闭蓝牙服务
	}

	// 菜单处理部分
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {//建立菜单
	 * MenuInflater inflater = getMenuInflater();
	 * inflater.inflate(R.menu.option_menu, menu); return true; }
	 */

	/*
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { //菜单响应函数
	 * switch (item.getItemId()) { case R.id.scan:
	 * if(_bluetooth.isEnabled()==false){ Toast.makeText(this, "Open BT......",
	 * Toast.LENGTH_LONG).show(); return true; } // Launch the
	 * DeviceListActivity to see devices and do scan Intent serverIntent = new
	 * Intent(this, DeviceListActivity.class);
	 * startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); return
	 * true; case R.id.quit: finish(); return true; case R.id.clear: smsg="";
	 * ls.setText(smsg); return true; case R.id.save: Save(); return true; }
	 * return false; }
	 */

	// 连接按键响应函数
	public void onConnectButtonClicked(View v) {
		if (_bluetooth.isEnabled() == false) { // 如果蓝牙服务不可用则提示
			Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
			return;
		}

		// 如未连接设备则打开DeviceListActivity进行设备搜索
		Button btn = (Button) findViewById(R.id.Button03);
		if (_socket == null) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class); // 跳转程序设置
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // 设置返回宏定义
			tts.speak("系统正在尝试连接蓝牙！",
					TextToSpeech.QUEUE_ADD, null);
		} else {
			// 关闭连接socket
			try {

				is.close();
				_socket.close();
				_socket = null;
				bRun = false;
				// 语音
				tts.speak("系统断开蓝牙连接",
						TextToSpeech.QUEUE_ADD, null);
				btn.setText("Link");
				// 延时程序
				try {
					Thread.currentThread();
					Thread.sleep(1000); // 延时时间
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 完成延时
			} catch (IOException e) {
			}
		}
		return;
	}

	// 保存按键响应函数
	public void onSaveButtonClicked(View v) {
		Save();
	}

	// 清除按键响应函数
	public void onClearButtonClicked(View v) {
		smsg = "";
		fmsg = "";
		SmartNFBPmessage = "";
		SmartNFBPstationStart="";
		SmartNFBPstation_num=0;
		SmartNFBPstationStart="";
		SmartNFBP2wordmessage="00";
		dis.setText(smsg);
		text0.setText("Let's Go");//显示路径
		textroad.setText("Road");	//显示当前的相对位置符号
		textstation.setText("Station");//显示车站
		return;
	}

	// 退出按键响应函数
	public void onQuitButtonClicked(View v) {
		tts.speak("系统正在关闭，欢迎再次使用",//系统正在关闭，欢迎再次使用
				TextToSpeech.QUEUE_ADD, null);
		// 延时程序   
		try {
			Thread.currentThread();
			Thread.sleep(5000); // 延时时间
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 完成延时
		finish();
	}

	// 保存功能实现
	private void Save() {
		// 显示对话框输入文件名
		LayoutInflater factory = LayoutInflater.from(MainActivity.this); // 图层模板生成器句柄
		final View DialogView = factory.inflate(R.layout.sname, null); // 用sname.xml模板生成视图模板
		new AlertDialog.Builder(MainActivity.this).setTitle("File Name")
				.setView(DialogView) // 设置视图模板
				.setPositiveButton("确定", new DialogInterface.OnClickListener() // 确定按键响应函数
						{
							public void onClick(DialogInterface dialog,
									int whichButton) {
								EditText text1 = (EditText) DialogView
										.findViewById(R.id.sname); // 得到文件名输入框句柄
								filename = text1.getText().toString(); // 得到文件名

								try {
									if (Environment.getExternalStorageState()
											.equals(Environment.MEDIA_MOUNTED)) { // 如果SD卡已准备好

										filename = filename + ".txt"; // 在文件名末尾加上.txt
										File sdCardDir = Environment
												.getExternalStorageDirectory(); // 得到SD卡根目录
										File BuildDir = new File(sdCardDir,
												"/data"); // 打开data目录，如不存在则生成
										if (BuildDir.exists() == false)
											BuildDir.mkdirs();
										File saveFile = new File(BuildDir,
												filename); // 新建文件句柄，如已存在仍新建文档
										FileOutputStream stream = new FileOutputStream(
												saveFile); // 打开文件输入流
										stream.write(fmsg.getBytes());
										stream.close();
										Toast.makeText(MainActivity.this,
												"存储成功！", Toast.LENGTH_SHORT)
												.show();
									} else {
										Toast.makeText(MainActivity.this,
												"没有存储卡！", Toast.LENGTH_LONG)
												.show();
									}

								} catch (IOException e) {
									return;
								}

							}
						}).setNegativeButton("取消", // 取消按键响应函数,直接退出对话框不做任何处理
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show(); // 显示对话框
	}
}