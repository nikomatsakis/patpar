package ex02.entities;

import ex02.entities.lights.LightArea;
import ex02.entities.lights.LightDirected;
import ex02.entities.lights.LightPoint;
import ex02.entities.primitives.Box;
import ex02.entities.primitives.Cylinder;
import ex02.entities.primitives.Disc;
import ex02.entities.primitives.Rectangle;
import ex02.entities.primitives.Sphere;
import ex02.entities.primitives.Torus;

public class EntityFactory {
	
	// Returns an IEntity given an entity name
	public static IEntity createEntity(String entityName) {
		if (entityName == null || entityName.length() == 0) return null;
		
		entityName = entityName.toLowerCase();
		
		if ("scene".equals(entityName)) return new Scene();
		if ("camera".equals(entityName)) return new Camera();
		if ("rectangle".equals(entityName)) return new Rectangle();
		if ("disc".equals(entityName)) return new Disc();
		if ("sphere".equals(entityName)) return new Sphere();
		if ("cylinder".equals(entityName)) return new Cylinder();
		if ("box".equals(entityName)) return new Box();
		if ("torus".equals(entityName)) return new Torus();
		if ("light-point".equals(entityName)) return new LightPoint();
		if ("light-directed".equals(entityName)) return new LightDirected();
		if ("light-area".equals(entityName)) return new LightArea();
				
		return null;
	}

}
