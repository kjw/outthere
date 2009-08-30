package tbc.scene;

import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import tbc.data.spatial.Axis;
import tbc.data.spatial.Bounds;
import tbc.data.spatial.Point2D;
import tbc.data.spatial.Point3D;
import tbc.data.spatial.QuadTree;
import tbc.data.spatial.Volume;

/** A world where all entities are positioned on a 2d plane. Rendering is only
 * performed on those scene objects that fall within the cameras view, determined
 * by a quad tree.
 * <p>
 * Use of the quad tree demands that child scene objects take into account their
 * children when calculating their bounds. Though, if a scene object only wants to
 * draw its children when it is on screen, even if its children are on screen
 * before it itself is, the scene object should return a bounds that does not
 * include those children.
 */
public class PlaneWorld extends ScnObj implements ScnObj.Camera
{
    /** The visible world distance between the centre of the screen and the closest
     * edge.
     */
    public static final float MINIMUM_VISIBLE_RADIUS = 25.0f;
    
    private QuadTree<ScnObj> worldObjs;
    
    private Point3D viewCentroid = new Point3D(0.0f, 0.0f, 0.0f);
    
    private float[] projection = new float[4 * 4];
    
    private float[] worldTranslation = new float[4 * 4];
    
    public PlaneWorld(float minX, float minY, float maxX, float maxY)
    {
        worldObjs = new QuadTree<ScnObj>(minX, minY, maxX, maxY, 1, 1.5f);
    }
    
    @Override
    protected void onDescendantAdded(ScnObj o)
    {
        /* Add the thing to our quad tree and listen for volume changed,
         * so we can update the objects positon in the quad tree. */
        o.setVolumeChangedListener(new VolumeChangedListener()
        {
            @Override
            public void tellVolumeChanged(ScnObj o, Volume v)
            {
                worldObjs.move(v.toBounds(Axis.Z), o);
            }
        });
        
        Volume v = o.getVolume();
        worldObjs.add(v.toBounds(Axis.Z), o);
    }
    
    @Override
    protected void onDescendantRemoved(ScnObj o)
    {
        /* Remove the thing from our quad tree */
        o.setVolumeChangedListener(null);
        worldObjs.remove(o);
    }
    
    // TODO
//    @Override
//    protected ArrayList<ScnObj> onCreateChildRenderList()
//    {
//        // TODO This only modifies the top-level list. Is this ok? Perhaps a RenderLister
//        // interface, assigned to ScnObj of the planeworld on descdentant calls?
//        ArrayList<ScnObj> toRender = new ArrayList<ScnObj>();
//        Bounds screenBounds = getScreenBounds();
//        for (ScnObj so : getChildren())
//        {
//            // TODO Use quadtree instead of direct bounds check. 
//            if (screenBounds.containsBounds(so.getVolume().toBounds(Axis.Z)))
//            {
//                toRender.add(so);
//            }
//        }
//        return toRender;
//    }
    
    @Override
    public float[] onCalculateProjTransform()
    {
        /* Configured when setViewCentroid is called. */
        return projection;
    }
    
    @Override
    protected float[] onCalculateModelTransform()
    {
        /* Translate the world to view centroid. */
        return worldTranslation;
    }
    
    public Point3D getViewCentroid()
    {
        return viewCentroid;
    }
    
    public void setViewCentroid(Point3D c)
    {
        this.viewCentroid = c.copy();
        
        int[] viewport = getScene().getViewport();
        float widthRadius, heightRadius;
        
        if (viewport[2] > viewport[3])
        {
            float ratio = viewport[2] / (float) viewport[3];
            widthRadius = ratio * MINIMUM_VISIBLE_RADIUS;
            heightRadius = MINIMUM_VISIBLE_RADIUS;
        }
        else
        {
            float ratio = viewport[3] / (float) viewport[2];
            widthRadius = MINIMUM_VISIBLE_RADIUS;
            heightRadius = ratio * MINIMUM_VISIBLE_RADIUS;
        }
        
        /* Set up the projection matrix to produce an orthagonal view */
        Matrix.setIdentityM(projection, 0);
        Matrix.orthoM(projection, 0, -widthRadius, widthRadius, 
                                     -heightRadius, heightRadius, 
                                     -1.0f, 1.0f);
//        Matrix.translateM(projection, 0, c.x, c.y, c.z); // TODO Move to world matrix.
        // TODO Player touch thresholds should use values related to the ortho matrix
        // values.
        // TODO getScreenBounds() should take into account projection and world
        // transform.
        
        Matrix.setIdentityM(worldTranslation, 0);
        Matrix.translateM(worldTranslation, 0, -c.x, -c.y, -c.z);
    }
    
    public Point2D getWorldForScreen(float x, float y)
    {
        Log.i("Player", "From screen: x = " + x + ", y = " + y);
        
        float[] model    = new float[4 * 4];
        int[]   view     = getScene().getViewport();
        float[] location = new float[3];
        
        Matrix.setIdentityM(model, 0);
        
        y = view[3] - y; // screen gl and screen android coords are y inverse
        GLU.gluUnProject(x, y, 0.0f, worldTranslation, 0, projection, 0, view, 0, location, 0);
        
        Log.i("Player", "To world: x = " + location[0] + ", y = " + location[1]);
        return new Point2D(location[0], location[1]);
    }
    
    public Bounds getScreenBounds()
    {
        Point3D viewCentroid = getViewCentroid();
        return new Bounds(viewCentroid.x - 100f, viewCentroid.x + 100f,
                          viewCentroid.y - 100f, viewCentroid.y + 100f); // TODO
    }

}
