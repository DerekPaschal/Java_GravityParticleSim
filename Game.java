import java.util.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

class Game
{	
	static final double GravG = 0.000000000066740831;//Gravitational constant
	Field field;
	Vec3 window;
	double timestep;
	int secs_per_sec;
	int accuracy_multiple;
	Vec3 new_click_xy;
	Vec3 new_drag_xy;
	
	
	int coloring;
	boolean cameraUp;
	boolean cameraDown;
	boolean cameraLeft;
	boolean cameraRight;
	boolean show_center;
	double default_density;
	
	///State '1' is making central "suns" and clicking creates light orbiting bodys
	///State '2' creates a collapsing disk of light particles, which you shoot with heavier bodies
	int state;
		
	Game(Field new_field, Vec3 new_window) throws IOException
	{
		//Initializations
		this.window = new_window;
		this.field = new_field;
		
		//Defaults
		this.new_click_xy = new Vec3();
		this.new_drag_xy = new Vec3();
		this.cameraUp = false;
		this.cameraDown = false;
		this.cameraLeft = false;
		this.cameraRight = false;
		this.show_center = true;
		this.coloring = 0;
		this.state = 1;
		this.accuracy_multiple = 1;
		this.secs_per_sec = 0;
		this.default_density = 4000000.0; //In units Kg
		this.changeState(1);
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
		this.field.EulerIntegrate(timestep);
		
		
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
		//addWaitingParts();
	}
	
	
	
	///------------------------------------------------------------------
	/// Completely Clears the part_list of all entries,
	/// reseting the Particle Field.
	///------------------------------------------------------------------ 
	public void Clear()
	{
		Particle workingPart;
		int parts = field.part_list.size();
		synchronized(this.field)
		{
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
					partIterator.remove();
			}
		}
		Wait(parts/4);
	}
	
	
	public void Wait(int millis)
	{
		try{
		TimeUnit.MILLISECONDS.sleep(millis);
		} catch (InterruptedException e){}
	}
	
	
	///------------------------------------------------------------------
	/// This set of functions moves the particles Up, Down, Left, or 
	/// Right an amount dependent on timestep and secs_per_sec when 
	/// their respective booleans are true.
	///------------------------------------------------------------------ 
	public void movePartsUp()
	{
		Particle workingPart;
		synchronized(this.field)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
					workingPart.pos.y = workingPart.pos.y- (2*timestep / divide);
			}
		}
	}
	
	
	public void movePartsDown()
	{
		Particle workingPart;
		synchronized(this.field)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
					workingPart.pos.y = workingPart.pos.y+ (2*timestep / divide);
			}
		}
	}
	
	
	public void movePartsLeft()
	{
		Particle workingPart;
		synchronized(this.field)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
					workingPart.pos.x = workingPart.pos.x- (2*timestep / divide);
			}
		}
	}
	
	
	public void movePartsRight()
	{
		Particle workingPart;
		synchronized(this.field)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
					workingPart.pos.x = workingPart.pos.x+ (2*timestep / divide);
			}
		}
	}
	
	
	
	
	public void changeState(int i)
	{
		switch (i)
		{
			case 1:
				this.Clear();
				this.accuracy_multiple = 2;
				this.secs_per_sec = 1;
				this.state = 1;
				this.show_center = true;
				this.field.collide_on = true;
				
				break;
				
			case 2:
				this.Clear();
				this.state = 2;
				this.accuracy_multiple = 2;
				this.secs_per_sec = 1;
				this.show_center = true;
				this.field.collide_on = true;
				
				double new_size_min = 4;
				double new_size_max = 6;
				double new_density = this.default_density*0.75;
				//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
				//double new_mass_min = ((4.0/3.0)*3.14*Math.pow(new_size_min,3) * this.density);
				//double new_mass_max = ((4.0/3.0)*3.14*Math.pow(new_size_max,3) * this.density);
				createPartDisk(null,300,0,600,false,true, new Vec3(this.window.x/2,this.window.y/2,0.0),
									new_size_min, new_size_max, new_density, true, 0.0, 0.2/this.GravG, new Vec3(250,250,250));
				break;
				
			case 3:
				this.Clear();
				this.state = 3;
				this.accuracy_multiple = 2;
				this.secs_per_sec = 1;
				this.show_center = false;
				this.field.collide_on = true;
				this.coloring = 1;
				
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
			new_click_xy = new Vec3(new_x, new_y,0.0);
			int new_size = 3;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
			double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * this.default_density); //Mass dependent on Volume of Sphere
			//double new_mass = 0.0;
			Vec3 vel = createOrbitingTraj(null,new_click_xy);
			Particle newPart = new Particle(new_click_xy, vel, new_size, new_mass, 0.5, 0.1/GravG, true, new Vec3(250,250,250));
			addNewParticle(newPart);
		}
		
		//State 2: Gets Current Mouse Position, stores in new_click_xy
		if (state == 2)
		{
			new_click_xy= new Vec3(new_x, new_y,0.0);
		}
		
		//State 3: Creates 'black hole' and disk on click location
		if (state == 3)
		{
			int new_size = 5;
			double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * this.default_density*500);
			Particle newPart = new Particle(new Vec3(new_x, new_y, 0.0), new Vec3(), new_size, new_mass, 0.0, 0.0, false, new Vec3(100,0,0));
			
			synchronized (this.field)
			{
				createPartDisk(newPart, 200, 20, 1699, true, false, new Vec3(), 4.0, 5.0, 0.0, false, 0.0, 0.0, new Vec3(240,240,240));
				addNewParticle(newPart);
			}
		}
		
	}
	
	
	
	///------------------------------------------------------------------
	/// This will perform different actions when the Left Mouse 
	/// button is Released, depending on the current State.
	///------------------------------------------------------------------
	public void ClickRelease(int new_x, int new_y)
	{	
		//State 2: Creates Randomly Colored Bouncy Particle; Velocity related to Current Mouse Position distance from new_click_xy
		if (state == 2)
		{
			Vec3 RGB = new Vec3((int)(Math.random()*132) + 123,(int)(Math.random()*132) + 123,(int)(Math.random()*132) + 123);
			new_drag_xy = new Vec3(new_x, new_y, 0.0);
			
			Vec3 vel = new_drag_xy.sub_vec(new_click_xy);
			vel.divi(80);
			
			double new_size = 7;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
			double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) *  this.default_density*2);
			Particle newPart = new Particle(new_click_xy, vel, new_size, new_mass, 0.0, 0.1/this.GravG, true, RGB);
			addNewParticle(newPart);
		}
	}
	
	
	
	///------------------------------------------------------------------
	/// This will perform different actions when the Right Mouse 
	/// button is Clicked, depending on the current State.
	///------------------------------------------------------------------
	public void RightClick(int new_x, int new_y)
	{
		//State 1: Gets Current Mouse Position, stores in new_click_xy 
		if (state == 1)
		{
			new_click_xy = new Vec3(new_x, new_y, 0.0);
		}
		
		//State 2: Gets Current Mouse Position, stores in new_click_xy 
		if (state == 2)
		{
			new_click_xy = new Vec3(new_x, new_y, 0.0);
		}
		
		
		//if (state ==3)
		//{
		//	new_click_xy = new Vec3(new_x, new_y, 0.0);
		//}
		
	}
	
	
	
	///------------------------------------------------------------------
	/// This will perform different actions when the Right Mouse 
	/// button is Released, depending on the current State.
	///------------------------------------------------------------------
	public void RightRelease(int new_x, int new_y)
	{	
		//State 1: Creates Randomly Colored Particle; Radius and Mass related to Current Mouse Position distance from new_click_xy
		if (state == 1)
		{
			Vec3 RGB = new Vec3((int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123);
			new_drag_xy = new Vec3(new_x, new_y, 0.0);
			double new_size = Math.sqrt(Math.pow(new_drag_xy.x - new_click_xy.x,2) + Math.pow(new_drag_xy.y - new_click_xy.y,2));
			if (new_size < 5)
				new_size = 5;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density;
			double new_mass = ((4.0/3.0)*3.14*new_size * new_size * new_size * this.default_density*2);
			Particle newPart = new Particle(new_click_xy, new Vec3(), new_size, new_mass, 1.0, 0.1/this.GravG, false, RGB);
			addNewParticle(newPart);
		}
		
		//State 2: Creates Randomly Colored Bouncy Particle; Radius and Mass related to Current Mouse Position distance from new_click_xy
		if (state == 2)
		{
			Vec3 RGB = new Vec3((int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123);
			new_drag_xy = new Vec3(new_x, new_y, 0.0);
			double new_size = Math.sqrt(Math.pow(new_drag_xy.x - new_click_xy.x,2) + Math.pow(new_drag_xy.y - new_click_xy.y,2));
			if (new_size < 3)
				new_size = 3;
			if (new_size > 100)
				new_size = 100;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density;
			double new_mass = ((4.0/3.0)*3.14*new_size * new_size * new_size * this.default_density);
			Particle newPart = new Particle(new_click_xy, new Vec3(), new_size, new_mass, 0.0, 0.1/this.GravG, true, RGB);
			addNewParticle(newPart);
		}
		/*
		if (state ==3)
		{
			Vec3 new_vel = new_click_xy.sub_vec(new Vec3(new_x, new_y, 0.0));
			new_vel.divi(-80);
			
			int new_size = 5;
			double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * this.default_density*500);
			Particle newPart = new Particle(new_click_xy, new_vel, new_size, new_mass, 0.0, 0.0, false, new Vec3(200,64,64));
			
			//createPartDisk(newPart, 200, 20, 999, true, false, new Vec3(), 3.0, 4.0, 0.0, false, 0.0, 0.0, new Vec3(255,255,255));
			addNewParticle(newPart);
		}
		*/
	}
	
	
	
	
	
	
	
	///
	/// Creators
	///
	
	
	///------------------------------------------------------------------
	/// A synchronized, safe way to add Particles to the part_list,
	/// called whenever a new Particle is to be made.
	///------------------------------------------------------------------ 
	public void addNewParticle(Particle part)
	{
		synchronized(this.field)
		{
			if (allow_new_part(part))
				field.part_list.add(part);
		}
	}
	
	
	
	public boolean allow_new_part(Particle testingPart)
	{	
		Particle workingPart;
		double distance_sqrd, r2;
		synchronized(this.field)
		{
			ListIterator<Particle> partIterator = this.field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
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
	
	
	
	///------------------------------------------------------------------
	/// Creates a Particle with Inital Values from Arguments that Orbits
	/// around the current Center of Mass.
	///------------------------------------------------------------------ 
	public Vec3 createOrbitingTraj(Particle centerParticle,Vec3 new_pos)
	{	
		Vec3 centerpos = new Vec3(field.mass_center);
		double orbitmass = field.total_mass;
		if (centerParticle != null)
		{
			centerpos = new Vec3(centerParticle.pos);
			orbitmass = centerParticle.mass;
		}
		
		//Calculate the orbital velocity of the new particle
		double cm_distance = Math.sqrt(Math.pow(centerpos.x - new_pos.x,2) + Math.pow(centerpos.y - new_pos.y,2));
		double OrbitV = 0.0;
		if (cm_distance >= 1)
			OrbitV = Math.sqrt((field.GravG * orbitmass) / cm_distance);
		
		//Find unit tangent direction between particles
		Vec3 tan = new Vec3(new_pos.y - centerpos.y, centerpos.x - new_pos.x, 0.0);
		if (tan.length() >= 0.001)
			tan.divi(tan.length());
		
		//Apply the Orbital Velocity to the Tangent Vector to create an Orbital Velocity vector
		tan.multi(OrbitV);
		if (centerParticle != null)
			tan.addi_vec(centerParticle.vel);
		return tan;
	}
	
	
	
	
	///------------------------------------------------------------------
	/// Creates a set of Particles in a Disk.  The Radius, Number of
	/// Particles, Spin, and Center of the Disk are Arguments.  The
	/// Radius, Mass, Bounce, Elasticity, and Color of the particles
	/// are also Arguments.
	///------------------------------------------------------------------ 
	public void createPartDisk(Particle centerParticle,double outer_radius,double inner_radius, int parts, boolean orbiting, boolean balanced, Vec3 center, 
								double new_size_min, double new_size_max, double density, boolean bounce, double elasticity, double repulse, Vec3 RGB)
	{
		double r = 1;
		double theta;
		double x;
		double y;
		double z;
		double mass;
		double size;
		double center_mass = 0;
		Vec3 velocity = new Vec3();
		Vec3 new_pos = new Vec3();
		Particle newPart;
		
		
		if (centerParticle != null)
		{
			center = new Vec3(centerParticle.pos);
			center_mass = centerParticle.mass;
		}
		
		for (int i=0; i < parts; i++)
		{
			//mass = (Math.random() * (new_mass_max - new_mass_min)) + new_mass_min;
			size = (Math.random() * (new_size_max - new_size_min)) + new_size_min;
			mass = ((4.0/3.0)*3.14*Math.pow(size,3) * density);
			if (balanced)
			{
				r = (Math.random() * ((outer_radius*outer_radius) - (inner_radius*inner_radius))) + (inner_radius*inner_radius);
				theta = Math.random() * 6.28;
				x = (Math.sqrt(r) * Math.cos(theta)) + center.x;
				y = (Math.sqrt(r) * Math.sin(theta)) + center.y;
				z = 0.0;
			}
			else
			{
				r = (Math.random() * (outer_radius - inner_radius)) + inner_radius;
				theta = Math.random() * 6.28;
				x = (r * Math.cos(theta)) + center.x;
				y = (r * Math.sin(theta)) + center.y;
				z = 0.0;
			}
			new_pos = new Vec3(x,y,z);
			
			if (orbiting)
			{	
				velocity = createOrbitingTraj(centerParticle, new_pos);
			}
			
			newPart = new Particle(new_pos, velocity, size, mass, elasticity, repulse, bounce, RGB);
			addNewParticle(newPart);
		}
	}
	
	
}




///------------------------------------------------------------------
/// 
/// 
///------------------------------------------------------------------ 