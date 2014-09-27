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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity {

	private World world;

	private Handler handler;

	private myView myView;

	private Bitmap bitmap;

	private PolyDefault terrainPolygon;

	private BodyDef bodyDef;

	private Body body;

	private FixtureDef fixtureDef;

	private EdgeShape shape;

	private ArrayList<Body> balls = new ArrayList<Body>();

	private PolyDefault explosionPolygon;
	// 小球之间间隔(单位为1/60秒)
	protected int timeInterBalls = 100;
	// 实际屏幕高/1440(测试机屏幕高)
	private float heightRate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 去标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 分辨率适配
		Display display = getWindowManager().getDefaultDisplay();
		heightRate = (float) display.getHeight() / 1440f;

		world = new World(new Vec2(0, 10f));
		myView = new myView(this, heightRate);
		//关闭硬件加速
		myView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		// 地图图片
		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.testpic);
		myView.setBitmap(bitmap);
		// 自定义地图边缘
		setTerrain();

		setContentView(myView);
		
		//消息队列，用来模拟物理世界迭代以及刷新视图
		handler = new Handler();
		handler.post(update);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 模拟爆炸多边形
		explosionPolygon = createCircle(20, new Point((int) event.getX(),
				(int) event.getY()), 60 * heightRate);
		// 多边形运算：模拟破坏地图效果
		// 注意：PolyDefault类是一个多边形的集合，它可能包含多个多边形
		terrainPolygon = (PolyDefault) terrainPolygon
				.difference(explosionPolygon);
		// 先创建出新地图刚体，再把原地图刚体摧毁
		createEdgeBody(terrainPolygon);
		Body oriBody = findBody("oriTerrain");
		if (oriBody != null) {
			world.destroyBody(oriBody);
		}
		return super.onTouchEvent(event);
	}

	private PolyDefault createCircle(int polyNum, Point centerPoint,
			float radius) {
		// TODO Auto-generated method stub
		double angle = 2 * Math.PI / polyNum;
		PolyDefault circlePolygon = new PolyDefault();
		for (int i = 0; i < polyNum; i++) {
			float x = (float) (centerPoint.x + radius * Math.cos(angle * i));
			float y = (float) (centerPoint.y + radius * Math.sin(angle * i));
			circlePolygon.add(x, y);
		}
		return circlePolygon;
	}

	public Body createTestBall(float ballX, float ballY, float radius) {
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

		Body ballBody = world.createBody(ballDef);
		ballBody.createFixture(def);
		ballBody.setUserData("ball");

		return ballBody;
	}

	private void setTerrain() {
		// TODO Auto-generated method stub
		terrainPolygon = new PolyDefault();
		terrainPolygon.add(500f * heightRate, 200f * heightRate);
		terrainPolygon.add(1000f * heightRate, 300f * heightRate);
		terrainPolygon.add(1500f * heightRate, 200f * heightRate);
		terrainPolygon.add(1500f * heightRate, 1200f * heightRate);
		terrainPolygon.add(500f * heightRate, 1200f * heightRate);
		// 初始化地图刚体
		createEdgeBody(terrainPolygon);
	}

	private void createEdgeBody(Poly terrainPoly) {
		// TODO Auto-generated method stub

		// 刷新刚体时，需要把原刚体信息更新
		Body oriBody = findBody("newTerrain");
		if (oriBody != null) {
			oriBody.setUserData("oriTerrain");
		}

		// 新建地图边缘刚体
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
				// 创建小段刚体
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
		Vec2 v1 = new Vec2((float) innerPoly.getX(pointNum_1) / Constants.RATE,
				(float) innerPoly.getY(pointNum_1) / Constants.RATE);
		Vec2 v2 = new Vec2((float) innerPoly.getX(pointNum_2) / Constants.RATE,
				(float) innerPoly.getY(pointNum_2) / Constants.RATE);

		shape.set(v1, v2);
		fixtureDef.shape = shape;
		body.createFixture(fixtureDef);
	}

	private Runnable update = new Runnable() {

		@Override
		public void run() {
			// 测试小球
			if (timeInterBalls == 100) {
				balls.add(createTestBall(1000f * heightRate / Constants.RATE,
						20f * heightRate / Constants.RATE, 20f * heightRate
								/ Constants.RATE));
				timeInterBalls = 0;
			}
			timeInterBalls++;
			// 设置Box2D世界迭代
			world.step(1f / 60f, 10, 8);
			// 传递绘制信息
			myView.setBalls(balls);
			myView.setTerrainPolygon(terrainPolygon);
			myView.invalidate();
			
			handler.postDelayed(update, (long) (1000f/60f));
		}
	};

}
