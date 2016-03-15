import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import javax.swing.Timer;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Game extends JFrame { //implements ActionListener
	Model model;
	View view;
	int screen_x;
	int screen_y;
	double fps_round;

	public Game() throws Exception 
	{
		//screen_x = 1920;
		//screen_y = 1017;
		//screen_x = 1366;
		//screen_y = 705;
		
		this.setTitle("Particle Sim");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		
		screen_x = this.getWidth() - 16; //getContentPane()
		screen_y = this.getHeight() - 39;
		
		this.model = new Model(screen_x, screen_y);
		view = new View(this.model, screen_x, screen_y); //, screen_x, screen_y
		
		MouseController mousecontroller = new MouseController(this.model);
		KeyController keycontroller = new KeyController(this.model);
		this.addMouseListener(mousecontroller);
		this.addKeyListener(keycontroller);
		this.getContentPane().add(view);
		view.addMouseListener(mousecontroller);
		view.addKeyListener(keycontroller);
		
		this.fps_round = 0.0;
		
		//new Timer(17, this).start(); // Indirectly calls actionPerformed at regular intervals
		DoSim();
	}
	
	public void DoSim()
	{	
		while (true)
			SimControl();
	}

	public void SimControl()
	{
		//Begin timer
		long new_frame_time = System.nanoTime();
		
		//double accuracy_multiple = 10;
		
		int accuracy_multiple = model.accuracy_multiple;
		int secs_per_sec = model.secs_per_sec;
		if (accuracy_multiple < 1)
		{
			System.out.println("Accuracy must be set to 1 or higher!");
			System.exit(0);
		}
		
		double timestep = 1.0/accuracy_multiple;
		this.model.timestep = timestep;
		//this.model.secs_per_sec = secs_per_sec;
		for (int i = 0; i < accuracy_multiple * secs_per_sec; i++)
			model.update();
		
		repaint(); // Indirectly calls View.paintComponent in its own thread (?)
		try{
		TimeUnit.MILLISECONDS.sleep(1);
		} catch (InterruptedException e){}
		
		
		//End Timer
		long wait_time =(long)(17000000 - (System.nanoTime() - new_frame_time));
		
		//This block is not measured by wait_time
		this.view.vel_color = this.model.vel_color;
		this.view.is_lag = (wait_time < 0);
		if (wait_time > 1000)
		{
			try{
			TimeUnit.NANOSECONDS.sleep(wait_time);
			} catch (InterruptedException e){}
		}
	}

	public static void main(String[] args) throws Exception {
		new Game();
	}
}
