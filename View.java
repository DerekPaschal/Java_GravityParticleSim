//import javax.swing.JFrame;
//import java.awt.Color;
import javax.swing.JPanel;
//import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.IOException;
//import java.awt.Graphics;
import java.io.File;
import java.awt.*;
//import java.util.*;

class View extends JPanel{ 
	Model model;
	Particle workingPart;
	int screen_x, screen_y;
	boolean is_lag;
	//listIterator<Particle> partIterator;

	View(Model m, int new_screen_x, int new_screen_y) throws IOException //
	{
		this.model = m;
		screen_x = new_screen_x;
		screen_y = new_screen_y;
		is_lag = false;
	}

	public void paintComponent(Graphics g) 
	{
		g.setColor(new Color(32,32,32));
		g.fillRect(0,0,screen_x,screen_y);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//partIterator = model.m_part_list.listIterator();
		
		//while (partIterator.hasNext())
		for (int i = 0; i < this.model.m_part_list.size(); i++)
		{
			//Get the current Particle in the list
			workingPart = this.model.m_part_list.get(i);
			//workingPart = partIterator.next();
			workingPart.draw(g2);
		}
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
		
		if (!is_lag)
			g.setColor(new Color(255,255,255));
		else
			g.setColor(new Color(255, 64,64));
		String num_parts = "" + this.model.m_part_list.size();
		g.drawString(num_parts, 0,10);
		
		g.setColor(new Color(255,255,255));
		g.drawLine((int)this.model.mass_center.x -5, (int)this.model.mass_center.y, 
					(int)this.model.mass_center.x +5, (int)this.model.mass_center.y);
		g.drawLine((int)this.model.mass_center.x, (int)this.model.mass_center.y - 5, 
					(int)this.model.mass_center.x, (int)this.model.mass_center.y + 5);
		//g.drawLine((screen_x/2) - 5, screen_y/2, (screen_x/2) +5, screen_y/2);
		//g.drawLine((screen_x/2), (screen_y/2)-5, (screen_x/2), (screen_y/2) +5);
	}
}
