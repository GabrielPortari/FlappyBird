package com.gabrielportari.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Game extends ApplicationAdapter {
	private SpriteBatch spriteBatch;
	private Texture[] bird;
	private Texture background;

	//Atributos de configuração
	private int deviceHeight, deviceWidth;
	private float birdAnimCount = 0;
	private float gravity = 0;
	private float birdStartHeight = 0;
	@Override
	public void create () {
		spriteBatch = new SpriteBatch();
		bird = new Texture[3];

		bird[0] = new Texture("passaro1.png");
		bird[1] = new Texture("passaro2.png");
		bird[2] = new Texture("passaro3.png");
		background = new Texture("fundo.png");

		deviceWidth = Gdx.graphics.getWidth();
		deviceHeight = Gdx.graphics.getHeight();

		birdStartHeight = deviceHeight/2;
	}

	@Override
	public void render () {
		spriteBatch.begin();

		// Background render
		spriteBatch.draw(background, 0, 0, deviceWidth, deviceHeight);

		// animação do passaro
		if(birdAnimCount>3)
			birdAnimCount = 0;
		spriteBatch.draw(bird[(int) birdAnimCount], 100, birdStartHeight);
		birdAnimCount += Gdx.graphics.getDeltaTime() * 8;

		// click listener event na tela
		if(Gdx.input.justTouched()){
			gravity = -20;
		}

		// aplicando gravidade no passaro
		if(birdStartHeight > 0 || gravity < 0)
			birdStartHeight -= gravity;
		gravity += 1;

		spriteBatch.end();
	}
	
	@Override
	public void dispose () {
	}
}
