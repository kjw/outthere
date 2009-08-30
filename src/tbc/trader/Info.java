package tbc.trader;

import java.util.Hashtable;

import tbc.data.Constants;

import android.util.Log;

/** 
 * This class defines our data structures for holding the data that fills our
 * game rules, but also, it will define the game's content, using the class
 * structures it defines.
 */
public class Info
{
    public abstract class Type
    {
        public String key;
        public String typeName;
    }
    
    public abstract class MobileType extends Type
    {
        public float maxForwardVel; /* Units per second */
        public float maxAngularVel; /* Rads per second */
        public float angularAccel; /* Rads per second per second */
        public float forwardAccel; /* Units per second per second */
    }
    
    public class ShipType extends MobileType
    {
        public String model;
        public int wepMountPoints;
        public float[] wepMountAngles;
        public int maxEquipEnergy;
    }
    
    public abstract class BulletType extends MobileType
    {
        public int againstShieldStrength;
        public int againstHullStrength;
        public long deathTime; /* time until the bullet disappears */
    }
    
    public class PhysicalBulletType extends BulletType
    {
        public String model;
    }
    
    public class EnergyBulletType extends BulletType
    {
        public int renderColor;
    }
    
    public class MissleBulletType extends MobileType
    {
        public int explosionRadius;
        // yada yada
    }
    
    public class OrbitalType extends MobileType
    {
        public String model;
    }
    
    public class PlanetType extends MobileType
    {
        
    }
    
    public class AsteroidType extends MobileType
    {
        
    }
    
    public class ContainerType extends MobileType
    {
        
    }
    
    public class DerilictShipType extends ShipType
    {
        
    }
    
    public abstract class ItemType extends Type
    {
        
    }
    
    public abstract class WeaponType extends ItemType
    {
        public float arcRads;
        public String bulletType;
        public long shotIntervalTime; /* time between shots */
        public long bringUpTime;      /* time between fire command and first shot */
    }
    
    public class EnergyWeaponType extends WeaponType
    {
    }
    
    public class MissileWeaponType extends WeaponType
    {
        
    }
    
    public class PhysicalWeaponType extends WeaponType
    {
        
    }

    // yada yada
    
    private static Info info;
    
    private Hashtable<String, Type> t = new Hashtable<String, Type>();   
    
    public Type get(String key)
    {
        if (t.containsKey(key))
        {
            return t.get(key);
        }
        else
        {
            Log.w("GameTypeDict", "Unknown type key: " + key);
            return null;
        }
    }
    
    public void add(String key, Type type)
    {
        if (t.containsKey(key))
        {
            Log.w("GameTypeDict", "Duplicate type key: " + key);
        }
        
        t.put(key, type);
    }
    
    public static Info getSet(String infoSetName)
    {
        // TODO Honour dataSetName once there is somewhere to read these from.
        if (info == null)
        {
            Info i = new Info();
            
            /* Make a pretend ship type */
            Info.ShipType si = i.new ShipType();
            si.maxAngularVel = 2.0f * Constants.ONE_ROT;
            si.maxForwardVel = 45.0f;
            si.forwardAccel = 35.0f;
            si.angularAccel = 3.0f * Constants.ONE_ROT;
            si.key = "ship/swan-pedalo";
            si.typeName = "Swan Pedalo";
            si.model = "model/swan.model";
            
            i.t.put(si.key, si);
            
            /* Make a pretend cruiser ship type */
            Info.ShipType si2 = i.new ShipType();
            si2.maxAngularVel = 0.6f * Constants.ONE_ROT;
            si2.maxForwardVel = 10.0f;
            si2.forwardAccel = 15.0f;
            si2.angularAccel = 1.5f * Constants.ONE_ROT;
            si2.wepMountPoints = 1;
            si2.wepMountAngles = new float[] { 0.0f };
            si2.key = "ship/cruiser";
            si2.typeName = "Cruiser";
            si2.model = "model/cruiser.model";
            
            i.t.put(si2.key, si2);
            
            Info.EnergyBulletType bi = i.new EnergyBulletType();
            bi.maxForwardVel = 45.0f;
            bi.forwardAccel  = 45000.0f; // mul max vel by 1s for immediate max speed
            bi.deathTime     = 750l;
            bi.key = "bullet/pow";
            
            i.t.put(bi.key, bi);
            
            Info.EnergyWeaponType wi = i.new EnergyWeaponType();
            wi.bringUpTime = 1000l;
            wi.shotIntervalTime = 600l;
            wi.key = "weapon/pow";
            
            i.t.put(wi.key, wi);
            
            return i;
        }
        else
        {
            return info;
        }
    }
}
