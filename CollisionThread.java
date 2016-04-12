import java.util.*;

class CollisionThread extends Thread
{
	static final double GravG = 0.000000000066740831;//Gravitational constant
	ArrayList<Particle> part_list;
	int begin;
	int end;
	double timestep;
	boolean grav_on, collide_on;
	
	CollisionThread(ArrayList<Particle> in_part_list, double in_timestep, double in_begin, double in_end, boolean in_collide_on, boolean in_grav_on)
	{
		this.part_list = in_part_list;
		this.timestep = in_timestep;
		this.begin = (int)in_begin;
		this.end = (int)in_end;
		this.grav_on = in_grav_on;
		this.collide_on = in_grav_on;
	}
	
	public void run()
	{
		Particle part1;
		Particle part2;
		
		for (int i = begin; i < end; i++)
		{
			part1 = this.part_list.get(i);
			
			for (int j = 0; j < this.part_list.size(); j++)
			{
				part2 = this.part_list.get(j);
				
				//Do Absorbsion Collision
				if((!part1.remove) && (!part2.remove) && (collide_on) && (part1 != part2))
					absorbCollision(part1,part2);
				
				//Do 'Bounce' Collision
				if((!part1.remove) && (collide_on) && (!part2.remove) && (part1 != part2))
						pressureCollision(part1,part2,timestep);
				
				//Do Gravity Calculation
				if ((grav_on) && (part1 != part2) && (!part1.remove) && (!part2.remove))//&& (distance >= Math.max(part1.radius, part2.radius))
				{
					double distance = part1.pos.distance(part2.pos);
					double VectorG = this.GravG * part2.mass / (distance*distance*distance);
					part1.acc.addi(VectorG * (part2.pos.x - part1.pos.x), VectorG * (part2.pos.y - part1.pos.y), VectorG * (part2.pos.z - part1.pos.z));
				}
			}
		}
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
		
		if ((distance >= Math.max(part2.radius, part1.radius)) && ((part1.mass >= 1) || (part2.mass >= 1)) )
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
}