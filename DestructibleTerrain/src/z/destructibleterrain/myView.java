package z.destructibleterrain;

import java.util.ArrayList;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.View;

public class myView extends View {

	private PolyDefault terrainPolygon;
	private Bitmap bitmap;
	private ArrayList<Body> balls;
	private float heightRate;

	public myView(Context context, float heightRate) {
		super(context);
		this.heightRate = heightRate;
		setBackgroundColor(Color.WHITE);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		// 绘制测试小球
		drawBall(canvas);
		
		// 绘制地图
		// 注意：PolyDefault类是一个多边形的集合，它可能包含多个多边形
		for (int i = 0; i < getTerrainPolygon().getNumInnerPoly(); i++) {
			// 分别绘制各个多边形
			drawPolygon(getTerrainPolygon().getInnerPoly(i), canvas);

		}

	}

	private void drawBall(Canvas canvas) {
		// TODO Auto-generated method stub
		Paint mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.GREEN);
		for (int i = 0; i < balls.size(); i++) {

			canvas.save();
			canvas.drawCircle(balls.get(i).getPosition().x * Constants.RATE,
					balls.get(i).getPosition().y * Constants.RATE,
					20 * heightRate, mPaint);
			canvas.restore();
		}
	}

	private void drawPolygon(Poly poly, Canvas canvas) {
		// TODO Auto-generated method stub
		Path path = new Path();
		PointF point = new PointF((float)poly.getX(0), (float)poly.getY(0));
		path.moveTo(point.x, point.y);
		for (int i = 1; i < poly.getInnerPoly(0).getNumPoints(); i++) {
			point.x = (float) poly.getX(i);
			point.y = (float) poly.getY(i);
			path.lineTo(point.x, point.y);
		}
		path.close();
		// 如果当前多边形是空洞，涂为背景色
		if (poly.isHole()) {
			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawPath(path, paint);
		} else {
			canvas.save();
			canvas.clipPath(path);
			canvas.drawBitmap(bitmap, 0, 0, null);
			canvas.restore();
		}
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public PolyDefault getTerrainPolygon() {
		return terrainPolygon;
	}

	public void setTerrainPolygon(PolyDefault terrainPolygon) {
		this.terrainPolygon = terrainPolygon;
	}

	public ArrayList<Body> getBalls() {
		return balls;
	}

	public void setBalls(ArrayList<Body> balls) {
		this.balls = balls;
	}

}
