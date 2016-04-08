import java.util.*;
import java.io.IOException;

class Field
{	
	static final double GravG = 0.000000000066740831;//Gravitational constant
	
	LinkedList<Particle> part_list;
	Vec3 window;
	
	Vec3 mass_center;
	private Vec3 mass_center_temp;
	double total_mass;
	private double total_mass_temp;
	
	Field(Vec3 new_window)
	{
		//Initializations
		this.part_list = new LinkedList<Particle>();
		this.mass_center = new Vec3();
		this.total_mass = 0.0;
		this.window = new_window;
		
		//Defaults
		this.mass_center_temp = new Vec3();
		this.total_mass_temp = 0.0;
	}
	
	
	
	public void EulerIntegrate(double timestep)
	{
		Particle workingPart;
		//Reset running totals for center of mass and total mass
		total_mass_temp = 0.0;
		mass_center_temp = new Vec3();
		
		synchronized(this.part_list)
		{
			
			//Run collision code
			ListIterator<Particle> partIterator = part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				runCollisions(workingPart,timestep);
				if (workingPart.remove)
				{
					partIterator.remove();
				}
			}
			
			
			// Update Particle accelerations and collisions
			partIterator = part_list.listIterator();
			while (partIterator.hasNext())
			{
				workingPart = partIterator.next();
				updateAcc(workingPart, timestep);
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

	
	
	public void runCollisions(Particle part1, double timestep)
	{
		//Wall Collision Detection
		Particle workingPart;
		part1.remove = wallCollision(part1);
		
		//Particle Absorb Detection
		if (!part1.remove)
		{	
			ListIterator<Particle> partIterator = part_list.listIterator();
			while(partIterator.hasNext())
			{
				workingPart = partIterator.next();
				absorbCollision(part1, workingPart);
			}
		}
		//Particle 'bounce' Detection
		if (!part1.remove)
		{	
			ListIterator<Particle> partIterator = part_list.listIterator();
			while(partIterator.hasNext())
			{
				workingPart = partIterator.next();
				pressureCollision(part1, workingPart,timestep);
			}
		}
		
	}
	
	

	///------------------------------------------------------------------
	/// Calculate acceleration from Gravity between the current Particle
	/// and the Working Particle. Adds this acceleration to the 
	/// acceleration vector of the current Particle.
	///------------------------------------------------------------------ 
	public void Gravity(Particle part1, Particle part2)
	{
		double distance = part1.pos.distance(part2.pos);
		if (distance < (part1.radius + part2.radius) * 0.1)
			return;
		double VectorG = GravG * part2.mass / (distance*distance*distance);
		
		part1.acc.addi(VectorG * (part2.pos.x - part1.pos.x), VectorG * (part2.pos.y - part1.pos.y), VectorG * (part2.pos.z - part1.pos.z));
	}
	
	
	
	
	
	
	public void updateAcc(Particle part1, double timestep)
	{
		part1.acc = new Vec3();
		
		ListIterator<Particle> partIterator = part_list.listIterator();
		while(partIterator.hasNext())
		{
			Particle workingPart = partIterator.next();
			Gravity(part1, workingPart);
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
	
	
	
	public double findDistance(Particle part1, Particle part2)
	{
		return part1.pos.distance(part2.pos);
	}
	
	
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
		
		double repulse = 0.1 / GravG;// * part1.radius;
		double press_acc = restitution * repulse * overlap * overlap;//Math.log((overlap*10)+1)
		
		//Eventual additional Velocity Vector
		Vec3 press_vel = unit_norm.mult(press_acc * timestep);

		part2.vel.addi_vec(press_vel.div(part2.mass));//part2.mass
		part1.vel.addi_vec(press_vel.div(-part1.mass)); //part1.mass
		
		return;
	}

	
	
	public void absorbCollision(Particle part1, Particle part2)
	{	
		if (part1 == part2 || part1.radius > part2.radius || part1.mass <= 0 || part2.mass <= 0)
			return;
		
		double distance = part1.pos.distance(part2.pos);
		
										
		if (distance >= part2.radius - (part1.radius/1.5))
			return;
		
		part1.remove = true;
		double mass_add = part1.mass + part2.mass;
		part2.vel = new Vec3(((part2.vel.x * part2.mass) + (part1.vel.x * part1.mass))/mass_add,
								((part2.vel.y * part2.mass) + (part1.vel.y * part1.mass))/mass_add,
								((part2.vel.z * part2.mass) + (part1.vel.z * part1.mass))/mass_add);
		
		part2.radius = Math.cbrt((part1.radius*part1.radius*part1.radius) + (part2.radius*part2.radius*part2.radius));
		//part2.radius = Math.sqrt((part1.radius*part1.radius) + (part2.radius*part2.radius));
		part2.RGB = new Vec3(((part2.RGB.x * part2.mass) + (part1.RGB.x * part1.mass))/mass_add,
							((part2.RGB.y * part2.mass) + (part1.RGB.y * part1.mass))/mass_add,
							((part2.RGB.z * part2.mass) + (part1.RGB.z * part1.mass))/mass_add );
							
		part2.pos = new Vec3(((part2.pos.x * part2.mass) + (part1.pos.x * part1.mass))/mass_add,
							((part2.pos.y * part2.mass) + (part1.pos.y * part1.mass))/mass_add,
							((part2.pos.z * part2.mass) + (part1.pos.z * part1.mass))/mass_add );
							
		part2.mass += part1.mass;
	}
	
	
	
	public boolean wallCollision(Particle part1)
	{
		if (part1.pos.y > (window.y + part1.radius))
		{
			return true;
		}
		
		if (part1.pos.y < -part1.radius)
		{
			return true;
		}
		
		if (part1.pos.x > (window.x + part1.radius))
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