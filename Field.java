import java.util.*;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

class Field
{	
	static final double GravG = 0.000000000066740831;//Gravitational constant
	ArrayList<Particle> part_list;
	Vec3 window;
	int calc_threads;
	boolean grav_on;
	boolean collide_on;
	
	Vec3 mass_center;
	private Vec3 mass_center_temp;
	double total_mass;
	private double total_mass_temp;
	
	ExecutorService executor;
	Object remove_lock;
	
	Field(Vec3 new_window, int CoreCount)
	{
		//Initializations
		this.part_list = new ArrayList<Particle>();
		this.mass_center = new Vec3();
		this.total_mass = 0.0;
		this.window = new_window;
		this.calc_threads = Math.min(CoreCount-1,8);
		
		this.mass_center_temp = new Vec3();
		this.total_mass_temp = 0.0;
		this.grav_on = true;
		this.collide_on = true;
		
		executor = Executors.newCachedThreadPool();
		remove_lock = new Object();
	}
	
	
	
	public synchronized void EulerIntegrate(double timestep)
	{
		//Reset running totals for center of mass and total mass
		total_mass_temp = 0.0;
		mass_center_temp = new Vec3();
		Particle part1;
		
		ListIterator<Particle> partIterator;
		double thread_count = (double)(calc_threads);
		
		//Run particle-wall collision and reset particle accelerations
		for(int i = 0; i < this.part_list.size(); i++)
		{
			part1 = this.part_list.get(i);
			part1.acc = new Vec3();
			part1.remove = wallCollision(part1);
		}
		
		
		//Run particle-particle collision
		double divided = this.part_list.size()/thread_count;
		boolean t_grav_on = this.grav_on;
		boolean t_collide_on = this.collide_on;
		
		CountDownLatch latch = new CountDownLatch(calc_threads);
		for (int i = 0; i<calc_threads;i++)
		{
			executor.execute(new EulerRun(this.part_list, timestep, divided * i,divided * (i+1),t_grav_on,t_collide_on,latch));
		}
		
		//Wait
		try {latch.await();}catch(InterruptedException e){}
		
		//Remove particles no longer in the field
		synchronized (remove_lock)
		{
			for(int i = 0; i < this.part_list.size(); i++)
			{
				part1 = this.part_list.get(i);
				if (part1.remove)
				{
					this.part_list.remove(i);
				}
			}
		}
		
		
		//Do Velocity and Position Update, and Center of Mass Iteration
		for(int i = 0; i < this.part_list.size(); i++)
		{
			part1 = this.part_list.get(i);
			
			part1.vel.x = part1.vel.x + (part1.acc.x * timestep);
			part1.vel.y = part1.vel.y + (part1.acc.y * timestep);
			part1.vel.z = part1.vel.z + (part1.acc.z * timestep);
			
			part1.pos.x = part1.pos.x + (part1.vel.x * timestep);
			part1.pos.y = part1.pos.y + (part1.vel.y * timestep);
			part1.pos.z = part1.pos.z + (part1.vel.z * timestep);
			
			mass_center_temp.addi(part1.mass * part1.pos.x, part1.mass * part1.pos.y, part1.mass * part1.pos.z);
			total_mass_temp += part1.mass;
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