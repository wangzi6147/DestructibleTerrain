package z.destructibleterrain;

import java.util.ArrayList;

import com.seisw.util.geom.Clip;
import com.seisw.util.geom.Point2D;
import com.seisw.util.geom.PolyDefault;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

public class myView extends View {

	private ArrayList<ArrayList<Point>> terrainPolygons;
	private ArrayList<Point> explosionPlygons;
	private Canvas canvas;
	private ArrayList<ArrayList<Point>> resultPolygon;

	public myView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setPolygons();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		explosionPlygons = createCircle(20, new Point((int)event.getX(), (int)event.getY()), 30);
		//System.out.println(event.getX());
		//terrainPolygons.add(explosionPlygons);
		for(int i = 0;i<terrainPolygons.size();i++){
			resultPolygon = Clipper.clipPolygon(terrainPolygons.get(i), explosionPlygons);
			terrainPolygons.remove(i);
			for(int j = 0;j<resultPolygon.size();j++){
				terrainPolygons.add(resultPolygon.get(j));
			}
		}
		
		invalidate();
		return super.onTouchEvent(event);
	}
	private void setPolygons() {
		// TODO Auto-generated method stub

		terrainPolygons = new ArrayList<ArrayList<Point>>();
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				if(i!=5&&j!=5){
					
				ArrayList<Point> thePoly = new ArrayList<Point>();
				thePoly.add(new Point(i * 50 + 5, j * 50 + 5));
				thePoly.add(new Point(i * 50 + 55, j * 50 + 5));
				thePoly.add(new Point(i * 50 + 55, j * 50 + 55));
				thePoly.add(new Point(i * 50 + 5, j * 50 + 55));
				terrainPolygons.add(thePoly);
				}
			}
		}
		
	}

	private ArrayList<Point> createCircle(int polyNum, Point centerPoint, int radius) {
		// TODO Auto-generated method stub
		double angle = 2*Math.PI/polyNum;
		ArrayList<Point> circleArray = new ArrayList<Point>();
		for(int i = 0;i<polyNum;i++){
			Point polyPoint = new Point();
			polyPoint.x = (int) (centerPoint.x+radius*Math.cos(angle*i));
			polyPoint.y = (int) (centerPoint.y+radius*Math.sin(angle*i));
			circleArray.add(polyPoint);
		}
		return circleArray;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		this.canvas = canvas;
		for(int i = 0;i<terrainPolygons.size();i++){
			drawPolygon(terrainPolygons.get(i));
		}
	}

	private void drawPolygon(ArrayList<Point> polygon) {
		// TODO Auto-generated method stub
		Path path = new Path();
		Point point = polygon.get(0);
		path.moveTo(point.x, point.y);
		for(int i = 1;i<polygon.size();i++){
			point = polygon.get(i);
			path.lineTo(point.x, point.y);
		}
		path.close();
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawPath(path, paint);
	}


}
