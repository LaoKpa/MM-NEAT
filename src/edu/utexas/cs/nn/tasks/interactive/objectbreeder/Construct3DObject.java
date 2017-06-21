package edu.utexas.cs.nn.tasks.interactive.objectbreeder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.vecmath.Vector3d;

import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.util.CartesianGeometricUtilities;
import edu.utexas.cs.nn.util.graphics.GraphicsUtil;

/**
 * Series of utility methods associated with rendering 
 * a 3D object created from a series of vertexes
 * 
 * @author Isabel Tweraser
 *
 */
public class Construct3DObject {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Container pane = frame.getContentPane();
		pane.setLayout(new BorderLayout());

		// slider to control horizontal rotation
		JSlider headingSlider = new JSlider(-180, 180, 0);
		pane.add(headingSlider, BorderLayout.SOUTH);

		// slider to control vertical rotation
		JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
		pane.add(pitchSlider, BorderLayout.EAST);

		// panel to display render results
		@SuppressWarnings("serial")
		JPanel renderPanel = new JPanel() {
			public void paintComponent(Graphics g) {
				List<Triangle> tris = new LinkedList<Triangle>();
				tris.addAll(cubeConstructor(new Vertex(0,0,0), 50.0, Color.RED));
				tris.addAll(cubeConstructor(new Vertex(10,0,0), 50.0, Color.GREEN));
				tris.addAll(cubeConstructor(new Vertex(0,10,0), 50.0, Color.YELLOW));
				tris.addAll(cubeConstructor(new Vertex(0,20,0), 50.0, Color.GREEN));
				tris.addAll(cubeConstructor(new Vertex(0,0,20), 50.0, Color.GRAY));
				
				Graphics2D g2 = (Graphics2D) g;
				double heading = Math.toRadians(headingSlider.getValue());
				double pitch = Math.toRadians(pitchSlider.getValue());
				//rotate(g2, tris, getWidth(), getHeight());
				drawTriangles(g2,tris,getWidth(), getHeight(), heading, pitch);
			}
		};
		pane.add(renderPanel, BorderLayout.CENTER);

		headingSlider.addChangeListener(e -> renderPanel.repaint());
		pitchSlider.addChangeListener(e -> renderPanel.repaint());

		frame.setSize(400, 400);
		frame.setVisible(true);
	}
	
	/**
	 * Renders list of triangles as a cube. 
	 * 
	 * @param g2 graphics instance
	 * @param tris representative list of triangles
	 * @param width width of image
	 * @param height height of image
	 * @param heading input heading value for JSlider
	 * @param pitch input pitch value for JSlider
	 */
	protected static void drawTriangles(Graphics2D g2, List<Triangle> tris, int width, int height, double heading, double pitch) {
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, width, height);	
		// get Matrix3 used to align vectors of triangles in relation to each other to render cube
		Matrix3 transform = getTransform(heading, pitch);
		//construct image based on input Matrix3 instance (position of JSliders)
		BufferedImage img = getImage(tris, width, height, transform);
		//draw image in graphics instance
		g2.drawImage(img, 0, 0, null);
	}
	
	/**
	 * Constructs a BufferedImage of the current 3D image rotation based on the current heading and pitch.
	 * 
	 * @param tris list of triangles
	 * @param width width of image
	 * @param height height of image
	 * @param heading Input horizontal position from JSlider
	 * @param pitch Input vertical position from JSlider
	 * @return BufferedImage representing current view of 3D image
	 */
	private static BufferedImage getImage(List<Triangle> tris, int width, int height, double heading, double pitch) {
		Matrix3 transform = getTransform(heading, pitch);
		return getImage(tris, width, height, transform);
	}
	
	/**
	 * Creates Matrix3 instance that is used to transform/manipulate a list of Triangles into a 
	 * rendered cube.
	 * 
	 * @param heading Input horizontal position from JSlider
	 * @param pitch Input vertical position from JSlider
	 * @return Matrix3 used to manipulate vectors of triangles in list
	 */
	private static Matrix3 getTransform(double heading, double pitch) {
		Matrix3 headingTransform = new Matrix3(new double[] {
				Math.cos(heading), 0, -Math.sin(heading),
				0, 1, 0,
				Math.sin(heading), 0, Math.cos(heading)
		});
		Matrix3 pitchTransform = new Matrix3(new double[] {
				1, 0, 0,
				0, Math.cos(pitch), Math.sin(pitch),
				0, -Math.sin(pitch), Math.cos(pitch)
		});
		Matrix3 transform = headingTransform.multiply(pitchTransform);
		return transform;
	}
	
	/**
	 * Constructs BufferedImage from list of triangles based on the input Matrix3 specifications
	 * (positions of JSliders determining rotation of 3D image)
	 * 
	 * @param tris list of triangles
	 * @param width width of image
	 * @param height height of image
	 * @param transform Matrix3 instance determining where JSlider rotation occurs for image construction
	 * @return BufferedImage representing current view of 3D image
	 */
	private static BufferedImage getImage(List<Triangle> tris, int width, int height, Matrix3 transform) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		double[] zBuffer = new double[img.getWidth() * img.getHeight()];
		// initialize array with extremely far away depths
		for (int q = 0; q < zBuffer.length; q++) {
			zBuffer[q] = Double.NEGATIVE_INFINITY;
		}

		for (Triangle t : tris) {
			Vertex v1 = transform.transform(t.v1);
			v1.x += width / 2;
			v1.y += height / 2;
			Vertex v2 = transform.transform(t.v2);
			v2.x += width / 2;
			v2.y += height / 2;
			Vertex v3 = transform.transform(t.v3);
			v3.x += width / 2;
			v3.y += height / 2;
			
			Vertex norm = getNorm(v1, v2, v3);

			double angleCos = Math.abs(norm.z);

			int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
			int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
			int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
			int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

			double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
					double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
					double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
					if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
						double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
						int zIndex = y * img.getWidth() + x;
						if (zBuffer[zIndex] < depth) {
							img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
							zBuffer[zIndex] = depth;
						}
					}
				}
			}

		}
		return img;
	}
	
	private static Vertex getNorm(Vertex v1, Vertex v2, Vertex v3) {
		Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
		Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
		Vertex norm = new Vertex(
				ab.y * ac.z - ab.z * ac.y,
				ab.z * ac.x - ab.x * ac.z,
				ab.x * ac.y - ab.y * ac.x
				);
		double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
		norm.x /= normalLength;
		norm.y /= normalLength;
		norm.z /= normalLength;
		return norm;
	}
	
	/**
	 * Returns shading of color based on angle to make rendering of three
	 * dimensions look more accurate.
	 * 
	 * @param color Color being shaded
	 * @param shade Result color after shading
	 * @return
	 */
	public static Color getShade(Color color, double shade) {
		double redLinear = Math.pow(color.getRed(), 2.4) * shade;
		double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
		double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

		int red = (int) Math.pow(redLinear, 1/2.4);
		int green = (int) Math.pow(greenLinear, 1/2.4);
		int blue = (int) Math.pow(blueLinear, 1/2.4);

		return new Color(red, green, blue);
	}
	
	/**
	 * Takes in a list of vertexes, a triangle sidelength, and a desired color and 
	 * returns a list of triangles that construct a series of cubes centered at the various
	 * vertexes. 
	 * 
	 * @param centers List of vertexes where cubes will be constructed
	 * @param sideLength length of triangle side
	 * @param color desired color of cubes
	 * @return List of triangles that construct a series of cubes centered at the various vertexes
	 */
	public static List<Triangle> getShape(List<Vertex> centers, double sideLength, Color color) {
		List<Triangle> tris = new ArrayList<>();
		for(Vertex v: centers) { //construct individual cubes and add them to larger list
			tris.addAll(cubeConstructor(v, sideLength, color));
		}
		return tris;
	}
	
	/**
	 * Creates a list of vertexes where cube pixels will be constructed in a shape based on 
	 * the CPPN
	 * 
	 * @param cppn network used to modify shape being constructed
	 * @param imageWidth width of screen
	 * @param imageHeight height of screen
	 * @param cubeSize size of cube
	 * 
	 * TODO: explain parameters
	 * 
	 * @param shapeWidth width of shape being constructed 
	 * @param shapeHeight height of shape being constructed
	 * @param shapeDepth depth of shape being constructed
	 * @param inputMultipliers determines whether inputs are turned on or off
	 * @return List of vertexes denoting center points of all cubes being constructed
	 */
	public static List<Vertex> getVertexesFromCPPN(Network cppn, int imageWidth, int imageHeight, int cubeSize, int shapeWidth, int shapeHeight, int shapeDepth, double[] inputMultipliers) {
		List<Vertex> result = new ArrayList<>();
		for(int x = 0; x < shapeWidth; x++) {
			for(int y = 0; y < shapeHeight; y++) {
				for(int z = 0; z < shapeDepth; z++) {
					double[] inputs = get3DObjectCPPNInputs(x, y, z, shapeWidth, shapeHeight, shapeDepth);
					//determine whether inputs are turned on or off
					for(int i = 0; i < inputMultipliers.length; i++) {
						inputs[i] = inputs[i] * inputMultipliers[i];
					}	
					double[] output = cppn.process(inputs);
					if(output[0] > 0.1) {
						double actualX = -(cubeSize*shapeWidth/2.0) + (cubeSize/2.0) + x*cubeSize;
						double actualY = -(cubeSize*shapeHeight/2.0) + (cubeSize/2.0) + y*cubeSize;
						double actualZ = -(cubeSize*shapeDepth/2.0) + (cubeSize/2.0) + z*cubeSize;
						result.add(new Vertex(actualX, actualY, actualZ));
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns CPPN inputs for 3D object construction
	 * 
	 * @param x Current voxel x-coordinate
	 * @param y Current voxel y-coordinate
	 * @param z current voxel z-coordinate
	 * @param width width of shape
	 * @param height height of shape
	 * @param depth depth of shape
	 * @return inputs to CPPN (x, y, z coordinates of a Vertex and bias)
	 */
	public static double[] get3DObjectCPPNInputs(int x, int y, int z, int width, int height, int depth) {
		Vertex v = new Vertex(x, y, z);
		Vertex newV = centerAndScale(v, width, height, depth);
		return new double[]{newV.x, newV.y, newV.z, GraphicsUtil.BIAS};
	}
	
	/**
	 * Method that centers and scales a vertex based on the width, height, and depth of the shape
	 * 
	 * @param toScale Vertex to be scaled
	 * @param width width of shape
	 * @param height height of shape
	 * @param depth depth of shape
	 * @return scaled vertex
	 */
	// TODO: Move to Cartesian util
	public static Vertex centerAndScale(Vertex toScale, int width, int height, int depth) {
		double newX = CartesianGeometricUtilities.centerAndScale(toScale.x, width); //scaled x coordinate
		double newY = CartesianGeometricUtilities.centerAndScale(toScale.y, height); //scaled y coordinate
		double newZ = CartesianGeometricUtilities.centerAndScale(toScale.z, depth); //scaled z coordinate
		assert !Double.isNaN(newX) : "newX is NaN! width="+width+", height="+height+", toScale="+toScale;
		assert !Double.isNaN(newY) : "newY is NaN! width="+width+", height="+height+", toScale="+toScale;
		assert !Double.isNaN(newZ) : "newZ is NaN! width="+width+", height="+height+", toScale="+toScale;
		return new Vertex(newX, newY, newZ);
	}
	/**
	 * Method that takes in a color, a vertex and a sidelength of a desired cube
	 * and returns a list of triangles that can be used to construct the cube.
	 * 
	 * @param center vertex representing center of line
	 * @param sideLength length of one side of the cube
	 * @param color desired color of the cube
	 * @return list of triangles used to construct cube
	 */
	public static List<Triangle> cubeConstructor(Vertex center, double sideLength, Color color) {
		double halfLength = (sideLength/2);
		
		List<Triangle> tris = new ArrayList<>();
		tris.add(new Triangle(center.add(new Vertex(halfLength, halfLength, halfLength)),
				center.add(new Vertex(halfLength, halfLength, -halfLength)),
				center.add(new Vertex(-halfLength, halfLength, -halfLength)),
				color));
		tris.add(new Triangle(center.add(new Vertex(-halfLength, halfLength, halfLength)),
				center.add(new Vertex(halfLength, halfLength, halfLength)),
				center.add(new Vertex(-halfLength, halfLength, -halfLength)),
				color));

		tris.add(new Triangle(center.add(new Vertex(-halfLength, halfLength, halfLength)),
				center.add(new Vertex(-halfLength, -halfLength, halfLength)),
				center.add(new Vertex(halfLength, halfLength, halfLength)),
				color));
		tris.add(new Triangle(center.add(new Vertex(halfLength, halfLength, halfLength)),
				center.add(new Vertex(-halfLength, -halfLength, halfLength)),
				center.add(new Vertex(halfLength, -halfLength, halfLength)),
				color));

		tris.add(new Triangle(center.add(new Vertex(halfLength, halfLength, halfLength)),
				center.add(new Vertex(halfLength, halfLength, -halfLength)),
				center.add(new Vertex(halfLength, -halfLength, halfLength)),
				color));
		tris.add(new Triangle(center.add(new Vertex(halfLength, -halfLength, -halfLength)),
				center.add(new Vertex(halfLength, -halfLength, halfLength)),
				center.add(new Vertex(halfLength, halfLength, -halfLength)),
				color));

		tris.add(new Triangle(center.add(new Vertex(-halfLength, -halfLength, halfLength)),
				center.add(new Vertex(-halfLength, -halfLength, -halfLength)),
				center.add(new Vertex(halfLength, -halfLength, halfLength)),
				color));
		tris.add(new Triangle(center.add(new Vertex(halfLength, -halfLength, -halfLength)),
				center.add(new Vertex(halfLength, -halfLength, halfLength)),
				center.add(new Vertex(-halfLength, -halfLength, -halfLength)),
				color));

		tris.add(new Triangle(center.add(new Vertex(-halfLength, -halfLength, -halfLength)),
				center.add(new Vertex(-halfLength, halfLength, -halfLength)),
				center.add(new Vertex(halfLength, -halfLength, -halfLength)),
				color));
		tris.add(new Triangle(center.add(new Vertex(halfLength, halfLength, -halfLength)),
				center.add(new Vertex(halfLength, -halfLength, -halfLength)),
				center.add(new Vertex(-halfLength, halfLength, -halfLength)),
				color));

		tris.add(new Triangle(center.add(new Vertex(-halfLength, -halfLength, -halfLength)),
				center.add(new Vertex(-halfLength, halfLength, -halfLength)),
				center.add(new Vertex(-halfLength, -halfLength, halfLength)),
				color));
		tris.add(new Triangle(center.add(new Vertex(-halfLength, halfLength, halfLength)),
				center.add(new Vertex(-halfLength, -halfLength, halfLength)),
				center.add(new Vertex(-halfLength, halfLength, -halfLength)),
				color));
		return tris;
	}
	
	/**
	 * Produces an array of images that are meant to animate a 3-dimensional object
	 * rotating. This is done by repeatedly calling getImage() at different input 
	 * headings and pitches.
	 * 
	 * @param tris array of triangles representing cube
	 * @param imageWidth width of image
	 * @param imageHeight height of image
	 * @param startTime beginning of animation
	 * @param endTime end of animation
	 * @return Array of BufferedImages that can be played as an animation of a 3D object
	 */
	// TODO: Give better name
	public static BufferedImage[] objectGenerator(List<Triangle> tris, int imageWidth, int imageHeight, int startTime, int endTime, double pitch) {
		BufferedImage[] images = new BufferedImage[(endTime-startTime)+1];
		for(int i = startTime; i <= endTime; i++) {
			// We're not clear why 4pi is used here instead of 2pi
			images[i-startTime] = getImage(tris, imageWidth, imageHeight, (i*4*Math.PI)/images.length, pitch);
		}
		return images;
	}
	
	/**
	 * Produces an array of images that are meant to animate a 3-dimensional object
	 * rotating based on an input CPPN. 
	 * 
	 * @param cppn input network
	 * @param imageWidth width of image
	 * @param imageHeight height of image
	 * @param sideLength size of cube (voxel)
	 * @param shapeWidth width of shape being generated
	 * @param shapeHeight height of shape being generated
	 * @param shapeDepth depth of shape being generated
	 * @param color desired color of object
	 * @param startTime start time of animation
	 * @param endTime end time of animation
	 * @param pitch input y-rotation of image
	 * @param inputMultipliers array determining whether to turn inputs on or off
	 * @return
	 */
	public static BufferedImage[] generate3DObjectFromCPPN(Network cppn, int imageWidth, int imageHeight, int sideLength, int shapeWidth, int shapeHeight, int shapeDepth, Color color, int startTime, int endTime, double pitch, double[] inputMultipliers) {
		List<Vertex> cubeVertexes = getVertexesFromCPPN(cppn, imageWidth, imageHeight, sideLength, shapeWidth, shapeHeight, shapeDepth, inputMultipliers);
		List<Triangle> tris = getShape(cubeVertexes, sideLength, color);
		BufferedImage[] resultImages = objectGenerator(tris, imageWidth, imageHeight, startTime, endTime, pitch);
		return resultImages;
	}
}

@SuppressWarnings("serial")
class Vertex extends Vector3d {
	public Vertex(double x, double y, double z) {
		super(x,y,z);
	}

	/**
	 * Copy constructor
	 * @param other
	 */
	public Vertex(Vertex other) {
		this(other.x, other.y, other.z);
	}

	public Vertex add(Vertex v) {
		Vertex newV = new Vertex(this);
		newV.add((Vector3d) v);
		return newV;
	}
}


class Triangle {
	Vertex v1;
	Vertex v2;
	Vertex v3;
	Color color;
	Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.color = color;
	}
}

class Matrix3 {
	double[] values;
	Matrix3(double[] values) {
		this.values = values;
	}
	Matrix3 multiply(Matrix3 other) {
		double[] result = new double[9];
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				for (int i = 0; i < 3; i++) {
					result[row * 3 + col] +=
							this.values[row * 3 + i] * other.values[i * 3 + col];
				}
			}
		}
		return new Matrix3(result);
	}
	Vertex transform(Vertex in) {
		return new Vertex(
				in.x * values[0] + in.y * values[3] + in.z * values[6],
				in.x * values[1] + in.y * values[4] + in.z * values[7],
				in.x * values[2] + in.y * values[5] + in.z * values[8]
				);
	}
}
