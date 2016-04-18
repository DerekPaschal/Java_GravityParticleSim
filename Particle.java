import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.util.*;

class Particle
{	
	double radius;
	double mass;
	
	Vec3 pos;
	Vec3 vel;
	Vec3 acc;
	Vec3 RGB;
	
	boolean bounces;
	double elasticity;
	double repulse;
	boolean remove;
	
	///------------------------------------------------------------------
	/// Construct the particle with all initial conditions as
	/// parameters to the function.
	///------------------------------------------------------------------ 
	Particle(Vec3 new_pos, Vec3 new_vel, double new_radius, double new_mass,
			double new_elasticity, double new_repulse, boolean new_bounces, Vec3 new_RGB)
	{
		this.acc = new Vec3();
		this.vel = new Vec3(new_vel);
		this.pos = new Vec3(new_pos);
		this.RGB = new Vec3(new_RGB);
		this.radius = new_radius;
		this.mass = new_mass;
		this.elasticity = new_elasticity;
		this.repulse = new_repulse;
		this.bounces = new_bounces;
		this.remove = false;
	}
	
	
	
	public void draw(Graphics2D g2, int coloring)
	{	
		int draw_diameter;
		//int draw_pos_x;
		//int draw_pos_y;
		
		//this.RGB = new Vec3(Math.min(255, this.RGB.x), Math.min(255, this.RGB.y), Math.min(255, this.RGB.z));
		
		//Velocity coloring
		if (coloring == 1)
		{
			double speed = this.vel.length();
			g2.setColor(new Color((int)Math.min(speed*132, 255), (int)64, (int)Math.max(255 - speed*132, 0)));
		}
		
		//Default coloring
		else
			g2.setColor(new Color((int)RGB.x, (int)RGB.y, (int)RGB.z));
		
		
		double late_const = 1.0;
		
		if (this.radius >= 6 && !bounces)
		{
			draw_diameter = (int)(this.radius *1.1 * 2);
			//draw_pos_x = (int)(this.pos.x - this.radius * 1.1);
			//draw_pos_y = (int)(this.pos.y - this.radius * 1.1);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
			g2.fillOval((int)(this.pos.x - this.radius * 1.1), (int)(this.pos.y - this.radius * 1.1), draw_diameter, draw_diameter);
		
		
			draw_diameter = (int)(this.radius *2);
			//draw_pos_x = (int)(this.pos.x - this.radius );
			//draw_pos_y = (int)(this.pos.y - this.radius );
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g2.fillOval((int)(this.pos.x - this.radius ), (int)(this.pos.y - this.radius ), draw_diameter, draw_diameter);

		
			draw_diameter = (int)(this.radius *.9*2);
			//draw_pos_x = (int)(this.pos.x - this.radius*.9 );
			//draw_pos_y = (int)(this.pos.y - this.radius*.9 );
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
			g2.fillOval((int)(this.pos.x - this.radius*.9 ), (int)(this.pos.y - this.radius*.9 ), draw_diameter, draw_diameter);
			
			late_const = 0.80;
		}
		
		
		draw_diameter = (int)(this.radius *late_const*2);
		//draw_pos_x = (int)(this.pos.x - this.radius*late_const );
		//draw_pos_y = (int)(this.pos.y - this.radius*late_const );
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g2.fillOval((int)(this.pos.x - this.radius*late_const), (int)(this.pos.y - this.radius*late_const), draw_diameter, draw_diameter);
		
		
		
		
	}
	
	
	
	
		/*
	public void bounceCollision()
	{
		if ((workingPart.mass < 1) && (this.mass < 1))
			return;
		
		//Detect and resolve collisions
		double r = (workingPart.radius + this.radius);
										
		if (working_dist >= r)
			return;
		
		boolean has_mass = (this.mass >= 1);
		
		//Calculate Relative velocity
		Vec3 rv = new Vec3(workingPart.vel.x - this.vel.x, workingPart.vel.y - this.vel.y);
		
		//Find unit normal direction between particles
		Vec3 norm = new Vec3(workingPart.pos.x - this.pos.x, workingPart.pos.y - this.pos.y);
		Vec3 unit_norm = norm;
		if (norm.length() >= 0.01)
			unit_norm.divi(norm.length());
		
		//Calculate Velocity in normal direction and return if negative for intuitive results
		double velAlongNorm = rv.DotProduct(unit_norm);
		if(velAlongNorm > 0)
			return;
		
		//Find minimum restitution for intuitive results
		double e = Math.min(this.elasticity, workingPart.elasticity);
		
		//Find unit tangent vector between particles
		Vec3 unit_tan = new Vec3(-unit_norm.y, unit_norm.x);
		
		//Find velocity componants of particles projected on norm and tan 
		double norm1 = unit_norm.DotProduct(this.vel);
		double tan1_prime = unit_tan.DotProduct(this.vel);
		double norm2 = unit_norm.DotProduct(workingPart.vel);
		double tan2_prime = unit_tan.DotProduct(workingPart.vel);
		
		//Find new velocity componants on norm and tan
		double norm1_prime = (norm1 *(this.mass - workingPart.mass) + 2*workingPart.mass*norm2)/(this.mass + workingPart.mass);
		double norm2_prime = (norm2*(workingPart.mass - this.mass) + 2*this.mass*norm1)/(this.mass + workingPart.mass);
		
		//Assign values for norm and tan back to vectors along unit_norm
		Vec3 norm1_vec = unit_norm.mult(norm1_prime * e);
		Vec3 tan1_vec = unit_tan.mult(tan1_prime * e);
		Vec3 norm2_vec = unit_norm.mult(norm2_prime * e);
		Vec3 tan2_vec = unit_tan.mult(tan2_prime * e);
		
		//replace old particle velocity vectors
		if (workingPart.bounces)
			this.vel.replace(norm1_vec.x + tan1_vec.x, norm1_vec.y + tan1_vec.y);
		if (has_mass && workingPart.bounces)
			workingPart.vel.replace(norm2_vec.x + tan2_vec.x, norm2_vec.y + tan2_vec.y);
	}
	*/
	
	/*
	public void positionCorrection(Vec3 unit_norm)
	{
			
		//Use distance to find the seperation that the particles need to be seperated to be not colliding
		double pen_depth = (this.radius + workingPart.radius) - working_dist;
		
		//How much in each direction both particles needs to move to be out of eachother
		Vec3 move_on_norm = unit_norm.mult(pen_depth);
		
		
		//Orient and apply x correction
		if (this.pos.x < workingPart.pos.x)
		{
			//this.pos.x = this.pos.x - ((move_on_norm.x * this.mass)/(this.mass * workingPart.mass));
			//workingPart.pos.x = workingPart.pos.x + ((move_on_norm.x * workingPart.mass) / (this.mass * workingPart.mass));
			this.pos.x = this.pos.x - (move_on_norm.x/2);
			workingPart.pos.x = workingPart.pos.x + (move_on_norm.x/2);
		}
		else
		{
			//this.pos.x = this.pos.x + ((move_on_norm.x * this.mass)/(this.mass * workingPart.mass));
			//workingPart.pos.x = workingPart.pos.x - ((move_on_norm.x * workingPart.mass) / (this.mass * workingPart.mass));
			this.pos.x = this.pos.x + (move_on_norm.x/2);
			workingPart.pos.x = workingPart.pos.x - (move_on_norm.x/2);
		}
		
		//Orient and apply y correction
		if(this.pos.y < workingPart.pos.y)
		{
			//this.pos.y = this.pos.y - ((move_on_norm.y * this.mass) / (this.mass * workingPart.mass));
			//workingPart.pos.y = workingPart.pos.y + ((move_on_norm.y * workingPart.mass) / (this.mass * workingPart.mass));
			this.pos.y = this.pos.y - (move_on_norm.x/2);
			workingPart.pos.y = workingPart.pos.y + (move_on_norm.x/2);
		}
		else
		{
			//this.pos.y = this.pos.y + ((move_on_norm.y * this.mass) / (this.mass * workingPart.mass));
			//workingPart.pos.y = workingPart.pos.y - ((move_on_norm.y * workingPart.mass) / (this.mass * workingPart.mass));
			this.pos.y = this.pos.y + (move_on_norm.x/2);
			workingPart.pos.y = workingPart.pos.y - (move_on_norm.x/2);
		}
	}
	*/	
}