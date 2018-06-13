package plugins;

import org.junit.Test;
import com.threed.jpct.SimpleVector;
import org.junit.Assert;

public class SimpleVectorTest {
	
	@Test
	public void testNewSimpleVector() throws Exception {
        SimpleVector vector = new SimpleVector();
        Assert.assertNotNull(vector);
    }
	
	@Test
	public void testNewSimpleVectorWithParameters() throws Exception {
		int x = 20;
		int y = 30;
		int z = 40;
        SimpleVector vector = new SimpleVector(x,y,z);
        Assert.assertEquals(vector.x, x, 0);
    }
	
	@Test
	public void testNewSimpleVectorCreatedFromDifferentSimpleVector() throws Exception{
		SimpleVector vector1 = new SimpleVector(10,20,30);
		SimpleVector vector2 = new SimpleVector(vector1);
		Assert.assertEquals(10, vector2.x, 0);
	}
	
	@Test
	public void testAddTwoSimpleVectors() throws Exception{
		SimpleVector vector1 = new SimpleVector(10,20,30);
		SimpleVector vector2 = new SimpleVector(100,100,100);
		SimpleVector vector3 = vector2.calcAdd(vector1);
		Assert.assertEquals(110, vector3.x, 0);
	}
	
	@Test
	public void testSetSimpleVectorToDifferentSimpleVector() throws Exception{
		SimpleVector vector1 = new SimpleVector(10,20,30);
		SimpleVector vector2 = new SimpleVector(100,100,100);
		vector2.set(vector1);
		Assert.assertEquals(10, vector2.x, 0);
	}
	
	@Test
	public void testRotateSimpleVector() throws Exception{
		int x = 20;
		int y = 30;
		int z = 40;
		SimpleVector vector1 = new SimpleVector(x,y,z);
		vector1.rotateX(180);
		Assert.assertNotEquals(y, vector1.y, 0);
	}
	
	@Test
	public void testDistanceSimpleVector() throws Exception{
		SimpleVector vector1 = new SimpleVector(10,100,100);
		SimpleVector vector2 = new SimpleVector(100,100,100);
		double distance = vector2.x - vector1.x;
		Assert.assertEquals(distance, vector1.distance(vector2), 0);
	}
}
