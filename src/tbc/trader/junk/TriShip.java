package tbc.trader.junk;
import tbc.scene.tidbits.Bobin;

public class TriShip extends Bobin
{
    @Override
    public int[] getVerts()
    {
        return new int[] {
                0, one, 0,
                one, -one, 0,
                -one, -one, 0,
        };
    }
    
    @Override
    public int[] getColors()
    {
        return new int[] {
                one, one, one, one,
                one, one, one, one,
                one, one ,one, one,
        };
    }
}
