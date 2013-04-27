package com.badlogicgames.superjumper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogicgames.superjumper.World.WorldListener;

public class GameScreen implements Screen {
	static final int GAME_READY = 0;
	static final int GAME_RUNNING = 1;
	static final int GAME_PAUSED = 2;
	static final int GAME_LEVEL_END = 3;
	static final int GAME_OVER = 4;

	Game game;
	int state;
	OrthographicCamera guiCam;
	Vector3 touchPoint;
	SpriteBatch batcher;
	World world;
	WorldListener worldListener;
	WorldRenderer renderer;
	Rectangle pauseBounds;
	Rectangle resumeBounds;
	Rectangle quitBounds;
	Rectangle nosBounds;
	int lastScore;
	String scoreString;

	public GameScreen (Game game) {
		this.game = game;
		state = GAME_READY;
		guiCam = new OrthographicCamera(320, 480);
		guiCam.position.set(320 / 2, 480 / 2, 0);
		touchPoint = new Vector3();
		batcher = new SpriteBatch();
		worldListener = new WorldListener() {
			@Override
			public void jump () {
				Assets.playSound(Assets.jumpSound);
			}

			@Override
			public void highJump () {
				Assets.playSound(Assets.highJumpSound);
			}

			@Override
			public void hit () {
				Assets.playSound(Assets.hitSound);
			}

			@Override
			public void coin () {
				Assets.playSound(Assets.coinSound);
			}

			@Override
			public void life () {
				// TODO Auto-generated method stub

			}

			@Override
			public void projectile () {
				// TODO Auto-generated method stub

			}


		};
		world = new World(worldListener);
		renderer = new WorldRenderer(batcher, world);
		pauseBounds = new Rectangle(320 - 64, 480 - 64, 64, 64);
		resumeBounds = new Rectangle(160 - 96, 240, 192, 36);
		quitBounds = new Rectangle(160 - 96, 240 - 36, 192, 36);
		nosBounds = new Rectangle(320 - 64, 480 - 430, 64, 64);
		lastScore = 0;
		scoreString = "SCORE: 0";
	}

	public void update (float deltaTime) {
		if (deltaTime > 0.1f) deltaTime = 0.1f;

		switch (state) {
		case GAME_READY:
			updateReady();
			break;
		case GAME_RUNNING:
			updateRunning(deltaTime);
			break;
		case GAME_PAUSED:
			updatePaused();
			break;
		case GAME_LEVEL_END:
			updateLevelEnd();
			break;
		case GAME_OVER:
			updateGameOver();
			break;
		}
	}

	private void updateReady () {
		if (Gdx.input.justTouched()) {
			state = GAME_RUNNING;
			//World.setGravity(0, 1);
		}
	}

	private void updateRunning (float deltaTime) {
		if (Gdx.input.justTouched()) 
		{

			guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));
			if ((!(OverlapTester.pointInRectangle(pauseBounds, touchPoint.x, touchPoint.y)))&&
				(!(OverlapTester.pointInRectangle(nosBounds, touchPoint.x, touchPoint.y))))
				world.ShotProjectile();
			else if (OverlapTester.pointInRectangle(pauseBounds, touchPoint.x, touchPoint.y)) 
			{
				Assets.playSound(Assets.clickSound);
				state = GAME_PAUSED;
				return;
			}

			else 	if (OverlapTester.pointInRectangle(nosBounds, touchPoint.x, touchPoint.y)) 
			{
				//Gdx.app.debug("UPDATEGRAVITY", "sto cliccando");
				world.nosActivate();
				return;
			}
		}
		//es: world.Squirrels(); si pu� creare una funzione in game tale e quale a questa di modo 
		//da non disperdere il principio dell MVC
		//float len = world.squirrels.size();
		//for (int i = 0; i < len; i++) {
		//Squirrel squirrel = world.squirrels.get(i);
		//if(squirrel.nosuse==1)
		//	if(world.squirrels.get(i).nostime==0)world.squirrels.get(i).nostime=world.squirrels.get(i).stateTime;
		//	world.Turbo();
		//	world.turbo=world.turbo+1;
		//}


		ApplicationType appType = Gdx.app.getType();
		// should work also with Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer)
		if (appType == ApplicationType.Android || appType == ApplicationType.iOS) {
			world.update(deltaTime, Gdx.input.getAccelerometerX());
		} else {
			float accel = 0;
			if (Gdx.input.isKeyPressed(Keys.DPAD_LEFT)) accel = 5f;
			if (Gdx.input.isKeyPressed(Keys.DPAD_RIGHT)) accel = -5f;
			world.update(deltaTime, accel);
		}
		if (world.score != lastScore) {
			lastScore = world.score;
			scoreString = "SCORE: " + lastScore;
		}
		if (world.state == World.WORLD_STATE_NEXT_LEVEL) {
			state = GAME_LEVEL_END;
		}
		if (world.state == World.WORLD_STATE_GAME_OVER) {
			state = GAME_OVER;
			if (lastScore >= Settings.highscores[4])

				scoreString = "NEW HIGHSCORE: " + lastScore;
			else
				scoreString = "SCORE:" + lastScore;
			Settings.addScore(lastScore);
			Settings.save();
		}
	}

	private void updatePaused () {
		if (Gdx.input.justTouched()) {
			guiCam.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

			if (OverlapTester.pointInRectangle(resumeBounds, touchPoint.x, touchPoint.y)) {
				Assets.playSound(Assets.clickSound);
				state = GAME_RUNNING;
				return;
			}

			if (OverlapTester.pointInRectangle(quitBounds, touchPoint.x, touchPoint.y)) {
				Assets.playSound(Assets.clickSound);
				game.setScreen(new MainMenuScreen(game));
				return;
			}
		}
	}

	private void updateLevelEnd () {
		if (Gdx.input.justTouched()) {
			world = new World(worldListener);
			renderer = new WorldRenderer(batcher, world);
			world.score = lastScore;
			state = GAME_READY;
		}
	}

	private void updateGameOver () {
		if (Gdx.input.justTouched()) {
			game.setScreen(new MainMenuScreen(game));

		}
	}

	public void draw (float deltaTime) {
		GLCommon gl = Gdx.gl;
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		renderer.render();
		guiCam.update();
		batcher.setProjectionMatrix(guiCam.combined);
		batcher.enableBlending();
		batcher.begin();
		switch (state) {
		case GAME_READY:
			presentReady();
			break;
		case GAME_RUNNING:
			presentRunning();
			break;
		case GAME_PAUSED:
			presentPaused();
			break;
		case GAME_LEVEL_END:
			presentLevelEnd();
			break;
		case GAME_OVER:
			presentGameOver();
			break;
		}
		batcher.end();
	}

	private void presentReady () {
		batcher.draw(Assets.ready, 160 - 192 / 2, 240 - 32 / 2, 192, 32);
	}

	private void presentRunning () {
		batcher.draw(Assets.pause, 320 - 47, 480 - 59, 44, 44);
		//	Assets.font.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		Assets.font.draw(batcher, scoreString, 60, 480 - 28);

		batcher.draw(Assets.coin9, 0, 220, 270, 280);

		String scoret;
		scoret = world.shot+"x ";
		Assets.font.draw(batcher, scoret, 2, 480 - 250);
		batcher.draw(Assets.portaproj, 320 - 318, 480 - 250, 35, 35);
	}

	private void presentPaused () {
		batcher.disableBlending();
		WorldRenderer.drawGradient(batcher, Assets.rect, 0, 0, 320, 480,Color.BLACK,Assets.colore, false);
		batcher.enableBlending();
		Assets.font.draw(batcher, "R e s u m e",160 - 85, 265);
		Assets.font.draw(batcher, "Q u i t",160 - 45, 230 );
		//batcher.draw(Assets.pauseMenu, 160 - 192 / 2, 240 - 96 / 2, 192, 96);
		Assets.font.getRegion().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		Assets.font.draw(batcher, scoreString, 18, 480 - 10);
	}

	private void presentLevelEnd () {
		String topText = "your friends ...";
		String bottomText = "aren't here!";
		float topWidth = Assets.font.getBounds(topText).width;
		float bottomWidth = Assets.font.getBounds(bottomText).width;
		Assets.font.draw(batcher, topText, 160 - topWidth / 2, 480 - 40);
		Assets.font.draw(batcher, bottomText, 160 - bottomWidth / 2, 40);
	}

	private void presentGameOver () {
		Assets.font.draw(batcher, "G A M E  O V E R",160 - 200 / 2, 300);
		//batcher.draw(Assets.gameOver, 160 - 160 / 2, 240 - 96 / 2, 160, 96);
		float scoreWidth = Assets.font.getBounds(scoreString).width;
		Assets.font.draw(batcher, scoreString, 160 - scoreWidth / 2, 480 - 20);
	}

	@Override
	public void render (float delta) {
		update(delta);
		draw(delta);
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void show () {
	}

	@Override
	public void hide () {
	}

	@Override
	public void pause () {
		if (state == GAME_RUNNING) state = GAME_PAUSED;
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}
}