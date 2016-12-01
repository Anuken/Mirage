package io.anuke.Mirage;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;
import com.bitfire.utils.ShaderLoader;

import io.anuke.ucore.UCore;
import io.anuke.ucore.graphics.Hue;
import io.anuke.ucore.graphics.PixmapUtils;
import io.anuke.ucore.graphics.ShapeUtils;
import io.anuke.usound.AudioPlayer;

public class Mirage extends ApplicationAdapter{
	SpriteBatch batch;
	OrthographicCamera camera;
	BitmapFont font;
	Pixmap pixmap;
	Texture texture;
	Texture colors;
	TextureRegion region;
	int pixmapsize = 200;
	float camscale = 1.5f;
	float si;
	float scale = 2f;
	float scale2 = 1200f;
	float scale3 = 4.7f;
	float scale4 = 0f;
	int bars = 31;
	int frame;
	Bloom bloom;
	Bloom bloom2;
	ShaderProgram shader;
	PostProcessor processor;
	PostProcessor barprocessor;
	// GifRecorder recorder;
	AudioPlayer player;
	float[] logs = new float[pixmapsize];
	float[] barvals = new float[32];
	boolean[] barswitch = new boolean[32];
	boolean flip = false;
	boolean sw = false; // switch
	float fallspeed = 0.3f;
	Array<Integer> intmap = new Array<Integer>();
	boolean random = false;
	float fadeout;
	float hoff = 0f;
	float[] heights = new float[bars];
	MotionBlur blur;
	boolean played = false;
	
	@Override
	public void create(){
		UCore.maximizeWindow();
		Gdx.input.setInputProcessor(input);
		font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"), Gdx.files.internal("fonts/font.png"), false);
		pixmap = new Pixmap(pixmapsize, pixmapsize, Format.RGBA8888);
		Pixmap.setBlending(Blending.None);
		initPixmap();
		texture = new Texture(pixmap);
		region = new TextureRegion(texture);
		camera = new OrthographicCamera(Gdx.graphics.getWidth() / camscale, Gdx.graphics.getHeight() / camscale);
		batch = new SpriteBatch();
		// recorder = new GifRecorder(batch);
		// recorder.setOpenKey(Keys.Y);
		colors = PixmapUtils.blankTexture();
		player = new AudioPlayer(32);

		for(int i = 0; i < logs.length; i++)
			logs[i] = (float) Math.log(i);

		for(int x = 0; x < pixmapsize; x++){
			for(int y = 0; y < pixmapsize; y++){
				intmap.add(y * pixmapsize + x);
			}
		}

		intmap.shuffle();

		ShapeUtils.region = new TextureRegion(colors);
		ShapeUtils.thickness = 5f;
		initshader();
	}

	void initshader(){
		ShaderLoader.BasePath = "shaders/";
		processor = new PostProcessor(false, false, true);
		barprocessor = new PostProcessor(false, false, true);
		bloom = new Bloom((int) (Gdx.graphics.getWidth() * 0.25f), (int) (Gdx.graphics.getHeight() * 0.25f));
		bloom.setBloomIntesity(3);
		
		bloom2 = new Bloom((int) (Gdx.graphics.getWidth() * 0.25f), (int) (Gdx.graphics.getHeight() * 0.25f));
		bloom2.setBloomIntesity(3);
		
		processor.addEffect(bloom);
		barprocessor.addEffect(bloom2);
		
		blur = new MotionBlur();
		blur.setBlurOpacity(0.94f);
		processor.addEffect(blur);
		
		processor.rebind();
		barprocessor.rebind();
	}

	@Override
	public void render(){
		if(Gdx.input.isKeyPressed(Keys.ESCAPE))
			Gdx.app.exit();
		
		if(Gdx.input.isKeyJustPressed(Keys.INSERT) && !played){
			player.playFile(Gdx.files.internal("music/fusion.mp3"));
			played = true;
		}
		
		UCore.clearScreen(Color.BLACK);
		
		
		batch.enableBlending();
		processor.capture();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		draw();
		batch.end();
		processor.render();
		
		
		bloom2.enableBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_COLOR );
		barprocessor.capture();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch.begin();
		preDraw();
		drawBars();
		drawGUI();
		
		batch.end();
		camera.update();
		doInput();
		
		barprocessor.render();
		
	}

	void initPixmap(){
		for(int i = 0; i < 4; i++){
			Color c = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), 1);
			// if(Math.random() < 0.3) c.set(0);
			if(Math.random() < 0.33){
				drawCircle(140 - i * 25, c);
				drawCircle(130 - i * 25, c.sub(new Color(0.1f, 0.1f, 0.1f, 0f)));
			}else if(Math.random() < 0.66){
				drawSquare(140 - i * 25, c);
				drawSquare(130 - i * 25, c.sub(new Color(0.1f, 0.1f, 0.1f, 0f)));
			}else{
				drawRect(140 - i * 25, c);
				drawRect(130 - i * 25, c.sub(new Color(0.1f, 0.1f, 0.1f, 0f)));
			}
		}
	}

	void draw(){

		float size = pixmap.getWidth() / 1.5f + heights[bars / 2] / 2f;

		Color color = batch.getColor().cpy();

		float tsize = pixmap.getWidth() / 1.1f;

		batch.setColor(1f, 1f, 1f, 0.13f);
		batch.draw(region, -tsize, -tsize, tsize, tsize, tsize * 2, tsize * 2, 2, 2, -45);

		float switchscl = 100;
		float sw = (heights[0] > 100 ? 1f : 0);

		batch.setColor(color);
		batch.draw(region, -size / 2, -size / 2 + sw * (barswitch[0] ? -switchscl : switchscl), size / 2, size / 2,
				size, size, 2, 2, -45);
		batch.draw(region, -size / 2 - 300, -size / 2 + 100 + sw * (barswitch[0] ? switchscl : -switchscl), size / 2,
				size / 2, size, size, 2, 2, 0);
		batch.draw(region, -size / 2 + 300, -size / 2 + 100 + sw * (barswitch[0] ? switchscl : -switchscl), size / 2,
				size / 2, size, size, 2, 2, -90);

	}

	void preDraw(){
		float[] samples = player.spectrum;
		int width = Gdx.graphics.getWidth();
		int h = 480;

		float barWidth = ((float) width / (float) bars);

		int nb = (samples.length / bars) / 2;

		for(int i = 0; i < bars; i++){
			int histoX = bars / 2 - Math.abs(bars / 2 - i);

			float height = (float) Math.pow(scale(avg(histoX, nb)), 1.1f);

			batch.setColor(Hue.blend(Hue.fromHSB(height / width + 0.5f, 1f, 1f),
					Hue.fromHSB(-height / h + hoff - 0.5f, 1f, 1f), (float) histoX / (bars / 2f)));
			Color color = batch.getColor();
			// color.a = 0.1f;
			batch.setColor(color);

			batch.draw(colors, i * barWidth, 0, barWidth, height);
		}
	}

	void drawBars(){
		float[] topValues = player.topValues;
		float[] maxValues = player.maxValues;
		float[] samples = player.spectrum;
		int width = Gdx.graphics.getWidth();

		float barWidth = ((float) width / (float) bars);

		int nb = (samples.length / bars) / 2;

		for(int i = 0; i < bars; i++){

			int histoX = i;

			float height = scale(avg(histoX, nb));
			//batch.setColor(Hue.blend(Hue.fromHSB(-height / width + 0.1f, 1f, 1f), Hue.fromHSB(height / h, 1f, 1f),
			//		(float) histoX / (bars / 2f)));
			batch.setColor(new Color(color(i)));
			float bh = 0;
			batch.draw(colors, 0, i * barWidth + bh / 2, height, bh, 0, 0, 16, 5, false, false);
			batch.draw(colors, Gdx.graphics.getWidth(), i * barWidth + bh / 2, -height, bh, 0, 0, 16, 5, false, false);
		}

		for(int i = 0; i < bars; i++){
			int histoX = bars / 2 - Math.abs(bars / 2 - i);
			heights[i] = scale(avg(histoX, nb));

			if(avg(histoX, nb) > maxValues[histoX]){
				maxValues[histoX] = avg(histoX, nb);
			}

			if(avg(histoX, nb) > topValues[histoX]){
				topValues[histoX] = avg(histoX, nb);
			}

			topValues[histoX] -= fallspeed;
		}
		
		Color mul = (Hue.fromHSB(scale(avg(bars / 2 - Math.abs(bars / 2 - (bars-1)), nb)) / 100 + 3f, 0.5f, 1f));

		for(int i = 0; i < bars; i++){
			int histoX = bars / 2 - Math.abs(bars / 2 - i);

			float height = scale(avg(histoX, nb));
			
			
			batch.setColor(new Color(color(i)).mul(1.2f).mul(mul));
			
			//batch.setColor(Hue.blend(Hue.fromHSB(-height / width + 0.1f, 1f, 1f),
			//		Hue.fromHSB(height / h + hoff, 1f, 1f), (float) histoX / (bars / 2f)));

			batch.draw(colors, i * barWidth, heights[bars - 1 - Math.abs(i - bars / 2)], barWidth, height);

			//batch.setColor(Hue.blend(Hue.fromHSB(height / width + 0.1f, 1f, 1f),
			//		Hue.fromHSB(-height / h + hoff, 1f, 1f), (float) histoX / (bars / 2f)));
			
			batch.setColor(new Color(color2(i)).mul(1.2f).mul(mul));
			
			batch.draw(colors, i * barWidth, 0, barWidth, heights[bars - 1 - Math.abs(i - bars / 2)]);

			batch.setColor(Hue.fromHSB(height / 100 + 3f, 0.5f, 1f));
		}

	}
	
	int color(int bar){
		if(bar > bars/2) bar = bars-1 - bar;
		return pixmap.getPixel((int)((float)bar/bars*pixmap.getWidth()), (int)((float)bar/bars*pixmap.getHeight()));
	}
	
	int color2(int bar){
		if(bar > bars/2) bar = bars-1 - bar;
		bar = bars-1-bar;
		return pixmap.getPixel((int)((float)bar/bars*pixmap.getWidth()), (int)((float)bar/bars*pixmap.getHeight()));
	}

	private float scale(float x){
		return 10f + (float) (float) Math.pow(x, 1.2f) * 1.2f;
	}

	void clear(){
		pixmap.setColor(0);
		pixmap.fill();

		initPixmap();
	}

	void drawGUI(){
		font.setColor(Color.WHITE);
		// font.draw(batch, scale3 + " " + Gdx.graphics.getFramesPerSecond(), 0,
		// 30);
		font.setColor(Color.RED);

		if(fadeout > 1){
			fadeout -= 0.008f * Gdx.graphics.getDeltaTime() * 60;
		}

		if(fadeout > 0){
			batch.setColor(0, 0, 0, UCore.clamp(1f - (fadeout - 1f)));
			batch.draw(colors, 0, 0, Gdx.graphics.getWidth() * 10, Gdx.graphics.getHeight() * 10);
		}
	}

	void doInput(){
		if(Gdx.input.isKeyPressed(Keys.E)){
			scale3 += 0.1f;
		}

		if(Gdx.input.isKeyJustPressed(Keys.INSERT)){
			//player.pause();
			//try{Thread.sleep(10);}catch (Exception e){}
			
			//if(Gdx.files.local("song.mp3").exists())
			//	player.playFile(Gdx.files.local("song.mp3"));
			//else if(Gdx.files.local("song.ogg").exists())
			//	player.playFile(Gdx.files.local("song.ogg"));
		}

		if(Gdx.input.isKeyPressed(Keys.Q) && Gdx.input.isKeyPressed(Keys.W) && scale3 > 0){
			scale3 -= 1f;
		}

		if(Gdx.input.isKeyPressed(Keys.E) && Gdx.input.isKeyPressed(Keys.W)){

			scale3 += 1f;
		}

		if(Gdx.input.isKeyPressed(Keys.Q) && scale3 > 0){
			scale3 -= 0.1f;
		}

		if(Gdx.input.isKeyJustPressed(Keys.SHIFT_LEFT)){
			clear();
		}

		boolean ys = false;
		int bars = 32;

		for(int i = 0; i < bars; i++){
			int histoX = i;

			int nb = (2048 / bars) / 2;
			float avg = avg(histoX, nb);

			barvals[i] = avg;
			if(avg > player.topValues[i])
				barswitch[i] = !barswitch[i];
		}

		if(barvals[4] >= player.topValues[4] && barvals[4] > 20)
			clear();
		
		scale3 += (flip ? 1 : -1) * barvals[31] / 500f;

		if(barvals[3] >= player.topValues[31]){
			if(!flip){
				if(scale3 <= 10)
					flip = true;
			}else{
				if(scale3 >= 3)
					flip = false;
			}
		}

		if(barvals[8] >= player.topValues[8] && player.isPlaying()){
			hoff += 0.02f;
		}

		if(barvals[6] >= player.topValues[6] && Math.random() < 0.2){
			scale3 += MathUtils.random(-0.95f, 1);
			if(Math.random() < 0.3)
				clear();
		}

		// if(barvals[8] >= player.topValues[8]){
		// bloom.setBaseSaturation(barvals[8]/40f);

		// }

		if(barvals[0] > 1f && Gdx.graphics.getFrameId() % 2 == 0){
			ys = true;
		}

		if(ys){
			for(int i = 0; i < 6; i++){
				for(int x = 0; x < pixmap.getWidth(); x++){

					for(int y = 0; y < pixmap.getHeight(); y++){

						int pix = pixmap.getPixel(x, y);

						if(pix != 0){
							int randx = (int) ((MathUtils.cos(y * scale2 + logs[y] * scale3 + scale4)) * scale),
									randy = (int) ((MathUtils.cos(x * scale2 + logs[x] * scale3 + scale4)) * scale);

							if(pixmap.getPixel(x + randx, y + randy) == 0 && !(x + randx >= pixmap.getWidth())
									&& !(x + randx < 0) && !(y + randy >= pixmap.getHeight()) && !(y + randy < 0)){
								pixmap.drawPixel(x, y, 0);
								pixmap.drawPixel(x + randx, y + randy, pix);
							}
						}
					}
				}
			}

			updatePixmap();
		}

		if(fadeout <= 0.00001f && !player.isPlaying() && played){
			fadeout = 2f;
		}

		if(Gdx.input.isKeyJustPressed(Keys.R)){
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			PixmapIO.writePNG(Gdx.files.external(
					"Documents/Mirage/mirage" + sdf.format(Calendar.getInstance().getTime()) + ".png"), pixmap);
		}
	}

	int barint(int i){
		return barswitch[i] ? 1 : -1;
	}

	private float avg(int pos, int nb){
		int sum = 0;
		for(int i = 0; i < nb; i++){
			sum += player.spectrum[pos + i];
		}

		return (float) (sum / nb);
	}

	void updatePixmap(){
		texture.draw(pixmap, 0, 0);
	}

	InputAdapter input = new InputAdapter(){
		public boolean scrolled(int amount){
			if(amount > 0){
				camera.zoom += amount / 10f;
			}else{
				if(camera.zoom > 0.1f){
					camera.zoom += amount / 10f;
				}
			}

			return false;
		}
	};

	void drawSquare(int range, Color c){
		pixmap.setColor(c);
		pixmap.fillRectangle(pixmap.getWidth() / 2 - range / 2, pixmap.getWidth() / 2 - range / 2, range, range);
	}

	void drawRect(int range, Color c){
		pixmap.setColor(c);
		pixmap.fillRectangle(pixmap.getWidth() / 2 - range / 2, pixmap.getWidth() / 2 - range / 2, range, range);
	}

	void drawCircle(int range, Color c){
		pixmap.setColor(c);
		pixmap.fillCircle(pixmap.getWidth() / 2, pixmap.getWidth() / 2, range / 2);
	}

	void drawTriangle(int range, Color c){
		pixmap.setColor(c);
		pixmap.fillTriangle(pixmap.getHeight() / 2 - range / 2, pixmap.getWidth() / 2 - range / 2,
				pixmap.getWidth() / 2, pixmap.getHeight() / 2 + range / 2, pixmap.getWidth() / 2 + range / 2,
				pixmap.getHeight() / 2 - range / 2);
		// pixmap.fillCircle(pixmap.getWidth() / 2, pixmap.getWidth() / 2, range
		// / 2);
	}
	
	public void dispose(){
		player.dispose();
	}
}
