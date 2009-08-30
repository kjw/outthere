package tbc.trader;

import tbc.data.Constants;

public class Medium
{
    private float angularDrag; // rads per second per second
    private float movementDrag; // units per second per second
    
    public float getAngularDrag()
    {
        return angularDrag;
    }
    
    public float getMovementDrag()
    {
        return movementDrag;
    }
    
    public static Medium createVacuum()
    {
        Medium m = new Medium();
        m.angularDrag = 0.0f;
        m.movementDrag = 0.0f;
        return m;
    }
    
    public static Medium createAir()
    {
        Medium m = new Medium();
        m.angularDrag = Constants.ONE_ROT;
        m.movementDrag = 2.0f;
        return m;
    }
}
