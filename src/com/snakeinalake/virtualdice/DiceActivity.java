package com.snakeinalake.virtualdice;

import yuku.ambilwarna.AmbilWarnaDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.snakeinalake.engine.OrangeDevice;
import com.snakeinalake.engine.core.OColor4f;
import com.snakeinalake.engine.core.vector3df;
import com.snakeinalake.engine.input.updated.OInputManager;
import com.snakeinalake.engine.utilities.OUtilClock.IFC_UNIT;

public class DiceActivity extends Activity{
	OrangeDevice device;
	DiceRenderer renderer;
	AlertDialog dNumDie,dBGCol,dDiceCol,dAbout;
	AmbilWarnaDialog awdBGCol = null, awdDiceCol = null, awdDotCol = null;
	public Handler handle;
	public SoundPool pool;
	int dieSounds[];
	int Orientation;
	boolean playAudio;
	int pDieNum;
	OColor4f pBGCol,pDiceCol,pDotCol;
	SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppRater.app_launched(this);
        GLSurfaceView surf = new GLSurfaceView(this);
        device = new OrangeDevice(this);
        Orientation = getResources().getConfiguration().orientation;
        OInputManager input = new OInputManager(device);
        device.setInputHandler(input);
        input.attachToSurface(surf); 
        
        setupDialog();
        
        prefs = this.getPreferences(MODE_PRIVATE);
	        
	        
	    playAudio = prefs.getBoolean("audio", true);
	    pDieNum = prefs.getInt("numdie", 5);
	    pBGCol = new OColor4f(
    			prefs.getFloat("bgcolr", 0),
    			prefs.getFloat("bgcolg", 0.8f),
    			prefs.getFloat("bgcolb", 0)
    		);
	    pDiceCol = new OColor4f(
    			prefs.getFloat("dicecolr", 0.8f),
    			prefs.getFloat("dicecolg", 0),
    			prefs.getFloat("dicecolb", 0)
    		);
	    pDotCol = new OColor4f(
    			prefs.getFloat("dotcolr", 0),
    			prefs.getFloat("dotcolg", 0),
    			prefs.getFloat("dotcolb", 0) 
    		);
        
        Handler.Callback cb = new Handler.Callback(){
			@Override
			public boolean handleMessage(Message msg) {
				switch(msg.arg1){
					case 0: //set dice count
						dNumDie.show();
						break;
					case 1:
						dieCollision();
						break;
					case 11:
						thunderstorm();
						break;
					case 111:
						moo();
						break;
					case 3: //set background color
						dBGCol.show();
						break;
					case 33: //set dice color
						dDiceCol.show();
						break;
					case -1: //show "about" info
						dAbout.show();
						break;
					case 2:
						//Toggle Audio
						SharedPreferences.Editor pedit = prefs.edit();
							pedit.putBoolean("audio", !playAudio);
						pedit.commit();
						playAudio = !playAudio;
						break;
					default:
						return false;
				}
				return true;
			}        	
        };
        handle = new Handler(cb);
         
        renderer = new DiceRenderer(device, handle);
        renderer.orienationChanged(Orientation);
        renderer.setDie(pDieNum);
        renderer.setBGCol(pBGCol);
        renderer.setDiceCol(pDiceCol);
        renderer.setDotCol(pDotCol);
        if(savedInstanceState!=null)
        if(savedInstanceState.containsKey("numDice")){
	        int numdice = savedInstanceState.getInt("numDice");
	        renderer.setDie(numdice);
	        for(int i=0; i<numdice; i++){
	        	renderer.loadDie(
	        			new vector3df(
		        					savedInstanceState.getFloat("dice["+i+"].pos.x"),
		        					savedInstanceState.getFloat("dice["+i+"].pos.y"),
		        					savedInstanceState.getFloat("dice["+i+"].pos.z")
	        					),
	        			new vector3df(
		        					savedInstanceState.getInt("dice["+i+"].goal.x"),
		        					0,
		        					savedInstanceState.getInt("dice["+i+"].goal.z")
	        					)
	        	);
	        }
        }
        
        
        device.setRenderer(renderer);
        surf.setRenderer(renderer);
        
        setContentView(surf);
    }
    int counter = 0;
    int sndint = 100;
    public void dieCollision(){
    	try{
	    	if(!playAudio)
	    		return;
	    	if(!device.getMainClock().isSetTimer("audio")){
	    		device.getMainClock().setTimer("audio", sndint, IFC_UNIT.IFC_MILI);
	    	}else
	    		if(device.getMainClock().getTimer("audio")<=0){
			    	pool.play(dieSounds[counter], 1, 1, 1, 0, 1.0f);
			    	counter++;
			    	if(counter>=dieSounds.length-1) 
			    		counter = 0;
			    	device.getMainClock().setTimer("audio", sndint, IFC_UNIT.IFC_MILI);
	    		}else
	    			return;
    	}catch(Exception e){
    		return;
    	}
    }
    MediaPlayer storm;
    public void thunderstorm(){
    	try{
	    	if(!playAudio)
	    		return;
	    		storm.start();
    	}catch(Exception e){
    		return;
    	}
    }
    public void moo(){
    	try{
	    	if(!playAudio)
	    		return;
	    	if(!device.getMainClock().isSetTimer("mootimer")){
	    		device.getMainClock().setTimer("mootimer", 2500, IFC_UNIT.IFC_MILI);
	    		pool.play(dieSounds[dieSounds.length-1], 1, 1, 1, 0, 1.0f);
	    	}
	    	if(device.getMainClock().getTimer("mootimer")<=0){
	    		pool.play(dieSounds[dieSounds.length-1], 1, 1, 1, 0, 1.0f);
	    		device.getMainClock().setTimer("mootimer", 2500, IFC_UNIT.IFC_MILI);
	    	}
    	}catch(Exception e){
    		return;
    	}
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	if(newConfig.orientation!=Orientation){
    		Orientation = newConfig.orientation;
    		renderer.orienationChanged(Orientation);
    	}
    	super.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onResume() {
        pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        dieSounds = new int[]{
        	pool.load(this, R.raw.dicethrow4, 1),  	
			pool.load(this, R.raw.diethrow1, 1),
            pool.load(this, R.raw.dicethrow1, 1),
            pool.load(this, R.raw.diethrow3, 1),
            pool.load(this, R.raw.dicethrow2, 1),
            pool.load(this, R.raw.diethrow2, 1),
            pool.load(this, R.raw.dicethrow3, 1),
            pool.load(this, R.raw.moo, 1)
        };
        storm = MediaPlayer.create(this, R.raw.wind);
    	super.onResume();
    }
    @Override
    protected void onPause() {
    	if(pool!=null){
	    	pool.release();
	    	pool = null;
    	}
    	super.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	/*		What is saved?
    	 * 	Dice Positions and Goals
    	 *  Number of Dice
    	 * */
    	try{
    	for(int i=0; i<renderer.die.length; i++){
    		if(renderer.die[i]==null)
    			continue;
    		vector3df pos = renderer.die[i].node.getPosition();    		
    		outState.putFloat("dice["+i+"].pos.x", pos.x);
    		outState.putFloat("dice["+i+"].pos.y", pos.y);
    		outState.putFloat("dice["+i+"].pos.z", pos.z);
    		
    		outState.putInt("dice["+i+"].goal.x", renderer.die[i].getXGoal());
    		outState.putInt("dice["+i+"].goal.y", 0);
    		outState.putInt("dice["+i+"].goal.z", renderer.die[i].getZGoal());
    	}
    	outState.putInt("numDice", renderer.dieActive);
    	}catch(Exception e){
    		//We lost some dice positions, probably on an orientation change. No big deal
    	}
    	super.onSaveInstanceState(outState);
    }
    private void setupDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How many die do you want to use?");
        builder.setItems(new String[]{"1","2","3","4","5"}, new OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				renderer.setDie(arg1+1);
				SharedPreferences.Editor pedit = prefs.edit();
					pedit.putInt("numdie", arg1+1);
				pedit.commit();
			}        	
        });
        dNumDie = builder.create();
        
//BGColor picker
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a background color:");
        builder.setItems(new String[]{"Green","Blue","Red","Gray","Custom..."}, new OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				SharedPreferences.Editor pedit = prefs.edit();
				float bgr=1,bgg=1,bgb=1;
				switch(arg1){
				case 0: //Green
					bgr = 0.0f; bgg = 0.81f; bgb = 0.0f;
					break;
				case 1: //Blue
					bgr = 0.0f; bgg = 0.0f; bgb = 0.4f;
					break;
				case 2: //Red
					bgr = 0.81f; bgg = 0.0f; bgb = 0.0f;
					break;
				case 3: //Gray
					bgr = 0.52f; bgg = 0.52f; bgb = 0.52f;
					break;
				case 4: //Custom
					awdBGCol.show();
					bgr = 1.0f; bgg = 1.0f; bgb = 1.0f;
					break;
				}
					pedit.putFloat("bgcolr", bgr);
					pedit.putFloat("bgcolg", bgg);
					pedit.putFloat("bgcolb", bgb);
				pedit.commit();
				pBGCol.r = bgr;
				pBGCol.g = bgg;
				pBGCol.b = bgb;
				renderer.setBGCol(pBGCol);
			}        	
        });
        dBGCol = builder.create();
        
//Dice/Dot Color picker
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a dice/dot color:");
        builder.setItems(new String[]{"Red/Black","Red/White","Blue/Black","Blue/White","Custom Dice...","Custom Dot..."}, new OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
				SharedPreferences.Editor pedit = prefs.edit();
				float dicer=1,diceg=1,diceb=1;
				float dotr=1,dotg=1,dotb=1;
				switch(arg1){
				case 0: //Red/Black
					dicer = 0.81f; diceg = 0.0f; diceb = 0.0f;
					dotr = 0.0f; dotg = 0.0f; dotb = 0.0f;
					break;
				case 1: //Red/White
					dicer = 0.81f; diceg = 0.0f; diceb = 0.0f;
					dotr = 1.0f; dotg = 1.0f; dotb = 1.0f;
					break;
				case 2: //Blue/Black
					dicer = 0.0f; diceg = 0.0f; diceb = 0.81f;
					dotr = 0.0f; dotg = 0.0f; dotb = 0.0f;
					break;
				case 3: //Blue/White
					dicer = 0.0f; diceg = 0.0f; diceb = 0.81f;
					dotr = 1.0f; dotg = 1.0f; dotb = 1.0f;
					break;
				case 4: //Custom Dice
					awdDiceCol.show();
					dicer = 1.0f; diceg = 1.0f; diceb = 1.0f;
					dotr = pDotCol.r; dotg = pDotCol.g; dotb = pDotCol.b;
					break;
				case 5: //Custom Dot
					awdDotCol.show();
					dicer = pDiceCol.r; diceg = pDiceCol.g; diceb = pDiceCol.b;
					dotr = 0.0f; dotg = 0.0f; dotb = 0.0f;
					break;
				}
				pedit.putFloat("dicecolr", dicer);
				pedit.putFloat("dicecolg", diceg);
				pedit.putFloat("dicecolb", diceb);
				pedit.putFloat("dotcolr", dotr);
				pedit.putFloat("dotcolg", dotg);
				pedit.putFloat("dotcolb", dotb);
				pedit.commit();
				pDiceCol.r = dicer;
				pDiceCol.g = diceg;
				pDiceCol.b = diceb;
				pDotCol.r = dotr;
				pDotCol.g = dotg;
				pDotCol.b = dotb;
				renderer.setDiceCol(pDiceCol);
				renderer.setDotCol(pDotCol);
			}        	
        });
        dDiceCol = builder.create();
        
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Virtual Dice 3D");
        builder.setMessage("created by Alejandro Ramallo\n@AlexRamallo\nSnakeinalake Studios\nhttp://snakeinalake.com/");
        builder.setOnCancelListener(new OnCancelListener(){
        	@Override
        	public void onCancel(DialogInterface dialog) {
        		dialog.dismiss();
        	}
        });
        
        dAbout = builder.create();
        
        //COLOR PICKERS
        awdBGCol = new AmbilWarnaDialog(this, 1, new AmbilWarnaDialog.OnAmbilWarnaListener() {					
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				pBGCol.r = ((float)Color.red(color))/255.0f;
				pBGCol.g = ((float)Color.green(color))/255.0f;
				pBGCol.b = ((float)Color.blue(color))/255.0f;
				pBGCol.a = 1.0f;
				
				SharedPreferences.Editor pedit = prefs.edit();
				pedit.putFloat("bgcolr", pBGCol.r);
				pedit.putFloat("bgcolg", pBGCol.g);
				pedit.putFloat("bgcolb", pBGCol.b);
				pedit.commit();
				
				renderer.setBGCol(pBGCol);
			}
			@Override
			public void onCancel(AmbilWarnaDialog dialog) {
				// TODO Auto-generated method stub
			}
		});
        awdDiceCol = new AmbilWarnaDialog(this, 1, new AmbilWarnaDialog.OnAmbilWarnaListener() {					
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				pDiceCol.r = ((float)Color.red(color))/255.0f;
				pDiceCol.g = ((float)Color.green(color))/255.0f;
				pDiceCol.b = ((float)Color.blue(color))/255.0f;
				pDiceCol.a = 1.0f;
				
				SharedPreferences.Editor pedit = prefs.edit();
				pedit.putFloat("dicecolr", pDiceCol.r);
				pedit.putFloat("dicecolg", pDiceCol.g);
				pedit.putFloat("dicecolb", pDiceCol.b);
				pedit.commit();
				
				renderer.setDiceCol(pDiceCol);
			}
			@Override
			public void onCancel(AmbilWarnaDialog dialog) {
				// TODO Auto-generated method stub
			}
		});
        awdDotCol = new AmbilWarnaDialog(this, 1, new AmbilWarnaDialog.OnAmbilWarnaListener() {					
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				pDotCol.r = ((float)Color.red(color))/255.0f;
				pDotCol.g = ((float)Color.green(color))/255.0f;
				pDotCol.b = ((float)Color.blue(color))/255.0f;
				pDotCol.a = 1.0f;
				
				SharedPreferences.Editor pedit = prefs.edit();
				pedit.putFloat("dotcolr", pDotCol.r);
				pedit.putFloat("dotcolg", pDotCol.g);
				pedit.putFloat("dotcolb", pDotCol.b);
				pedit.commit();
				renderer.setDotCol(pDotCol);
			}
			@Override
			public void onCancel(AmbilWarnaDialog dialog) {
				// TODO Auto-generated method stub
			}
		});
        
    }

}


