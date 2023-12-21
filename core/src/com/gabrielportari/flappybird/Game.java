package com.gabrielportari.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Game extends ApplicationAdapter {
	private SpriteBatch spriteBatch;
	private Random random;
	private int score = 0;
	private int maxScore = 0;
	private boolean isScore;

	private int GAME_STATUS = GAME_PAUSED;
	private static final int GAME_PAUSED = 0;
	private static final int GAME_STARTED = 1;
	private static final int GAME_ENDED = 2;

	private BitmapFont textScore;
	private BitmapFont textHigherScore;
	private BitmapFont textRestart;

	private ShapeRenderer shapeRenderer;
	private Circle birdCircle;
	private Rectangle bottomPipeRectangle;
	private Rectangle topPipeRectangle;

	private Texture[] bird;
	private Texture background;
	private Texture bottomPipe, topPipe;
	private Texture gameOver;

	private float birdAnimCount = 0;
	private float gravity = 0;
	private float birdStartHeight = 0;
	private float birdStartWidth = 0;

	private int deviceHeight, deviceWidth;

	private float pipeWidth;
	private float pipeHeight;
	private float pipeSpacement;

	private Sound flyingSound;
	private Sound colisionSound;
	private Sound scoreSound;

	private Preferences preferences;

	private OrthographicCamera orthographicCamera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	@Override
	public void create () {
		initializeTexture();
		initializeObjects();
	}

	@Override
	public void render () {
		//limpar frames
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		gameStateVerify();
		scoreValidation();
		drawTextures();
		colisionDetector();
	}

	private void drawTextures(){
		spriteBatch.setProjectionMatrix(orthographicCamera.combined);

		spriteBatch.begin();

		spriteBatch.draw(background, 0, 0, deviceWidth, deviceHeight);

		spriteBatch.draw(bird[(int) birdAnimCount], 50 + birdStartWidth, birdStartHeight);

		spriteBatch.draw(bottomPipe, pipeWidth, deviceHeight/2 - bottomPipe.getHeight() - pipeSpacement/2 + pipeHeight);
		spriteBatch.draw(topPipe, pipeWidth, deviceHeight/2 + pipeSpacement/2 + pipeHeight);

		textScore.draw(spriteBatch, String.valueOf(score), deviceWidth/2-40, deviceHeight-200);

		if (GAME_STATUS == GAME_ENDED){
			spriteBatch.draw(gameOver, deviceWidth/2-gameOver.getWidth()/2, deviceHeight/2-gameOver.getHeight()/2);
			textRestart.draw(spriteBatch, "Toque para reiniciar", deviceWidth/2 - 140, deviceHeight/2 - gameOver.getHeight()/2 - 10);
			textHigherScore.draw(spriteBatch, "Seu recorde é " + maxScore + " pontos", deviceWidth/2 - 140, deviceHeight/2 - gameOver.getHeight() - 20);
		}
		spriteBatch.end();
	}

	private void initializeTexture(){
		bird = new Texture[3];
		bird[0] = new Texture("passaro1.png");
		bird[1] = new Texture("passaro2.png");
		bird[2] = new Texture("passaro3.png");

		background = new Texture("fundo.png");

		bottomPipe = new Texture("cano_baixo_maior.png");
		topPipe = new Texture("cano_topo_maior.png");

		gameOver = new Texture("game_over.png");
	}

	private void initializeObjects(){
		spriteBatch = new SpriteBatch();

		random = new Random();

		deviceWidth = (int) VIRTUAL_WIDTH;
		deviceHeight = (int) VIRTUAL_HEIGHT;
		birdStartHeight = deviceHeight/2;
		pipeWidth = deviceWidth;
		pipeSpacement = 300;

		//configuração do placar
		textScore = new BitmapFont();
		textScore.setColor(Color.WHITE);
		textScore.getData().setScale(10);

		//texto para reiniciar
		textRestart = new BitmapFont();
		textRestart.setColor(Color.GREEN);
		textRestart.getData().setScale(2);

		//melhor placar
		textHigherScore = new BitmapFont();
		textHigherScore.setColor(Color.RED);
		textHigherScore.getData().setScale(2);


		//formas para colisões
		shapeRenderer = new ShapeRenderer();
		birdCircle = new Circle();
		bottomPipeRectangle = new Rectangle();
		topPipeRectangle = new Rectangle();

		//inicializar sons
		flyingSound = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		colisionSound = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		scoreSound = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		//recuperar scores
		preferences = Gdx.app.getPreferences("flappyBirdMaxScore");
		maxScore = preferences.getInteger("maxScore", 0);

		//configuracoes camera
		orthographicCamera = new OrthographicCamera();
		orthographicCamera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, orthographicCamera);

	}


	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	private void gameStateVerify(){
		/*
		Status 0 - jogo pausado, esperando toque para iniciar
		Status 1 - jogo em andamento
		Status 2 - fim do jogo, colisão
		 */
		boolean screenTouch = Gdx.input.justTouched();
		if(GAME_STATUS == GAME_PAUSED){
			if(screenTouch){
				gravity = -15;
				GAME_STATUS = GAME_STARTED;
				flyingSound.play();
			}
		}else if(GAME_STATUS == GAME_STARTED){
			//recuperar pontuação
			if(score > maxScore){
				maxScore = score;
				preferences.putInteger("maxScore", maxScore);
			}
			// movimentação do cano
			pipeWidth -= Gdx.graphics.getDeltaTime() * 400; // velocidade do cano
			if(pipeWidth < -bottomPipe.getWidth()){ // recriar o cano ao chegar no fim
				pipeWidth = deviceWidth;
				pipeHeight = random.nextInt(800) - 400; //variação da altura dos canos
				isScore = false;
			}
			// aplicando gravidade no passaro
			if(birdStartHeight > 0 || screenTouch) {
				birdStartHeight -= gravity;
			}
			gravity += 1;

			// touch listener para fazer o passaro pular
			if(screenTouch) {
				gravity = -15;
				flyingSound.play();
			}
		}else if (GAME_STATUS == GAME_ENDED){
			birdStartWidth -= Gdx.graphics.getDeltaTime()*200;

			if(screenTouch) {
				GAME_STATUS = 0;
				score = 0;
				gravity = 0;
				birdStartWidth = 0;
				birdStartHeight = deviceHeight/2;
				pipeWidth = deviceWidth;
			}
		}

		// variação bater de asas do passaro
		birdAnimCount += Gdx.graphics.getDeltaTime() * 8;
		if(birdAnimCount>3) {
			birdAnimCount = 0;
		}

	}

	private void colisionDetector(){
		/* desenho da forma do passaro */
		int birdHeight = bird[0].getHeight()/2;
		int birdWidth = bird[0].getWidth()/2;
		birdCircle.set(50 + birdStartWidth + birdWidth, birdStartHeight + birdHeight, birdWidth);

		/* desenho da forma dos canos */
		int topPipeHeight = topPipe.getHeight();
		int topPipeWidth = topPipe.getWidth();
		topPipeRectangle.set(pipeWidth, deviceHeight/2 + pipeSpacement/2 + pipeHeight, topPipeWidth, topPipeHeight);

		int bottomPipeHeight = bottomPipe.getHeight();
		int bottomPipeWidth = bottomPipe.getWidth();
		bottomPipeRectangle.set(pipeWidth, deviceHeight/2 - bottomPipe.getHeight() - pipeSpacement/2 + pipeHeight, bottomPipeWidth, bottomPipeHeight);

		//verificação de colisão
		boolean topColision = Intersector.overlaps(birdCircle, topPipeRectangle);
		boolean bottomColision = Intersector.overlaps(birdCircle, bottomPipeRectangle);
		if(topColision || bottomColision){
			if(GAME_STATUS == GAME_STARTED){
				GAME_STATUS = GAME_ENDED;
				colisionSound.play();
			}
		}

		/*shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.circle(50 + birdStartWidth + birdWidth, birdStartHeight + birdHeight, birdWidth);
		shapeRenderer.rect(pipeWidth, deviceHeight/2 + pipeSpacement/2 + pipeHeight, topPipeWidth, topPipeHeight);
		shapeRenderer.rect(pipeWidth, deviceHeight/2 - bottomPipe.getHeight() - pipeSpacement/2 + pipeHeight, bottomPipeWidth, bottomPipeHeight);
		shapeRenderer.end();*/
	}

	private void scoreValidation(){
		if(pipeWidth < 50-bird[0].getWidth()){
			if(!isScore){
				scoreSound.play();
				score++;
				isScore = true;
			}
		}
	}

	@Override
	public void dispose () {
	}
}
