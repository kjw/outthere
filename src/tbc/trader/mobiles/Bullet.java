package tbc.trader.mobiles;

import tbc.scene.PlaneWorld;
import tbc.trader.GameContext;
import tbc.trader.Info;
import tbc.trader.Medium;
import tbc.trader.Mobile;
import tbc.trader.Info.BulletType;
import tbc.trader.Info.MobileType;
import tbc.trader.junk.Cube;

public class Bullet extends Mobile
{
    public Bullet(MobileType t, PlaneWorld pw, Medium m, GameContext gc)
    {
        super(t, pw, m, gc);
        
        addChild(new Cube(0.5f));
    }
    
    @Override
    public void onUpdate(long tDelta, long tIndex)
    {
        super.onUpdate(tDelta, tIndex);
        
        /* Kill the bullet if it has outlived its death time. */
        BulletType bt = (Info.BulletType) getType();
        if (tIndex >= getCreationTime() + bt.deathTime)
        {
            getWorld().removeChild(this);
        }
    }
}
