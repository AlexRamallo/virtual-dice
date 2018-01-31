package com.snakeinalake.virtualdice;

import java.util.Random;

import com.snakeinalake.engine.core.vector3df;
import com.snakeinalake.engine.nodes.OMeshSceneNode;
import com.snakeinalake.engine.utilities.OBasicCollision;

public class CDice { 
	public interface soundInterface{
		public void onCollision();
	}
	private static final float angles[] = {0.0f, 90.0f, 180.0f, 270.0f, 360.0f};
	CDice[] die;
	public boolean reacted[];
	int id;
	public OMeshSceneNode node;
	private vector3df pos, rot;
	private float gravity, hspeed, zspeed, vspeed, max, ground, roof, friction;
	private float boundX, boundY;
	private boolean resX,resY,resZ;
	private int xGoal, yGoal, zGoal;
	Random grand;
	public CDice(OMeshSceneNode Node, int Id, CDice[] Die){
		die = Die;
		grand = new Random(System.nanoTime());
		id = Id;
		node = Node;
		gravity = 1f;
		hspeed = 0;
		zspeed = 0;
		vspeed = 0;
		pos = node.getPosition();
		rot = node.getRotation();
		max = 1500f;
		ground = 0;
		roof = 20;
		resX = resY = resZ = false;
		
		friction = 0.1f;
		reacted = new boolean[die.length];
		boundX = 8;
		boundY = 8;
	}
	public void setXGoal(int set){
		xGoal = set;
		resX = true;
	}
	public int getXGoal(){
		return xGoal;
	}
	public void setZGoal(int set){
		zGoal = set;
		resZ = true;
	}
	public int getZGoal(){
		return zGoal;
	}
	private soundInterface snd;
	public void setSoundInterface(soundInterface SND){
		snd = SND;
	}
	float normalize(float angle){
		return angle%360;
	}
	private int round(int num) {
		int mult = 30;
	    return num+(mult-(num%mult));
	}
	/**
	 * Called on a collision to handle sfx
	 * */
	private void onCollision(){
		if(hspeed!=0 || vspeed!=0)
			if(snd!=null)
				snd.onCollision();
	}
	/**
	 * Smack it!
	 * */
	public void smack(float str){
		smack(str,true);
	}
	public void smack(float str, boolean audible){
		grand.setSeed(System.nanoTime());
		resX = resY = resZ = false;
		vspeed = str;		
		hspeed = (str)* (grand.nextDouble()<0.5f?-1:1);
		zspeed = (str)* (grand.nextDouble()<0.5f?-1:1);
		if(audible);
		onCollision();
		for(int i=0; i<die.length; i++)
			reacted[i]=false;
	}
	private void collisionStep(){
		float size = 2.1f;
		for(int i=0; i<die.length; i++){
			if(i==id || die[i].reacted[id] || !die[i].node.isVisible())continue;
			
			
			if(OBasicCollision.distance2D(pos.x, pos.z, die[i].pos.x, die[i].pos.z)<size){
				smack(1, false);
				die[i].smack(1, false);
				if(Math.abs((pos.y-ground) - (die[i].pos.y-ground))<0.25f){
					reacted[i] = true;
					if(Math.abs(hspeed)<=friction && Math.abs(zspeed)<=friction){
						float mdX = boundX-Math.abs(pos.x);
						float mdZ = boundY-Math.abs(pos.z);
						
						float tdX = boundX-Math.abs(die[i].pos.x);
						float tdZ = boundY-Math.abs(die[i].pos.z);
						
						if(mdX<tdX){
							//die[i].pos.x = pos.x-size;
						}else{
							pos.x = die[i].pos.x-size;
						}
						if(mdZ<tdZ){
							//die[i].pos.z = pos.z-size;
						}else{
							pos.z = die[i].pos.z-size;
						}
						
					}else{
						if(hspeed>zspeed){
							hspeed*=-0.5f;
						}else if(zspeed>hspeed){
							zspeed*=-0.5f;
						}
					}
				}
			}
		}
	}
	public void step(){
		if(!node.isVisible()) return;
		collisionStep();
		//Friction
		if(pos.y==ground){
			if(Math.abs(hspeed)!=0)
				hspeed=(Math.abs(hspeed)-friction)*(hspeed<0?-1:1);
			if(Math.abs(zspeed)!=0)
				zspeed=(Math.abs(zspeed)-friction)*(zspeed<0?-1:1);
		}
		
		//Physics		
		vspeed-=gravity;	
		pos.y+=vspeed;
		pos.x+=hspeed;
		pos.z+=zspeed;
		if(resX){
			float goal = angles[xGoal];
			normalize(rot.x);
			float diff = rot.x-goal;
			if(Math.abs(diff)-30>-30)
				rot.x-=((diff>0)?1:-1)*30f;
			else
				rot.x = goal;
		}else{
			rot.x += hspeed*3;			
			rot.x=normalize(rot.x);			
		}
		
		if(resZ){
			float goal = angles[zGoal];
			normalize(rot.y);
			float diff = rot.y-goal;
			if(Math.abs(diff)-30>-30)
				rot.y-=((diff>0)?1:-1)*30f;
			else
				rot.y = goal;
		}else{
			rot.y += zspeed*3;			
			rot.y=normalize(rot.y);			
		}
		
		//Clips
		/*if(OBasicCollision.distance2D(0, 0, pos.x, pos.z)>radius){
			hspeed*=-0.7f;
			zspeed*=-0.7f;
			if(Math.abs(hspeed)<0.2f)
				hspeed=0;
			if(Math.abs(zspeed)<0.2f)
				zspeed=0;
			float dir = OBasicCollision.vectorDirection(0,0,pos.x, pos.z);
			pos.x = (float) (Math.cos(dir)*(radius-0.1f));
			pos.z = -(float) (Math.sin(dir)*(radius-0.1f));
		}*/
		if(Math.abs(pos.x)>boundX){
			pos.x = boundX*(pos.x<0?-1:1);
			if(Math.abs(hspeed)>1f)
				onCollision();
			hspeed*=-0.5f;
		}
		if(Math.abs(pos.z)>boundY){
			pos.z = boundY*(pos.z<0?-1:1);
			zspeed*=-0.5f;
		}
		if(pos.y<ground){
			pos.y = ground;
			if(Math.abs(vspeed)>1f)
				onCollision();
			vspeed*=-0.7f;
		}
		if(pos.y>roof)
			pos.y = roof;
		
		if(Math.abs(vspeed)<=gravity && vspeed!=0){
			vspeed=0;
			resY = true;
			yGoal = (int) Math.round(grand.nextDouble()%(angles.length-1));
		}
		if(Math.abs(hspeed)<=friction && hspeed!=0){
			hspeed=0;
		}
		if(Math.abs(hspeed)<=friction*10 && !resX){
			resX = true;
			xGoal = (int) Math.round(grand.nextDouble()*(angles.length-1));
			rot.x = round((int)rot.x);
		}
		if(Math.abs(zspeed)<=friction && zspeed!=0){
			zspeed=0;
		}
		if(Math.abs(zspeed)<=friction*10 && !resZ){
			resZ = true;
			zGoal = (int) Math.round(grand.nextDouble()*(angles.length-1));
			rot.y = round((int)rot.y);
		}
		
		if(vspeed<-max)
			vspeed = -max;
		if(vspeed>max)
			vspeed = max;

	}
}
