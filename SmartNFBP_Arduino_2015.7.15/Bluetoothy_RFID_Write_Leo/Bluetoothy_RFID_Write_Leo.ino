/** include library **/
#include "nfc.h"
#include <Arduino.h>
///////////////////////////////////////////////////////////////////////////
//  PN532 模块定义
// SDA  ----> A4
// SCL  ----> A5
#define LEDPin 13		//LED 的引脚
#define Buzzer 7		//蜂鸣器引脚
// 超声波模块引脚定义
#define TrigPin 2		//发送波
#define EchoPin 3		//回波
//////////////////////////////////////////////////////////////////////////
#define KEY_yes 6		//确认建按钮引脚
#define KEY_no 5		//取消键按钮引脚
#define KEY_help 4		//帮助键按钮引脚
/////////////////////////////////////////////////////////////////////////
/*
硬件目前需要升级以下方面：
 1.读卡能力
 2.写入能力
 3.蜂鸣器响应
 4.省电
 
 目前硬件能力：
 通过输入控制命令字来检查：
 -->	ON!	开启设备
 -->	OFF	关闭设备
 -->	ONR	读卡
 -->	ONW	写卡
 -->	OND	探测距离
 
 另外有三个按钮
 分别是：Y确认	N取消	H帮助
 
 蜂鸣器做成了Beep 也就是只有节奏的区分，没有音调的区分
 在之后的开发中可以换一个元件，来完成更强大的功能。
 
 */
//////////////////////////////////////////////////////////////////////////



// 常量定义
unsigned char KEY_NUM = 0;	//按键判断
//bool Flag_KEY_Set = 0;
//unsigned char KEY2_Count = 0;

// 测量距离
float Value_cm;

char Uart_Buffer[20];	//接受用的字符串组
//char Uart_Buffer1[20];
char Uart_Count = 0;	//字符游标，来确定字符存放位置
//char Uart_Count1 = 0;
char temp = 0;
char temp1 = 0;
/** define a nfc class */
NFC_Module nfc;	////初始化


////////////////////////////////////////////////////////////////////////////////////////////////////////
///////// 初始化程序 ///////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////

void setup(void)
{
  Serial.begin(9600);
  pinMode(TrigPin, OUTPUT);		// 超声波输出
  pinMode(EchoPin, INPUT);		// 超声波回波输入
  pinMode(LEDPin, OUTPUT);		// LED灯 输出
  pinMode(Buzzer, OUTPUT);		//设置输出引脚
  pinMode(KEY_yes,INPUT_PULLUP);        //开关引脚输入
  pinMode(KEY_no,INPUT_PULLUP);	        //开关引脚输入
  pinMode(KEY_help,INPUT_PULLUP);	//开关引脚输入

  //NFC 初始化
  nfc.begin();					//初始化 NFC 板块
  //打印系统
  Serial.println("Smart_NFBP System Start!");
  Serial.println("This One is edit by Dream-Makers");
  //系统自检
  uint32_t versiondata = nfc.get_version();		//判断硬件版本 
  if (! versiondata) {							//如果版本返回0，则死循环
    Serial.print("Didn't find PN53x board");	//没有发现硬件
    while (1);									// 停止运行
  }

  // Got ok data, print it out!
  // 找到硬件并读取版本号，打印出来
  Serial.print("Found chip PN5");
  Serial.println((versiondata >> 24) & 0xFF, HEX);	//硬件版本号
  Serial.print("Firmware ver. ");
  Serial.print((versiondata >> 16) & 0xFF, DEC);	//硬件框架号
  Serial.print('.');
  Serial.println((versiondata >> 8) & 0xFF, DEC);	//硬件框架号

  /** Set normal mode, and disable SAM */
  nfc.SAMConfiguration();		//设置模式？！？

  //Serial.println("Please Input 'ON!' to start System.");
  //Serial.println("Please Input 'OFF!' to close System.");
  digitalWrite(LEDPin, HIGH);	//打开 LED
  // 蜂鸣器提示
  digitalWrite(Buzzer, HIGH);	//蜂鸣器响
  delay(40);					//延时
  digitalWrite(Buzzer, LOW);
  delay(80);
  digitalWrite(Buzzer, HIGH);	//蜂鸣器响
  delay(80);					//延时
  digitalWrite(Buzzer, LOW);
  delay(50);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////
///////// 主循环程序 ///////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////
void loop(void)
{
  //////////////////////////////////////////////////////////////////////////////////////////////////////
  // 原始程序控制代码
  unsigned char i;
  while (Serial.available() > 0)
  {
    temp = Serial.read();
    if (temp == 'O')
    {
      Uart_Count = 0;
    }

    Uart_Buffer[Uart_Count++] = temp;
    if (Uart_Count == 19)
    {
      Uart_Count = 0;
    }

    //开机
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'N' && Uart_Buffer[2] == '!')
    {
      digitalWrite(LEDPin, HIGH);	//打开继电器
      Serial.println("System LEDPin ON Complete!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(100);					//延时
      digitalWrite(Buzzer, LOW);	
      Serial.println("Input 'ONR' =Read Cards!");
      Serial.println("Input 'ONW' =Write Cards!");
      Serial.println("Input 'OND' =Detect Distence!");
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }

    }

    //关闭
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'F' && Uart_Buffer[2] == 'F' && Uart_Buffer[3] == '!')
    {
      digitalWrite(LEDPin, LOW);		//关闭继电器
      Serial.println("LEDPin OFF Complete!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(100);					//延时
      digitalWrite(Buzzer, LOW);
      delay(50);
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(100);
      digitalWrite(Buzzer, LOW);
      delay(50);
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(50);					//延时
      digitalWrite(Buzzer, LOW);
      delay(50);
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(50);					//延时
      digitalWrite(Buzzer, LOW);

      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

    // 读取
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'N' && Uart_Buffer[2] == 'R') //read card
    {
      ///////////////////////////////////////////////////////////////////////////
      //Serial.println("System_Read_Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);
      u8 buf[32], sta;

      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        //Serial.print("UUID_length:");
        //Serial.print(buf[0], DEC);
        //Serial.println();
        //Serial.print("UUID:");
        //nfc.puthex(buf+1, buf[0]);
        //Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          //Serial.println("Authentication_success.");

          // uncomment following lines for writing data to blok 4
          /*
           strcpy((char*)block, "Elechoues - NFC");
           sta = nfc.MifareWriteBlock(blocknum, block);
           if(sta){
           Serial.println("Write block successfully:");
           }
           */

          /** read block 4 */
          sta = nfc.MifareReadBlock(blocknum, block);
          if (sta) {
            //Serial.println("Read_block_0_successfully:");

            nfc.puthex(block, 16);//打印IC卡里第一个区域里面 1个字符
            Serial.println();
          }

          /** read block 5 */
          sta = nfc.MifareReadBlock(blocknum + 1, block);
          if (sta) {
            //Serial.println("Read_block_1_successfully:");

            //nfc.puthex(block, 16);
            //Serial.println();
          }

          /** read block 6 */
          sta = nfc.MifareReadBlock(blocknum + 2, block);
          if (sta) {
            //Serial.println("Read_block_2_successfully:");

            //nfc.puthex(block, 16);

            //Serial.println();
          }

          /** read block 7 */
          sta = nfc.MifareReadBlock(blocknum + 3, block);
          if (sta) {
            //Serial.println("Read_block_3_successfully:");

            //nfc.puthex(block, 16);
            //Serial.println();
          }
        }
      }
      /////////////////////////////////////////////////////
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

    // 写入/////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'N' && Uart_Buffer[2] == 'W') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="H#NJ0205#LeoLiu$";
          strcpy((char*)block, "H#NJ0205#LeoLiu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }
	// 8点写入  XJK 15
	// 写入 A /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'W' && Uart_Buffer[2] == 'A') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="A#NJ0215#Amy_Lu$";
          strcpy((char*)block, "A#NJ0215#Amy_Lu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 B /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'W' && Uart_Buffer[2] == 'B') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="B#NJ0215#Amy_Lu$";
          strcpy((char*)block, "B#NJ0215#Amy_Lu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 C /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'W' && Uart_Buffer[2] == 'C') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="C#NJ0215#Amy_Lu$";
          strcpy((char*)block, "C#NJ0215#Amy_Lu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 D /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'W' && Uart_Buffer[2] == 'D') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="D#NJ0215#Amy_Lu$";
          strcpy((char*)block, "D#NJ0215#Amy_Lu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 E /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'W' && Uart_Buffer[2] == 'E') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="E#NJ0215#Amy_Lu$";
          strcpy((char*)block, "E#NJ0215#Amy_Lu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }


	// 写入 F /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'W' && Uart_Buffer[2] == 'F') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="F#NJ0215#Amy_Lu$";
          strcpy((char*)block, "F#NJ0215#Amy_Lu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }


	// 写入 G /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'W' && Uart_Buffer[2] == 'G') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="G#NJ0215#Amy_Lu$";
          strcpy((char*)block, "G#NJ0215#Amy_Lu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 H /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'W' && Uart_Buffer[2] == 'H') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="H#NJ0215#Amy_Lu$";
          strcpy((char*)block, "H#NJ0215#Amy_Lu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

         // 8点写入  XMF 10
	// 写入 A /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'X' && Uart_Buffer[2] == 'A') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="A#NJ0210#LeoLiu$";
          strcpy((char*)block, "A#NJ0210#LeoLiu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 B /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'X' && Uart_Buffer[2] == 'B') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="B#NJ0210#LeoLiu$";
          strcpy((char*)block, "B#NJ0210#LeoLiu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 C /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'X' && Uart_Buffer[2] == 'C') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="C#NJ0210#LeoLiu$";
          strcpy((char*)block, "C#NJ0210#LeoLiu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 D /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'X' && Uart_Buffer[2] == 'D') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="D#NJ0210#LeoLiu$";
          strcpy((char*)block, "D#NJ0210#LeoLiu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 E /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'X' && Uart_Buffer[2] == 'E') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="E#NJ0210#LeoLiu$";
          strcpy((char*)block, "E#NJ0210#LeoLiu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }


	// 写入 F /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'X' && Uart_Buffer[2] == 'F') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="F#NJ0210#LeoLiu$";
          strcpy((char*)block, "F#NJ0210#LeoLiu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }


	// 写入 G /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'X' && Uart_Buffer[2] == 'G') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="G#NJ0210#LeoLiu$";
          strcpy((char*)block, "G#NJ0210#LeoLiu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

	// 写入 H /////////////////////////////////////////////////////////////////////////////////////
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'X' && Uart_Buffer[2] == 'H') //write card
    {
      Serial.println("System Write Cards!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);

      u8 buf[32], sta;


      /** Polling the mifar card, buf[0] is the length of the UID */
      sta = nfc.InListPassiveTarget(buf);

      /** check state and UID length */
      if (sta && buf[0] == 4) {
        /** the card may be Mifare Classic card, try to read the block */
        Serial.print("UUID length:");
        Serial.print(buf[0], DEC);
        Serial.println();
        Serial.print("UUID:");
        nfc.puthex(buf + 1, buf[0]);
        Serial.println();
        /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
        u8 key[6] = {
          0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
        };
        u8 blocknum = 4;
        /** Authentication blok 4 */
        sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
        if (sta) {
          /** save read block data */
          u8 block[16];
          Serial.println("Authentication success.");

          ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          //写入卡片
          String Word="";
          Word="H#NJ0210#LeoLiu$";
          strcpy((char*)block, "H#NJ0210#LeoLiu$"); //Write some thing to cards  在这里写入一些文字
          Serial.println("Write "+Word+" to block.... ");
          sta = nfc.MifareWriteBlock(blocknum, block);
          if (sta) {
            Serial.println("Write block successfully ^-^");
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
            delay(50);
            digitalWrite(Buzzer, HIGH);	//蜂鸣器响
            delay(50);					//延时
            digitalWrite(Buzzer, LOW);
          }
        }
      }
      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }
    }

    //距离探测
    if (Uart_Buffer[0] == 'O' && Uart_Buffer[1] == 'N' && Uart_Buffer[2] == 'D') //Detect distence
    {
      Serial.println("System Report Distance!");
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(100);					//延时
      digitalWrite(Buzzer, LOW);
      delay(50);
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);
      delay(50);
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(200);					//延时
      digitalWrite(Buzzer, LOW);
      delay(50);
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      digitalWrite(TrigPin, LOW); //低高低电平发一个短时间脉冲去TrigPin
      delayMicroseconds(2);
      digitalWrite(TrigPin, HIGH);
      delayMicroseconds(10);
      digitalWrite(TrigPin, LOW);
      Value_cm = float( pulseIn(EchoPin, HIGH) * 17 ) / 1000; //将回波时间换算成cm
      //读取一个引脚的脉冲（HIGH或LOW）。例如，如果value是HIGH，pulseIn()会等待引脚变为HIGH，开始计时，再等待引脚变为LOW并停止计时。
      //返回脉冲的长度，单位微秒。如果在指定的时间内无脉冲函数返回。
      //此函数的计时功能由经验决定，长时间的脉冲计时可能会出错。计时范围从10微秒至3分钟。（1秒=1000毫秒=1000000微秒）
      //接收到的高电平的时间（us）* 340m/s / 2 = 接收到高电平的时间（us） * 17000 cm / 1000000 us = 接收到高电平的时间 * 17 / 1000  (cm)
      Serial.println("Distence is :");
      Serial.print(Value_cm);
      Serial.println("cm");
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      delay(500);
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(500);					//延时
      digitalWrite(Buzzer, LOW);

      for (i = 0 ; i < 20 ; i++)
      {
        Uart_Buffer[i] = '0';
      }

    }
  }
  /////////////////////////////////////////////////////////////////////////////////////
  ///////  主循环程序  ////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////

  //读取卡片
  //readcards();            // Read Card  
  //测试距离
  //distance();            // test  distance  
  //检测开关状态
  //Scan_KEY();
  // 根据开关发送字符
  switch( KEY_NUM )
  {
  case 1:			//按键Yes执行程序
    KEY_NUM = 0;		//清空按键标志
    Serial.println("Y!");
    break;
  case 2:			//按键No执行程序
    KEY_NUM = 0;		//清空按键标志
    Serial.println("N!");
    break;
  case 3:			//按键Help执行程序
    KEY_NUM = 0;		//清空按键标志
    Serial.println("H!");
    break;
  default : 
    break;
  }

  delay(50);
  /////////////////////////////////////////////////////////////////////////////
}
////////////////////////////////// END Loop  //////////////////////////////////////

// 读取卡片信息
void readcards()
{
  ///////////////////////////////////////////////////////////////////////////
  //Serial.println("System_Read_Cards!");
  u8 buf[32], sta;//8位 32数组

  /** Polling the mifar card, buf[0] is the length of the UID */
  sta = nfc.InListPassiveTarget(buf);	//判断卡片是否存在

  /** check state and UID length */
  if (sta && buf[0] == 4) {
    /** the card may be Mifare Classic card, try to read the block */
    //Serial.print("UUID_length:");
    //Serial.print(buf[0], DEC);
    //Serial.println();
    //Serial.print("UUID:");
    //nfc.puthex(buf+1, buf[0]);
    //Serial.println();
    /** factory default KeyA: 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF */
    u8 key[6] = {
      0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
    };
    u8 blocknum = 4;
    /** Authentication blok 4 */
    sta = nfc.MifareAuthentication(0, blocknum, buf + 1, buf[0], key);
    if (sta) {
      /** save read block data */
      u8 block[16];	//开始存储收到的区域 
      //Serial.println("Authentication_success.");

      // uncomment following lines for writing data to blok 4
      /*
       strcpy((char*)block, "Elechoues - NFC");
       sta = nfc.MifareWriteBlock(blocknum, block);
       if(sta){
       Serial.println("Write block successfully:");
       }
       */

      /** read block 4 */
      sta = nfc.MifareReadBlock(blocknum, block);
      if (sta) {
        //Serial.println("Read_block_0_successfully:");

        nfc.puthex(block, 16);	//打印IC卡里第一个区域里面 1个字符
        //////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////

        Serial.println();		//换行
        //////////  Beep ///////////////////
        digitalWrite(Buzzer, HIGH);	//蜂鸣器响
        delay(200);					//延时
        digitalWrite(Buzzer, LOW);
        ////////////////////////////////////
      }

      /** read block 5 */
      sta = nfc.MifareReadBlock(blocknum + 1, block);
      if (sta) {
        //Serial.println("Read_block_1_successfully:");

        //nfc.puthex(block, 16);
        //Serial.println();
      }

      /** read block 6 */
      sta = nfc.MifareReadBlock(blocknum + 2, block);
      if (sta) {
        //Serial.println("Read_block_2_successfully:");

        //nfc.puthex(block, 16);

        //Serial.println();
      }

      /** read block 7 */
      sta = nfc.MifareReadBlock(blocknum + 3, block);
      if (sta) {
        //Serial.println("Read_block_3_successfully:");

        //nfc.puthex(block, 16);
        //Serial.println();
      }
    }
  }
  /////////////////////////////////////////////////////
}

void distance()
{
  digitalWrite(TrigPin, LOW); //低高低电平发一个短时间脉冲去TrigPin
  delayMicroseconds(2);
  digitalWrite(TrigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(TrigPin, LOW);
  Value_cm = float( pulseIn(EchoPin, HIGH) * 17 ) / 1000; //将回波时间换算成cm
  //读取一个引脚的脉冲（HIGH或LOW）。例如，如果value是HIGH，pulseIn()会等待引脚变为HIGH，开始计时，再等待引脚变为LOW并停止计时。
  //返回脉冲的长度，单位微秒。如果在指定的时间内无脉冲函数返回。
  //此函数的计时功能由经验决定，长时间的脉冲计时可能会出错。计时范围从10微秒至3分钟。（1秒=1000毫秒=1000000微秒）
  //接收到的高电平的时间（us）* 340m/s / 2 = 接收到高电平的时间（us） * 17000 cm / 1000000 us = 接收到高电平的时间 * 17 / 1000  (cm)
  if (Value_cm < 100)
  {
    Serial.print("V!_");
    Serial.println(Value_cm);
    for (int m = 0; m < 10; m++)
    {
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(30);					//延时
      digitalWrite(Buzzer, LOW);
      delay(30);
      digitalWrite(Buzzer, HIGH);	//蜂鸣器响
      delay(30);
      digitalWrite(Buzzer, LOW);
      delay(30);
    }
  }
}


void Scan_KEY()
{
  // 按键 Yes
  if( digitalRead(KEY_yes) == LOW )			//按键1扫描
  {
    delay(20);								//延时去抖
    if( digitalRead(KEY_yes) == LOW )
    {
      while(digitalRead(KEY_yes) == LOW);	//等待松手
      KEY_NUM = 1;
    }
  }
  // 按键 No
  if( digitalRead(KEY_no) == LOW )			//按键2扫描
  {
    delay(20);								//延时去抖
    if( digitalRead(KEY_no) == LOW )
    {
      while(digitalRead(KEY_no) == LOW);	//等待松手
      KEY_NUM = 2;
    }
  }
  // 按键 Help
  if( digitalRead(KEY_help) == LOW )			//按键2扫描
  {
    delay(20);								//延时去抖
    if( digitalRead(KEY_help) == LOW )
    {
      while(digitalRead(KEY_help) == LOW);	//等待松手
      KEY_NUM = 3;
    }
  }
}


