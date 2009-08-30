package tbc.trader.junk;

import tbc.scene.tidbits.Bobin;

public class Thrust extends Bobin
{
    @Override
    public int[] getVerts()
    {
        return new int[] {
                // top
                -one/2, -one, 0,
                one/2, -one, 0,
                0, -(one+(one/2)), 0,
                // middle
                -one/2, -(one+(one/2)), 0,
                one/2, -(one+(one/2)), 0,
                0, -(one*2), 0,
                // bottom
                -one/2, -(one*2), 0,
                one/2, -(one*2), 0,
                0, -((one*2)+(one/2)), 0,
        };
    }
    
    @Override
    public int[] getColors()
    {
        return new int[] {
                one, one/4, 0, one,
                one, one/4, 0, one,
                one, one/4, 0, one,

                one, one/2, 0, one,
                one, one/2, 0, one,
                one, one/2, 0, one,

                one, one, 0, one,
                one, one, 0, one,
                one, one, 0, one,
        };
    }

}
