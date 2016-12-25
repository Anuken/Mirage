package io.anuke.Mirage;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.MotionBlur;

public enum Theme{
	normal,
	festive{
		Color[] colors = {Color.WHITE, Color.RED, Color.RED, Color.FOREST, Color.WHITE};
		
		{
			clearColor = new Color(0.2f, 0.2f, 0.35f, 1f);
		}
		
		public void drawPixmap(){
			for(int i = 0; i < 4; i++){
				Color c = colors[MathUtils.random(4)].cpy();//new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), 1);
				m.drawRand(140 - i * 25, c);
				m.drawRand(130 - i * 25, c.sub(0.1f, 0.1f, 0.1f, 0f));
			}
		}
		
		public void config(Bloom bloom, MotionBlur blur){
			blur.setBlurOpacity(0.92f);
			bloom.setBloomIntesity(2);
		}
	};
	public Color clearColor = Color.BLACK;
	public static Mirage m = Mirage.i;
	
	public void drawPixmap(){
		for(int i = 0; i < 4; i++){
			Color c = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), 1);
			m.drawRand(140 - i * 25, c);
			m.drawRand(130 - i * 25, c.sub(0.1f, 0.1f, 0.1f, 0f));
		}
	}
	
	public void config(Bloom bloom, MotionBlur blur){
		
	}
}
