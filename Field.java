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
	//GravityThread g1,g2,g3,g4,g5,g6,g7,g8;
	
	Field(Vec3 new_window, int CoreCount)
	{
		//Initializations
		this.part_list = new ArrayList<Particle>();
		this.mass_center = new Vec3();
		this.total_mass = 0.0;
		this.window = new_window;
		this.core_count = Math.min(CoreCount,7); //Currently no improvement is noted when core_count > 7
		
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
			this.c1.start();
			if (thread_count > 1)
			{
				this.c2 = new CollisionThread(this.part_list, timestep, cdivided, cdivided*2, this.collide_on, this.grav_on);
				this.c2.start();
			}
			if (thread_count > 2)
			{
				this.c3 = new CollisionThread(this.part_list, timestep, cdivided*2, cdivided*3, this.collide_on, this.grav_on);
				this.c3.start();
			}
			if (thread_count > 3)
			{
				this.c4 = new CollisionThread(this.part_list, timestep, cdivided*3, cdivided*4, this.collide_on, this.grav_on);
				this.c4.start();
			}
			if (thread_count > 4)
			{
				this.c5 = new CollisionThread(this.part_list, timestep, cdivided*4, cdivided*5, this.collide_on, this.grav_on);
				this.c5.start();
			}
			if (thread_count > 5)
			{
				this.c6 = new CollisionThread(this.part_list, timestep, cdivided*5, cdivided*6, this.collide_on, this.grav_on);
				this.c6.start();
			}
			if (thread_count > 6)
			{
				this.c7 = new CollisionThread(this.part_list, timestep, cdivided*6, cdivided*7, this.collide_on, this.grav_on);
				this.c7.start();
			}
			if (thread_count > 7)
			{
				this.c8 = new CollisionThread(this.part_list, timestep, cdivided*7, cdivided*8, this.collide_on, this.grav_on);
				this.c8.start();
			}
				
			try
			{ 
				/*
				if (thread_count == 1)
					this.c1.join();
				if (thread_count == 2)
					this.c2.join();
				if (thread_count == 3)
					this.c3.join(); 
				if (thread_count == 4)
					this.c4.join();
				if (thread_count == 5)
					this.c5.join();
				if (thread_count == 6)
					this.c6.join();
				if (thread_count == 7)
					this.c7.join();
				if (thread_count == 8)
					this.c8.join();
				*/
				
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

	/*
	public void pressureCollision(Particle part1, Particle part2, double timestep)
	{
		if (!part1.bounces || !part2.bounces)
			return;
		
		double distance = part1.pos.distance(part2.pos);
		//Detect and resolve collisions
		double r = (part2.radius + part1.radius);
										
		if (distance >= r)
			return;
		
		//Find unit normal direction between particles
		Vec3 norm = new Vec3(part2.pos.x - part1.pos.x, part2.pos.y - part1.pos.y, part2.pos.z - part1.pos.z);
		Vec3 unit_norm = norm;
		if (norm.length() >= 0.1)
			unit_norm.divi(norm.length());
		else
			return;
		
		double restitution = 1.0;
		//Calculate Relative velocity
		Vec3 rv = new Vec3(part2.vel.x - part1.vel.x, part2.vel.y - part1.vel.y, part2.vel.z - part1.vel.z);
		//Calculate Velocity in normal direction and apply restitution if negative
		double velAlongNorm = rv.DotProduct(unit_norm);
		if(velAlongNorm > 0)
			restitution = Math.min(part1.elasticity, part2.elasticity);
		
		//Find overlap of particles
		double overlap = r - distance;
		
		double repulse = part1.repulse;// * part1.radius;
		double press_acc = restitution * repulse * overlap * overlap;//Math.log((overlap*10)+1)
		
		//Eventual additional Velocity Vector
		Vec3 press_vel = unit_norm.mult(press_acc * timestep);

		part2.vel.addi_vec(press_vel.div(part2.mass));//part2.mass
		part1.vel.addi_vec(press_vel.div(-part1.mass)); //part1.mass
		
		return;
	}
	
	public void absorbCollision(Particle part1, Particle part2)
	{	
		if (part1 == part2 || (part1.mass < 1 && part2.mass < 1))
			return;
		
		double distance = part1.pos.distance(part2.pos);
		
		if ((distance >= Math.max(part2.radius, part1.radius)) && ((part1.mass >= 1) && (part2.mass >= 1)) )
			return;
		if (distance >= (part1.radius + part2.radius))
			return;
		
		if (part1.mass >= part2.mass)
			part2.remove = true;
		if (part2.mass > part1.mass)
			part1.remove = true;
		
		double inverse_total_mass = 0.5;
		if (part1.mass >= 1 || part2.mass >= 1)
		{
			inverse_total_mass = 1.0/(part1.mass + part2.mass);
		}
		
		if(part1.mass >= 1 && part2.mass >= 1)
		{
			Particle heavyPart = part1;
			if (part2.mass > part1.mass)
				heavyPart = part2;
			
			heavyPart.vel = new Vec3(((part2.vel.x * part2.mass) + (part1.vel.x * part1.mass))*inverse_total_mass,
									((part2.vel.y * part2.mass) + (part1.vel.y * part1.mass))*inverse_total_mass,
									((part2.vel.z * part2.mass) + (part1.vel.z * part1.mass))*inverse_total_mass);
			
			heavyPart.radius = Math.cbrt((part1.radius*part1.radius*part1.radius) + (part2.radius*part2.radius*part2.radius));
			
			heavyPart.RGB = new Vec3(((part2.RGB.x * part2.mass) + (part1.RGB.x * part1.mass))*inverse_total_mass,
								((part2.RGB.y * part2.mass) + (part1.RGB.y * part1.mass))*inverse_total_mass,
								((part2.RGB.z * part2.mass) + (part1.RGB.z * part1.mass))*inverse_total_mass );
								
			heavyPart.pos = new Vec3(((part2.pos.x * part2.mass) + (part1.pos.x * part1.mass))*inverse_total_mass,
								((part2.pos.y * part2.mass) + (part1.pos.y * part1.mass))*inverse_total_mass,
								((part2.pos.z * part2.mass) + (part1.pos.z * part1.mass))*inverse_total_mass );
								
			heavyPart.mass = part1.mass + part2.mass;
		}
	}
	*/
	
}