import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.awt.*;
import java.util.*;

class View extends JPanel{ 
	//Field field;
	Game game;
	boolean is_lag;
	Vec3 window;
	
	//ListIterator<Particle> partIterator;

	View(Game game, Vec3 window) throws IOException
	{
		this.game = game;
		//screen_x = new_screen_x;
		//screen_y = new_screen_y;
		this.window = window;
		is_lag = false;
		//vel_color = false;
	}

	public void paintComponent(Graphics g) 
	{
	
		g.setColor(new Color(32,32,32));
		g.fillRect(0,0,(int)window.x,(int)window.y);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		Particle workingPart;
		synchronized(this.game.field)
		{
			for (int i = 0; i < this.game.field.part_list.size(); i++)
			{
				workingPart = this.game.field.part_list.get(i);
				if (workingPart != null)
					workingPart.draw(g2,this.game.coloring);
			}
		}
		
		
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
		g.setColor(new Color(255,255,255));
		
		String output = "Speed: " + this.game.secs_per_sec;
		g.drawString(output, 2, 27);
		
		output = "Accuracy: " + this.game.accuracy_multiple;
		g.drawString(output, 2, 42);
		
		output = "Threads: " + this.game.field.calc_threads;
		g.drawString(output, 2, 57);
			
		if (is_lag)
			g.setColor(new Color(255, 132, 132));
		
		output = "Particles: " + this.game.field.part_list.size();
		g.drawString(output, 2, 12);
		
		if (this.game.show_center)
		{
			g.setColor(new Color(255,255,255));
			synchronized(this.game.field.mass_center)
			{
				g.drawLine((int)this.game.field.mass_center.x -5, (int)this.game.field.mass_center.y, 
							(int)this.game.field.mass_center.x +5, (int)this.game.field.mass_center.y);
				g.drawLine((int)this.game.field.mass_center.x, (int)this.game.field.mass_center.y - 5, 
							(int)this.game.field.mass_center.x, (int)this.game.field.mass_center.y + 5);
			}
		}
	}
}
