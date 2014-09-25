package z.destructibleterrain;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import ygame.extension.with_third_party.YIOnContactListener;
import ygame.framework.core.YABaseDomain;

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

public class myView extends View {
	// 屏幕与真实世界的比例 40px=1m
	private final static float RATE = 60f;

	private PolyDefault terrainPolygon;
	private PolyDefault explosionPolygon;
	private Bitmap bitmap;
	private World world;
	private BodyDef bodyDef;
	private FixtureDef fixtureDef;
	private Body ballBody;
	private EdgeShape shape;
	private Body body;

	public myView(Context context, World world) {
		super(context);
		this.world = world;
		// TODO Auto-generated constructor stub
		setTerrain();
		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.testpic);
		createTestBall(1000f / RATE, 10f / RATE, 20f / RATE);
	}

	private void createTestBall(float ballX, float ballY, float radius) {
		// TODO Auto-generated method stub
		CircleShape ballShape = new CircleShape();
		ballShape.setRadius(radius);
		FixtureDef def = new FixtureDef();
		def.restitution = 0.3f;
		def.friction = 0.2f;
		def.density = 1f;
		def.shape = ballShape;
		BodyDef ballDef = new BodyDef();
		ballDef.type = BodyType.DYNAMIC;
		ballDef.position.set(ballX, ballY);

		ballBody = world.createBody(ballDef);
		ballBody.createFixture(def);
		ballBody.setUserData("ball");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		explosionPolygon = createCircle(20, new Point((int) event.getX(),
				(int) event.getY()), 60);
		terrainPolygon = (PolyDefault) terrainPolygon
				.difference(explosionPolygon);
		// 先创建出新地图刚体，再把原地图刚体摧毁
		createEdgeBody(terrainPolygon);
		Body oriBody = findBody("oriTerrain");
		if (oriBody != null) {
			world.destroyBody(findBody("oriTerrain"));

		}
		return super.onTouchEvent(event);
	}

	private void setTerrain() {
		// TODO Auto-generated method stub
		terrainPolygon = new PolyDefault();
		terrainPolygon.add(500, 200);
		terrainPolygon.add(1500, 200);
		terrainPolygon.add(1500, 1200);
		terrainPolygon.add(500, 1200);
		//初始化地图刚体
		createEdgeBody(terrainPolygon);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		drawBall(canvas);
		for (int i = 0; i < terrainPolygon.getNumInnerPoly(); i++) {

			drawPolygon(terrainPolygon.getInnerPoly(i), canvas);

		}

		
	}

	private void drawBall(Canvas canvas) {
		// TODO Auto-generated method stub
		Paint mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.GREEN);
		canvas.save();
		canvas.drawCircle(ballBody.getPosition().x * RATE,
				ballBody.getPosition().y * RATE, 20, mPaint);
		canvas.restore();
	}

	private void createEdgeBody(Poly terrainPoly) {
		// TODO Auto-generated method stub
		Body oriBody = findBody("newTerrain");
		if (oriBody != null) {
			oriBody.setUserData("oriTerrain");

		}

		bodyDef = new BodyDef();
		bodyDef.position.set(0, 0);
		bodyDef.type = BodyType.STATIC;
		body = world.createBody(bodyDef);
		body.setUserData("newTerrain");

		fixtureDef = new FixtureDef();
		shape = new EdgeShape();
		for (int i = 0; i < terrainPoly.getNumInnerPoly(); i++) {

			int numPoints = terrainPoly.getInnerPoly(i).getNumPoints();
			createShortEdge(terrainPoly.getInnerPoly(i), numPoints - 1, 0);
			for (int j = 0; j < numPoints - 1; j++) {
				createShortEdge(terrainPoly.getInnerPoly(i), j, j + 1);

			}
		}
	}

	private Body findBody(String string) {
		// TODO Auto-generated method stub
		Body body = world.getBodyList();
		while (body != null) {
			if (body.getUserData() == string) {
				return body;
			}
			body = body.getNext();
		}
		return null;
	}

	private void createShortEdge(Poly innerPoly, int pointNum_1, int pointNum_2) {
		// TODO Auto-generated method stub
		Vec2 v1 = new Vec2((float) innerPoly.getX(pointNum_1) / RATE,
				(float) innerPoly.getY(pointNum_1) / RATE);
		Vec2 v2 = new Vec2((float) innerPoly.getX(pointNum_2) / RATE,
				(float) innerPoly.getY(pointNum_2) / RATE);

		shape.set(v1, v2);
		fixtureDef.shape = shape;
		body.createFixture(fixtureDef);
	}

	private void drawPolygon(Poly poly, Canvas canvas) {
		// TODO Auto-generated method stub
		Path path = new Path();
		Point point = new Point((int) poly.getX(0), (int) poly.getY(0));
		path.moveTo(point.x, point.y);
		for (int i = 1; i < poly.getInnerPoly(0).getNumPoints(); i++) {
			point.x = (int) poly.getX(i);
			point.y = (int) poly.getY(i);
			path.lineTo(point.x, point.y);
		}
		path.close();
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

	private PolyDefault createCircle(int polyNum, Point centerPoint, int radius) {
		// TODO Auto-generated method stub
		double angle = 2 * Math.PI / polyNum;
		PolyDefault circlePolygon = new PolyDefault();
		for (int i = 0; i < polyNum; i++) {
			int x = (int) (centerPoint.x + radius * Math.cos(angle * i));
			int y = (int) (centerPoint.y + radius * Math.sin(angle * i));
			circlePolygon.add(x, y);
		}
		return circlePolygon;
	}
}
