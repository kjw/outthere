package tbc.trader;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import tbc.data.spatial.Point2D;
import tbc.data.spatial.Point3D;
import tbc.scene.PlaneWorld;
import tbc.scene.Scene;
import tbc.scene.SceneActivity;
import tbc.supercheck.Gen;
import tbc.test.RunSuperCheck;
import tbc.trader.Info.ShipType;
import tbc.trader.controls.AccelCamera;
import tbc.trader.junk.Cube;
import tbc.trader.mobiles.Ship;

public class TestActivity extends SceneActivity
{
    private static final String DATA_SET = "SpaceTrader1";
    
    /** Number of milliseconds until we consider a touch press as firing
     * weapons.
     */
    private static final long MOVE_TIMEOUT = 200l;
    
    private Player player;
    
    private PlaneWorld world;
    
    private Ship playerShip;
    
    @Override
    protected Scene onCreateScene()
    {
    	Gen.g().setParams(RunSuperCheck.getTestParams());
    	
        player = new Player();
        player.addCameraControl(new AccelCamera(getApplicationContext()));
        
        world = new PlaneWorld(-10000f, -10000f, 10000f, 10000f);
        Scene s = new Scene();
        GameContext context = new GameContext(DATA_SET);
        
        s.addChild(world);
        playerShip = (Ship) Ship.makePlayerShip(player, 
                                (ShipType)context.getInfoSet().get("ship/cruiser"), 
                                Medium.createAir(),
                                world,
                                context);
        
        playerShip.setWeapon(0, new Item(context.getInfoSet().get("weapon/pow")));
        playerShip.setWeaponSlotPrepared(0, true);
        
        for (int i=0; i<150; i++) 
        {
        	Cube c = Cube.arbitrary(Gen.g());
        	Point3D p = Point3D.arbitrary(Gen.g());
        	p.z = 0.0f;
        	c.setRelativePos(p);
        	world.addChild(c);
        }
        
        return s;
    }
    
    @Override
    protected SceneView onCreateSceneView(Context context, Scene scene)
    {
        return new SceneActivity.SceneView(context, scene)
        {
            @Override
            public boolean onTouchEvent(MotionEvent event)
            {
                int action = event.getAction();
                Point2D location = world.getWorldForScreen(event.getX(), event.getY());
                    
                if (action == MotionEvent.ACTION_UP)
                {
                    player.invokeStopFire();
                    if (event.getEventTime() - event.getDownTime() < MOVE_TIMEOUT)
                    {
                        player.invokeMoveAt(location);
                    }
                    return true;
                }
                else if (action == MotionEvent.ACTION_DOWN)
                {
                    player.invokeBeginFireAt(location);
                    return true;
                }
                else if (action == MotionEvent.ACTION_MOVE)
                {
                    player.invokeMoveFireAt(location);
                    return true;
                }
                return false;
            }
            
            @Override
            public boolean onKeyPreIme(int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode >= KeyEvent.KEYCODE_0
                        && keyCode < playerShip.getWeaponSlotCount())
                {
                    int slotId = keyCode - KeyEvent.KEYCODE_0;
                    
                    if (playerShip.isWeaponSlotOccupied(slotId))
                    {
                        playerShip.toggleWeaponSlotPrepared(slotId);
                    }
                    
                    return true;
                }
                
                return false;
            }
        };
    }
}
