class Vec3
{
	double x;
	double y;
	double z;
	
	Vec3()
	{
		x = 0.0;
		y = 0.0;
		z = 0.0;
	}
	
	Vec3(double new_x, double new_y, double new_z)
	{
		x = new_x;
		y = new_y;
		z = new_z;
	}
	
	Vec3(Vec3 B)
	{
		x = B.x;
		y = B.y;
		z = B.z;
	}
	
	
	
	
	public Vec3 add(double a, double b, double c)
	{
		return new Vec3 (x+a, y+b, z+c);
	}
	
	public Vec3 sub(double a, double b, double c)
	{
		return new Vec3 (x-a, y-b, z-c);
	}
	
	public Vec3 mult(double d)
	{
		return new Vec3(x*d, y*d, z*d);
	}
	
	public Vec3 div(double d)
	{
		return new Vec3(x/d,y/d,z/d);
	}
	
	
	
	
	public Vec3 add_vec(Vec3 B)
	{
		return new Vec3(x + B.x, y + B.y, z + B.z);
	}
	
	public Vec3 sub_vec(Vec3 B)
	{
		return new Vec3(x - B.x, y - B.y, z - B.z);
	}
	
	
	
	
	public void addi(double a, double b, double c)
	{
		x+=a; y+=b; z+=c;
		return;
	}
	
	public void multi(double d)
	{
		x*=d; y*=d; z*=d;
		return;
	}
	
	public void divi(double d)
	{
		x/=d; y/=d; z/=d;
		return;
	}
	
	
	
	
	public void addi_vec(Vec3 B)
	{
		x += B.x; y += B.y; z += B.z;
	}
	
	
	
	
	public double DotProduct(Vec3 B)
	{
		return (x*B.x + y*B.y + z*B.z);
	}
	
	public double length()
	{
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public double distance(Vec3 B)
	{
		return Math.sqrt((x - B.x)*(x - B.x) + (y - B.y)*(y - B.y) + (z - B.z)*(z- B.z));		
	}
}