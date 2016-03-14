import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.util.*;

class Particle
{
	static final double GravG = 0.000000000066740831;//Gravitational constant
	
	double elasticity;
	double radius;
	double mass;
	
	Vec3 pos;
	Vec3 vel;
	Vec3 acc;
	Vec3 RGB;
	
	static Vec3 window;
	static LinkedList<Particle> part_list;
	ListIterator<Particle> partIterator;
	
	int draw_diameter;
	int draw_pos_x;
	int draw_pos_y;

	boolean remove;
	boolean bounces;
	Particle workingPart;
	double timestep;
	double working_dist;
	
	
	///------------------------------------------------------------------
	/// Construct the particle with all initial conditions as
	/// parameters to the function.
	///------------------------------------------------------------------ 
	Particle(LinkedList<Particle> model_part_list, Vec3 new_window,
				Vec3 new_pos, Vec3 new_vel, 
				double new_radius, double new_elasticity, double new_mass, boolean new_bounces,
				Vec3 new_RGB)
	{
		this.acc = new Vec3();
		this.vel = new Vec3(new_vel);
		this.pos = new Vec3(new_pos);
		this.RGB = new Vec3(new_RGB);
		
		if (window == null)
			window = new Vec3(new_window);
		if (part_list == null)
			part_list = model_part_list;
		
		radius = new_radius;
		elasticity = new_elasticity;
		
		mass = new_mass;
		bounces = new_bounces;
		remove = false;
	}
	
	
	///------------------------------------------------------------------
	/// Calculate acceleration from Gravity between the current Particle
	/// and the Working Particle. Adds this acceleration to the 
	/// acceleration vector of the current Particle.
	///------------------------------------------------------------------ 
	public void Gravity()
	{
		if (working_dist < (this.radius + workingPart.radius) * 0.1)
			return;
		double VectorG = ((GravG * workingPart.mass) / (working_dist*working_dist*working_dist));
		
		this.acc.addi(VectorG * (workingPart.pos.x - this.pos.x), VectorG * (workingPart.pos.y - this.pos.y));
	}
	
	
	
	public boolean updateAcc()
	{
		//Wall collision detection
		if (this.wallCollision())
			remove = true;
		
		if (!remove)
		{
			this.acc.clear();
			
			synchronized(part_list)
			{
				// Everything in the While Loop is complexity O(n^2), 
				// so optimize here first
				boolean is_collide = false;
				partIterator = part_list.listIterator();
				while(partIterator.hasNext())
				{
					workingPart = partIterator.next();
					if(workingPart != this)
					{
						//Find distance to particle
						findDistance();
					
						//Detect collisions while we are at it
						if (this.bounces && !workingPart.remove && (workingPart.pos.x > this.pos.x) && workingPart.bounces && (workingPart.mass > 1) && (this.mass > 1))
						{
							if (pressureCollision())
								is_collide = true;
						}
						if (!this.bounces || is_collide)
						{
							absorbCollision();
							is_collide = false;
						}
						
						//Apply gravity
						if (workingPart.mass >= 1)
							Gravity();
						
					}
				}
			}
		}
		return remove;
	}
	public void updateVel()
	{
		this.vel.x = this.vel.x + (this.acc.x * timestep);
		this.vel.y = this.vel.y + (this.acc.y * timestep);
	}
	public void updatePos()
	{
		this.pos.x = this.pos.x + (this.vel.x * timestep);
		this.pos.y = this.pos.y + (this.vel.y * timestep);
	}
	
	
	
	public void findDistance()
	{
		working_dist = Math.sqrt((workingPart.pos.x - this.pos.x)*(workingPart.pos.x - this.pos.x) +
							(workingPart.pos.y - this.pos.y)*(workingPart.pos.y - this.pos.y));
	}
	
	
	
	public boolean pressureCollision()
	{
		
		//Detect and resolve collisions
		double r = (workingPart.radius + this.radius);
										
		if (working_dist >= r)
			return false;
		
		//Find unit normal direction between particles
		Vec3 norm = new Vec3(workingPart.pos.x - this.pos.x, workingPart.pos.y - this.pos.y);
		Vec3 unit_norm = norm;
		if (norm.length() >= 0.1)
			unit_norm.divi(norm.length());
		else
			return true;
		
		double restitution = 1.0;
		//Calculate Relative velocity
		Vec3 rv = new Vec3(workingPart.vel.x - this.vel.x, workingPart.vel.y - this.vel.y);
		//Calculate Velocity in normal direction and return if negative for intuitive results
		double velAlongNorm = rv.DotProduct(unit_norm);
		if(velAlongNorm > 0)
			restitution = elasticity;
		
		//if (rv.length() > 0.001 && velAlongNorm > 0)
		//	vel_portion = 1- (velAlongNorm / rv.length());
		//System.out.println("vel_port: " + vel_portion);
		
		//Find overlap of particles
		double overlap = r - working_dist;
		//if (overlap > this.radius/2 && rv.length() < 1)
		//{
			//System.out.println("This is true");
		//	return;
		//}
		//overlap = Math.min(overlap,radius * 0.1);
		//Find minimum restitution for intuitive results
		//double e = Math.min(this.elasticity, workingPart.elasticity);
		
		double repulse = 10000000 * 1000;// * this.radius;
		double press_acc = restitution * repulse * overlap;//Math.log((overlap*10)+1)
		
		//System.out.println("press_acc: " + press_acc);
		
		//force vector
		Vec3 press_vel = unit_norm.mult(press_acc * timestep);
		//System.out.println("press.x: " + press_vec.x);
		//System.out.println("vel1: " + this.vel.x);
		workingPart.vel.addi_vec(press_vel.div(workingPart.mass));//workingPart.mass
		this.vel.addi_vec(press_vel.div(-this.mass)); //this.mass
		//System.out.println("vel2: " + this.vel.x);
		
		return true;
	}

	
	
	public void absorbCollision()
	{
		if ((workingPart.mass < 1) && (this.mass < 1))
			return;
		
		Particle larger;
		Particle smaller;
		if (this.mass >= workingPart.mass)
		{
			larger = this;
			smaller = workingPart;
		}
		else
		{
			larger = workingPart;
			smaller = this;
		}
										
		if (working_dist >= larger.radius - (smaller.radius/1.5))
			return;
		
		smaller.remove = true;
		double mass_add = smaller.mass + larger.mass;
		larger.vel.replace( ((larger.vel.x * larger.mass) + (smaller.vel.x * smaller.mass))/mass_add,
								((larger.vel.y * larger.mass) + (smaller.vel.y * smaller.mass))/mass_add);
		
		larger.radius = Math.cbrt((smaller.radius*smaller.radius*smaller.radius) + (larger.radius*larger.radius*larger.radius));
		//larger.radius = Math.sqrt((smaller.radius*smaller.radius) + (larger.radius*larger.radius));
		larger.RGB.replace( ((larger.RGB.x * larger.mass) + (smaller.RGB.x * smaller.mass))/mass_add,
							((larger.RGB.y * larger.mass) + (smaller.RGB.y * smaller.mass))/mass_add,
							((larger.RGB.z * larger.mass) + (smaller.RGB.z * smaller.mass))/mass_add );
							
		larger.pos.replace( ((larger.pos.x * larger.mass) + (smaller.pos.x * smaller.mass))/mass_add,
							((larger.pos.y * larger.mass) + (smaller.pos.y * smaller.mass))/mass_add,
							((larger.pos.z * larger.mass) + (smaller.pos.z * smaller.mass))/mass_add );
							
		larger.mass += smaller.mass;
	}
	
	
	
	public boolean wallCollision()
	{
		if (pos.y > (window.y + (radius*1.1)))
		{
			return true;
		}
		
		if (pos.y < -(radius*1.1))
		{
			return true;
		}
		
		if (pos.x > (window.x + (radius*1.1)))
		{
			return true;
		}
		
		if (pos.x < -radius*1.1)
		{
			return true;
		}
		return false;
	}

	
	
	public void draw(Graphics2D g2)
	{	
		g2.setColor(new Color((int)RGB.x, (int)RGB.y, (int)RGB.z));
		double late_const = 1.0;
		double draw_radius = radius;
		if (draw_radius >= 6 && !bounces)
		{
			draw_diameter = (int)Math.round(draw_radius *2.2);
			draw_pos_x = (int)Math.round(this.pos.x - draw_radius * 1.1);
			draw_pos_y = (int)Math.round(this.pos.y - draw_radius * 1.1);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
			g2.fillOval(draw_pos_x, draw_pos_y, draw_diameter, draw_diameter);
		
		
			draw_diameter = (int)Math.round(draw_radius *2);
			draw_pos_x = (int)Math.round(this.pos.x - draw_radius );
			draw_pos_y = (int)Math.round(this.pos.y - draw_radius );
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g2.fillOval(draw_pos_x, draw_pos_y, draw_diameter, draw_diameter);

		
			draw_diameter = (int)Math.round(draw_radius *.9*2);
			draw_pos_x = (int)Math.round(this.pos.x - draw_radius*.9 );
			draw_pos_y = (int)Math.round(this.pos.y - draw_radius*.9 );
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
			g2.fillOval(draw_pos_x, draw_pos_y, draw_diameter, draw_diameter);
			
			late_const = 0.80;
		}
		
		draw_diameter = (int)Math.round(draw_radius *late_const*2);
		draw_pos_x = (int)Math.round(this.pos.x - draw_radius*late_const );
		draw_pos_y = (int)Math.round(this.pos.y - draw_radius*late_const );
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		g2.fillOval(draw_pos_x, draw_pos_y, draw_diameter, draw_diameter);
		
		
		
		
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