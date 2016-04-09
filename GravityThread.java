import java.util.*;

class GravityThread implements Runnable
{
	static final double GravG = 0.000000000066740831;//Gravitational constant
	Field field;
	int begin;
	int end;
	double timestep;
	GravityThread(Field caller_field, double in_begin, double in_end, double in_timestep)
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
			updateGravAcc(workingPart, this.timestep);
		}
	}
	
	///------------------------------------------------------------------
	/// Calculate acceleration from Gravity between the current Particle
	/// and the rest of the field. Sets this acceleration to the 
	/// acceleration vector of the current Particle.
	///------------------------------------------------------------------ 
	public void updateGravAcc(Particle part1, double timestep)
	{
		part1.acc = new Vec3();
		if(!this.field.grav_on)
			return;
		
		//ListIterator<Particle> partIterator = part_list.listIterator();
		//while(partIterator.hasNext())
		for (int i = 0; i < this.field.part_list.size(); i++)
		{
			Particle part2 = this.field.part_list.get(i);
			//Gravity(part1, workingPart);
			double distance = part1.pos.distance(part2.pos);
			if (distance >= (part1.radius + part2.radius) * 0.1)
			{
				double VectorG = this.GravG * part2.mass / (distance*distance*distance);
				part1.acc.addi(VectorG * (part2.pos.x - part1.pos.x), VectorG * (part2.pos.y - part1.pos.y), VectorG * (part2.pos.z - part1.pos.z));
			}
		}	
	}
}