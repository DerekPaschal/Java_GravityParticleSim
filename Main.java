import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import javax.swing.Timer;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main extends JFrame {
	Game game;
	View view;
	Field field;
	MouseController mousecontroller;
	KeyController keycontroller;
	int CoreCount;
	
	Vec3 window;

	public Main() throws Exception 
	{
		this.CoreCount = Runtime.getRuntime().availableProcessors();
		
		this.setTitle("Physics Game");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		
		//this.screen_x = this.getWidth();
		//this.screen_y = this.getHeight();
		this.window = new Vec3(this.getWidth(), this.getHeight(), Math.min(this.getWidth(),this.getHeight()));
		
		this.field = new Field(this.window,this.CoreCount);
		this.game = new Game(this.field, this.window);
		this.view = new View(this.game, this.window);
		
		this.mousecontroller = new MouseController(this.game);
		this.keycontroller = new KeyController(this.game);
		
		this.addMouseListener(this.mousecontroller);
		this.addKeyListener(this.keycontroller);
		
		this.getContentPane().add(this.view);
		this.view.addMouseListener(this.mousecontroller);
		this.view.addKeyListener(this.keycontroller);
		
		SimControl();
	}

	public void SimControl()
	{
		while (true)
		{
			//Begin timer
			long new_frame_time = System.nanoTime();
			
			int accuracy_multiple = this.game.accuracy_multiple;
			int secs_per_sec = this.game.secs_per_sec;
			//if (accuracy_multiple <= 0)
			//{
			//	System.out.println("Accuracy must be set to 0 or higher!");
			//	System.exit(0);
			//}
			
			double timestep = 1.0/accuracy_multiple;
			this.game.timestep = timestep;
			for (long i = 0; i < accuracy_multiple * secs_per_sec; i++)
				this.game.update();
			
			repaint(); // Indirectly calls View.paintComponent in its own thread (?), will paint whenever it feels like it
			try{
			TimeUnit.MILLISECONDS.sleep(1);
			} catch (InterruptedException e){}
			
			
			//End Timer
			long wait_time =(long)(17000000 - (System.nanoTime() - new_frame_time));//17000000
			
			//This block is not measured by wait_time
			this.view.is_lag = (wait_time < 0);
			if (wait_time > 1000)
			{
				try{
				TimeUnit.NANOSECONDS.sleep(wait_time-100);
				} catch (InterruptedException e){}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Main();
	}
}
