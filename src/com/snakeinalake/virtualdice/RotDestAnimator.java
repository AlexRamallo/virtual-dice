package com.snakeinalake.virtualdice;

import com.snakeinalake.engine.OrangeDevice;
import com.snakeinalake.engine.animators.ONodeAnimator;
import com.snakeinalake.engine.ortho.ONode2D;
import com.snakeinalake.engine.utilities.OUtilClock;

public class RotDestAnimator extends ONodeAnimator{

	private float curAngle, DstAngle, Speed, Tolerance;
	public RotDestAnimator(OrangeDevice Device, float speed, float tolerance) {
		super(Device);
		Speed = speed;
		Tolerance = tolerance;
	}
	
	@Override
	public void run(ONode2D node, OUtilClock clock, float delay) {
		if(Math.abs(curAngle-DstAngle)<=Tolerance){
			curAngle = DstAngle;
			return;
		}
		int dir = 1;
		if(curAngle<DstAngle)
			dir = -1;
		curAngle+=Speed*clock.getElapsed()*dir;
	}
	
	@Override
	public void run(ONode2D node) {
		if(Math.abs(curAngle-DstAngle)<=Tolerance){
			curAngle = DstAngle;
			return;
		}
		int dir = 1;
		if(curAngle<DstAngle)
			dir = -1;
		curAngle+=Speed*dir;
	}
	
	public void setDest(float set){
		DstAngle = set;
	}

}
