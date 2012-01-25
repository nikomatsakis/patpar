package ex02;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Utils {
	
	// Converts an array of double in [0, 1] to an Integer representing a color
	public static int floatArrayToColorInt(double[] color) {
		int red = Math.min(255, (int)Math.round(color[0] * 255));
		int green = Math.min(255, (int)Math.round(color[1] * 255));
		int blue = Math.min(255, (int)Math.round(color[2] * 255)); 
		int colorInt = (red << 16 & 0xFF0000) |
					   (green << 8 & 0xFF00) |
					   (blue & 0xFF);
						
		return colorInt;
	}
	
	// Loads a texture into a matrix (3rd dimension values are in [0, 1])
	public static double[][][] loadTexture(String textureFileName) throws IOException {
		BufferedImage img = ImageIO.read(new File(textureFileName));
		int textureWidth = img.getWidth();
		int textureHeight = img.getHeight();
		double[][][] texture = new double[textureHeight][textureWidth][3];
		for (int i = 0; i < textureHeight; i++) {
			for (int j = 0; j < textureWidth; j++) {
				int rgb = img.getRGB(j, i);
				
				int red = (rgb >> 16) & 0xFF;
				int green = (rgb >> 8) & 0xFF;
				int blue = rgb & 0xFF;

				texture[i][j][0] = red / 255F;
				texture[i][j][1] = green / 255F;
				texture[i][j][2] = blue / 255F;
			}
		}
		
		return texture;						
	}
}
