package com.tender.saucer.wave;

import java.util.Calendar;
import java.util.LinkedList;

import android.util.Log;

import com.tender.saucer.collision.BodyData;
import com.tender.saucer.color.ColorScheme;
import com.tender.saucer.shapebody.enemy.Enemy;
import com.tender.saucer.shapebody.enemy.PenaltyEnemy;
import com.tender.saucer.shapebody.powerup.Powerup;
import com.tender.saucer.stuff.Constants;
import com.tender.saucer.stuff.GameState;
import com.tender.saucer.stuff.Model;
import com.tender.saucer.update.IPersistentUpdate;
import com.tender.saucer.update.ITransientUpdate;
import com.tender.saucer.update.UpdateHandler;

/**
 * 
 * Copyright 2014
 * @author Alex Schimpf
 *
 */

public final class WaveMachine
{
	public static int level = 0;	
	public static int numEnemiesLeft = 10;	
	public static int numPowerupsLeft = 0;
	public static LinkedList<Class<?>> enemyTypes = new LinkedList<Class<?>>();
	
	private static int numEnemyBuildsLeft = 10;
	private static long lastEnemyBuildTime = 0;
	private static float enemyBuildCooldown = Constants.DEFAULT_WAVE_ENEMY_BUILD_COOLDOWN;	
	private static long lastPowerupBuildTime = 0;
	private static float powerupBuildCooldown = Constants.DEFAULT_WAVE_POWERUP_BUILD_COOLDOWN;
	
	private WaveMachine()
	{
	}
	
	public static void init()
	{
		level = 0;
		numEnemiesLeft = 10;
		numPowerupsLeft = 0;
		enemyTypes = new LinkedList<Class<?>>();
		numEnemyBuildsLeft = 10;
		lastEnemyBuildTime = 0;
		enemyBuildCooldown = Constants.DEFAULT_WAVE_ENEMY_BUILD_COOLDOWN;	
		lastPowerupBuildTime = 0;
		powerupBuildCooldown = Constants.DEFAULT_WAVE_POWERUP_BUILD_COOLDOWN;
	}

	public static void beginNextWave()
	{	
		ColorScheme.applyNewScheme();
		
		level++;
		initCurrEnemyTypes();
		lastEnemyBuildTime = 0;
		numEnemiesLeft = level * 10;
		numEnemyBuildsLeft = numEnemiesLeft;
		enemyBuildCooldown = Math.max(700, Constants.DEFAULT_WAVE_ENEMY_BUILD_COOLDOWN - (level * 200));
		powerupBuildCooldown = Math.max(15000, Constants.DEFAULT_WAVE_POWERUP_BUILD_COOLDOWN - (level * 1000));
		lastPowerupBuildTime = Calendar.getInstance().getTimeInMillis();
		
		Model.state = GameState.WAVE_MACHINE_RUNNING;
	}
	
	public static void update()
	{
		if(numEnemiesLeft <= 0 && numPowerupsLeft <= 0)
		{
			Model.state = GameState.WAVE_RECESS_RUNNING;
			WaveRecess.begin();
		}
		else 
		{
			Enemy enemy = tryBuildRandomEnemy();
			if(enemy != null)
			{
				enemy.attachToScene();
				enemy.setInMotion();
			}
			
			Powerup powerup = tryBuildRandomPowerup();
			if(powerup != null)
			{
				powerup.attachToScene();
				powerup.setInMotion();
			}
		}
	}

	private static Enemy tryBuildRandomEnemy()
	{	
		long currTime = Calendar.getInstance().getTimeInMillis();
		long timeElapsed = currTime - lastEnemyBuildTime;
		if(timeElapsed > enemyBuildCooldown && numEnemyBuildsLeft > 0)
		{
			numEnemyBuildsLeft--;
			lastEnemyBuildTime = currTime;
			return buildRandomEnemy();
		}
		
		return null;
	}
	
	private static Enemy buildRandomEnemy()
	{
		try 
		{
			Enemy enemy;
			if(Math.random() <= Constants.PENALTY_PROBABILITY)
			{
				enemy = new PenaltyEnemy();
			}
			else
			{
				int choice = (int)(Math.random() * enemyTypes.size());
				enemy = (Enemy)enemyTypes.get(choice).newInstance();
			}
			
			enemy.body.setUserData(new BodyData(enemy));			
			Model.transients.add(enemy);
			
			return enemy;		
		} 
		catch (Exception e) 
		{
			return null;
		}
	}
	
	private static Powerup tryBuildRandomPowerup()
	{
		long currTime = Calendar.getInstance().getTimeInMillis();
		long timeElapsed = currTime - lastPowerupBuildTime;
		if(timeElapsed > powerupBuildCooldown)
		{
			numPowerupsLeft++;
			lastPowerupBuildTime = currTime;
			return buildRandomPowerup();
		}
		
		return null;
	}
	
	private static Powerup buildRandomPowerup()
	{
		try 
		{
			int choice = (int)(Math.random() * Constants.POWERUP_CLASSES.length);
			Powerup powerup = (Powerup)Constants.POWERUP_CLASSES[choice].newInstance();
			powerup.body.setUserData(new BodyData(powerup));			
			Model.transients.add(powerup);
			
			return powerup;		
		} 
		catch (Exception e) 
		{
			return null;
		}
	}
	
	private static void initCurrEnemyTypes()
	{
		enemyTypes.clear();

		int numEnemyTypes = Constants.ENEMY_CLASSES.length;
		for(int i = 0; i < numEnemyTypes; i++)
		{
			int choice = (int)(Math.random() * numEnemyTypes);
			enemyTypes.add(Constants.ENEMY_CLASSES[choice]);
		}
	}
}
