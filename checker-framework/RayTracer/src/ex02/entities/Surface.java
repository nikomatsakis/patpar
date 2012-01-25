package ex02.entities;

import ex02.Parser;
import ex02.Utils;

public class Surface {
	public static final int TYPE_FLAT = 1;
	public static final int TYPE_CHECKERS = 2;
	public static final int TYPE_TEXTURE = 3;
	
	int typeId;
	String type;
	double[] diffuse = { 0.8F, 0.8F, 0.8F };
	double[] specular = { 1.0F, 1.0F, 1.0F };
	double[] ambient = { 0.1F, 0.1F, 0.1F };
	double[] emission = { 0, 0, 0 };
	double shininess = 100.0F;
	double checkersSize = 0.1F;
	double[] checkersDiffuse1 = { 1.0F, 1.0F, 1.0F };
	double[] checkersDiffuse2 = { 0.1F, 0.1F, 0.1F };	
	double reflectance = 0.0F;		
	String textureFileName;
	int textureWidth;
	int textureHeight;
	double[][][] texture;						
	
	// Returns the texture color for a given 2D point in [0, 1] coordinates
	public double[] getTextureColor(double[] point2D) {		 
		int textureX = Math.abs((int)Math.round(point2D[0] * textureWidth)) % textureWidth; 
		int textureY = Math.abs((int)Math.round(point2D[1] * textureHeight)) % textureHeight;
		
		return texture[textureY][textureX];
	}
	
	// Returns the checkers color for a given 2D point in [0, 1] coordinates
	public double[] getCheckersColor(double[] point2D) {
		 double checkersX = Math.abs(Math.floor(point2D[0] / checkersSize) % 2);
		 double checkersY = Math.abs(Math.floor(point2D[1] / checkersSize) % 2);
		 
		 if (checkersX == 0 && checkersY == 0) return checkersDiffuse2;
		 if (checkersX == 0 && checkersY == 1) return checkersDiffuse1;
		 if (checkersX == 1 && checkersY == 0) return checkersDiffuse1;
		 if (checkersX == 1 && checkersY == 1) return checkersDiffuse2;
		 
		 return null;
	}
	
	public void postInit() {
		
	}
	
	// Read parameters into members
	public boolean parseParameter(String name, String[] args) throws Exception {
		boolean parsed = false;
		
		if ("mtl-type".equals(name)) { 
			type = args[0]; parsed = true;
			
			if ("flat".equals(args[0])) typeId = TYPE_FLAT;
			if ("checkers".equals(args[0])) typeId = TYPE_CHECKERS;
			if ("texture".equals(args[0])) typeId = TYPE_TEXTURE;
		}
		
		if ("mtl-diffuse".equals(name)) { diffuse = Parser.parseVector(args); parsed = true; }
		if ("mtl-specular".equals(name)) { specular = Parser.parseVector(args); parsed = true; }
		if ("mtl-ambient".equals(name)) { ambient = Parser.parseVector(args); parsed = true; }
		if ("mtl-emission".equals(name)) { emission = Parser.parseVector(args); parsed = true; }
		if ("mtl-shininess".equals(name)) { shininess = Double.parseDouble(args[0]); parsed = true; }
		if ("checkers-size".equals(name)) { checkersSize = Double.parseDouble(args[0]); parsed = true; }
		if ("checkers-diffuse1".equals(name)) { checkersDiffuse1 = Parser.parseVector(args); parsed = true; }
		if ("checkers-diffuse2".equals(name)) { checkersDiffuse2 = Parser.parseVector(args); parsed = true; }
		if ("texture".equals(name)) { 
			textureFileName = args[0]; parsed = true;
			texture = Utils.loadTexture(textureFileName);
			textureWidth = texture.length;
			textureHeight = texture[0].length;
		}
		if ("reflectance".equals(name)) { reflectance = Double.parseDouble(args[0]); parsed = true; }
		
		return parsed;		
	}			
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double[] getDiffuse() {
		return diffuse;
	}
	public void setDiffuse(double[] diffuse) {
		this.diffuse = diffuse;
	}
	public double[] getSpecular() {
		return specular;
	}
	public void setSpecular(double[] specular) {
		this.specular = specular;
	}
	public double[] getAmbient() {
		return ambient;
	}
	public void setAmbient(double[] ambient) {
		this.ambient = ambient;
	}
	public double[] getEmission() {
		return emission;
	}
	public void setEmission(double[] emission) {
		this.emission = emission;
	}
	public double getShininess() {
		return shininess;
	}
	public void setShininess(double shininess) {
		this.shininess = shininess;
	}
	public double getCheckersSize() {
		return checkersSize;
	}
	public void setCheckersSize(double checkersSize) {
		this.checkersSize = checkersSize;
	}
	public double[] getCheckersDiffuse1() {
		return checkersDiffuse1;
	}
	public void setCheckersDiffuse1(double[] checkersDiffuse1) {
		this.checkersDiffuse1 = checkersDiffuse1;
	}
	public double[] getCheckersDiffuse2() {
		return checkersDiffuse2;
	}
	public void setCheckersDiffuse2(double[] checkersDiffuse2) {
		this.checkersDiffuse2 = checkersDiffuse2;
	}
	public double[][][] getTexture() {
		return texture;
	}
	public void setTexture(double[][][] texture) {
		this.texture = texture;
	}
	public double getReflectance() {
		return reflectance;
	}
	public void setReflectance(double reflectance) {
		this.reflectance = reflectance;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}	
	
	
}
