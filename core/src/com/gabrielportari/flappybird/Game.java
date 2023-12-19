package com.gabrielportari.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Random;

public class Game extends ApplicationAdapter {
	private SpriteBatch spriteBatch;
	private Random random;
	private BitmapFont textScore;
	private int score;
	private boolean isScore;

	private Texture[] bird;
	private float birdAnimCount = 0;
	private float gravity = 0;
	private float birdStartHeight = 0;

	private int deviceHeight, deviceWidth;
	private Texture background;

	private Texture bottomPipe, topPipe;
	private float pipeWidth;
	private float pipeHeight;
	private float pipeSpacement;


	@Override
	public void create () {
		initializeTexture();
		initializeObjects();
	}

	@Override
	public void render () {
		gameStateVerify();
		scoreValidation();
		drawTextures();
	}

	private void drawTextures(){
		spriteBatch.begin();

		spriteBatch.draw(background, 0, 0, deviceWidth, deviceHeight);

		spriteBatch.draw(bird[(int) birdAnimCount], 50, birdStartHeight);

		spriteBatch.draw(bottomPipe, pipeWidth, deviceHeight/2 - bottomPipe.getHeight() - pipeSpacement/2 + pipeHeight);
		spriteBatch.draw(topPipe, pipeWidth, deviceHeight/2 + pipeSpacement/2 + pipeHeight);

		textScore.draw(spriteBatch, String.valueOf(score), deviceWidth/2, deviceHeight-200);

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

	}

	private void initializeObjects(){
		spriteBatch = new SpriteBatch();

		random = new Random();

		deviceWidth = Gdx.graphics.getWidth();
		deviceHeight = Gdx.graphics.getHeight();
		birdStartHeight = deviceHeight/2;
		pipeWidth = deviceWidth;
		pipeSpacement = 300;

		//configuração do placar
		textScore = new BitmapFont();
		textScore.setColor(Color.WHITE);
		textScore.getData().setScale(10);
	}

	private void gameStateVerify(){
		// movimentação do cano
		pipeWidth -= Gdx.graphics.getDeltaTime() * 300; // velocidade do cano
		if(pipeWidth < -bottomPipe.getWidth()){ // recriar o cano ao chegar no fim
			pipeWidth = deviceWidth;
			pipeHeight = random.nextInt(800) - 400; //variação da altura dos canos
			isScore = false;
		}
		// variação bater de asas do passaro
		birdAnimCount += Gdx.graphics.getDeltaTime() * 8;
		if(birdAnimCount>3) {
			birdAnimCount = 0;
		}

		// touch listener para fazer o passaro pular
		if(Gdx.input.justTouched())
			gravity = -20;

		// aplicando gravidade no passaro
		if(birdStartHeight > 0 || gravity < 0)
			birdStartHeight -= gravity;
		gravity += 1;
	}

	private void scoreValidation(){
		if(pipeWidth < 50-bird[0].getWidth()){
			if(!isScore){
				score++;
				isScore = true;
			}
		}
	}

	@Override
	public void dispose () {
	}
}
