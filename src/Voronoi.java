import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class Voronoi {

	ArrayList<Point> points;
	int numPoints;
	ArrayList<Triangle> T = new ArrayList<>();
	int numT = 0;

	public Voronoi() throws FileNotFoundException {
		// TODO Auto-generated constructor stub
		points = new ArrayList<>();
		// take input points

		Scanner input = new Scanner(new File("input.txt"));
		numPoints = input.nextInt();
		for (int i =0;i<numPoints;i++){
			points.add(new Point(input.nextDouble(), input.nextDouble()));
		}
		input.close();

		DelaunayTriangulation();
		MakeDual();
		
	}
	
	double ccw(Point p1, Point p2, Point p3) {
		return ((p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x));
	}
	
	double distance(Point p1, Point p2) {
		return Math.sqrt((p2.x-p1.x)*(p2.x-p1.x) + (p2.y-p1.y)*(p2.y-p1.y));
	}
	
	boolean pointOnEdge(Point p1, Point p2, Point p3) {
		if (distance(p1,p3) + distance(p3,p2) - distance(p1,p2) <= 0.0001) return true;
		return false;
	}
	
	boolean pointEquals(Point p1, Point p2) {
		return (p1.x == p2.x && p1.y == p2.y);
	}
	
	private Point findNeighbor(Point p1, Point p2, Point other) {
		for (int i=0;i<T.size();i++) {
			Triangle t = T.get(i);
			if (pointEquals(p1, t.a) && pointEquals(p2, t.b) && !pointEquals(other, t.c)) return t.c;
			else if (pointEquals(p1, t.a) && pointEquals(p2, t.c) && !pointEquals(other, t.b)) return t.b;
			else if (pointEquals(p1, t.b) && pointEquals(p2, t.a) && !pointEquals(other, t.c)) return t.c;
			else if (pointEquals(p1, t.b) && pointEquals(p2, t.c) && !pointEquals(other, t.a)) return t.a;
			else if (pointEquals(p1, t.c) && pointEquals(p2, t.a) && !pointEquals(other, t.b)) return t.b;
			else if (pointEquals(p1, t.c) && pointEquals(p2, t.b) && !pointEquals(other, t.a)) return t.a;	
		}
		return null;
	}
	
	private Triangle findNeighborTriangle(Point p1, Point p2, Point other) {
		for (int i=0;i<T.size();i++) {
			Triangle t = T.get(i);
			if ((pointEquals(p1, t.a) && pointEquals(p2, t.b) && !pointEquals(other, t.c)) || 
					(pointEquals(p1, t.a) && pointEquals(p2, t.c) && !pointEquals(other, t.b)) || 
					(pointEquals(p1, t.b) && pointEquals(p2, t.a) && !pointEquals(other, t.c)) || 
					(pointEquals(p1, t.b) && pointEquals(p2, t.c) && !pointEquals(other, t.a)) || 
					(pointEquals(p1, t.c) && pointEquals(p2, t.a) && !pointEquals(other, t.b)) || 
					(pointEquals(p1, t.c) && pointEquals(p2, t.b) && !pointEquals(other, t.a))) return t;	
		}
		return null;
	}
	
	private Triangle findTriangle(Point p1, Point p2, Point p3) {
		for (int i=0;i<T.size();i++) {
			Triangle t = T.get(i);
			if ((pointEquals(p1, t.a) && pointEquals(p2, t.b) && pointEquals(p3, t.c)) ||
				(pointEquals(p1, t.a) && pointEquals(p2, t.c) && !pointEquals(p3, t.b)) || 
				(pointEquals(p1, t.b) && pointEquals(p2, t.a) && !pointEquals(p3, t.c)) ||
				(pointEquals(p1, t.b) && pointEquals(p2, t.c) && !pointEquals(p3, t.a)) ||
				(pointEquals(p1, t.c) && pointEquals(p2, t.a) && !pointEquals(p3, t.b)) ||
				(pointEquals(p1, t.c) && pointEquals(p2, t.b) && !pointEquals(p3, t.a)))	
				return t;
				
		}
		return null;
	}
	
	private ArrayList<Triangle> DelaunayTriangulation() throws FileNotFoundException {	
		printPoints();
		System.out.println("--- Delaunay ---");
		// find p0


		Point p0 = points.get(0);
		for (int i=1;i<numPoints;i++) {
			Point p1 = points.get(i);
			if (p1.y > p0.y || (p1.y == p0.y && p1.x > p0.x)) p0 = p1;
		}
		//System.out.println(p0);
		// initial triangle
		Point p1 = new Point(-300, -300);
		Point p2 = new Point(300, -300);
		Point p3 = new Point(0, 300);
		Triangle t = new Triangle(p1,p2,p3);
		T.add(t);
		numT++;
		// create random permutation
		Collections.shuffle(points);
		// main loop
		//System.out.println(points.size());
		for (int i=0;i<numPoints;i++) {
			Point pr = points.get(i);
			System.out.println("Inserting " + pr + " ... ");
			// find triangle containing pr
			for (int j=0;j<T.size();j++) {
				// check if point inside triangle
				t = T.get(j);
				if (ccw(t.a, t.b, pr) > 0 && ccw(t.b,t.c,pr) > 0 && ccw(t.c,t.a,pr) > 0) {
					Triangle t1, t2, t3;
					t1 = new Triangle(t.a, t.b, pr);
					t2 = new Triangle(t.b, t.c, pr);
					t3 = new Triangle(t.c, t.a, pr);
					T.add(t1);
					T.add(t2);
					T.add(t3);			
					T.remove(t);
					numT+=2;
					LegalizeEdge(pr, t.a, t.b, t1);
					LegalizeEdge(pr, t.b, t.c, t2);
					LegalizeEdge(pr, t.c, t.a, t3);
					t = null;
					break;
				}
				// pr lies on some edge
				else {
					Point pl = null;
					Point pk = null;
					Point pi = null;
					Point pj = null;
					if (pointOnEdge(t.a, t.b, pr)) {
						pi = t.a;
						pj = t.b;
						pk = t.c;
					}
					else if (pointOnEdge(t.b, t.c, pr)) {
						pi = t.b;
						pj = t.c;
						pk = t.a;
					}
					else if (pointOnEdge(t.c, t.a, pr)) {
						pi = t.c;
						pj = t.a;
						pk = t.b;
					}
					if (pi!=null && pj!=null && pk!=null) {
						pl = findNeighbor(pi, pj, pk);
						Triangle t1, t2, t3, t4;
						t1 = new Triangle(pi, pr, pk);
						t2 = new Triangle(pj, pr, pk);
						t3 = new Triangle(pi, pr, pl);
						t4 = new Triangle(pj, pr, pl);
						T.add(t1);
						T.add(t2);
						T.add(t3);
						T.add(t4);
						T.remove(t);
						numT+=3;
						LegalizeEdge(pr, pi, pk, t1);
						LegalizeEdge(pr, pk, pj, t2);
						LegalizeEdge(pr, pi, pl, t3);
						LegalizeEdge(pr, pl, pj, t4);
						t = null;
						break;
					}		
				}
				
			}
			printTriangles(false);
			
		}
		
		printTriangles(true);
		return T;
	}
	
	private void LegalizeEdge(Point pr, Point pi, Point pj, Triangle t) {
		// check if edge pi,pj is illegal
		Point center = getCircumcenter(pr,pi,pj);
		double radius = distance(center,pr);
		Point pk = findNeighbor(pi, pj, pr);
		if (pk == null) return;
		// System.out.println(pk);
		// edge is illegal
		if (distance(center,pk) < radius) {
			T.remove(t);
			T.remove(findTriangle(pi, pj, pk));
			Triangle t1, t2;
			t1 = new Triangle(pj, pr, pk);
			t2 = new Triangle(pj, pk, pi);
			numT++;
			LegalizeEdge(pr, pi, pk, t1);
			LegalizeEdge(pr, pk, pj, t2);
		}
		
	}
	
	void MakeDual() throws FileNotFoundException {
		ArrayList<Point> VPoint = new ArrayList<>();
		for (int i=0;i<T.size();i++) {
			Triangle t = T.get(i);
			t.setCenter(getCircumcenter(t.a, t.b, t.c));
			VPoint.add(t.getCenter());
		}
		PrintWriter out2 = new  PrintWriter(new File("output2.txt"));
		for (int i=0;i<T.size();i++) {
			Triangle t = T.get(i);
			Triangle t1 = findNeighborTriangle(t.a, t.b, t.c);
			if (t1 == null) continue;
			out2.println(t.getCenter() + " " + t1.getCenter());
			t1 = findNeighborTriangle(t.b, t.c, t.a);
			if (t1 == null) continue;
			out2.println(t.getCenter() + " " + t1.getCenter());
			t1 = findNeighborTriangle(t.c, t.a, t.b);
			if (t1 == null) continue;
			out2.println(t.getCenter() + " " + t1.getCenter());
		}
		out2.close();
	}
	
	void printPoints() {
		System.out.println("--- Points ---");
		for (int i=0;i<numPoints;i++) {
			System.out.println(points.get(i));
		}
	}
	
	void printTriangles(boolean file) throws FileNotFoundException {
		if (file) {
			PrintWriter out = new PrintWriter(new File("output.txt"));
			out.println(T.size());
			for (int i=0;i<T.size();i++) {
				out.println(T.get(i));
			}
			out.close();
		}
		else {
			System.out.println("--- Triangles ---");
			for (int i=0;i<T.size();i++) {
				System.out.println(T.get(i));
			}
		}
		
	}
	
	public Point getCircumcenter(Point a, Point b, Point c) {
		// determine midpoints (average of x & y coordinates)
		Point midAB = Midpoint(a, b);
		Point midBC = Midpoint(b, c);
		// determine slope
		double slopeAB = -1 / Slope(a, b);
		double slopeBC = -1 / Slope(b, c);

		double bAB = midAB.y - slopeAB * midAB.x;
		double bBC = midBC.y - slopeBC * midBC.x;

		double x = (bAB - bBC) / (slopeBC - slopeAB);
		Point cc = new Point(x,	(slopeAB * x) + bAB);

		return cc;
	}
	
	public Point Midpoint(Point a, Point b) {
		return new Point(
			(a.x + b.x) / 2,
			(a.y + b.y) / 2
		);
	}
	
	public double Slope(Point from, Point to) {
		return (to.y - from.y) / (to.x - from.x);
	}


}
