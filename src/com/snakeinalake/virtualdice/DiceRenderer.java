package com.snakeinalake.virtualdice;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.snakeinalake.engine.COglRenderer;
import com.snakeinalake.engine.OrangeDevice;
import com.snakeinalake.engine.animators.ORotationAnimator;
import com.snakeinalake.engine.core.OColor4f;
import com.snakeinalake.engine.core.OTileset;
import com.snakeinalake.engine.core.vector3df;
import com.snakeinalake.engine.input.updated.OInputManager;
import com.snakeinalake.engine.input.updated.OVirtualButton;
import com.snakeinalake.engine.nodes.OCameraSceneNode;
import com.snakeinalake.engine.nodes.OMeshSceneNode;
import com.snakeinalake.engine.ortho.ONode2D;
import com.snakeinalake.engine.ortho.ONode2DAnimatedTile;
import com.snakeinalake.engine.ortho.ONode2DRect;
import com.snakeinalake.engine.ortho.ONode2DSprite;
import com.snakeinalake.engine.ortho.tiles.OAnimationCallback;
import com.snakeinalake.engine.ortho.tiles.OAnimationParser;
import com.snakeinalake.engine.utilities.OUtilClock;
import com.snakeinalake.engine.utilities.OUtilClock.IFC_UNIT;

public class DiceRenderer extends COglRenderer{
	Context context;
	OInputManager input;
	OVirtualButton smack,dset,sndbtn,colbtn,dcolbtn,abtbtn,closebtn;
	ONode2DRect masterui;
	Handler handle;
	int numDie, unit=0;
	//boolean orbit;
	private boolean LANDSCAPE;
	public CDice[] die;
	public OMeshSceneNode[] dots;
	OMeshSceneNode shadows[];
	OUtilClock clock;
	public int dieActive;
	OColor4f pBGCol,pDiceCol,pDotCol;
	public DiceRenderer(OrangeDevice Device, Handler d) {
		super(Device);
		handle = d;
		context = device.getContext();
		input = device.getInput();
		numDie = 5;
		dieActive = 5;
		pBGCol = new OColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		pDiceCol = new OColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		pDotCol = new OColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		clock = device.getClock();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config){
		device.setGl10(gl);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST); 
		gl.glEnable(GL10.GL_TEXTURE_2D);                              
		gl.glEnable(GL10.GL_BLEND);       
		gl.glDepthFunc(GL10.GL_LEQUAL);                               
		gl.glEnable(GL10.GL_CULL_FACE);                               
		gl.glCullFace(GL10.GL_BACK);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,GL10.GL_NICEST);
	} 
	OCameraSceneNode cam;
	vector3df campos;
	float camdir;
	RotDestAnimator rotanim;
	vector3df[] savedPos,savedRot;
	/**
	 * Called to load dice that were stored in an instance bundle
	 * */
	int ctr = 0;
	public void loadDie(vector3df pos, vector3df rot) {
		savedPos[ctr] = pos;
		savedRot[ctr] = rot;
		ctr++;
	}
	ArrayList<ONode2DSprite> btnIcons, btnBorders, btnBacks;
	OMeshSceneNode table,logo;
	int[] bgTex;
	ONode2DAnimatedTile supernova;
	OMeshSceneNode hurricane,wind,cows;
	public void setupScene(){		
		btnIcons = new ArrayList<ONode2DSprite>();
		btnBorders = new ArrayList<ONode2DSprite>();
		btnBacks = new ArrayList<ONode2DSprite>();
		
		device.getSceneManager().empty();
		device.getOrthoEnvironment().empty();
		
		cam = smgr.createCameraSceneNode(0, LANDSCAPE?30:45, 1, 0, 0, 0);
		campos = cam.getPosition();
		camdir = 0;
		
		hurricane = smgr.createMeshSceneNode("hurricane.obj");
		hurricane.getMesh().loadTexture(context, "hurricane.png");
		hurricane.addAnimator(new ORotationAnimator(device, 0, 5, 0));
		
		cows = smgr.createMeshSceneNode("cows.obj");
		cows.getMesh().loadTexture(context, "crappycow.png");
		cows.addAnimator(new ORotationAnimator(device, 0, -8, 0));
		cows.setParent(hurricane);
		
		wind = smgr.createMeshSceneNode("wind.obj");
		wind.getMesh().loadTexture(context, "wind.png");
		wind.addAnimator(new ORotationAnimator(device, 0, -12, 0));
		wind.setScale(1, 1.5f, 1);
		wind.getMesh().setAlpha(0.7f);
		wind.setParent(hurricane);
		
		wind.setVisible(false);
		cows.setVisible(false);
		hurricane.setVisible(false);
		

		table = smgr.createMeshSceneNode("plane.obj");
		table.getMesh().loadTexture(context, "felttex-sialwhite.png");
		logo = smgr.createMeshSceneNode("plane.obj");
		logo.getMesh().loadTexture(context, "felttex-logo.png");
		setBGCol(pBGCol);
		 
		die = new CDice[numDie];
		dots = new OMeshSceneNode[numDie];
		shadows = new OMeshSceneNode[numDie];
		CDice.soundInterface snd = new CDice.soundInterface() {
			@Override
			public void onCollision() {
				if(supernova_stage!=2){
					Message msg = new Message();
					msg.arg1 = 1;
					handle.sendMessage(msg);
				}
			}
		};
		for(int i=0; i<die.length; i++){
			OMeshSceneNode diceNode = smgr.createMeshSceneNode("dice.obj");
			OMeshSceneNode diceDotNode = smgr.createMeshSceneNode("dice.obj");
			diceNode.getMesh().loadTexture(context, "dice2.png");
			diceNode.getMesh().setColor(pDiceCol);
			
			diceDotNode.getMesh().loadTexture(context, "dice1.png");
			diceDotNode.getMesh().setColor(pDotCol); 
			dots[i] = diceDotNode;
			diceDotNode.setParent(diceNode);
			
			die[i] = new CDice(diceNode, i, die);
			die[i].setSoundInterface(snd);
			float ss = 2.1f;
			die[i].node.getPosition().x = (-(die.length*ss)/2)+(ss*i);
			if(i<dieActive){
				if(savedPos!=null)
					if(savedPos[i]!=null)
						diceNode.setPosition(savedPos[i]);
				if(savedRot!=null)
					if(savedRot[i]!=null){
						die[i].setXGoal((int)savedRot[i].x);
						die[i].setZGoal((int)savedRot[i].z);
					}
			} 
			OMeshSceneNode shad = smgr.createMeshSceneNode("plane.obj");
			shad.getMesh().loadTexture(context, "shadow.png");
			shad.setRotation(0, 0, 0);
			shad.setScale(0.2f,0.2f,0.2f);
			shad.setPosition(0, 0.15f, 0);
			shad.getMesh().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			shadows[i]=shad;
		}
		setDie(dieActive); 
		//die[die.length-1].node.setVisible(false);
		logo.setPosition(0f, 0.05f, 0f);
		logo.setScale(2f, 2f, 2f);
		table.setScale(6f, 6f, 6f);
				
		rotanim = new RotDestAnimator(device, 1.0f, 2.0f);
		unit = (int)Math.round(155f*(((LANDSCAPE?env.getHeight():env.getWidth())/155f)/6f));		
		if(unit>155)unit=155;
		masterui = env.addNode2DRect(1, 1);
		{
			ONode2DSprite sp1 = env.addNode2DSprite("btnBack2.png", unit, unit);
			ONode2DSprite sp2 = env.addNode2DSprite("btnBack1.png", unit, unit);
			ONode2DSprite border = env.addNode2DSprite("btnBorder.png", unit, unit);
			ONode2DSprite ico = env.addNode2DSprite("dset.png", unit, unit);
			btnIcons.add(ico);
			btnBorders.add(border);
			sp1.addAnimator(rotanim);
			sp2.addAnimator(rotanim);
			btnBacks.add(sp2);
			btnBacks.add(sp1);
			
			dset = (OVirtualButton)input.getPeripheralById(input.addButton(0, 0, sp1, sp2));
			ico.setParent(border);
			border.setParent(dset.getRoot());
		}
		{
			ONode2DSprite sp1 = env.addNode2DSprite("btnBack2.png", unit, unit);
			ONode2DSprite sp2 = env.addNode2DSprite("btnBack1.png", unit, unit);
			ONode2DSprite border = env.addNode2DSprite("btnBorder.png", unit, unit);
			ONode2DSprite ico = env.addNode2DSprite("sndbtn1.png", unit, unit);
			btnIcons.add(ico);
			btnBorders.add(border);
			sp1.addAnimator(rotanim);
			sp2.addAnimator(rotanim);
			btnBacks.add(sp2);
			btnBacks.add(sp1);
			
			sndbtn = (OVirtualButton)input.getPeripheralById(input.addButton(LANDSCAPE?0:unit, LANDSCAPE?unit:0, sp1, sp2));
			ico.setParent(border);
			border.setParent(sndbtn.getRoot());
		} 
		{
			ONode2DSprite sp1 = env.addNode2DSprite("btnBack2.png", unit, unit);
			ONode2DSprite sp2 = env.addNode2DSprite("btnBack1.png", unit, unit);
			ONode2DSprite border = env.addNode2DSprite("btnBorder.png", unit, unit);
			ONode2DSprite ico = env.addNode2DSprite("bgcol.png", unit, unit);
			btnIcons.add(ico);
			btnBorders.add(border);
			sp1.addAnimator(rotanim);
			sp2.addAnimator(rotanim);
			btnBacks.add(sp2);
			btnBacks.add(sp1);
			
			colbtn = (OVirtualButton)input.getPeripheralById(input.addButton(LANDSCAPE?0:unit*2, LANDSCAPE?unit*2:0, sp1, sp2));
			ico.setParent(border);
			border.setParent(colbtn.getRoot());
		}
		{
			ONode2DSprite sp1 = env.addNode2DSprite("btnBack2.png", unit, unit);
			ONode2DSprite sp2 = env.addNode2DSprite("btnBack1.png", unit, unit);
			ONode2DSprite border = env.addNode2DSprite("btnBorder.png", unit, unit);
			ONode2DSprite ico = env.addNode2DSprite("dicecol.png", unit, unit);
			btnIcons.add(ico);
			btnBorders.add(border);
			sp1.addAnimator(rotanim);
			sp2.addAnimator(rotanim);
			btnBacks.add(sp2);
			btnBacks.add(sp1);
			
			dcolbtn = (OVirtualButton)input.getPeripheralById(input.addButton(LANDSCAPE?0:unit*3, LANDSCAPE?unit*3:0, sp1, sp2));
			ico.setParent(border);
			border.setParent(dcolbtn.getRoot());
		}
		{
			ONode2DSprite sp1 = env.addNode2DSprite("btnBack2.png", unit, unit);
			ONode2DSprite sp2 = env.addNode2DSprite("btnBack1.png", unit, unit);
			ONode2DSprite border = env.addNode2DSprite("btnBorder.png", unit, unit);
			ONode2DSprite ico = env.addNode2DSprite("abtbtn.png", unit, unit);
			btnIcons.add(ico);
			btnBorders.add(border);  
			sp1.addAnimator(rotanim);
			sp2.addAnimator(rotanim);
			btnBacks.add(sp2);
			btnBacks.add(sp1); 
			
			abtbtn = (OVirtualButton)input.getPeripheralById(input.addButton(LANDSCAPE?0:unit*4, LANDSCAPE?unit*4:0, sp1, sp2));
			ico.setParent(border);
			border.setParent(abtbtn.getRoot());
		}
		{
			ONode2DSprite sp1 = env.addNode2DSprite("btnBack2.png", unit, unit);
			ONode2DSprite sp2 = env.addNode2DSprite("btnBack1.png", unit, unit);
			ONode2DSprite border = env.addNode2DSprite("btnBorder.png", unit, unit);
			ONode2DSprite ico = env.addNode2DSprite("closebtn.png", unit, unit);
			btnIcons.add(ico);
			btnBorders.add(border);
			sp1.addAnimator(rotanim);
			sp2.addAnimator(rotanim);
			btnBacks.add(sp2);
			btnBacks.add(sp1);
			
			closebtn = (OVirtualButton)input.getPeripheralById(input.addButton(LANDSCAPE?0:unit*5, LANDSCAPE?unit*5:0, sp1, sp2));
			ico.setParent(border);
			border.setParent(closebtn.getRoot());
		}
		{
			ONode2D sp1 = env.addNode2DRect(LANDSCAPE?env.getWidth()-unit:env.getWidth(), LANDSCAPE?env.getHeight():env.getHeight()-unit);
			sp1.setBlendFunction(GL10.GL_SRC_COLOR, GL10.GL_ONE);
			sp1.setColor(0, 0, 0, 0);
			smack = (OVirtualButton)input.getPeripheralById(input.addButton(LANDSCAPE?unit:0, LANDSCAPE?0:unit, sp1, sp1));
		}
		resetUICols();
		if(!LANDSCAPE)
			masterui.getPosition().x = -unit*5;
		else
			masterui.getPosition().y = -unit*5;
		 
		//SUPERNOVA 
		OTileset ts = new OTileset("supernova.xml"); 
		ts.load(device);
		supernova = env.addNode2DAnimatedTile(ts,
							OAnimationParser.getInstance().loadAnimation(context, "supernova.xml"),
							env.getHeight(),
							env.getHeight());
		supernova.setPosition(env.getWidth()/2, env.getHeight()/2);
		supernova.setInstructions(new OAnimationCallback(){
			@Override
			public void onAnimationEnd() {
				supernova_stage = 2;
				Message msg = new Message();
				msg.arg1 = 11;
				handle.sendMessage(msg);
				flash(1, 1, 1, 0.005f);
			}
			@Override
			public void onAnimationStart() {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onFrameStep(int curframe) {
				// TODO Auto-generated method stub
				
			}			
		});
		supernova.setCenter();
		
		explosionBlind = env.addNode2DRect(env.getWidth(), env.getHeight());
		explosionBlind.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_DST_ALPHA);
		explosionBlind.setVisible(false);
		hurricane.setVisible(false);
	}
	int str = 2;
	boolean analysis_reset = false;
	int TapCount = 0, closing = 0;
	public void resetUICols(){
		if(btnIcons!=null)
		for(int i=0; i<btnIcons.size(); i++){
			ONode2DSprite ico = btnIcons.get(i);
			//ico.setCenter();
			ico.setScale(0.7f, 0.7f);
			ico.setPosition((unit*0.15f), (unit*0.15f));
			float hill = 0.1f;
			ico.setColor(
					pDotCol.r - (0.1f*((pDotCol.r)>=hill?1:-1)),
					pDotCol.g - (0.1f*((pDotCol.g)>=hill?1:-1)),
					pDotCol.b - (0.1f*((pDotCol.b)>=hill?1:-1)),
					1.0f); 
		}
		if(btnBorders!=null)
		for(int i=0; i<btnBorders.size(); i++){ 
			ONode2DSprite border = btnBorders.get(i);
			float hill = 0.25f;
			border.setColor(
					pBGCol.r - (0.25f*(pBGCol.r>=hill?1:-1)),
					pBGCol.g - (0.25f*(pBGCol.g>=hill?1:-1)),
					pBGCol.b - (0.25f*(pBGCol.b>=hill?1:-1)),
					1.0f);
		}
		if(btnBacks!=null)
		for(int i=0; i<btnBacks.size(); i++){
			ONode2DSprite back = btnBacks.get(i);
			back.setColor(pDiceCol);
		}
	}
	
	ONode2DRect explosionBlind;
	float ecta = 0;
	public void EXPLODEMOTHERFUCKER(){
		explosionBlind.setVisible(true);
		supernova.setVisible(false);
		hurricane.setVisible(true);
		cows.setVisible(true);
		wind.setVisible(true);
		if(!clock.isSetTimer("exploding")){
			clock.setTimer("exploding", 13, IFC_UNIT.IFC_SECONDS);
		}
		if(!clock.isSetTimer("explosionSmackTick")){
			clock.setTimer("explosionSmackTick", 100, IFC_UNIT.IFC_MILI);
		}
		
		if(clock.getTimer("explosionSmackTick")<=0){
			for(int i=0; i<die.length; i++){
				die[i].smack(50);			
			}
			clock.setTimer("explosionSmackTick", 500, IFC_UNIT.IFC_MILI);
		}
		
		ecta+= 0.04f * clock.getElapsed();
		campos.x = (float)Math.cos(ecta*Math.PI/180)*60;
		campos.z = -(float)Math.sin(ecta*Math.PI/180)*60;
		campos.y = 7 + (float)Math.cos(ecta)*4;
		cam.setTarget(0, 15, 0);
		
		if(clock.getTimer("exploding")<=0){
			supernova_stage = 0;
			supernova.setScale(1,1);
			supernova.setAlpha(1.0f);
			supernova.setVisible(false);
			explosionBlind.setAlpha(1.0f);
			explosionBlind.setVisible(false);
			clock.removeTimer("exploding");
			campos.x = 0;
			campos.y = LANDSCAPE?30:45;
			campos.z = 1;
			hurricane.setVisible(false);
			cows.setVisible(false);
			wind.setVisible(false);
			cam.setTarget(0,0,0);
		}
	}
	float flashSpeed = 0.005f;
	public void flash(float r, float g, float b, float fspd){
		flashSpeed = fspd;
		explosionBlind.setColor(r, g, b, 1.0f);
	}
	int supernova_tapcount = 0;
	int supernova_stage = 0;
	float scaleupspd = 0;
	public void onDrawFrame(GL10 gl){
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(pBGCol.r, pBGCol.g, pBGCol.b, 1.0f);
		//gl.glClearColor(0, 0, 0, 1.0f);
		clock.count();
		if(analysis_reset){
			int Out[];
			OutcomeAnalyzer.getInstance().registerOutcome(dieActive, null, TapCount);
			TapCount = 0;
		}		
		if(smack.isReleased()){
			if(supernova_stage == 2){
				Message msg = new Message();
				msg.arg1 = 111;
				handle.sendMessage(msg);
			}else{
				analysis_reset = false;
				for(int i=0; i<die.length; i++){
					die[i].smack(str);
					str++;
					if(str>5)
						str = 5;				
					clock.setTimer("buildup", 250, IFC_UNIT.IFC_MILI);
				}
				if(supernova_stage == 0){
					if(!clock.isSetTimer("supernova_startup")){
						clock.setTimer("supernova_startup", 3f, IFC_UNIT.IFC_SECONDS);
					}else if(clock.getTimer("supernova_startup")<=0){
						clock.setTimer("supernova_life", 250, IFC_UNIT.IFC_MILI);
						supernova_stage = 1;
						supernova.setScale(0, 0);
						supernova.setVisible(true);
						supernova.playAnimSet("Fuck shit up!");
						supernova.setPause(false);
						supernova.setCurFrame(0);
					}
				}else if(supernova_stage == 1){
					clock.setTimer("supernova_life", 250, IFC_UNIT.IFC_MILI);
				}
			}
		}
		vector3df sns = supernova.getScale();
		vector3df snr = supernova.getRotation();
		switch(supernova_stage){
			default:
				supernova.setVisible(false);
				break;
			case 1: //buildup
				scaleupspd += 0.01f * clock.getElapsed();
				sns.x += 0.01f;
				sns.y = sns.x;
				if(sns.x>=1.5f)
					sns.x = 1.5f;
				supernova.setAlpha(sns.x/1.5f);
				snr.z += scaleupspd * 50f;		
				if(clock.getTimer("supernova_life")<=0)
					supernova_stage = -1;				
				break; 
			case 2:	//Explosion
				EXPLODEMOTHERFUCKER();
				clock.removeTimer("supernova_startup");
				break;
			case -1:
				supernova.setVisible(false);
				supernova_stage = 0;
				supernova.setPause(true);
				supernova.setCurFrame(0);
				sns.x = 1;
				sns.y = 1;
				sns.z = 1;
				clock.removeTimer("supernova_startup");
				break;
		}
		if(sndbtn.isReleased()){
			Message msg = new Message();
			msg.arg1 = 2;	//Toggle Audio;
			handle.sendMessage(msg);
		}
		if(colbtn.isReleased()){
			Message msg = new Message();
			msg.arg1 = 3;	//Set Wallpaper;
			handle.sendMessage(msg);
		}
		if(dcolbtn.isReleased()){
			Message msg = new Message();
			msg.arg1 = 33;	//Set Dice Color;
			handle.sendMessage(msg);
		}
		if(clock.isSetTimer("buildup")){
			if(clock.getTimer("buildup")<=0){
				str = 2;
			}
		}
		if(dset.isReleased()){
			Message msg = new Message();
			msg.arg1 = 0;	//Set Dice amount
			handle.sendMessage(msg);
		}
		if(abtbtn.isReleased()){
			Message msg = new Message();
			msg.arg1 = -1;	//Show About info
			handle.sendMessage(msg);
		}
		vector3df muip = masterui.getPosition();
		if(closebtn.isReleased()){
			if((LANDSCAPE?muip.y:muip.x)>=0){
				closing = -1;
			}else{
				closing = 1;
			}
		}
		if(closing == -1){
			float dst = -unit*5;
			float c = LANDSCAPE?muip.y:muip.x;
			if(c>dst){
				c -= 5f*clock.getElapsed();
			}else{
				c = dst;
				closing = 0;
			}
			if(LANDSCAPE)
				muip.y = c;
			else
				muip.x = c;
		}else if(closing == 1){
			float c = LANDSCAPE?muip.y:muip.x;
			if(c<0){
				c += 5f*clock.getElapsed();
			}else{
				c = 0;
				closing = 0;
			}
			if(LANDSCAPE)
				muip.y = c;
			else
				muip.x = c;
		}
			float msx = muip.x;
			float msy = muip.y;
			dset.setPosition(msx+(LANDSCAPE?0:unit*0), msy+(LANDSCAPE?unit*0:0));
			sndbtn.setPosition(msx+(LANDSCAPE?0:unit*1), msy+(LANDSCAPE?unit*1:0));
			colbtn.setPosition(msx+(LANDSCAPE?0:unit*2), msy+(LANDSCAPE?unit*2:0));
			dcolbtn.setPosition(msx+(LANDSCAPE?0:unit*3), msy+(LANDSCAPE?unit*3:0));
			abtbtn.setPosition(msx+(LANDSCAPE?0:unit*4), msy+(LANDSCAPE?unit*4:0));
			closebtn.setPosition(msx+(LANDSCAPE?0:unit*5), msy+(LANDSCAPE?unit*5:0));
		for(int i=0; i<die.length; i++){
			die[i].step();
			shadows[i].getPosition().x = die[i].node.getPosition().x;
			shadows[i].getPosition().z = die[i].node.getPosition().z;	
		}
		
		OColor4f eBfcol = explosionBlind.getColor();
		if(eBfcol.a>0)
			eBfcol.a-=flashSpeed;
		if(eBfcol.a<0)
			eBfcol.a = 0;
		
		smgr.renderAll(gl);
		env.renderAll(gl);
	}
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		smgr.notifySurfaceChange(gl, width, height);
		env.notifySurfaceChange(gl, width, height);
		setupScene();
	}

	public void shake() {
		return;
		/*for(int i=0; i<die.length; i++){
			die[i].smack(1);
		}*/
	}
	/**
	 * Set the number of die
	 * */
	public void setDie(int num) {
		savedPos = new vector3df[num];
		savedRot = new vector3df[num];
		dieActive = num;
		if(die==null)return;
		
		for(int i=0; i<die.length; i++){
			die[i].node.setVisible(i<num);
			dots[i].setVisible(i<num);
			shadows[i].setVisible(i<num);
		}
	}
	public void setBGCol(OColor4f set) {
		pBGCol = set;
		if(table!=null){
			table.getMesh().setColor(set);
			float hill = 0.25f;
			logo.getMesh().setColor(
					set.r - (0.25f*(set.r>=hill?1:-1)),
					set.g - (0.25f*(set.g>=hill?1:-1)),
					set.b - (0.25f*(set.b>=hill?1:-1)),
					1.0f);
		}
		resetUICols();
	}
	public void setDiceCol(OColor4f set) {
		pDiceCol = set;
		if(die!=null)
		for(int i=0; i<die.length; i++){
			if(die[i]!=null){
				die[i].node.getMesh().setColor(set);
			}
		}
		resetUICols();
	}
	public void setDotCol(OColor4f set) {
		pDotCol = set;
		if(dots!=null)
		for(int i=0; i<dots.length; i++){
			if(dots[i]!=null){
				dots[i].getMesh().setColor(set);
			}
		}
		resetUICols();
	}

	/**
	 * Called when the screen orientation change to set the direction of the UI, so that the app doesn't have to be killed
	 * */
	public void orienationChanged(int orientation) {
		if(orientation == Configuration.ORIENTATION_LANDSCAPE)
			LANDSCAPE = true;
		else
			LANDSCAPE = false;
	}
}







