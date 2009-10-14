package tbc.data.spatial;

import tbc.data.Constants;
import tbc.supercheck.Gen;

public class Point2D
{
    public static final Point2D ZERO = new Point2D(0.0f, 0.0f);
    
    public static final String PARAM_MAX_DI = "Point2D.MAX_DI";
    
    public float x;
    public float y;
    
    public Point2D(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    
    public Point2D copy()
    {
        return new Point2D(x, y);
    }
    
    public float getLength()
    {
        return (float) Math.sqrt((x*x) + (y*y));
    }
    
    public boolean isZero()
    {
        return x == 0.0f && y == 0.0f;
    }
    
    public Point2D add(Point2D other)
    {
        return new Point2D(x + other.x, y + other.y);
    }
    
    public Point2D subtract(Point2D other)
    {
        return new Point2D(x - other.x, y - other.y);
    }
    
    public Point2D negative()
    {
        return new Point2D(-x, -y);
    }
    
    public float distanceFrom(Point2D other)
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
    public float dot(Point2D other)
    {
        return x*other.x + y*other.y;
    }
    
    public Point2D multiply(float scalar)
    {
        return new Point2D(x * scalar, y * scalar);
    }
    
    public float radsFromOrigin()
    {
        return (float) Math.atan2(-y, x) + Constants.QUARTER_ROT;
    }
    
    /** Retuns a normalised vector with angle `rads` from north. */
    public static Point2D normAtRads(float rads)
    {
        Point2D p = new Point2D((float) Math.sin(rads), 
                           (float) Math.cos(rads))
                    .normalise();
        
        return p;
    }
    
    public Point2D normalise()
    {
        final float length = getLength();
        if (length > 0)
        {
            return new Point2D(x / length, y / length);
        }
        else
        {
            return new Point2D(0, 0);
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Point2D)
        {
            Point2D p = (Point2D) o;
            if (p.x == x && p.y == y)
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString()
    {
        return "2D("+x+","+y+")";
    }
    
    public static Point2D arbitrary(Gen gen)
    {
       int maxDi = gen.getIntParam(PARAM_MAX_DI);
       
       switch (gen.select(0.1f, 0.45f, 0.45f))
       {
       case 0:
           return ZERO;
       case 1:
    	   return new Point2D(-gen.arbFloat(maxDi), -gen.arbFloat(maxDi));
       case 2: default:
           return new Point2D(gen.arbFloat(maxDi), gen.arbFloat(maxDi));
       }
    }
}
