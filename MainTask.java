import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainTask extends TimerTask
{
	Main main;
	MainTask(Main in_main)
	{
		this.main = in_main;
	}
	
	public void run()
	{
		long new_frame_time = System.nanoTime();
		
		int accuracy_multiple = this.main.game.accuracy_multiple;
		int secs_per_sec = this.main.game.secs_per_sec;
		double timestep = 1.0/accuracy_multiple;
		this.main.game.timestep = timestep;
		for (long i = 0; i < accuracy_multiple * secs_per_sec; i++)
			this.main.game.update();
	
		main.repaint(0);
		long lag_time = (long)((System.nanoTime() - new_frame_time) - 25000000);
		main.view.is_lag = (lag_time > 0);
	}
}