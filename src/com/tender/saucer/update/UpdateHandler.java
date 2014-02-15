package com.tender.saucer.update;

import java.util.LinkedList;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.primitive.Rectangle;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;

import com.tender.saucer.stuff.Constants;
import com.tender.saucer.stuff.GameState;
import com.tender.saucer.stuff.Model;
import com.tender.saucer.wave.WaveMachine;
import com.tender.saucer.wave.WaveIntermission;

/**
 * 
 * Copyright 2014
 * @author Alex Schimpf
 *
 */

public final class UpdateHandler implements IUpdateHandler
{
	public void onUpdate(float secondsElapsed) 
	{		
		switch(Model.state)
		{
			case WAVE_RUNNING:	
				Model.waveMachine.update();	
				updateHUDText();
				break;
			case WAVE_INTERMISSION:
				Model.waveIntermission.update();
				break;
			case GAME_PAUSED:
				return;
			case GAME_OVER:
				Model.main.runOnUiThread(new Runnable() 
				{
				    public void run() 
				    {
				    	Model.main.showGameOverDialog();
				    }
				});
				break;	
		}

		Model.player.update();
		Model.background.update();
		updateTransients();
	}

	public void reset() 
	{
	}
	
	private void updateTransients()
	{
		@SuppressWarnings("unchecked")
		LinkedList<ITransientUpdate> transientsClone = (LinkedList<ITransientUpdate>)Model.transients.clone();
		for(ITransientUpdate transientClone : transientsClone)
		{	
			if(transientClone.update())
			{
				transientClone.done();
				Model.transients.remove(transientClone);
			}
		}
	}
	
	private void updateHUDText()
	{
		Model.player.score++;
		Model.scoreText.setText("" + Model.player.score);
		
		float width = (Model.player.health / Constants.DEFAULT_PLAYER_HEALTH) * (Constants.CAMERA_WIDTH - 10);		
		Model.lifeBar.setWidth(width);
		
		Model.waveText.setText("Wave " + Model.waveMachine.level);
		Model.waveText.setX((Constants.CAMERA_WIDTH - Model.waveText.getWidth()) / 2);
	}
}