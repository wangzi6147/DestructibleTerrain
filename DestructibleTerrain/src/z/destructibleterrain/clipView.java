package z.destructibleterrain;

import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

public class clipView extends View {
	
	private PolyDefault terrainPolygon;
	private PolyDefault explosionPolygon;
	private Canvas canvas;
	private Bitmap bitmap;

	public clipView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setTerrain();
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.testpic);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		explosionPolygon = createCircle(20, new Point((int)event.getX(), (int)event.getY()), 60);
		terrainPolygon = (PolyDefault) terrainPolygon.difference(explosionPolygon);
		
		invalidate();
		return super.onTouchEvent(event);
	}
	private void setTerrain() {
		// TODO Auto-generated method stub
		terrainPolygon = new PolyDefault();
		terrainPolygon.add(5,5);
		terrainPolygon.add(1005, 5);
		terrainPolygon.add(1005, 1005);
		terrainPolygon.add(5, 1005);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		this.canvas=canvas;
		
		for(int i = 0; i<terrainPolygon.getNumInnerPoly(); i++){
			
			drawPolygon(terrainPolygon.getInnerPoly(i));
		}
	}

	private void drawPolygon(Poly poly) {
		// TODO Auto-generated method stub
		Path path = new Path();
		Point point = new Point((int)poly.getX(0),(int)poly.getY(0));
		path.moveTo(point.x, point.y);
		for(int i = 1;i<poly.getInnerPoly(0).getNumPoints();i++){
			point.x = (int) poly.getX(i);
			point.y = (int) poly.getY(i);
			path.lineTo(point.x, point.y);
		}
		path.close();
		if(poly.isHole()){
			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawPath(path, paint);
		}else {
			canvas.save();
			canvas.clipPath(path);
			canvas.drawBitmap(bitmap, 0,0, null);
			canvas.restore();
		}
	}

	private PolyDefault createCircle(int polyNum, Point centerPoint, int radius) {
		// TODO Auto-generated method stub
		double angle = 2*Math.PI/polyNum;
		PolyDefault circlePolygon = new PolyDefault();
		for(int i = 0;i<polyNum;i++){
			int x = (int) (centerPoint.x+radius*Math.cos(angle*i));
			int y = (int) (centerPoint.y+radius*Math.sin(angle*i));
			circlePolygon.add(x,y);
		}
		return circlePolygon;
	}
}
