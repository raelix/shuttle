/**
 * 
 */
package com.badlogicgames.superjumper;

import java.util.LinkedList;
import java.util.Random;

import com.badlogic.gdx.Gdx;

/**
 * @author phra
 *
 */
public class MultiWorld extends World {
	int position;
	private float precdelta, precaccelx, precaccely;
	protected static FullDuplexBuffer buffer = new FullDuplexBuffer();
	public static String enemy = "";
	public Bob bobMulti;
	public LinkedList<Projectile> projEnemy;
	private boolean flag = true;
	public Text positionText = new Text(SCOREPOSITIONX*0.2f,SCOREPOSITIONY*0.92f,"P.");
	
	/**
	 * @param seed
	 */
	public MultiWorld (int seed) {
		super();
		this.randgenerate = new Random(seed);
		this.bobMulti = new Bob(UI.HALFSCREENWIDTH,0);
		this.projEnemy = new LinkedList<Projectile>();
		positionText.update(0,"P." + position);
		this.texts.offer(positionText);
		scoretext.update(0, "TIME = " + score);
		this.texts.offer(ammotext);
		this.texts.offer(lifetext);
		
	}

	@Override
	public void update(float deltaTime, float accelX) {
		
		super.update(deltaTime,accelX);
		switch (this.state) {
		case CONSTANTS.GAME_RUNNING:
			Pacco pkt;
			boolean flag = true;
//			while ((pkt = buffer.takePaccoInNOBLOCK()) != null) {
			if ((pkt = buffer.takePaccoInNOBLOCK()) != null) {
				switch (pkt.getType()) {
				case PROTOCOL_CONSTANTS.PACKET_TYPE_BOB_MULTI:
					PaccoUpdateBobMulti pktbob;
					try {
						pktbob = new PaccoUpdateBobMulti(pkt);
					} catch (ProtocolException e) {
						System.out.println("PKT FUORI DAL PROTOCOLLO.");
						break;
					}
					this.precdelta = pktbob.getDeltaTime();
					this.precaccelx = pktbob.getAccelX();
					this.precaccely = pktbob.getAccelY();
					Gdx.app.debug("Position bob", " precdelta= "+ this.precdelta + "posx= "+ this.precaccelx +" posy= "+this.precaccely);
					Gdx.app.debug("Position Enemy", "deltatime= "+ deltaTime + "posx= "+ bob.position.x +" posy= "+bob.position.y);
					updateBobMulti(this.precdelta,this.precaccelx,this.precaccely);
					flag = false;
					break;
				case PROTOCOL_CONSTANTS.PACKET_END:
					this.state = CONSTANTS.GAME_LEVEL_END;
					break;
				case PROTOCOL_CONSTANTS.PACKET_PROJECTILE:
					PaccoProiettile paccoproj = new PaccoProiettile(pkt);
					paccoproj.deserialize();
					Gdx.app.debug("Update.Multiworld", "paccoproj.getX()= "+paccoproj.getX()+" paccoproj.getY()"+paccoproj.getY());
					projEnemy.offer(new Projectile(paccoproj.getX(), paccoproj.getY(), Projectile.WIDTH, Projectile.HEIGHT));
					break;
				default:
					System.out.println("PKT FUORI DAL PROTOCOLLO.");
					break;
				}
			}
			//if (flag) bobMulti.update(deltaTime);
			//Gdx.app.debug("pkt component2", "precdelta= "+ deltaTime + "accelx= "+ accelX +" accely= " + this.bob.velocity.y);
			buffer.putPaccoOutNOBLOCK(new PaccoUpdateBobMulti(deltaTime, bob.position.x, bob.position.y));

			if(bob.position.y > bobMulti.position.y)position=1;
			else position = 2;
			for (int i = 0; i < projEnemy.size() && i >= 0; i++) {
				Projectile projectile = projEnemy.get(i);
				projectile.update(deltaTime);
				for(int j=0;j<platforms.size() && j >= 0;j++) {
					Platform platform=platforms.get(j);
					if (OverlapTester.overlapRectangles(platform.bounds, projectile.bounds)) {
						Gdx.input.vibrate(new long[] { 1, 20}, -1); 
						score += 100;
						projEnemy.remove(i--);
						explosions.offer(new Explosion(platform.position.x-Platform.PLATFORM_WIDTH/2, platform.position.y-Platform.PLATFORM_HEIGHT/2,Platform.PLATFORM_WIDTH*2,Platform.PLATFORM_HEIGHT*2,0));
						platforms.remove(j--);
						break;
					}
					else if (OverlapTester.overlapRectangles(bob.bounds, projectile.bounds)){
						projEnemy.remove(i--);
						this.LifeLess();
						break;
					}
				}
				if (projectile.position.y > bobMulti.position.y+20){ 
					projEnemy.remove(i--);
					break;
				}
			}/*
			if(OverlapTester.overlapRectangles(bob.bounds, bobMulti.bounds)){
				bob.hitPlatform();
				Gdx.input.vibrate(new long[] { 1, 20}, -1); 
				bob.gravity.x=-10;
				bobMulti.gravity.x=10;
			}*/
			for(int i=0;i<projectiles.size() && i >= 0;i++){
				Projectile projectile=projectiles.get(i);
				if (OverlapTester.overlapRectangles(bobMulti.bounds, projectile.bounds)) {
					Gdx.input.vibrate(new long[] { 1, 20, 40, 20}, -1); 
					score += 100;
					if((projectile.type==1||projectile.type==2)){
						explosions.offer(new Explosion(bobMulti.position.x, bobMulti.position.y,Platform.PLATFORM_WIDTH,Platform.PLATFORM_HEIGHT,0));
						projectiles.remove(i--);
					}
					else if (projectile.type==0) projectiles.remove(projectile);
					break;
				}
			}
			break;
		case GAME_OVER:
			if (this.flag) {
				Gdx.app.debug("GameOver Case MultiWorld ","pacco end");
				buffer.putPaccoOutNOBLOCK(new PaccoEnd());
				this.state = CONSTANTS.GAME_OVER;
				this.flag = false;
			}
			break;
		}
	}

	private void updateBobMulti (float deltaTime, float accelX, float accelY) {
//		bobMulti.position.add(((-accelX / 10) * Bob.BOB_MOVE_VELOCITY)* deltaTime, accelY * deltaTime);
		bobMulti.position.x=accelX ;
		bobMulti.position.y=accelY ;
//		Gdx.app.debug("updatebobmulti","deltatime="+deltaTime+"accX="+accelX+"accY="+accelY);
//		Gdx.app.debug("updatebobomulti", "bobMulti.position.y = " + bobMulti.position.y + " bob.position.y = " + bob.position.y);

	}
	
	@Override
	public void updateTexts(float deltaTime) {
		ammotext.update(deltaTime, shot + "x");
		lifetext.update(deltaTime, life + "x");
		positionText.update(deltaTime,"P."+position);
		scoretext.update(deltaTime, "TIME = " + score);
		for (int i = 0; i < this.texts.size() && i >= 0; i++) {
			Text text = this.texts.get(i);
			text.update(deltaTime);
			if (text.duration != -1 && text.stateTime > text.duration)
				texts.remove(i--);
		}
	}
	
}
