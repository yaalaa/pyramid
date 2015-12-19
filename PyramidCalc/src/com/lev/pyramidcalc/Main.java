package com.lev.pyramidcalc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	static double svgScale = 354.33072 / 10;

	static class Point {
		double x;
		double y;
		
		Point() {
		}
		
		Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		public void set(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		public void set(Point pt) {
			if (this != pt) {
				x = pt.x;
				y = pt.y;
			}
		}
		
		public double norma() {
			return Math.sqrt(x *x + y * y);
		}
		
		public Point coef(double coef) {
			return new Point(x * coef, y * coef);
		}
		
		public Point normalize() {
			return coef(1 / norma());
		}
		
		public String dumpSvg() {
			return String.format("%.4f,%.4f", x * svgScale, y * svgScale);
		}
	}
	
	public static Point add(Point a, Point b) {
		return new Point(a.x + b.x, a.y + b.y);
	}
	
	public static Point sub(Point a, Point b) {
		return new Point(a.x - b.x, a.y - b.y);
	}
	
	public static double scalar(Point a, Point b) {
		return a.x * b.x + a.y * b.y;
	}
	
	public static Point proj(Point a, Point b, Point pt) {
		Point v = sub(b, a).normalize();
		
		return add(a, v.coef(scalar(v, sub(pt, a))));
	}
	
	public static double distance(Point a, Point b) {
		return sub(a, b).norma();
	}
	
	public static double diameter(List<Point> points) {
		double out = 0;
		
		if (points != null) {
			for (int idx = 0; idx < points.size(); idx++) {
				Point a = points.get(idx);
				
				for (int subIdx = idx + 1; subIdx < points.size(); subIdx++) {
					Point b = points.get(subIdx);
					
					double cur = distance(a, b);
					
					if (cur > out) {
						out = cur;
					}
				}
			}
		}
		
		return out;
	}
	
	static List<Point> allPointsOfPolygons(List<Polygon> polygons) {
		ArrayList<Point> out = new ArrayList<Point>();
		
		if (polygons != null) {
			for (Polygon item: polygons) {
				out.addAll(item.getPoints());
			}
		}
		
		return out;
	}
	
	static String dumpSvg(Polygon polygon) {
		StringBuilder out = new StringBuilder();
		
		do {
			if (polygon == null) {
				break;
			}
			
			List<Point> points = polygon.getPoints();
			
			if (points.size() < 2) {
				break;
			}
			
			out.append("<path d=\"M");
			
			for (Point pt: points) {
				out.append(" ");
				out.append(pt.dumpSvg());
			}
			
			out.append(" Z\" />");
		} while (false);
		
		return out.toString();
	}
	
	static String dumpSvg(List<Polygon> polygons) {
		StringBuilder out = new StringBuilder();
		
		if (polygons != null) {
			for (Polygon polygon: polygons) {
				String s = dumpSvg(polygon);
				
				if (s.length() > 0) {
					out.append(s);
					out.append("\n");
				}
			}
		}
		
		return out.toString();
	}
	
	static class Polygon {
		public final Point[] pt;

		public Polygon(int n) {
			assert(n > 2);
			
			pt = new Point[n];
			
			for (int idx = 0; idx < pt.length; idx++) {
				pt[idx] = new Point();
			}
		}
		
		public List<Point> getPoints() {
			return Arrays.asList(pt);
		}
		
		public static Polygon triangle()
		{
			return new Polygon(3);
		}
		
		public static Polygon quadrangle()
		{
			return new Polygon(4);
		}
		
		public Polygon mirrorBySide(int ptIdx) {
			Polygon out = new Polygon(pt.length);
			
			Point a = pt[ptIdx];
			Point b = pt[(ptIdx + 1) % pt.length];
			
			Point v = sub(b, a).normalize();
			
			out.pt[0] = b;
			out.pt[1] = a;
			
			for (int idx = 2; idx < pt.length; idx++) {
				Point cur = pt[(ptIdx - idx + 1 + pt.length) % pt.length];
				
				Point proj = add(a, v.coef(scalar(v, sub(cur, a))));
				
				out.pt[idx].set(sub(proj.coef(2), cur));
			}
			
			return out;
		}
		
		Polygon outerStripe(int ptIdx, double width) {
			assert(pt.length == 3);
			
			Polygon out = Polygon.quadrangle();
			
			Point a = pt[ptIdx];
			Point b = pt[(ptIdx + 1) % pt.length];
			Point c = pt[(ptIdx + 2) % pt.length];
			
			Point v = sub(b, a).normalize();
			
			Point proj = add(a, v.coef(scalar(v, sub(c, a))));
			
			Point vh = sub(c, proj);
			
			double h = vh.norma();
			
			Point c1 = sub(proj.coef(2), c);
			
			double coefProp = (h - width) / h;
			
			Point a1 = add(c1.coef(1 - coefProp), a.coef(coefProp));
			Point b1 = add(c1.coef(1- coefProp), b.coef(coefProp));
			
			out.pt[0] = b;
			out.pt[1] = a;
			out.pt[2] = a1;
			out.pt[3] = b1;
			
			return out;
		}
	}
	
	public static void main(String[] args) {
		
		// side length, cm
		final double a = 10;
		
		// stripe width, cm
		final double d = 1;
		
		// inscribed circle radius, cm
		final double r = a * Math.sqrt(3) / 6;
		
		// height, cm
		final double h = a * Math.sqrt(3) / 2;
		
		ArrayList<Polygon> triangles = new ArrayList<Polygon>();
		
		// main triangle
		Polygon main = Polygon.triangle();
		
		main.pt[0].set(a / 2, -r);
		main.pt[1].set(0, h - r);
		main.pt[2].set(-a / 2, -r);
		
		triangles.add(main);
		
		// bottom triangle
		Polygon bottom = main.mirrorBySide(2);
		
		triangles.add(bottom);
	
		// right triangle
		Polygon right = main.mirrorBySide(0);
		
		triangles.add(right);
		
		// left triangle
		Polygon left = main.mirrorBySide(1);
		
		triangles.add(left);
		
		// stripes
		ArrayList<Polygon> stripes = new ArrayList<Polygon>();
		
		// left stripe
		Polygon leftStripe = bottom.outerStripe(1, d);
		
		stripes.add(leftStripe);
		
		// right stripe
		Polygon rightStripe = bottom.outerStripe(2, d);
		
		stripes.add(rightStripe);
		
		// bottom left stripe
		Polygon bottomLeftStripe = left.outerStripe(1, d);
		
		stripes.add(bottomLeftStripe);
		
		// all polygons
		ArrayList<Polygon> polygons = new ArrayList<Polygon>();
		
		polygons.addAll(triangles);
		polygons.addAll(stripes);
		
		// some parameters
		System.out.println("diameter=" + diameter(allPointsOfPolygons(polygons)));
		
		// dump SVG
		System.out.println("svg:\n" + dumpSvg(polygons));
	}
	
	
}
