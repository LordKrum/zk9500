package com.zkteco.biometric;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class BiometricController {
	
	Boolean getting_patient, setting_existing, setting_new, start_case_num_fail, getting_fingers;
	Boolean is_right_index, is_right_thumb, is_left_index, is_left_thumb;
	int finger_id, start_id;
	
	@FXML
	private Label results_prompt;
	@FXML
	private Label results_prompt1;
	@FXML
	private Label results_prompt2;
	
	@FXML
	private ImageView view_fingerprint;
	@FXML
	private ImageView view_fingerprint1;
	@FXML
	private ImageView view_fingerprint2;
	
	@FXML
	private Button right_index;
	
	@FXML
	private Button right_thumb;
	
	@FXML
	private Button left_index;
	
	@FXML
	private Button left_thumb;
	
	@FXML
	private Button right_index1;
	
	@FXML
	private Button right_thumb1;
	
	@FXML
	private Button left_index1;
	
	@FXML
	private Button left_thumb1;
	
	@FXML
	private TextField case_id;
	
	//the width of fingerprint image
	int fpWidth = 0;
	//the height of fingerprint image
	int fpHeight = 0;
	//for verify test
	private byte[] lastRegTemp = new byte[2048];
	//the length of lastRegTemp
	private int cbRegTemp = 0;
	//pre-register template
	private byte[][] regtemparray = new byte[3][2048];
	//Register
	private boolean bRegister = false;
	//Identify
	private boolean bIdentify = true;
	//finger id
	private int iFid = 1;
	
	private int nFakeFunOn = 1;
	//must be 3
	static final int enroll_cnt = 3;
	//the index of pre-register function
	private int enroll_idx = 0;
	
	private byte[] imgbuf = null;
	private byte[] template = new byte[2048];
	private int[] templateLen = new int[1];
	
	private boolean mbStop = true;
	private long mhDevice = 0;
	private long mhDB = 0;
	private WorkThread workThread = null;
	
	public void FreeSensor()
	{
		mbStop = true;
		try {		//wait for thread stopping
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (0 != mhDB)
		{
			FingerprintSensorEx.DBFree(mhDB);
			mhDB = 0;
		}
		if (0 != mhDevice)
		{
			FingerprintSensorEx.CloseDevice(mhDevice);
			mhDevice = 0;
		}
		FingerprintSensorEx.Terminate();
	}
	
	public static void writeBitmap(byte[] imageBuf, int nWidth, int nHeight,
			String path) throws IOException {
		java.io.FileOutputStream fos = new java.io.FileOutputStream(path);
		java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);

		int w = (((nWidth+3)/4)*4);
		int bfType = 0x424d; // ä½�å›¾æ–‡ä»¶ç±»åž‹ï¼ˆ0â€”1å­—èŠ‚ï¼‰
		int bfSize = 54 + 1024 + w * nHeight;// bmpæ–‡ä»¶çš„å¤§å°�ï¼ˆ2â€”5å­—èŠ‚ï¼‰
		int bfReserved1 = 0;// ä½�å›¾æ–‡ä»¶ä¿�ç•™å­—ï¼Œå¿…é¡»ä¸º0ï¼ˆ6-7å­—èŠ‚ï¼‰
		int bfReserved2 = 0;// ä½�å›¾æ–‡ä»¶ä¿�ç•™å­—ï¼Œå¿…é¡»ä¸º0ï¼ˆ8-9å­—èŠ‚ï¼‰
		int bfOffBits = 54 + 1024;// æ–‡ä»¶å¤´å¼€å§‹åˆ°ä½�å›¾å®žé™…æ•°æ�®ä¹‹é—´çš„å­—èŠ‚çš„å��ç§»é‡�ï¼ˆ10-13å­—èŠ‚ï¼‰

		dos.writeShort(bfType); // è¾“å…¥ä½�å›¾æ–‡ä»¶ç±»åž‹'BM'
		dos.write(changeByte(bfSize), 0, 4); // è¾“å…¥ä½�å›¾æ–‡ä»¶å¤§å°�
		dos.write(changeByte(bfReserved1), 0, 2);// è¾“å…¥ä½�å›¾æ–‡ä»¶ä¿�ç•™å­—
		dos.write(changeByte(bfReserved2), 0, 2);// è¾“å…¥ä½�å›¾æ–‡ä»¶ä¿�ç•™å­—
		dos.write(changeByte(bfOffBits), 0, 4);// è¾“å…¥ä½�å›¾æ–‡ä»¶å��ç§»é‡�

		int biSize = 40;// ä¿¡æ�¯å¤´æ‰€éœ€çš„å­—èŠ‚æ•°ï¼ˆ14-17å­—èŠ‚ï¼‰
		int biWidth = nWidth;// ä½�å›¾çš„å®½ï¼ˆ18-21å­—èŠ‚ï¼‰
		int biHeight = nHeight;// ä½�å›¾çš„é«˜ï¼ˆ22-25å­—èŠ‚ï¼‰
		int biPlanes = 1; // ç›®æ ‡è®¾å¤‡çš„çº§åˆ«ï¼Œå¿…é¡»æ˜¯1ï¼ˆ26-27å­—èŠ‚ï¼‰
		int biBitcount = 8;// æ¯�ä¸ªåƒ�ç´ æ‰€éœ€çš„ä½�æ•°ï¼ˆ28-29å­—èŠ‚ï¼‰ï¼Œå¿…é¡»æ˜¯1ä½�ï¼ˆå�Œè‰²ï¼‰ã€�4ä½�ï¼ˆ16è‰²ï¼‰ã€�8ä½�ï¼ˆ256è‰²ï¼‰æˆ–è€…24ä½�ï¼ˆçœŸå½©è‰²ï¼‰ä¹‹ä¸€ã€‚
		int biCompression = 0;// ä½�å›¾åŽ‹ç¼©ç±»åž‹ï¼Œå¿…é¡»æ˜¯0ï¼ˆä¸�åŽ‹ç¼©ï¼‰ï¼ˆ30-33å­—èŠ‚ï¼‰ã€�1ï¼ˆBI_RLEBåŽ‹ç¼©ç±»åž‹ï¼‰æˆ–2ï¼ˆBI_RLE4åŽ‹ç¼©ç±»åž‹ï¼‰ä¹‹ä¸€ã€‚
		int biSizeImage = w * nHeight;// å®žé™…ä½�å›¾å›¾åƒ�çš„å¤§å°�ï¼Œå�³æ•´ä¸ªå®žé™…ç»˜åˆ¶çš„å›¾åƒ�å¤§å°�ï¼ˆ34-37å­—èŠ‚ï¼‰
		int biXPelsPerMeter = 0;// ä½�å›¾æ°´å¹³åˆ†è¾¨çŽ‡ï¼Œæ¯�ç±³åƒ�ç´ æ•°ï¼ˆ38-41å­—èŠ‚ï¼‰è¿™ä¸ªæ•°æ˜¯ç³»ç»Ÿé»˜è®¤å€¼
		int biYPelsPerMeter = 0;// ä½�å›¾åž‚ç›´åˆ†è¾¨çŽ‡ï¼Œæ¯�ç±³åƒ�ç´ æ•°ï¼ˆ42-45å­—èŠ‚ï¼‰è¿™ä¸ªæ•°æ˜¯ç³»ç»Ÿé»˜è®¤å€¼
		int biClrUsed = 0;// ä½�å›¾å®žé™…ä½¿ç”¨çš„é¢œè‰²è¡¨ä¸­çš„é¢œè‰²æ•°ï¼ˆ46-49å­—èŠ‚ï¼‰ï¼Œå¦‚æžœä¸º0çš„è¯�ï¼Œè¯´æ˜Žå…¨éƒ¨ä½¿ç”¨äº†
		int biClrImportant = 0;// ä½�å›¾æ˜¾ç¤ºè¿‡ç¨‹ä¸­é‡�è¦�çš„é¢œè‰²æ•°(50-53å­—èŠ‚)ï¼Œå¦‚æžœä¸º0çš„è¯�ï¼Œè¯´æ˜Žå…¨éƒ¨é‡�è¦�

		dos.write(changeByte(biSize), 0, 4);// è¾“å…¥ä¿¡æ�¯å¤´æ•°æ�®çš„æ€»å­—èŠ‚æ•°
		dos.write(changeByte(biWidth), 0, 4);// è¾“å…¥ä½�å›¾çš„å®½
		dos.write(changeByte(biHeight), 0, 4);// è¾“å…¥ä½�å›¾çš„é«˜
		dos.write(changeByte(biPlanes), 0, 2);// è¾“å…¥ä½�å›¾çš„ç›®æ ‡è®¾å¤‡çº§åˆ«
		dos.write(changeByte(biBitcount), 0, 2);// è¾“å…¥æ¯�ä¸ªåƒ�ç´ å� æ�®çš„å­—èŠ‚æ•°
		dos.write(changeByte(biCompression), 0, 4);// è¾“å…¥ä½�å›¾çš„åŽ‹ç¼©ç±»åž‹
		dos.write(changeByte(biSizeImage), 0, 4);// è¾“å…¥ä½�å›¾çš„å®žé™…å¤§å°�
		dos.write(changeByte(biXPelsPerMeter), 0, 4);// è¾“å…¥ä½�å›¾çš„æ°´å¹³åˆ†è¾¨çŽ‡
		dos.write(changeByte(biYPelsPerMeter), 0, 4);// è¾“å…¥ä½�å›¾çš„åž‚ç›´åˆ†è¾¨çŽ‡
		dos.write(changeByte(biClrUsed), 0, 4);// è¾“å…¥ä½�å›¾ä½¿ç”¨çš„æ€»é¢œè‰²æ•°
		dos.write(changeByte(biClrImportant), 0, 4);// è¾“å…¥ä½�å›¾ä½¿ç”¨è¿‡ç¨‹ä¸­é‡�è¦�çš„é¢œè‰²æ•°

		for (int i = 0; i < 256; i++) {
			dos.writeByte(i);
			dos.writeByte(i);
			dos.writeByte(i);
			dos.writeByte(0);
		}

		byte[] filter = null;
		if (w > nWidth)
		{
			filter = new byte[w-nWidth];
		}
		
		for(int i=0;i<nHeight;i++)
		{
			dos.write(imageBuf, (nHeight-1-i)*nWidth, nWidth);
			if (w > nWidth)
				dos.write(filter, 0, w-nWidth);
		}
		dos.flush();
		dos.close();
		fos.close();
	}

	public static byte[] changeByte(int data) {
		return intToByteArray(data);
	}
	
	public static byte[] intToByteArray (final int number) {
		byte[] abyte = new byte[4];  
	    // "&" ä¸Žï¼ˆANDï¼‰ï¼Œå¯¹ä¸¤ä¸ªæ•´åž‹æ“�ä½œæ•°ä¸­å¯¹åº”ä½�æ‰§è¡Œå¸ƒå°”ä»£æ•°ï¼Œä¸¤ä¸ªä½�éƒ½ä¸º1æ—¶è¾“å‡º1ï¼Œå�¦åˆ™0ã€‚  
	    abyte[0] = (byte) (0xff & number);  
	    // ">>"å�³ç§»ä½�ï¼Œè‹¥ä¸ºæ­£æ•°åˆ™é«˜ä½�è¡¥0ï¼Œè‹¥ä¸ºè´Ÿæ•°åˆ™é«˜ä½�è¡¥1  
	    abyte[1] = (byte) ((0xff00 & number) >> 8);  
	    abyte[2] = (byte) ((0xff0000 & number) >> 16);  
	    abyte[3] = (byte) ((0xff000000 & number) >> 24);  
	    return abyte; 
	}	 
		 
	public static int byteArrayToInt(byte[] bytes) {
		int number = bytes[0] & 0xFF;  
	    // "|="æŒ‰ä½�æˆ–èµ‹å€¼ã€‚  
	    number |= ((bytes[1] << 8) & 0xFF00);  
	    number |= ((bytes[2] << 16) & 0xFF0000);  
	    number |= ((bytes[3] << 24) & 0xFF000000);  
	    return number;  
	 }
	
	private class WorkThread extends Thread {
        @Override
        public void run() {
            super.run();
            int ret = 0;
            while (!mbStop) {
            	templateLen[0] = 2048;
            	if (0 == (ret = FingerprintSensorEx.AcquireFingerprint(mhDevice, imgbuf, template, templateLen)))
            	{
            		if (nFakeFunOn == 1)
                	{
                		byte[] paramValue = new byte[4];
        				int[] size = new int[1];
        				size[0] = 4;
        				int nFakeStatus = 0;
        				//GetFakeStatus
        				ret = FingerprintSensorEx.GetParameters(mhDevice, 2004, paramValue, size);
        				nFakeStatus = byteArrayToInt(paramValue);
        				System.out.println("ret = "+ ret +",nFakeStatus=" + nFakeStatus);
        				if (0 == ret && (byte)(nFakeStatus & 31) != 31)
        				{
        					results_prompt.setText("Is it a fake finger?\n");
        					return;
        				}
                	}
                	OnCatpureOK(imgbuf);
                	try {
                		OnExtractOK(template, templateLen[0]);
                	}catch(Exception e) {
                		System.out.println(e.getMessage());
                	}
            	}
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
		
	private void OnCatpureOK(byte[] imgBuf)
	{
		try {
			writeBitmap(imgBuf, fpWidth, fpHeight, "fingerprint.bmp");
			System.out.println("Getting patient " + getting_patient);
			System.out.println("Setting new patient " + setting_new);
			System.out.println("Setting existing patient " + setting_existing);
			if(getting_patient) {
				view_fingerprint.setImage(new Image(new File("fingerprint.bmp").toURI().toString()));
			} else if(setting_new) {
				view_fingerprint1.setImage(new Image(new File("fingerprint.bmp").toURI().toString()));
			} else if(setting_existing) {
				view_fingerprint2.setImage(new Image(new File("fingerprint.bmp").toURI().toString()));
			} 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void OnExtractOK(byte[] template, int len)
	{
		if(bRegister)
		{
			int[] fid = new int[1];
			int[] score = new int [1];
            int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
            if(setting_existing) {
            	if((is_right_index || is_right_thumb || is_left_index || is_left_thumb) != true) {
            		GUIHelper.runSafe(()->results_prompt2.setText("Please select finger first"));
            		return;
            	}
            	if(case_id.getText().isEmpty()) {
        			enroll_idx = 0;
        			GUIHelper.runSafe(()->results_prompt2.setText("Please enter Case ID first then press finger 3 times"));
        			return;
        		}
            }
            if (ret == 0)
            {
            	if(setting_new) {
            		GUIHelper.runSafe(()->results_prompt1.setText("Fingerprint is already registered, use new existing patient tab to add other fingers"));
    			} else if(setting_existing) {
    				if(is_right_index) {
    					GUIHelper.runSafe(()->results_prompt2.setText("Right index is already registered, select different finger"));
    				}else if(is_right_thumb) {
    					GUIHelper.runSafe(()->results_prompt2.setText("Right thumb already registered, select different finger"));
    				}else if(is_left_index) {
    					GUIHelper.runSafe(()->results_prompt2.setText("Left index already registered, select different finger"));
    				}else if(is_left_thumb) {
    					GUIHelper.runSafe(()->results_prompt2.setText("Left thumb already registered, select different finger"));
    				}
    			} 
                bRegister = true;
                enroll_idx = 0;
                return;
            }
            if (enroll_idx > 0 && FingerprintSensorEx.DBMatch(mhDB, regtemparray[enroll_idx-1], template) <= 0)
            {
            	if(setting_new) {
            		GUIHelper.runSafe(()->results_prompt1.setText("Press the right index finger " + (3 - enroll_idx) + " times for the enrollment"));
    			} else if(setting_existing) {
    				if(is_right_index) {
    					GUIHelper.runSafe(()->results_prompt2.setText("Wrong finger! Press the right index finger " + (3 - enroll_idx) + " times for the enrollment"));
    				}else if(is_right_thumb) {
    					GUIHelper.runSafe(()->results_prompt2.setText("Wrong finger! Press the right thumb " + (3 - enroll_idx) + " times for the enrollment"));
    				}else if(is_left_index) {
    					GUIHelper.runSafe(()->results_prompt2.setText("Wrong finger! Press the left index finger " + (3 - enroll_idx) + " times for the enrollment"));
    				}else if(is_left_thumb) {
    					GUIHelper.runSafe(()->results_prompt2.setText("Wrong finger! Press the left thumb " + (3 - enroll_idx) + " times for the enrollment"));
    				}
    			} 
                return;
            }
            System.arraycopy(template, 0, regtemparray[enroll_idx], 0, 2048);
            enroll_idx++;
            if (enroll_idx == 3) {
            	int[] _retLen = new int[1];
                _retLen[0] = 2048;
                byte[] regTemp = new byte[_retLen[0]];
                
                if (0 == (ret = FingerprintSensorEx.DBMerge(mhDB, regtemparray[0], regtemparray[1], regtemparray[2], regTemp, _retLen)) &&
                		0 == (ret = FingerprintSensorEx.DBAdd(mhDB, iFid, regTemp))) {
                	String prefix = "";
                	if(setting_new) {
                		if(start_id >= 1000) {
                    		prefix = "CNHK" + start_id;
                    	}else if(start_id >= 100) {
                    		prefix = "CNHK0" + start_id;
                    	}else if(start_id >= 10) {
                    		prefix = "CNHK00" + start_id;
                    	}else if(start_id >= 1) {
                    		prefix = "CNHK000" + start_id;
                    	}
                	}else if(setting_existing) {
                		prefix = case_id.getText();
                	}
                	
                	DatabaseHandler.insert(regTemp, iFid, prefix);
                	finger_id = iFid;
                	if(setting_new) start_id++;
                	iFid++;
                	cbRegTemp = _retLen[0];
                    System.arraycopy(regTemp, 0, lastRegTemp, 0, cbRegTemp);
                    //Base64 Template
                    if(setting_new) {
                    	GUIHelper.runSafe(()->results_prompt1.setText("Patient finger added, to add other fingers use the new existing patient tab"));
        			} else if(setting_existing) {
        				GUIHelper.runSafe(()->results_prompt2.setText("Patient fingerprint added\n"));
        			}
                } else {
                	if(setting_new) {
                		GUIHelper.runSafe(()->results_prompt1.setText("Enroll fail"));
        			} else if(setting_existing) {
        				GUIHelper.runSafe(()->results_prompt2.setText("Enroll fail"));
        			}
                }
                bRegister = false;
            } else {
            	if(setting_new) {
            		GUIHelper.runSafe(()->results_prompt1.setText("You need to press the same finger " + (3 - enroll_idx) + " times"));
    			} else if(setting_existing) {
    				GUIHelper.runSafe(()->results_prompt2.setText("You need to press the same finger " + (3 - enroll_idx) + " times"));
    			}
            }
		}
		else
		{
			if (bIdentify)
			{
				int[] fid = new int[1];
				int[] score = new int [1];
				int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
                if (ret == 0)
                {
                	GUIHelper.runSafe(()->results_prompt.setText("Fingerprint identified"));
                	finger_id = fid[0];
                }
                else
                {
                	GUIHelper.runSafe(()->results_prompt.setText("No records found"));
                	finger_id = -1;
                }
                    
			}
			/*
			else
			{
				if(cbRegTemp <= 0)
				{
					GUIHelper.runSafe(()->results_prompt.setText("Please register first!"));
				}
				else
				{
					int ret = FingerprintSensorEx.DBMatch(mhDB, lastRegTemp, template);
					if(ret > 0)
					{
						GUIHelper.runSafe(()->results_prompt.setText("Verify succ, score=" + ret));
					}
					else
					{
						GUIHelper.runSafe(()->results_prompt.setText("Verify fail, ret=" + ret));
					}
				}
			}
			*/
		}
	}
	
	@SuppressWarnings("unchecked")
	private void onClickStart() {
		if (0 != mhDevice)
		{
			//already inited
			GUIHelper.runSafe(()->results_prompt.setText("Please close device first!\n"));
			return;
		}
		int ret = FingerprintSensorErrorCode.ZKFP_ERR_OK;
		//Initialize
		cbRegTemp = 0;
		bRegister = false;
		bIdentify = false;
		iFid = 1;
		enroll_idx = 0;
		if (FingerprintSensorErrorCode.ZKFP_ERR_OK != FingerprintSensorEx.Init())
		{
			GUIHelper.runSafe(()->results_prompt.setText("Failed to start!"));
			return;
		}
		ret = FingerprintSensorEx.GetDeviceCount();
		if (ret < 0)
		{
			GUIHelper.runSafe(()->results_prompt.setText("No devices connected!"));
			FreeSensor();
			return;
		}
		if (0 == (mhDevice = FingerprintSensorEx.OpenDevice(0)))
		{
			GUIHelper.runSafe(()->results_prompt.setText("Open device fail"));
			FreeSensor();
			return;
		}
		if (0 == (mhDB = FingerprintSensorEx.DBInit()))
		{
			GUIHelper.runSafe(()->results_prompt.setText("Init DB fail"));
			FreeSensor();
			return;
		}
		
		int nFmt = 0;	//Ansi
		
		FingerprintSensorEx.DBSetParameter(mhDB,  5010, nFmt);				
		
		byte[] paramValue = new byte[4];
		int[] size = new int[1];
		
		size[0] = 4;
		FingerprintSensorEx.GetParameters(mhDevice, 1, paramValue, size);
		fpWidth = byteArrayToInt(paramValue);
		size[0] = 4;
		FingerprintSensorEx.GetParameters(mhDevice, 2, paramValue, size);
		fpHeight = byteArrayToInt(paramValue);
						
		imgbuf = new byte[fpWidth*fpHeight];
		//btnImg.resize(fpWidth, fpHeight);
		mbStop = false;
		workThread = new WorkThread();
	    workThread.start();// 线程启动
	    results_prompt.setText("Device Started");
	    
	    //get db fingerprints
	    ArrayList<ArrayList<?>> data = DatabaseHandler.getAll();
	    ArrayList<byte[]> fprints = (ArrayList<byte[]>) data.get(0);
		ArrayList<Integer> fid = (ArrayList<Integer>) data.get(1);
    	if(fprints.size() == fid.size()) { 
    		System.out.println("DB size match, " + fid.size());
    		int i;
    		for(i = 0; i < fprints.size(); i++) {
    			if(0 == (ret = FingerprintSensorEx.DBAdd(mhDB, fid.get(i), fprints.get(i)))){
    				System.out.println("Added patient fingerprint " + (i+1));
    			}else {
    				System.out.println("Failed to add patient fingerprint " + (i+1));
    			}
    			iFid = fid.get(i);	
    		}
    		if(fprints.size() > 0) {
    			iFid += 1;
    		}
    		System.out.println("Next finger id " + iFid);
    	}
    	
	}
	
	@FXML
	private void onClickNew() {
		getting_fingers = false;
		bRegister = false;
		if(start_case_num_fail) {
			GUIHelper.runSafe(()->results_prompt1.setText("Please Reconnect Records from menu options"));
			return;
		}
		getting_patient = setting_existing = setting_new = false;
		finger_id = -1;
		setting_new = true;
		if(0 == mhDevice)
		{
			GUIHelper.runSafe(()->results_prompt1.setText("Please Reconnect Device"));
			return;
		}
		if(!bRegister)
		{
			enroll_idx = 0;
			bRegister = true;
			GUIHelper.runSafe(()->results_prompt1.setText("Place right index finger 3 times"));
		}
	}
	
	@FXML
	private void onClickGet() {
		getting_fingers = false;
		if(start_case_num_fail) {
			GUIHelper.runSafe(()->results_prompt.setText("Please Reconnect Records from menu options!\n"));
			return;
		}
		getting_patient = setting_existing = setting_new = false;
		finger_id = -1;
		getting_patient  = true;
		if(0 == mhDevice)
		{
			GUIHelper.runSafe(()->results_prompt.setText("Please Reconnect Device!\n"));
			return;
		}
		if(bRegister)
		{
			enroll_idx = 0;
			bRegister = false;
		}
		if(!bIdentify)
		{
			bIdentify = true;
		}
		GUIHelper.runSafe(()->results_prompt.setText("Place finger on device"));
	}
	
	@FXML
	private void onClickSetExisting() {
		getting_fingers = false;
		bRegister = false;
		if(start_case_num_fail) {
			GUIHelper.runSafe(()->results_prompt2.setText("Please Reconnect Records from menu options!\n"));
			return;
		}
		getting_patient = setting_existing = setting_new = false;
		finger_id = -1;
		setting_existing  = true;
		if(0 == mhDevice)
		{
			GUIHelper.runSafe(()->results_prompt2.setText("Please Reconnect Device!\n"));
			return;
		}
		if(!bRegister)
		{
			enroll_idx = 0;
			bRegister = true;
			GUIHelper.runSafe(()->results_prompt2.setText("Enter case ID and select finger before placing finger 3 times!\n"));
		}
	}
	
	@FXML
	private void onClickClose() {
		FreeSensor();
		if(getting_patient) {
			GUIHelper.runSafe(()->results_prompt.setText("Device Closed!\n"));
		} else if(setting_new) {
			GUIHelper.runSafe(()->results_prompt1.setText("Device Closed!\n"));
		} else if(setting_existing) {
			GUIHelper.runSafe(()->results_prompt2.setText("Device Closed!\n"));
		}
	}
	
	@FXML
	private void onClickReconnect() {
		onClickStart();
	}
	
	@FXML
	private void onClickReconnectDatabase() {
		setStartCaseNum();
		if(start_case_num_fail) {
			if(getting_patient) {
				GUIHelper.runSafe(()->results_prompt.setText("Please Reconnect Records from menu options!\n"));
			} else if(setting_new) {
				GUIHelper.runSafe(()->results_prompt1.setText("Please Reconnect Records from menu options!\n"));
			} else if(setting_existing) {
				GUIHelper.runSafe(()->results_prompt2.setText("Please Reconnect Records from menu options!\n"));
			}
			return;
		}
	}
	
	@FXML
	private void onClickCopyGetPatient() {
		if(finger_id < 0) {
			GUIHelper.runSafe(()->results_prompt.setText("No records found"));
		}else {
			System.out.println(finger_id);
			String case_num = DatabaseHandler.getID(finger_id).get(0);
			copyClipboard(case_num);
			GUIHelper.runSafe(()->results_prompt.setText("ID copied: " + case_num));
		}
	}
	
	@FXML
	private void onClickCopyGetNewPatient() {
		if(finger_id < 0) {
			GUIHelper.runSafe(()->results_prompt1.setText("No records found"));
		}else {
			System.out.println(finger_id);
			String case_num = DatabaseHandler.getID(finger_id).get(0);
			copyClipboard(case_num);
			GUIHelper.runSafe(()->results_prompt1.setText("ID Copied: " + case_num));
		}
	}
	
	@FXML
	private void onClickCopyGetExistingPatient() {
		if(finger_id < 0) {
			GUIHelper.runSafe(()->results_prompt2.setText("No records found"));
		}else {
			System.out.println(finger_id);
			String case_num = DatabaseHandler.getID(finger_id).get(0);
			copyClipboard(case_num);
			GUIHelper.runSafe(()->results_prompt2.setText("ID Copied: " + case_num));
		}
	}
	
	@FXML
	private void onClickRightIndex() {
		getting_fingers = true;
		is_right_index = is_right_thumb = is_left_index = is_left_thumb = false;
		is_right_index = true;
		GUIHelper.runSafe(()->results_prompt2.setText("Place right index 3 times"));
		enroll_idx = 0;
		bRegister = true;
	}
	
	@FXML
	private void onClickRightThumb() {
		getting_fingers = true;
		is_right_index = is_right_thumb = is_left_index = is_left_thumb = false;
		is_right_thumb = true;
		GUIHelper.runSafe(()->results_prompt2.setText("Place right thumb 3 times"));
		enroll_idx = 0;
		bRegister = true;
	}
	
	@FXML
	private void onClickLeftIndex() {
		getting_fingers = true;
		is_right_index = is_right_thumb = is_left_index = is_left_thumb = false;
		is_left_index = true;
		GUIHelper.runSafe(()->results_prompt2.setText("Place left index 3 times"));
		enroll_idx = 0;
		bRegister = true;
	}
	
	@FXML
	private void onClickLeftThumb() {
		getting_fingers = true;
		is_right_index = is_right_thumb = is_left_index = is_left_thumb = false;
		is_left_thumb = true;
		GUIHelper.runSafe(()->results_prompt2.setText("Place left thumb 3 times"));
		enroll_idx = 0;
		bRegister = true;
	}
	
	@FXML
	private void onClickRefresh() {
		getting_fingers = false;
		is_right_index = is_right_thumb = is_left_index = is_left_thumb = false;
		if(setting_new) {
			GUIHelper.runSafe(()->results_prompt1.setText("Restart process of adding patient right index finger"));
		} else if(setting_existing) {
			GUIHelper.runSafe(()->results_prompt2.setText("Restart process of adding patient right index finger"));
		}
	}
	
	
	
	@FXML
	void initialize() {
		start_id = 1;
		getting_patient = setting_existing = setting_new = getting_fingers = false;
		finger_id = -1;
		start_case_num_fail = true;
		onClickStart();
		setStartCaseNum();
		/*
		try {
			TimeUnit.SECONDS.sleep(3);
		}catch(InterruptedException e) {
			System.out.println(e.getMessage());
		}
		*/
	}
	
	private void setStartCaseNum() {
		int start_num = DatabaseHandler.getSize();
		if(start_num < 0) {
			start_case_num_fail = true;
		}else {
			start_id += start_num;
			start_case_num_fail = false;
		}
		System.out.println("Starting CNHK at " + start_id);
	}
	
	private void copyClipboard(String id) {
    	Clipboard board = Clipboard.getSystemClipboard();
    	ClipboardContent content = new ClipboardContent();
    	content.putString(id);
    	board.setContent(content);
    }

}
