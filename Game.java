import java.util.*;
import java.io.IOException;

class Game
{	
	Field field;
	Vec3 window;
	double timestep;
	int secs_per_sec;
	int accuracy_multiple;
	Vec3 new_part_pos;
	Vec3 new_drag_xy;
	
	
	int coloring;
	boolean cameraUp;
	boolean cameraDown;
	boolean cameraLeft;
	boolean cameraRight;
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
		new_part_pos = new Vec3();
		new_drag_xy = new Vec3();
		this.cameraUp = false;
		this.cameraDown = false;
		this.cameraLeft = false;
		this.cameraRight = false;
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
		synchronized(field.part_list)
		{
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
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
		Particle workingPart;
		synchronized(field.part_list)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
					workingPart.pos.y = workingPart.pos.y- (timestep / divide);
			}
		}
	}
	
	
	public void movePartsDown()
	{
		Particle workingPart;
		synchronized(field.part_list)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
					workingPart.pos.y = workingPart.pos.y+ (timestep / divide);
			}
		}
	}
	
	
	public void movePartsLeft()
	{
		Particle workingPart;
		synchronized(field.part_list)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
					workingPart.pos.x = workingPart.pos.x- (timestep / divide);
			}
		}
	}
	
	
	public void movePartsRight()
	{
		Particle workingPart;
		synchronized(field.part_list)
		{
			double divide = Math.max(secs_per_sec,1);
			ListIterator<Particle> partIterator = field.part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				if (workingPart != null)
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
				this.accuracy_multiple = 5;
				this.secs_per_sec = 2;
				this.state = 1;
				break;
				
			case 2:
				this.Clear();
				this.state = 2;
				this.accuracy_multiple = 5;
				this.secs_per_sec = 1;
				double new_size_min = 3;
				double new_size_max = 5;
				double new_density = this.default_density;
				//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
				//double new_mass_min = ((4.0/3.0)*3.14*Math.pow(new_size_min,3) * this.density);
				//double new_mass_max = ((4.0/3.0)*3.14*Math.pow(new_size_max,3) * this.density);
				createPartDisk(null,400*400,0*0,400,false,true, new Vec3(this.window.x/2,this.window.y/2,0.0),
									new_size_min, new_size_max, new_density, true, 0.0, new Vec3(250,250,250));
				break;
				
			case 3:
				this.Clear();
				this.state = 3;
				this.accuracy_multiple = 5;
				this.secs_per_sec = 1;
				double new_size = 20;
				double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * this.default_density);
				Particle newPart = new Particle(new Vec3(150.0,200.0,200.0), new Vec3(), new_size, 0.8, new_mass, true, new Vec3(250,250,250));
				addNewParticle(newPart);
				newPart = new Particle(new Vec3(200.0,200.0,-200.0), new Vec3(), new_size, 0.8, new_mass, true, new Vec3(200,200,200));
				addNewParticle(newPart);
				newPart = new Particle(new Vec3(1000.0,200.0,0.0), new Vec3(), new_size, 0.8, new_mass, true, new Vec3(150,150,150));
				addNewParticle(newPart);
				newPart = new Particle(new Vec3(1000.0,603.0,0.0), new Vec3(), new_size, 0.8, new_mass, true, new Vec3(100,100,100));
				addNewParticle(newPart);
				
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
			new_part_pos = new Vec3(new_x, new_y,0.0);
			int new_size = 3;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
			double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * this.default_density); //Mass dependent on Volume of Sphere
			//double new_mass = 0.0;
			Vec3 vel = createOrbitingTraj(null,new_part_pos, new_size, new_mass, true, 0.95, new Vec3(250,250,250));
			Particle newPart = new Particle(new_part_pos, vel, new_size, 0.8, new_mass, true, new Vec3(250,250,250));
			addNewParticle(newPart);
		}
		
		//State 2: Gets Current Mouse Position, stores in new_part_pos
		if (state == 2)
		{
			new_part_pos= new Vec3(new_x, new_y,0.0);
		}
		
		//State 3: Creates black hole(?) center with orbiting disk
		/*
		if (state == 3)
		{
			new_part_pos = new Vec3(new_x, new_y,0.0);
			int new_size = 1;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
			//double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) * density); //Mass dependent on Volume of Sphere
			double new_mass = 1;
			Particle newPart = new Particle(new_part_pos, new Vec3(), new_size, 0.0, new_mass, false, new Vec3(250,250,250));
			addNewParticle(newPart);
			//double new_mass = 0.0;
			//Vec3 vel = createOrbitingTraj(null,new_part_pos, new_size, new_mass, true, 0.95, new Vec3(250,250,250));
			//createNewParticle(new_part_pos, vel, new_size, 0.8, new_mass, true, new Vec3(250,250,250));
		}
		*/
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
			new_drag_xy = new Vec3(new_x, new_y, 0.0);
			
			Vec3 vel = new_drag_xy.sub_vec(new_part_pos);
			vel.divi(80);
			
			double new_size = 6;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density; //Mass dependent on Area of Circle
			double new_mass = ((4.0/3.0)*3.14*Math.pow(new_size,3) *  this.default_density*2);
			Particle newPart = new Particle(new_part_pos, vel, new_size, 0.0, new_mass, true, RGB);
			addNewParticle(newPart);
			//createPartDisk();
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
			new_part_pos = new Vec3(new_x, new_y, 0.0);
		}
		
		//State 2: Gets Current Mouse Position, stores in new_part_pos 
		if (state == 2)
		{
			new_part_pos = new Vec3(new_x, new_y, 0.0);
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
			new_drag_xy = new Vec3(new_x, new_y, 0.0);
			double new_size = Math.sqrt(Math.pow(new_drag_xy.x - new_part_pos.x,2) + Math.pow(new_drag_xy.y - new_part_pos.y,2));
			if (new_size < 5)
				new_size = 5;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density;
			double new_mass = ((4.0/3.0)*3.14*new_size * new_size * new_size * this.default_density);
			Particle newPart = new Particle(new_part_pos, new Vec3(), new_size, 1.0, new_mass, false, RGB);
			addNewParticle(newPart);
		}
		
		//State 2: Creates Randomly Colored Bouncy Particle; Radius and Mass related to Current Mouse Position distance from new_part_pos
		if (state == 2)
		{
			Vec3 RGB = new Vec3((int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123, (int)(Math.random()*132) + 123);
			new_drag_xy = new Vec3(new_x, new_y, 0.0);
			double new_size = Math.sqrt(Math.pow(new_drag_xy.x - new_part_pos.x,2) + Math.pow(new_drag_xy.y - new_part_pos.y,2));
			if (new_size < 3)
				new_size = 3;
			if (new_size > 100)
				new_size = 100;
			//double new_mass = 2 * 3.14 * new_size * new_size * this.density;
			double new_mass = ((4.0/3.0)*3.14*new_size * new_size * new_size * this.default_density);
			Particle newPart = new Particle(new_part_pos, new Vec3(), new_size, 0.0, new_mass, true, RGB);
			addNewParticle(newPart);
		}
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
		synchronized(field.part_list)
		{
			if (allow_new_part(part))
				field.part_list.add(part);
		}
	}
	
	
	
	///------------------------------------------------------------------
	/// Creates a Particle with Inital Values from Arguments that Orbits
	/// around the current Center of Mass.
	///------------------------------------------------------------------ 
	public Vec3 createOrbitingTraj(Particle centerParticle,Vec3 new_pos, double new_size, double new_mass, boolean bounce, double elasticity, Vec3 RGB)
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
	public void createPartDisk(Particle centerParticle,double outer_sqrd_radius,double inner_sqrd_radius, int parts, boolean orbiting, boolean balanced, Vec3 center, 
								double new_size_min, double new_size_max, double density, boolean bounce, double elasticity, Vec3 RGB)
	{
		double r;
		double theta;
		double x;
		double y;
		double z;
		double mass;
		double size;
		Vec3 velocity = new Vec3();
		Vec3 new_pos = new Vec3();
		Particle newPart;
		
		
		if (centerParticle != null)
					center = new Vec3(centerParticle.pos);
		
			
		for (int i=0; i < parts; i++)
		{
			//mass = (Math.random() * (new_mass_max - new_mass_min)) + new_mass_min;
			size = (Math.random() * (new_size_max - new_size_min)) + new_size_min;
			mass = ((4.0/3.0)*3.14*Math.pow(size,3) * density);
			if (balanced)
			{
				r = (Math.random() * (outer_sqrd_radius - inner_sqrd_radius)) + inner_sqrd_radius;
				theta = Math.random() * 6.28;
				x = (Math.sqrt(r) * Math.cos(theta)) + center.x;
				y = (Math.sqrt(r) * Math.sin(theta)) + center.y;
				z = 0.0;
				new_pos = new Vec3(x,y,z);
			}
			
			
			if (!orbiting)
			{
				velocity = new Vec3();
			}
			
			newPart = new Particle(new_pos, velocity, size, elasticity, mass, bounce, RGB);
			addNewParticle(newPart);
		}
	}
	
	public boolean allow_new_part(Particle testingPart)
	{
		Particle workingPart;
		double distance_sqrd, r2;
		synchronized(field.part_list)
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
}




///------------------------------------------------------------------
/// 
/// 
///------------------------------------------------------------------ 