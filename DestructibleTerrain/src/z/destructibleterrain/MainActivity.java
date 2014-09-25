package z.destructibleterrain;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity {

	private World world;

	private Handler handler;

	private myView myView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		world = new World(new Vec2(0, 10f));
		myView = new myView(this, world);
		setContentView(myView);
		handler = new Handler();
		handler.post(update);

	}

	private Runnable update = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			world.step(1f / 60f, 10, 8);
			myView.invalidate();
			handler.postDelayed(update, 100 / 6);
		}
	};

}
