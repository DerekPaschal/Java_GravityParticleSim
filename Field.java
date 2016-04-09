import java.util.*;
import java.io.IOException;

class Field
{	
	static final double GravG = 0.000000000066740831;//Gravitational constant
	
	ArrayList<Particle> part_list;
	Vec3 window;
	
	Vec3 mass_center;
	private Vec3 mass_center_temp;
	double total_mass;
	private double total_mass_temp;
	
	boolean grav_on;
	
	Field(Vec3 new_window)
	{
		//Initializations
		this.part_list = new ArrayList<Particle>();
		this.mass_center = new Vec3();
		this.total_mass = 0.0;
		this.window = new_window;
		
		//Defaults
		this.mass_center_temp = new Vec3();
		this.total_mass_temp = 0.0;
		this.grav_on = true;
	}
	
	
	
	public void EulerIntegrate(double timestep)
	{
		//Reset running totals for center of mass and total mass
		total_mass_temp = 0.0;
		mass_center_temp = new Vec3();
		Particle workingPart;
		
		synchronized(this.part_list)
		{
			ListIterator<Particle> partIterator;
			double ThreadCount = 4;
			
			
			//Run particle-wall collision
			for(int i = 0; i < this.part_list.size(); i++)
			{
				workingPart = this.part_list.get(i);
				workingPart.remove = wallCollision(workingPart);
			}
			
			
			//Run particle-particle collision
			double divided = this.part_list.size()/ThreadCount;
			Thread t1 = new Thread(new CollisionThread(this, 0.0, divided, timestep));
			Thread t2 = new Thread(new CollisionThread(this, divided, divided*2, timestep));
			Thread t3 = new Thread(new CollisionThread(this, divided*2, divided*3, timestep));
			Thread t4 = new Thread(new CollisionThread(this, divided*3, divided*4, timestep));
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			try
			{ 
				t1.join();
				t2.join();
				t3.join();
				t4.join(); 
			} catch (InterruptedException e){}
			
			
			
			//Remove particles no longer in the field
			for(int i = 0; i < this.part_list.size(); i++)
			{
				workingPart = this.part_list.get(i);
				if (workingPart.remove)
				{
					this.part_list.remove(i);
				}
			}
			
			
			
			divided = this.part_list.size()/ThreadCount;
			// Update Gravity acceleration
			t1 = new Thread(new GravityThread(this, 0.0, divided, timestep));
			t2 = new Thread(new GravityThread(this, divided, divided*2, timestep));
			t3 = new Thread(new GravityThread(this, divided*2, divided*3, timestep));
			t4 = new Thread(new GravityThread(this, divided*3, divided*4, timestep));
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			try
			{ 
				t1.join();
				t2.join();
				t3.join();
				t4.join(); 
			} catch (InterruptedException e){}
			
			//Update Particle velocities
			partIterator = part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				updateVel(workingPart, timestep);
			}
			
			
			// Update Particle positions
			partIterator = part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				//System.out.println(""+workingPart.vel.x);
				updatePos(workingPart, timestep);
			}
			
			
			// Update Center of Mass position
			partIterator = part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				//Running totals for center of mass and total mass
				mass_center_temp.addi(workingPart.mass * workingPart.pos.x, workingPart.mass * workingPart.pos.y, workingPart.mass * workingPart.pos.z);
				total_mass_temp += workingPart.mass;
			}
			
		}
		
			//How to calculate center of mass
			if (total_mass_temp >= 1)
				mass_center_temp.divi(total_mass_temp);
			else
				mass_center_temp = new Vec3(window.x/2, window.y/2, 0.0);
			//Apply center of mass to public variables
			synchronized(mass_center)
			{
				mass_center = new Vec3(mass_center_temp);
				total_mass = total_mass_temp;
			}
			
			
	}

	
	
	
	
	

	
	
	public void updateVel(Particle part, double timestep)
	{
		part.vel.x = part.vel.x + (part.acc.x * timestep);
		part.vel.y = part.vel.y + (part.acc.y * timestep);
		part.vel.z = part.vel.z + (part.acc.z * timestep);
	}
	
	public void updatePos(Particle part, double timestep)
	{
		part.pos.x = part.pos.x + (part.vel.x * timestep);
		part.pos.y = part.pos.y + (part.vel.y * timestep);
		part.pos.z = part.pos.z + (part.vel.z * timestep);
	}
	
	
	
	public boolean wallCollision(Particle part1)
	{
		if (part1.pos.y > (this.window.y + part1.radius))
		{
			return true;
		}
		
		if (part1.pos.y < -part1.radius)
		{
			return true;
		}
		
		if (part1.pos.x > (this.window.x + part1.radius))
		{
			return true;
		}
		
		if (part1.pos.x < -part1.radius)
		{
			return true;
		}
		return false;
	}

	
}