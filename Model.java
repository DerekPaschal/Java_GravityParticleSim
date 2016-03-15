//import java.util.Random;
//import java.util.LinkedList;
//import java.util.Iterator;
import java.util.*;
import java.io.IOException;

class Model
{	
	static final double GravG = 0.000000000066740831;//Gravitational constant

	LinkedList<Particle> m_part_list;
	LinkedList<Particle> part_not_added;
	Particle workingPart;
	
	Vec3 window;
	double timestep;
	int secs_per_sec;
	int accuracy_multiple;
	Vec3 new_part_pos;
	Vec3 new_drag_xy;
	Vec3 mass_center;
	private Vec3 mass_center_temp;
	double total_mass;
	private double total_mass_temp;
	
	boolean vel_color;
	boolean cameraUp;
	boolean cameraDown;
	boolean cameraLeft;
	boolean cameraRight;
	double density;
	
	///State '1' is making central "suns" and clicking creates light orbiting bodys
	///State '2' creates a collapsing disk of light particles, which you shoot with heavier bodies
	int state;
		
	Model(int new_screen_x, int new_screen_y) throws IOException
	{
		//Initializations
		window = new Vec3(new_screen_x, new_screen_y);
		this.m_part_list = new LinkedList<Particle>();
		mass_center = new Vec3();
		total_mass = 0.0;
		
		//Defaults
		mass_center_temp = new Vec3();
		total_mass_temp = 0.0;
		new_part_pos = new Vec3();
		new_drag_xy = new Vec3();
		this.part_not_added = new LinkedList<Particle>();
		this.cameraUp = false;
		this.cameraDown = false;
		this.cameraLeft = false;
		this.cameraRight = false;
		this.vel_color = false;
		this.state = 1;
		this.accuracy_multiple = 10;
		this.timestep = 1.0/accuracy_multiple;
		this.secs_per_sec = 5;
		this.density = 4000000;
	}

	
	
	
	///
	/// Particle Field Management
	///

	
	
	///------------------------------------------------------------------
	/// This calls the functions which update the Particles Acceleration,
	/// Velocity, and Position Vectors according to the Euler Integrator
	/// method.  It is also responisble for adding new particles to the 
	/// list without disrupting the Iterators, calculating Center
	/// of Mass, Clearing the Field, and Moving the Camera.
	///------------------------------------------------------------------ 
	public void update()
	{	
		//Perform Euler Integration on Particles
		EulerIntegrate();
		
		
		//Check if camera should be moved
		if (cameraDown)
			movePartsDown();
		if (cameraUp)
			movePartsUp();
		if (cameraLeft)
			movePartsLeft();
		if (cameraRight)
			movePartsRight();
		
		
		//Add the new particles that have been waiting to enter the list
		addWaitingParts();
		
	}
	
	
	
	public void EulerIntegrate()
	{
		//Reset running totals for center of mass and total mass
		total_mass_temp = 0.0;
		mass_center_temp.replace(0.0,0.0,0.0);
		
		synchronized(m_part_list)
		{
			// Update Particle accelerations and center of mass
			ListIterator<Particle> partIterator = m_part_list.listIterator();
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
					//Running totals for center of mass and total mass
					mass_center_temp.addi(workingPart.mass * workingPart.pos.x, workingPart.mass * workingPart.pos.y);
					total_mass_temp += workingPart.mass;
				}
			}
			
			//How to calculate center of mass
			if (total_mass_temp >= 1)
				mass_center_temp.divi(total_mass_temp);
			else
				mass_center_temp.replace(window.x/2, window.y/2);
			
			
			//Apply center of mass to public variables
			synchronized(mass_center)
			{
				mass_center.clone(mass_center_temp);
				total_mass = total_mass_temp;
			}
			
			
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
		}
	}
	
	
	
	///------------------------------------------------------------------
	/// Adds all Particles in part_not_added to the m_part_list if
	/// they meet requirements.
	///------------------------------------------------------------------ 
	public void addWaitingParts()
	{
		synchronized(m_part_list)
		{
			synchronized(part_not_added)
			{
				//Do not use Iterator here, part_not_added is not protected
				for (int i = 0; i < part_not_added.size(); i++)	
				{
					//Allow new particle if it does not intersect with 
					//Particles already in the list
					if (allow_new_part(part_not_added.get(i)))
						m_part_list.add(part_not_added.get(i));
					
				}
				ClearNotAdded();
			}
		}
	}
	
	
	
	///------------------------------------------------------------------
	/// Completely Clears the m_part_list of all entries,
	/// reseting the Particle Field.
	///------------------------------------------------------------------ 
	public void Clear()
	{
		synchronized(m_part_list)
		{
			ListIterator<Particle> partIterator = m_part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				partIterator.remove();
			}
		}
	}
	
	
	
	public void ClearNotAdded()
	{
		synchronized(part_not_added)
		{
			ListIterator<Particle> partIterator = part_not_added.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				partIterator.remove();
			}
		}
	}
	
	
	
	///------------------------------------------------------------------
	/// This set of functions moves the particles Up, Down, Left, or 
	/// Right an amount dependent on timestep and secs_per_sec when 
	/// their respective booleans are true.
	///------------------------------------------------------------------ 
	public void movePartsUp()
	{
		synchronized(m_part_list)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = m_part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				workingPart.pos.y = workingPart.pos.y- (timestep / divide);
			}
		}
	}
	
	
	public void movePartsDown()
	{
		synchronized(m_part_list)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = m_part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				workingPart.pos.y = workingPart.pos.y+ (timestep / divide);
			}
		}
	}
	
	
	public void movePartsLeft()
	{
		synchronized(m_part_list)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = m_part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				workingPart.pos.x = workingPart.pos.x- (timestep / divide);
			}
		}
	}
	
	
	public void movePartsRight()
	{
		synchronized(m_part_list)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = m_part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				workingPart.pos.x = workingPart.pos.x+ (timestep / divide);
			}
		}
	}
	
	
	
	
	public void changeState(int i)
	{
		switch (i)
		{
			case 1:
				this.Clear();
				this.ClearNotAdded();
				this.accuracy_multiple = 10;
				this.secs_per_sec = 1;
				this.state = 1;
				break;
				
			case 2:
				this.ClearNotAdded();
				this.Clear();
				this.state = 2;
				this.accuracy_multiple = 10;
				this.secs_per_sec = 1;
				double new_size = 3;
				//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
				double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * this.density * 0.8);
				this.createPartDisk(200*200,0*0,200,false,true, new Vec3(this.window.x/2,this.window.y/2),
													new_size, 2* new_mass, true, 0.0, new Vec3(250,250,250));
				break;
		}
	}
	
	
	
	public void changeSpeed(int i)
	{	
		int newSpeed = this.secs_per_sec + i;
		if(newSpeed >= 0)
			this.secs_per_sec = newSpeed;
	}
	
	
	///
	/// Mouse Actions
	///
	
	
	///------------------------------------------------------------------
	/// This will perform different actions when the Left Mouse 
	/// button is Clicked, depending on the current State.
	///------------------------------------------------------------------
	public void OnClick(int new_x, int new_y) 
	{	
		//State 1: Creates a Small Orbiting Bouncy Particle around the Center of Mass
		if (state == 1)
		{
			new_part_pos.replace(new_x, new_y);
			int new_size = 3;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
			double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * density); //Mass dependent on Volume of Sphere
			//double new_mass = 0.0;
			Vec3 vel = createOrbitingParticle(new_part_pos, new_size, new_mass, true, 0.95, new Vec3(250,250,250));
			createNewParticle(new_part_pos, vel, new_size, 0.8, new_mass, true, new Vec3(250,250,250));
		}
		
		//State 2: Gets Current Mouse Position, stores in new_part_pos
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
		//State 2: Creates Randomly Colored Bouncy Particle; Velocity related to Current Mouse Position distance from new_part_pos
		if (state == 2)
		{
			Vec3 RGB = new Vec3((int)(Math.random()*132) + 123,(int)(Math.random()*132) + 123,(int)(Math.random()*132) + 123);
			new_drag_xy.replace(new_x, new_y);
			
			Vec3 vel = new_drag_xy.sub_vec(new_part_pos);
			vel.divi(80);
			
			double new_size = 6;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
			double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) *  this.density);
			createNewParticle(new_part_pos, vel, new_size, 0.0, new_mass, true, RGB);
			//this.part_not_added.add(new Particle(m_part_list, window, new_part_pos, vel, new_size, 0.4, new_mass, true, RGB));
		}
	}
	
	
	
	///------------------------------------------------------------------
	/// This will perform different actions when the Right Mouse 
	/// button is Clicked, depending on the current State.
	///------------------------------------------------------------------
	public void RightClick(int new_x, int new_y)
	{
		//State 1: Gets Current Mouse Position, stores in new_part_pos 
		if (state == 1)
		{
			new_part_pos.replace(new_x, new_y);
		}
		
		//State 2: Gets Current Mouse Position, stores in new_part_pos 
		if (state == 2)
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
		//State 1: Creates Randomly Colored Particle; Radius and Mass related to Current Mouse Position distance from new_part_pos
		if (state == 1)
		{
			Vec3 RGB = new Vec3((int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123);
			new_drag_xy.replace(new_x, new_y);
			double new_size = Math.sqrt(Math.pow(new_drag_xy.x - new_part_pos.x,2) + Math.pow(new_drag_xy.y - new_part_pos.y,2));
			if (new_size < 5)
				new_size = 5;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density;
			double new_mass = ((4.0/3.0)*3.14*new_size * new_size * new_size * this.density);
			createNewParticle(new_part_pos, new Vec3(), new_size, 1.0, new_mass, false, RGB);
		}
		
		//State 2: Creates Randomly Colored Bouncy Particle; Radius and Mass related to Current Mouse Position distance from new_part_pos
		if (state == 2)
		{
			Vec3 RGB = new Vec3((int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123);
			new_drag_xy.replace(new_x, new_y);
			double new_size = Math.sqrt(Math.pow(new_drag_xy.x - new_part_pos.x,2) + Math.pow(new_drag_xy.y - new_part_pos.y,2));
			if (new_size < 3)
				new_size = 3;
			if (new_size > 100)
				new_size = 100;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density;
			double new_mass = ((4.0/3.0)*3.14*new_size * new_size * new_size * density);
			createNewParticle(new_part_pos, new Vec3(), new_size, 0.0, new_mass, true, RGB);
		}
	}
	
	
	
	
	
	
	
	///
	/// Creators
	///
	
	
	///------------------------------------------------------------------
	/// A synchronized, safe way to add Particles to the 'waiting list'
	/// part_not_added, called on whenever a new Particle is to be made.
	///------------------------------------------------------------------ 
	public void createNewParticle(Vec3 new_pos, Vec3 new_vel, double new_size, double new_elasticity, double new_mass, boolean bounce, Vec3 RGB)
	{
		synchronized(part_not_added)
		{
			this.part_not_added.add(new Particle(this.m_part_list, this.window, new_pos, new_vel, new_size, new_elasticity , new_mass, bounce, RGB));
		}
	}
	
	
	
	///------------------------------------------------------------------
	/// Creates a Particle with Inital Values from Arguments that Orbits
	/// around the current Center of Mass.
	///------------------------------------------------------------------ 
	public Vec3 createOrbitingParticle(Vec3 new_pos, double new_size, double new_mass, boolean bounce, double elasticity, Vec3 RGB)
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
		return tan;
		//createNewParticle(new_pos, tan, new_size, elasticity , new_mass, bounce, RGB);
	}
	
	
	
	
	///------------------------------------------------------------------
	/// Creates a set of Particles in a Disk.  The Radius, Number of
	/// Particles, Spin, and Center of the Disk are Arguments.  The
	/// Radius, Mass, Bounce, Elasticity, and Color of the particles
	/// are also Arguments.
	///------------------------------------------------------------------ 
	public void createPartDisk(double outer_sqrd_radius,double inner_sqrd_radius, int parts, boolean orbiting, boolean balanced, Vec3 center, 
										double new_size, double new_mass, boolean bounce,double elasticity, Vec3 RGB)
	{
		
		double r;
		double theta;
		double x;
		double y;
		if (balanced)
		{
			synchronized(m_part_list)
			{
				for (int i=0; i < parts; i++)
				{
					r = (Math.random() * (outer_sqrd_radius - inner_sqrd_radius)) + inner_sqrd_radius;
					theta = Math.random() * 6.28;
					x = (Math.sqrt(r) * Math.cos(theta)) + center.x;
					y = (Math.sqrt(r) * Math.sin(theta)) + center.y;
					Vec3 new_pos = new Vec3(x,y);
					createNewParticle(new_pos, new Vec3(), new_size, elasticity, new_mass, bounce, RGB);
				}
			}
		}
	}
	
	
	
	public boolean allow_new_part(Particle testingPart)
	{
		double distance_sqrd, r2;
		synchronized(m_part_list)
		{
			ListIterator<Particle> partIterator = this.m_part_list.listIterator();
			while (partIterator.hasNext())
			{
				this.workingPart = partIterator.next();
				if (workingPart.bounces)
				{
					distance_sqrd = Math.abs((workingPart.pos.x - testingPart.pos.x)*(workingPart.pos.x - testingPart.pos.x) +
									(workingPart.pos.y - testingPart.pos.y)*(workingPart.pos.y - testingPart.pos.y));
					r2 = (testingPart.radius + workingPart.radius)*(testingPart.radius + workingPart.radius);
					if (distance_sqrd < r2)
						return false;
				}
			}
		}
		return true;
	}
}




///------------------------------------------------------------------
/// 
/// 
///------------------------------------------------------------------ 