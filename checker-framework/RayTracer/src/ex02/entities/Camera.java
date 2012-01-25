package ex02.entities;

import java.util.List;

import ex02.Parser;
import ex02.Parser.ParseException;
import ex02.blas.MathUtils;

public class Camera implements IEntity {	
	private double[] eye;
	private double[] lookAt;
	private double[] direction;
	private double[] upDirection;	
	private double[] rightDirection;
	private double[] viewplaneUp; 
	private double screenDist = 1;
	private double screenWidth = 2;	
	
	// Read parameters into members
	public void setParameter(String name, String[] args) throws Exception {
		if ("eye".equals(name)) eye = Parser.parseVector(args);
		if ("look-at".equals(name)) lookAt = Parser.parseVector(args);
		if ("direction".equals(name)) { 
			direction = Parser.parseVector(args);
			MathUtils.normalize(direction);
		}
		if ("up-direction".equals(name)) upDirection = Parser.parseVector(args);
		if ("screen-dist".equals(name)) screenDist = Double.parseDouble(args[0]);
		if ("screen-width".equals(name)) screenWidth = Double.parseDouble(args[0]);						
	}
	
	
	///////////////////////////  Getters & Setters  ///////////////////////////////

	public double[] getEye() {
		return eye;
	}
	public void setEye(double[] eye) {
		this.eye = eye;
	}
	public double[] getLookAt() {
		return lookAt;
	}
	public void setLookAt(double[] lookAt) {
		this.lookAt = lookAt;
	}
	public double getScreenDist() {
		return screenDist;
	}
	public void setScreenDist(double screenDist) {
		this.screenDist = screenDist;
	}
	public double getScreenWidth() {
		return screenWidth;
	}
	public void setScreenWidth(double screenWidth) {
		this.screenWidth = screenWidth;
	}
	
	public double[] getUpDirection() {
		return upDirection;
	}
	public void setUpDirection(double[] upDirection) {
		this.upDirection = upDirection;
	}
	
	public double[] getRightDirection() {
		return rightDirection;
	}
	public void setRightDirection(double[] rightDirection) {
		this.rightDirection = rightDirection;
	}

	public void postInit(List<IEntity> entities) throws ParseException {
		// If we didn't get a direction parameter, set it using the look-at parameter
		if (direction == null)
		{			
			direction = MathUtils.calcPointsDiff(eye, lookAt);
			MathUtils.normalize(direction);						
		}
		
		// Compute a right direction and a viewplane up direction (perpendicular to the look-at vector)
		rightDirection = MathUtils.crossProduct(upDirection, direction);
		MathUtils.normalize(rightDirection);
		MathUtils.multiplyVectorByScalar(rightDirection, -1);
		
		viewplaneUp = MathUtils.crossProduct(rightDirection, direction);		
		MathUtils.normalize(viewplaneUp);					
	}

	public double[] getDirection() {
		return direction;
	}

	public void setDirection(double[] direction) {
		this.direction = direction;
	}


	public double[] getViewplaneUp() {
		return viewplaneUp;
	}


	public void setViewplaneUp(double[] viewplaneUp) {
		this.viewplaneUp = viewplaneUp;
	}	
			
}
