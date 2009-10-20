package tbc.data.spatial;

import tbc.supercheck.Gen;

public class Bounds
{
    public static final String PARAM_MAX_DI = "Bounds.MAX_DI";
    
    public float x1;
    public float y1;
    public float x2;
    public float y2;
    
    public Bounds(float x1, float y1, float x2, float y2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
    
    public Bounds copy()
    {
        return new Bounds(x1, y1, x2, y2);
    }
    
    public float getWidth()
    {
        return x2 - x1;
    }
    
    public float getHeight()
    {
        return y2 - y1;
    }
    
    /** Multiply width and height by k. */
    public void enlarge(float k)
    {
        if (k == 1.0f) return;
        
        final float nubX = (getWidth() * k) / 2;
        final float nubY = (getHeight() * k) / 2;
        
        x1 -= nubX; x2 += nubX; y1 -= nubY; y2 += nubY;
    }
    
    /** Doesn't do any projection onto the plane - just ignores the z axis. */
    public boolean containsPoint(Point3D p)
    {
        return p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2;
    }
    
    public boolean containsPoint(Point2D p)
    {
        return p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2;
    }
    
    public boolean containsBounds(Bounds b)
    {
        return b.x1 >= x1 && b.x2 <= x2 && b.y1 >= y1 && b.y2 <= y2;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (other instanceof Bounds)
        {
            Bounds otherB = (Bounds) other;
            return x1 == otherB.x1 && x2 == otherB.x2 
                    && y1 == otherB.y1 && y2 == otherB.y2;
        }
        return false;
    }
    
    @Override
    public String toString() 
    {
        return "Bounds(frm:"+x1+","+y1+" to:"+x2+","+y2+")";
    }
    
    public static Bounds arbitrary(Gen gen) 
    {
    	final int maxDi = gen.getParams().getInt(PARAM_MAX_DI, 1000);
    	
        float fstX = gen.arbFloat(maxDi);
        float sndX = gen.arbFloat(maxDi);
        float fstY = gen.arbFloat(maxDi);
        float sndY = gen.arbFloat(maxDi);
        return new Bounds(fstX < sndX ? fstX : sndX,
                          fstY < sndY ? fstY : sndY,
                          fstX > sndX ? fstX : sndX,
                          fstY > sndY ? fstY : sndY);
    }

}
