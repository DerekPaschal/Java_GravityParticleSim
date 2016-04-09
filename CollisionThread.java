import java.util.*;

class CollisionThread implements Runnable
{
	static final double GravG = 0.000000000066740831;//Gravitational constant
	Field field;
	int begin;
	int end;
	double timestep;
	CollisionThread(Field caller_field, double in_begin, double in_end, double in_timestep)
	{
		this.field = caller_field;
		this.begin = (int)in_begin;
		this.end = (int)in_end;
		this.timestep = in_timestep;
	}
	
	public void run()
	{
		Particle workingPart;
		for (int i = begin; i < end; i++)
		{
			workingPart = this.field.part_list.get(i);
			runCollisions(workingPart,timestep);
		}
	}
	
	public void runCollisions(Particle part1, double timestep)
	{
		Particle workingPart;
		//Particle Absorb Detection
		if (!part1.remove)
		{	
			ListIterator<Particle> partIterator = this.field.part_list.listIterator();
			while(partIterator.hasNext())
			{
				workingPart = partIterator.next();
				absorbCollision(part1, workingPart);
			}
		}
		//Particle 'bounce' Detection
		if (!part1.remove)
		{	
			ListIterator<Particle> partIterator = this.field.part_list.listIterator();
			while(partIterator.hasNext())
			{
				workingPart = partIterator.next();
				pressureCollision(part1, workingPart,timestep);
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
}