package tbc.trader;

import android.opengl.Matrix;
import android.util.Log;

import tbc.data.Constants;
import tbc.data.spatial.Point2D;
import tbc.data.spatial.Point3D;
import tbc.data.spatial.Volume;
import tbc.scene.PlaneWorld;
import tbc.scene.ScnObj;
import tbc.trader.mobiles.Ship;

/** 
 * A mobile acts as a grouping for all the things that render to make up a
 * thing - be it a ship, bullet, missle, planet, space station, etc. Each of the
 * factory methods will make up a partial tree which is attached to a particular
 * parent. The root node of the partial tree is a Mobile object. Underneeth that
 * the tree differs depending on the mobile type.
 */
public abstract class Mobile extends ScnObj implements ScnObj.Updatable
{
    private Info.MobileType mobileType;
    
    private PlaneWorld planeWorld;
    
    private Medium medium;
    
    private GameContext context;
    
    private float angleRads    = 0.0f,
                  angularVel   = 0.0f, 
                  angularAccel = 0.0f;
    
    private Point2D vel   = new Point2D(0.0f, 0.0f), 
                    accel = new Point2D(0.0f, 0.0f); /* pos comes from ScnObj */
    
    public Mobile(Info.MobileType type, PlaneWorld pw, Medium m, GameContext gc)
    {
        this.mobileType = type;
        this.planeWorld = pw;
        this.medium = m;
        this.context = gc;
    }
    
    @Override
    public void onUpdate(long tDelta, long tIndex)
    {
        /* The order of things:
         * 1. Update angular velocity for angular acceleration.
         * 2. Update angular velocity for angular drag.
         * 3. Update angle for angular velocity.
         * 4. Update forward velocity for forward acceleration.
         * 5. Update forward velocity for forward drag.
         * 6. Update position for forward velocity.
         */
        
        /* Our values are on a per 1-second basis. */
        float mul = tDelta / 1000.0f;
        
        /* Update our angular velocity based on our angular acceleration. */
        angularVel += (mul * angularAccel);
        
        if (angularVel > mobileType.maxAngularVel)
        {
            angularVel = mobileType.maxAngularVel;
        }
        else if (angularVel < -mobileType.maxAngularVel)
        {
            angularVel = -mobileType.maxAngularVel;
        }
        
        /* ... and angular drag. */
        if (angularVel > 0.0f)
        {
            /* apply in negative direction */
            angularVel = Math.max(0.0f, angularVel - (mul * medium.getAngularDrag()));
        }
        else if (angularVel < 0.0f)
        {
            /* apply in positive direction */
            angularVel = Math.min(0.0f, angularVel + (mul * medium.getAngularDrag()));
        }
        
        /* Update our angle based on our angular velocity. */
        setAngle(angleRads + (mul * angularVel));
        
        /* And now to update our velocity based on acceleration. */
        vel.x += (mul * accel.x);
        vel.y += (mul * accel.y);
        
        if (vel.getLength() > mobileType.maxForwardVel)
        {
            vel = vel.normalise().multiply(mobileType.maxForwardVel);
        }
        
        /* Finally apply movement drag. */
        Point2D movementDragDirection = vel.normalise().negative();
        Point2D drag = movementDragDirection.multiply(mul * medium.getMovementDrag());
        Point2D newVel = vel.add(drag);
        
        if (newVel.dot(vel) < 0.0f)
        {
            /* If it is now inversed then take us to 0. */
            vel = new Point2D(0.0f, 0.0f);
        }
        else
        {
            vel = newVel;
        }
        
        /* And now to update our positon based on velocity. */
        Point3D pos = getRelativePos();
        pos.x += (mul * vel.x);
        pos.y += (mul * vel.y);
        setRelativePos(pos);
    }
    
    @Override
    public void onPostUpdate(long tDelta, long tIndex)
    {
        // TODO Do collision detection here.
    }
    
    @Override
    protected float[] onCalculateModelTransform()
    {
        float degs = -(angleRads * Constants.RAD_TO_DEG);
        float[] m = super.onCalculateModelTransform(); /* translation */
        Matrix.rotateM(m, 0, degs, 0.0f, 0.0f, 1.0f); /* rotation */
        return m;
    }
    
    @Override
    protected Volume onCalculateVolume()
    {
        Point3D pos = getAbsolutePos();
        return new Volume(pos.x - 10, pos.x + 10, 
                          pos.y - 10, pos.y + 10, 
                          pos.z - 10, pos.z + 10); // TODO Write me
                                                   // based on Model child
    }
    
    public void setAngularAccel(float aa)
    {
        this.angularAccel = aa;
    }
    
    public void setForwardAccel(Point2D fa)
    {
        this.accel = fa.copy();
    }
    
    /** Sets acceleration in the direction the mobile is current facing. So make
     * sure you set the angle you want before calling this method.
     */
    public void setForwardAccel(float accelMul)
    {
        Point2D accelDirection = Point2D.normAtRads(angleRads);
        accel = accelDirection.multiply(accelMul);
    }
    
    public Point2D getForwardVelocity()
    {
        return vel;
    }
    
    public Point2D getForwardAccel()
    {
        return accel.copy();
    }
    
    public float getAngularVelocity()
    {
        return angularVel;
    }
    
    /** @return Answers the current angle of the mobile, in rads from north. */
    public float getAngle()
    {
        return angleRads;
    }
    
    public void setAngle(float a)
    {
        angleRads = normAng(a);
        
    }
    
    public static float normAng(float a)
    {
        float r = (float) Math.IEEEremainder(a, Constants.ONE_ROT);
        if (r < 0.0f) r = Constants.ONE_ROT + r;
        return r;
    }
    
    public Info.MobileType getType()
    {
        return mobileType;
    }
    
    /** @return Answers an object that describes the medium that the mobile
     * considers itself to be travsersing. The medium's properties define a drag
     * force that is applied to the mobile after acceleration calculations.
     */
    public Medium getMedium()
    {
        return medium;
    }
    
    public PlaneWorld getWorld()
    {
        return planeWorld;
    }
    
    public GameContext getContext()
    {
        return context;
    }
    
    public static Mobile makePlayerShip(Player p,
                                 Info.ShipType st,
                                 Medium m,
                                 PlaneWorld pw,
                                 GameContext gc)
    {
        Ship s = new Ship(st, pw, m, gc, p);
        pw.addChild(s);
        return s;
    }

}
