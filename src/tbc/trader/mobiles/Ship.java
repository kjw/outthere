package tbc.trader.mobiles;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import tbc.data.spatial.Axis;
import tbc.data.spatial.Point2D;
import tbc.data.spatial.Point3D;
import tbc.data.spatial.Volume;
import tbc.scene.PlaneWorld;
import tbc.scene.ScnObj;
import tbc.scene.tidbits.Emitter;
import tbc.trader.GameContext;
import tbc.trader.Info;
import tbc.trader.Item;
import tbc.trader.Medium;
import tbc.trader.Mobile;
import tbc.trader.Info.ShipType;
import tbc.trader.Info.WeaponType;
import tbc.trader.junk.Cube;
import tbc.trader.junk.Thrust;
import tbc.trader.junk.TriShip;

/**
 * @author Karl Ward
 */
public class Ship extends Mobile
{
    public interface AiDelegate 
    {
        public void onAiUpdate(Ship ship, long tDelta, long tIndex);
        public void onLatentAiUpdate(Ship ship, long tDelta, long tIndex);
    }
    
    private class WeaponStateInfo
    {
        // TODO This stuff could be moved into a Weapon subclass of Item.
        private float   fireAngle     = 0.0f;
        private long    fireStartTime = -1l;
        private long    lastFireTime  = -1l;
        private boolean prepared      = false;
        private boolean firing        = false;
        private Item    weapon        = Item.NOTHING;
    }
    
    private WeaponStateInfo[] weaponStates;
    
    private Item[] equipList;
    
    private AiDelegate aiDelegate;
    
    private ScnObj forwardThrustVisual;
    
    public Ship(Info.ShipType st, PlaneWorld pw, Medium m, GameContext gc, AiDelegate aid)
    {
        super(st, pw, m, gc);
        this.aiDelegate = aid;
        
        /* Set up the weapon and equipment list based on ship type attributes. */
        weaponStates = new WeaponStateInfo[st.wepMountPoints];
        for (int i=0; i<weaponStates.length; i++)
        {
            weaponStates[i] = new WeaponStateInfo();
        }
        
        /* Set up the weapon emitter objects, shield and hull indicator planes,
         * the ship model, thrust emitters, damage emitter and shield hit
         * effect.
         */
        forwardThrustVisual = new Thrust();
        forwardThrustVisual.setVisible(false);
        
        addChild(new TriShip());
        addChild(forwardThrustVisual);
    }
    
    @Override
    public void onUpdate(long tDelta, long tIndex)
    {
        aiDelegate.onAiUpdate(this, tDelta, tIndex);
        super.onUpdate(tDelta, tIndex);
        aiDelegate.onLatentAiUpdate(this, tDelta, tIndex);
        
        /* Now have a look what our rot and forward acceleration are. If either
         * is non-zero then we make the thrust emitters fire.
         */
        forwardThrustVisual.setVisible(getForwardAccel().getLength() != 0);
        
        /* Do we want to make our weapons fire? */
        for (WeaponStateInfo wsi : weaponStates)
        {
            // TODO Move into Weapon?
            if (wsi.firing)
            {
                WeaponType wt = (WeaponType) wsi.weapon.getType();
                if (tIndex - wsi.fireStartTime >= wt.bringUpTime
                        && tIndex >= wsi.lastFireTime + wt.shotIntervalTime)
                {
                    Info.BulletType bt = (Info.BulletType) getContext().getInfoSet().get("bullet/pow");
                    Bullet b = new Bullet(bt, getWorld(), getMedium(), getContext());
                    b.setRelativePos(getAbsolutePos());
                    b.setAngle(wsi.fireAngle);
                    b.setForwardAccel(bt.forwardAccel);
                    getWorld().addChild(b);
                    
                    wsi.lastFireTime = tIndex;
                }
            }
        }
    }
    
    public void startPreparedWeaponFire(float angle)
    {
        long now = getScene().getNow();
        
        for (WeaponStateInfo wsi : weaponStates)
        {
            if (wsi.prepared)
            {
                wsi.fireAngle = angle;
                wsi.lastFireTime = now - ((WeaponType)wsi.weapon.getType()).shotIntervalTime;
                wsi.fireStartTime = now;
                wsi.firing = true;
            }
        }
        
        Log.i("Player", "Firing...");
    }
    
    public void stopAllWeaponFire()
    {
        for (WeaponStateInfo wsi : weaponStates)
        {
            wsi.firing = false;
        }
        
        Log.i("Player", "Stopped fire...");
    }
    
    public void movePreparedWeaponFire(float angle)
    {
        for (WeaponStateInfo wsi : weaponStates)
        {
            if (wsi.prepared)
            {
                wsi.fireAngle = angle;
            }
        }
    }
    
    public void setWeaponSlotPrepared(int slotId, boolean prepared)
    {
        weaponStates[slotId].prepared = prepared;
    }
    
    public void toggleWeaponSlotPrepared(int slotId)
    {
        weaponStates[slotId].prepared = !weaponStates[slotId].prepared;
        
        Log.i("Player", "Weapon in slot = 0"
                + "\ntype = " + weaponStates[slotId].weapon.getType().key
                + "\nprepared = " + weaponStates[slotId].prepared);
    }
    
    public boolean isWeaponSlotPrepared(int slotId)
    {
        return weaponStates[slotId].prepared;
    }
    
    public boolean isWeaponSlotOccupied(int slotId)
    {
        return weaponStates[slotId].weapon != Item.NOTHING;
    }
    
    /** @return Answers the number of weapon slots of this ship's ship type. */
    public int getWeaponSlotCount()
    {
        return ((ShipType) getType()).wepMountPoints;
    }
    
    public void setWeapon(int slotId, Item weapon)
    {
        weaponStates[slotId].weapon = weapon;
    }
    
    @Override
    protected void onDraw(GL10 gl, int detail)
    {
        if ((detail & ScnObj.DETAIL_DEBUG) != 0)
        {
            
        }
    }
}
