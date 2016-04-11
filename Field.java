import java.util.*;
import java.io.IOException;

class Field
{	
	static final double GravG = 0.000000000066740831;//Gravitational constant
	ArrayList<Particle> part_list;
	Vec3 window;
	int core_count;
	boolean grav_on;
	boolean collide_on;
	
	Vec3 mass_center;
	private Vec3 mass_center_temp;
	double total_mass;
	private double total_mass_temp;
	CollisionThread c1,c2,c3,c4,c5,c6,c7,c8;
	GravityThread g1,g2,g3,g4,g5,g6,g7,g8;
	
	Field(Vec3 new_window, int CoreCount)
	{
		//Initializations
		this.part_list = new ArrayList<Particle>();
		this.mass_center = new Vec3();
		this.total_mass = 0.0;
		this.window = new_window;
		this.core_count = Math.min(CoreCount,8);
		
		this.mass_center_temp = new Vec3();
		this.total_mass_temp = 0.0;
		this.grav_on = true;
		this.collide_on = true;
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
			double thread_count = (double)(core_count);
			
			
			//Run particle-wall collision and reset particle accelerations
			for(int i = 0; i < this.part_list.size(); i++)
			{
				workingPart = this.part_list.get(i);
				workingPart.acc = new Vec3();
				workingPart.remove = wallCollision(workingPart);
			}
			
			
			//Run particle-particle collision
			double cdivided = this.part_list.size()/thread_count;
			
			this.c1 = new CollisionThread(this.part_list, timestep, 0.0, cdivided, collide_on, grav_on);
			if (thread_count > 1)
				this.c2 = new CollisionThread(this.part_list, timestep, cdivided, cdivided*2, this.collide_on, this.grav_on);
			if (thread_count > 2)
				this.c3 = new CollisionThread(this.part_list, timestep, cdivided*2, cdivided*3, this.collide_on, this.grav_on);
			if (thread_count > 3)
				this.c4 = new CollisionThread(this.part_list, timestep, cdivided*3, cdivided*4, this.collide_on, this.grav_on);
			if (thread_count > 4)
				this.c5 = new CollisionThread(this.part_list, timestep, cdivided*4, cdivided*5, this.collide_on, this.grav_on);
			if (thread_count > 5)
				this.c6 = new CollisionThread(this.part_list, timestep, cdivided*5, cdivided*6, this.collide_on, this.grav_on);
			if (thread_count > 6)
				this.c7 = new CollisionThread(this.part_list, timestep, cdivided*6, cdivided*7, this.collide_on, this.grav_on);
			if (thread_count > 7)
				this.c8 = new CollisionThread(this.part_list, timestep, cdivided*7, cdivided*8, this.collide_on, this.grav_on);
			
			this.c1.start();
			if (thread_count > 1)
				this.c2.start();
			if (thread_count > 2)
				this.c3.start();
			if (thread_count > 3)
				this.c4.start();
			if (thread_count > 4)
				this.c5.start();
			if (thread_count > 5)
				this.c6.start();
			if (thread_count > 6)
				this.c7.start();
			if (thread_count > 7)
				this.c8.start();
			try
			{ 
				this.c1.join();
				if (thread_count > 1)
					this.c2.join();
				if (thread_count > 2)
					this.c3.join();
				if (thread_count > 3)
					this.c4.join(); 
				if (thread_count > 4)
					this.c5.join();
				if (thread_count > 5)
					this.c6.join();
				if (thread_count > 6)
					this.c7.join();
				if (thread_count > 7)
					this.c8.join();
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
			
			
			/*
			// Update Gravity acceleration
			double gdivided = this.part_list.size()/thread_count;
			
			this.g1 = new GravityThread(this.part_list, timestep, 0.0, gdivided);
			if (thread_count > 1)
				this.g2 = new GravityThread(this.part_list, timestep, gdivided, gdivided*2);
			if (thread_count > 2)
				this.g3 = new GravityThread(this.part_list, timestep, gdivided*2, gdivided*3);
			if (thread_count > 3)
				this.g4 = new GravityThread(this.part_list, timestep, gdivided*3, gdivided*4);
			if (thread_count > 4)
				this.g5 = new GravityThread(this.part_list, timestep, gdivided*4, gdivided*5);
			if (thread_count > 5)
				this.g6 = new GravityThread(this.part_list, timestep, gdivided*5, gdivided*6);
			if (thread_count > 6)
				this.g7 = new GravityThread(this.part_list, timestep, gdivided*6, gdivided*7);
			if (thread_count > 7)
				this.g8 = new GravityThread(this.part_list, timestep, gdivided*7, gdivided*8);
			
			this.g1.start();
			if (thread_count > 1)
				this.g2.start();
			if (thread_count > 2)
				this.g3.start();
			if (thread_count > 3)
				this.g4.start();
			if (thread_count > 4)
				this.g5.start();
			if (thread_count > 5)
				this.g6.start();
			if (thread_count > 6)
				this.g7.start();
			if (thread_count > 7)
				this.g8.start();
			
			try
			{ 
				this.g1.join();
				if (thread_count > 1)
					this.g2.join();
				if (thread_count > 2)
					this.g3.join();
				if (thread_count > 3)
					this.g4.join(); 
				if (thread_count > 4)
					this.g5.join();
				if (thread_count > 5)
					this.g6.join();
				if (thread_count > 6)
					this.g7.join(); 
				if (thread_count > 7)
					this.g8.join(); 
			} catch (InterruptedException e){}
			*/
			
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