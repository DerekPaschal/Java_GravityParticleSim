import java.util.*;

class GravityThread  extends Thread
{
	static final double GravG = 0.000000000066740831;//Gravitational constant
	ArrayList<Particle> part_list;
	int begin;
	int end;
	double timestep;
	
	GravityThread(ArrayList<Particle> in_part_list, double in_timestep, double in_begin, double in_end)
	{
		this.part_list = in_part_list;
		this.timestep = in_timestep;
		this.begin = (int)in_begin;
		this.end = (int)in_end;
	}
	
	public void run()
	{
		Particle part1;
		for (int i = begin; i < end; i++)
		{
			part1 = this.part_list.get(i);
			for (int j = 0; j < this.part_list.size(); j++)
			{
				Particle part2 = this.part_list.get(j);
				
				double distance = part1.pos.distance(part2.pos);
				if ((part1 != part2) && (distance >= Math.max(part1.radius, part2.radius)))//distance >= (part1.radius + part2.radius) * 0.1
				{
					double VectorG = this.GravG * part2.mass / (distance*distance*distance);
					part1.acc.addi(VectorG * (part2.pos.x - part1.pos.x), VectorG * (part2.pos.y - part1.pos.y), VectorG * (part2.pos.z - part1.pos.z));
				}
			}	
		}
	}
	
	///------------------------------------------------------------------
	/// Calculate acceleration from Gravity between the current Particle
	/// and the rest of the field. Sets this acceleration to the 
	/// acceleration vector of the current Particle.
	///------------------------------------------------------------------ 
	public void updateGravAcc(Particle part1, double timestep)
	{
		
	}
}