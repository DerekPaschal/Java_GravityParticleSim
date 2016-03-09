//import java.util.Random;
//import java.util.LinkedList;
//import java.util.Iterator;
import java.util.*;
import java.io.IOException;

class Model
{	
	static final double GravG = 0.000000000066740831;//Gravitational constant

	Random m_rand;
	LinkedList<Particle> m_part_list;
	LinkedList<Particle> part_not_added;
	ListIterator<Particle> partIterator;
	
	Particle workingPart;
	Vec3 window;
	double timestep;
	double secs_per_sec;
	Vec3 new_part_pos;
	Vec3 new_drag_xy;
	Vec3 mass_center;
	Vec3 mass_center_temp;
	double total_mass;
	double total_mass_temp;
	
	boolean load_new;
	boolean need_to_clear;
	boolean cameraUp;
	boolean cameraDown;
	boolean cameraLeft;
	boolean cameraRight;
	
	boolean correct_pos;
	
	///State '1' is making central "suns" and clicking creates light orbiting bodys
	///State '2' creates a collapsing disk of light particles, which you shoot with heavier bodies
	///
	int state;
		
	Model(int new_screen_x, int new_screen_y) throws IOException
	{
		window = new Vec3(new_screen_x, new_screen_y);
		this.m_rand = new Random();
		this.m_part_list = new LinkedList<Particle>();
		mass_center = new Vec3();
		mass_center_temp = new Vec3();
		
		total_mass = 0.0;
		total_mass_temp = 0.0;
		
		new_part_pos = new Vec3();
		new_drag_xy = new Vec3();
		this.part_not_added = new LinkedList<Particle>();
		need_to_clear = false;
		this.load_new = true;
		this.cameraUp = false;
		this.cameraDown = false;
		this.cameraLeft = false;
		this.cameraRight = false;
		this.correct_pos = false; 
		
		this.state = 1;
	}

	
	
	
	////
	//// Particle Field Management
	////

	
	///------------------------------------------------------------------
	/// This calls the functions which update the Particles Acceleration,
	/// Velocity, and Position Vectors according to the Euler Integrator
	/// method.  It is also responisble for adding new particles to the 
	/// list without disrupting the Iterators.
	///------------------------------------------------------------------ 
	public void update()
	{	
	
		total_mass_temp = 0.0;
		mass_center_temp.replace(0.0,0.0,0.0);
		
		// Update Particle accelerations and center of mass
		partIterator = m_part_list.listIterator();
		while (partIterator.hasNext())
		{
			workingPart = partIterator.next();
			
			workingPart.timestep = this.timestep;
			if (workingPart.updateAcc())
			{
				//System.out.println("Remove that part!");
				partIterator.remove();
			}
			else
			{
				mass_center_temp.addi(workingPart.mass * workingPart.pos.x, workingPart.mass * workingPart.pos.y);
				total_mass_temp += workingPart.mass;
			}
		}
		
		if (total_mass_temp >= 1)
			mass_center_temp.divi(total_mass_temp);
		else
			mass_center_temp.replace(-10, -10);
		
		mass_center.clone(mass_center_temp);
		total_mass = total_mass_temp;
		
		// Update Particle velocities
		partIterator = m_part_list.listIterator();
		while (partIterator.hasNext())
		{
			workingPart = partIterator.next();
			workingPart.updateVel();
		}
		
		// Update Particle positions
		partIterator = m_part_list.listIterator();
		while (partIterator.hasNext())
		{
			workingPart = partIterator.next();
			workingPart.updatePos();
		}
		
		
		//Check if Particle Field needs to be cleared
		if (need_to_clear)
		{
			Clear();
			need_to_clear = false;
		}
		
		//Check if cameraUp
		if (cameraDown)
			movePartsUp();
		if (cameraUp)
			movePartsDown();
		if (cameraLeft)
			movePartsRight();
		if (cameraRight)
			movePartsLeft();
		
		//Add the new particles that have been waiting to enter the list
		if (load_new)
			addWaitingParts();
	}
	
	
	
	public void addWaitingParts()
	{
		//Do not use Iterator here, part_not_added is not protected
		for (int i = 0; i < part_not_added.size(); i++)	
		{
			m_part_list.add(part_not_added.get(i));
			part_not_added.remove(i);
		}
	}
	
	
	
	public void Clear()
	{
		partIterator = m_part_list.listIterator();
		while (partIterator.hasNext())
		{
			workingPart = partIterator.next();
			partIterator.remove();
		}
	}
	
	
	
	public void movePartsUp()
	{
		partIterator = m_part_list.listIterator();
		while (partIterator.hasNext())
		{
			workingPart = partIterator.next();
			workingPart.pos.y = workingPart.pos.y- (timestep / secs_per_sec);
		}
	}
	
	
	public void movePartsDown()
	{
		partIterator = m_part_list.listIterator();
		while (partIterator.hasNext())
		{
			workingPart = partIterator.next();
			workingPart.pos.y = workingPart.pos.y+ (timestep / secs_per_sec);
		}
	}
	
	
	public void movePartsLeft()
	{
		partIterator = m_part_list.listIterator();
		while (partIterator.hasNext())
		{
			workingPart = partIterator.next();
			workingPart.pos.x = workingPart.pos.x- (timestep / secs_per_sec);
		}
	}
	
	
	public void movePartsRight()
	{
		partIterator = m_part_list.listIterator();
		while (partIterator.hasNext())
		{
			workingPart = partIterator.next();
			workingPart.pos.x = workingPart.pos.x+ (timestep / secs_per_sec);
		}
	}
	
	
	
	
	////
	//// Mouse Actions
	////
	
	
	///------------------------------------------------------------------
	/// This will perform different actions when the Left Mouse 
	/// button is Clicked, depending on the current State.
	///------------------------------------------------------------------
	public void OnClick(int new_x, int new_y) 
	{
		if (state == 1)
		{
			new_part_pos.replace(new_x, new_y);
			int new_size = 2;
			double new_mass = 2 * 3.14 * new_size * new_size * 200000; //Mass dependent on Area of Circle
			//double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * 40000); //Mass dependent on Volume of Sphere
			//double new_mass = 0.0;
			createOrbitingParticle(new_part_pos, new_size, new_mass, true, new Vec3(250,250,250));
		}
		
		if (state == 2)
		{
			new_part_pos.replace(new_x, new_y);
		}
	}
	
	
	///------------------------------------------------------------------
	/// This will perform different actions when the Left Mouse 
	/// button is Released, depending on the current State.
	///------------------------------------------------------------------
	public void ClickRelease(int new_x, int new_y)
	{
		if (state == 2)
		{
			Vec3 RGB = new Vec3((int)(Math.random()*164) + 91,(int)(Math.random()*164) + 91,(int)(Math.random()*164) + 91);
			new_drag_xy.replace(new_x, new_y);
			
			Vec3 vel = new_drag_xy.sub_vec(new_part_pos);
			vel.divi(200);
			
			double new_size = 5;
			double new_mass = 2 * 3.14 * new_size * new_size * 200000; //Mass dependent on Area of Circle
			//double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) *  100000);
			this.part_not_added.add(new Particle(m_part_list, window, new_part_pos, vel, new_size, 0.9, new_mass, true, RGB));
		}
	}
	
	
	///------------------------------------------------------------------
	/// This will perform different actions when the Right Mouse 
	/// button is Clicked, depending on the current State.
	///------------------------------------------------------------------
	public void RightClick(int new_x, int new_y)
	{
		if (state == 1)
		{
			new_part_pos.replace(new_x, new_y);
		}
	}
	
	
	///------------------------------------------------------------------
	/// This will perform different actions when the Right Mouse 
	/// button is Released, depending on the current State.
	///------------------------------------------------------------------
	public void RightRelease(int new_x, int new_y)
	{
		if (state == 1)
		{
			Vec3 RGB = new Vec3((int)(Math.random()*164) + 91,(int)(Math.random()*164) + 91,(int)(Math.random()*164) + 91);
			new_drag_xy.replace(new_x, new_y);
			double new_size = Math.sqrt(Math.pow(new_drag_xy.x - new_part_pos.x,2) + Math.pow(new_drag_xy.y - new_part_pos.y,2));
			if (new_size < 5)
				new_size = 5;
			double new_mass = 2 * 3.14 * new_size * new_size * 200000;
			//double new_mass = ((4.0/3.0)*3.14*new_size * new_size * new_size * 100000);
			this.part_not_added.add(new Particle(m_part_list, window, new_part_pos, new Vec3(), new_size, 1.0, new_mass, false, RGB));
		}
	}
	
	
	
	
	
	
	
	////
	//// Creators
	////
	
	
	public void createOrbitingParticle(Vec3 new_pos, double new_size, double new_mass, boolean bounce, Vec3 RGB)
	{
			//Calculate the orbital velocity of the new particle
			double cm_distance = Math.sqrt(Math.pow(mass_center.x - new_pos.x,2) + Math.pow(mass_center.y - new_pos.y,2));
			double OrbitV = 0.0;
			if (cm_distance >= 1)
				OrbitV = Math.sqrt((GravG * total_mass) / cm_distance);
			
			//Find unit tangent direction between particles
			Vec3 tan = new Vec3(new_pos.y - mass_center.y, mass_center.x - new_pos.x);
			if (tan.length() >= 0.001)
				tan.divi(tan.length());
			
			//Apply the Orbital Velocity to the Tangent Vector to create an Orbital Velocity vector
			tan.multi(OrbitV);
			this.part_not_added.add(new Particle(m_part_list, window, new_pos, tan, new_size, 1.0 , new_mass, bounce, RGB));
	}
	
	public void createPartDisk(double sqrd_radius, int parts, boolean orbiting, boolean balanced, Vec3 center, 
										double new_size, double new_mass, boolean bounce, Vec3 RGB)
	{
		double r;
		double theta;
		double x;
		double y;
		if (balanced)
		{
			this.load_new = false;
			for (int i=0; i<parts;i++)
			{
				r = m_rand.nextDouble() * sqrd_radius;
				theta = m_rand.nextDouble() * 6.28;
				x = (Math.sqrt(r) * Math.cos(theta)) + center.x;
				y = (Math.sqrt(r) * Math.sin(theta)) + center.y;
				Vec3 new_pos = new Vec3(x,y);
				this.part_not_added.add(new Particle(m_part_list, window, new_pos, new Vec3(), new_size, 0.9, new_mass, bounce, RGB));
			}
		}
		this.load_new = true;
	}
	
}




///------------------------------------------------------------------
/// 
/// 
///------------------------------------------------------------------ 