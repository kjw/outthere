package tbc.data.spatial;

import tbc.supercheck.Gen;

public class Point3D
{
    public static final Point3D ZERO = new Point3D(0.0f, 0.0f, 0.0f);
    
    public static final String PARAM_MAX_DI = "Point3D.MAX_DI";
    
    public float x;
    public float y;
    public float z;
    
    public Point3D(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Point3D add(Point3D other)
    {
        return new Point3D(x + other.x, y + other.y, z + other.z);
    }
    
    public Point3D subtract(Point3D other)
    {
        return new Point3D(x - other.x, y - other.y, z - other.z);
    }
    
    public Point3D multiply(float scalar)
    {
        return new Point3D(x * scalar, y * scalar, z * scalar);
    }
    
    public Point3D negative()
    {
        return new Point3D(-x, -y, -z);
    }
    
    public Point3D copy()
    {
        return new Point3D(x, y, z);
    }

    public float getLength()
    {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }
    
    public boolean isZero()
    {
        return x == 0.0f && y == 0.0f && z == 0.0f;
    }
    
    public float distanceFrom(Point3D other)
    {
        return this.subtract(other).getLength();
    }
    
    /**
     * Remember, remember:
     * -1      180 - The two vectors point in opposite directions
     * < 0     More than 90o - Obtuse
     * = 0     90 - The two vectors are at right angles ie. they are orthagonal
     * > 0     Less than 90o - Acute
     * 1       0 - The two vectors point in the same direction
     */
    public float dot(Point3D other)
    {
        return x*other.x + y*other.y + z*other.z;
    }
    
    public Point3D cross(Point3D other)
    {
        return new Point3D(y * other.z - other.y * z,
                           other.x * z - x * other.z,
                           x * other.y - other.x * y);
    }
    
    public Point3D normalise()
    {
        final float length = getLength();
        if (length > 0)
        {
            return new Point3D(x / length, y / length, z / length);
        }
        else
        {
            return new Point3D(0, 0, 0);
        }
    }
    
    public Point2D toPoint2D(Axis onAxis)
    {
        if (onAxis == Axis.X)
        {
            return new Point2D(y, z);
        }
        else if (onAxis == Axis.Y)
        {
            return new Point2D(x, z);
        }
        else
        {
            return new Point2D(x, y);
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Point3D)
        {
            Point3D p = (Point3D) o;
            if (p.x == x && p.y == y && p.z == z)
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString()
    {
        return "3D("+x+","+y+","+z+")";
    }
    
    public static Point3D arbitrary(Gen gen)
    {
       int maxDi = gen.getIntParam(PARAM_MAX_DI);
       
       switch (gen.select(0.1f, 0.45f, 0.45f))
       {
       case 0:
           return ZERO;
       case 1:
    	   return new Point3D(-gen.arbFloat(maxDi), -gen.arbFloat(maxDi), -gen.arbFloat(40));
       case 2: default:
           return new Point3D(gen.arbFloat(maxDi), gen.arbFloat(maxDi), gen.arbFloat(40));
       }
    }

}
