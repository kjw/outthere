package tbc.trader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;

import tbc.data.Constants;
import tbc.data.spatial.Axis;
import tbc.data.spatial.Point2D;
import tbc.scene.PlaneWorld;
import tbc.trader.mobiles.Ship;
import tbc.util.Later;

/** 
 * The player class handles user commands that are intended to operate on the
 * player's ship in some way. As such, it is implemented as an AiDelegate attached
 * to the player's ship's mobile, so that it can modify the acceleration properties 
 * of the player's ship.
 * <p>
 * Player commands are queued until callback methods that run on the scene thread
 * are called, when they are invoked.
 */
public class Player implements Ship.AiDelegate
{
    private static final float HALT_THRESHOLD    = PlaneWorld.MINIMUM_VISIBLE_RADIUS / 5.0f;
    private static final float MAX_VEL_THRESHOLD = PlaneWorld.MINIMUM_VISIBLE_RADIUS - HALT_THRESHOLD;
    
    private Queue<Later<Ship, ?>> laterQueue = new LinkedList<Later<Ship, ?>>();
    
    private ArrayList<ManeuverInfo> activeManeuvers = new ArrayList<ManeuverInfo>();
    
    /** A maneuver alters the forward or angular acceleration of the player ship 
     * while in an attempt to get another component, such as position or velocity, 
     * to a particular value.
     */
    private enum ManeuverType
    {
        NONE,
        
        TO_FORWARD_HALT,
        
        /** Like the above except forward momentum is not altered - the ship stops
         * rotating, gradually.
         */
        TO_ROTATION_HALT_AIR,
        
        /** Accelerate the mobile until it has the desired velocity. */
        TO_FORWARD_VELOCITY,
        
        /** Accelerate the mobile's angular velocity until the mobile's angle is
         * a particular value. 
         */
        TO_ANGLE_AIR,
        
        /** Continally accelerate the mobile's angular velocity so that it rotates
         * to the left.
         */
        ROTATE_LEFT,
        
        /** Continally accelerate the mobile's angular velocity so that it rotates
         * to the right.
         */
        ROTATE_RIGHT,
        
        /** Simply reduces whatever velocity we have to 0 over a period of time. 
         * Same as TO_FORWARD_VELOCITY with a value of 0.0f.
         */
        TO_FORWARD_HALT_AIR,
        
        TO_FORWARD_VELOCITY_AIR;
        
        private static final float FORWARD_DOT_TOL   = 0.9f;
        private static final float FORWARD_TOL       = 0.3f;
    }
    
    private class ManeuverInfo
    {
        public ManeuverType mt = ManeuverType.NONE;
        public float val;
        public float interim1;
        public long endTime    = -1l;
        public long startTime  = -1l;
    }
    
    @Override
    public void onAiUpdate(Ship ship, long tDelta, long tIndex)
    {
        /* Here we don't actually do AI. Instead it gives us a handly place to
         * perform the player's commands on the update/render thread.
         */
        Later<Ship, ?> next = laterQueue.poll();
        
        while (next != null)
        {
            next.execute(ship);
            next = laterQueue.poll();
        }
        
        /* Now we execute the player's maneuvers, if any. These could have been
         * added by the commands processed above.
         */
        for (ManeuverInfo mi : activeManeuvers)
        {
            if (mi.startTime == -1l)
            {
                mi.startTime = tIndex;
            }
            updateManeuver(mi, ship, tIndex);
        }
    }
    
    @Override
    public void onLatentAiUpdate(Ship ship, long tDelta, long tIndex)
    {
        /* Again, we don't do AI here, but instead update the camera position
         * to centre on the player ship's new location.
         */
        ship.getWorld().setViewCentroid(ship.getAbsolutePos());
    }
    
    /** Firing will begin a number of milliseconds after this call, unless
     * invokeStopFire() is called.
     */
    public void invokeBeginFireAt(final Point2D location)
    {
        laterQueue.add(new Later<Ship, Object>()
        {
            @Override
            protected void onDo(Ship to)
            {
                Point2D p = location.subtract(to.getAbsolutePos().toPoint2D(Axis.Z));
                float angle = p.radsFromOrigin();
                to.startPreparedWeaponFire(angle);
            }
        });
    }
    
    public void invokeMoveFireAt(final Point2D location)
    {
        laterQueue.add(new Later<Ship, Object>()
        {
            @Override
            protected void onDo(Ship to)
            {
                Point2D p = location.subtract(to.getAbsolutePos().toPoint2D(Axis.Z));
                float angle = p.radsFromOrigin();
                to.movePreparedWeaponFire(angle);
            }
        });
    }
    
    public void invokeStopFire()
    {
        laterQueue.add(new Later<Ship, Object>()
        {
            @Override
            protected void onDo(Ship to)
            {
                to.stopAllWeaponFire();
            }
        });
    }
    
    public void invokeMoveAt(final Point2D location)
    {
        laterQueue.add(new Later<Ship, Object>()
        {
            @Override
            public void onDo(Ship ship)
            {
                /* relative position of the move at compared to ship location */
                Point2D relLoc = location.subtract(ship.getAbsolutePos()
                        .toPoint2D(Axis.Z));

                /* The longer the distance of the press from the ship, the faster
                 * we set the forward velocity. */
                float distance = relLoc.getLength();
                
                Log.i("Player", "Click distance = " + distance);
                Log.i("Player", "Click angle = " + relLoc.radsFromOrigin());

                activeManeuvers.clear();

                if (distance <= HALT_THRESHOLD)
                {
                    addManeuver(ManeuverType.TO_FORWARD_HALT_AIR, 0.0f);
                    addManeuver(ManeuverType.TO_ROTATION_HALT_AIR, 0.0f);
                }
                else 
                {
                    addManeuver(ManeuverType.TO_ANGLE_AIR, relLoc.radsFromOrigin());

                    if (distance <= MAX_VEL_THRESHOLD)
                    {
                        float targetVel = (distance - HALT_THRESHOLD) / 
                        (MAX_VEL_THRESHOLD - HALT_THRESHOLD);
                        addManeuver(ManeuverType.TO_FORWARD_VELOCITY_AIR,
                                targetVel);
                    }
                    else
                    {
                        addManeuver(ManeuverType.TO_FORWARD_VELOCITY_AIR,
                                ship.getType().maxForwardVel);
                    }
                }
            }
        });  
    }
    
    private void addManeuver(ManeuverType mt, float val)
    {
        ManeuverInfo mi = new ManeuverInfo();
        mi.mt = mt;
        mi.val = val;
        activeManeuvers.add(mi);
    }
    
    private void updateManeuver(ManeuverInfo m, Ship ship, long tIndex)
    {
        Point2D cVel       = ship.getForwardVelocity();
        float cAngularVel  = ship.getAngularVelocity();
        float cAngle       = ship.getAngle();
        Medium medium      = ship.getMedium();
        Info.MobileType t  = ship.getType();
        
        switch (m.mt)
        {
        case TO_ROTATION_HALT_AIR:
        {
            ship.setAngularAccel(0.0f);
            activeManeuvers.remove(m);
            break;
        }
        case TO_FORWARD_HALT_AIR:
        {
            /* let the drag stop us. */
            ship.setForwardAccel(new Point2D(0.0f, 0.0f));
            activeManeuvers.remove(m);
            break;
        }   
        case TO_FORWARD_VELOCITY_AIR:
        {
            Point2D accelDirection = Point2D.normAtRads(cAngle);

            if (cVel.getLength() >= m.val + ManeuverType.FORWARD_TOL
                    && accelDirection.dot(cVel) > ManeuverType.FORWARD_DOT_TOL)
            {
                /* time to slow down by easing off forward thrust until drag
                 * brings our velocity down to what it should be */
                ship.setForwardAccel(new Point2D(0.0f, 0.0f));
            }
            else if (cVel.getLength() >= m.val
                    && accelDirection.dot(cVel) > ManeuverType.FORWARD_DOT_TOL)
            {
                /* set to oppose drag exactly to maintain vel */
                ship.setForwardAccel(accelDirection.multiply(medium.getMovementDrag()));
            }
            else
            {
                /* accelerate to get our velocity in the right direction with
                 * the right magnitude */
                ship.setForwardAccel(accelDirection.multiply(ship.getType().forwardAccel));
            }
            break;
        }
        case TO_ANGLE_AIR:
        {
            if (m.interim1 == 0.0f)
            {
                /* Work out the additive direction that will get to the desired 
                 * angle with the least rotation.
                 */
                float shortestAngle = m.val - cAngle;
                if (shortestAngle > Constants.HALF_ROT)
                {
                    shortestAngle -= Constants.ONE_ROT;
                }
                else if (shortestAngle < -Constants.HALF_ROT)
                {
                    shortestAngle += Constants.ONE_ROT;
                }
                
                m.interim1 = shortestAngle > 0 ? 1.0f : -1.0f;
                
                /* calculate the time we need to thrust */
                // TODO Explain this rough, anticipated avg angular vel thing.
                float shortestAngleAbs = Math.abs(shortestAngle);
                float roughVel = ((ship.getAngularVelocity() + t.maxAngularVel) / 4) * 3;
                float seconds = shortestAngleAbs / roughVel;
                float secsMul = 1.0f - (shortestAngleAbs / Constants.HALF_ROT);
                seconds += seconds * secsMul;
                m.endTime = m.startTime + ((long) (seconds * 1000));
            }
            
            if (tIndex >= m.endTime)
            {
                ship.setAngularAccel(0.0f);
                activeManeuvers.remove(m);
            }
            else
            {
                ship.setAngularAccel(ship.getType().angularAccel * m.interim1);
            }
            
            break;
        }
        }
    }
}
