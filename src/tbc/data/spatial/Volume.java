package tbc.data.spatial;

import tbc.supercheck.Gen;

public class Volume
{
	public static final String PARAM_MAX_DI = "Volume.MAX_DI";
	
    public float x1;
    public float y1;
    public float z1;
    public float x2;
    public float y2;
    public float z2;
    
    public Volume()
    {
    }
    
    public Volume(float x1, float x2, float y1, float y2, float z1, float z2)
    {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.z1 = z1;
        this.z2 = z2;
    }
    
    /** Projects the volume onto a plane at proj=0 by ignoring the axis proj. */
    public Bounds toBounds(Axis proj)
    {
        if (proj == Axis.X)
        {
            return new Bounds(y1, y2, z1, z2);
        }
        else if (proj == Axis.Y)
        {
            return new Bounds(x1, x2, z1, z2);
        }
        else
        {
            return new Bounds(x1, x2, y1, y2);
        }
    }
    
    /** @return The returned volume will encompass the volume this method is 
     * called on and the volume `other`.
     */
    public Volume include(Volume other)
    {
        return new Volume(x1 < other.x1 ? x1 : other.x1,
                          x2 > other.x2 ? x2 : other.x2,
                          y1 < other.y1 ? y1 : other.y1,
                          y2 > other.y2 ? y2 : other.y2,
                          z1 < other.z1 ? z1 : other.z1,
                          z2 > other.z2 ? z2 : other.z2);
    }
    
    @Override
    public String toString()
    {
        return "Volume(frm:"+x1+","+y1+","+z1+" to:"+x2+","+y2+","+z2+")";
    }
    
    public static Volume arbitrary(Gen gen)
    {
    	final int maxDi = gen.getParams().getInt(PARAM_MAX_DI, 1000);
    	
        float fstX = gen.arbFloat(maxDi);
        float sndX = gen.arbFloat(maxDi);
        float fstY = gen.arbFloat(maxDi);
        float sndY = gen.arbFloat(maxDi);
        float fstZ = gen.arbFloat(maxDi);
        float sndZ = gen.arbFloat(maxDi);
        return new Volume(fstX < sndX ? fstX : sndX,
                          fstX > sndX ? fstX : sndX,
                          fstY < sndY ? fstY : sndY,
                          fstY > sndY ? fstY : sndY,
                          fstZ < sndZ ? fstZ : sndZ,
                          fstZ > sndZ ? fstZ : sndZ);
    }

}
