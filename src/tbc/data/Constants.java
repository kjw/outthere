package tbc.data;

public final class Constants
{
    /** The number of rads in a rotation. */
    public static final float ONE_ROT     = (float) (2 * Math.PI);
    
    /** The number of rads in a half of a rotation. */
    public static final float HALF_ROT    = (float) (Math.PI);
    
    /** The number of rads in a quarter of a rotation. */
    public static final float QUARTER_ROT = (float) (Math.PI / 2.0f);
    
    public static final float ATAN2_POS_Y = (float) Math.atan2(1, 0);
    
    public static final float ATAN2_NEG_Y = (float) Math.atan2(-1, 0);
    
    public static final float ATAN2_NEG_X = (float) Math.atan2(0, -1);
    
    public static final float ATAN2_POS_X = (float) Math.atan2(0, 1);
    
    public static final float RAD_TO_DEG  = (float) (180.0f / Math.PI);
    
    public static final float DEG_TO_RAD  = (float) (Math.PI / 180.0);
}
