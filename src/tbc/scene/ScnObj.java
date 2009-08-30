package tbc.scene;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.Matrix;

import tbc.data.spatial.Point3D;
import tbc.data.spatial.Volume;

public abstract class ScnObj
{
    public static final int DETAIL_DEBUG   = 0x1 << 0;
    public static final int DETAIL_NORM    = 0x1 << 1;
    public static final int DETAIL_OUTLINE = 0x1 << 2;
    
    private ArrayList<ScnObj> children = new ArrayList<ScnObj>();
    
    private ScnObj  parent             = null;
    
    private Point3D relPos             = new Point3D(0.0f, 0.0f, 0.0f);
    
    private Volume  volume             = null;
    
    private boolean visible            = true;
    
    private long    creationTime       = -1l; /* set once added to a scene */
    
    private VolumeChangedListener volumeChangedListener;
    
    public void addChild(ScnObj o)
    {
        if (o.parent != null)
        {
            o.parent.removeChild(o);
        }
        
        o.parent = this;
        children.add(o);
        
        tellAncestorsAdded(this, o);
    }
    
    /** Tell ancestors of `from` that the scene object `o` and all
     * children underneeth it have been added to the scene tree.
     */
    private void tellAncestorsAdded(ScnObj from, ScnObj o)
    {
        ScnObj p = from;
        while ((p = p.parent) != null)
        {
            p.onDescendantAdded(o);
        }
        
        for (ScnObj child : o.getChildren())
        {
            tellAncestorsAdded(from, child);
        }
    }
    
    public void removeChild(ScnObj o)
    {
        children.remove(o);
        o.parent = null;
        
        tellAncestorsRemoved(this, o);
    }
    
    /** Tell ancestors of `from` that the scene object `o` and all children
     * underneeth it have been removed from the scene tree.
     */
    private void tellAncestorsRemoved(ScnObj from, ScnObj o)
    {
        ScnObj p = from;
        while ((p = p.parent) != null)
        {
            p.onDescendantRemoved(o);
        }
        
        for (ScnObj child : o.getChildren())
        {
            tellAncestorsRemoved(from, child);
        }
    }
    
    /** If a scene object is made invisible then its children will be hidden too.
     * Though their visible flags will not be altered by this call.
     */
    public void setVisible(boolean v)
    {
        this.visible = v;
    }
    
    public boolean isVisible()
    {
        return visible;
    }
    
    public void setVolumeChangedListener(VolumeChangedListener vcl)
    {
        this.volumeChangedListener = vcl;
    }
    
    public Point3D getAbsolutePos()
    {
        return parent.relPos.add(relPos);
    }
    
    public Point3D getRelativePos()
    {
        return relPos.copy();
    }
    
    public void setRelativePos(Point3D p)
    {
        this.relPos = p;
    }
    
    public Volume getVolume()
    {
        if (volume == null)
        {
            volume = onCalculateVolume();
            if (volumeChangedListener != null)
            {
                volumeChangedListener.tellVolumeChanged(this, volume);
            }
        }
        return volume;
    }
    
    public void invalidateVolume()
    {
        volume = null;
    }
    
    public Scene getScene()
    {
        ScnObj p = this;
        while (!((p = p.parent) instanceof Scene))
            ;
        
        if (p != null)
        {
            return (Scene)p;
        }
        else
        {
            throw new IllegalArgumentException("ScnObj has no Scene ancestor.");
        }
    }
    
    public Camera getCamera()
    {
        ScnObj p = this;
        while (!((p = p.parent) instanceof Camera))
            ;
        
        if (p != null)
        {
            return (Camera)p;
        }
        else
        {
            throw new IllegalArgumentException("ScnObj has no Camera ancestor.");
        }
    }
    
    public ScnObj getParent()
    {
        return parent;
    }
    
    public ArrayList<ScnObj> getChildren()
    {
        return children;
    }
    
    public void moveToRelativePos(Point3D p)
    {
        relPos = p.copy();
        invalidateVolume();
    }
    
    public void moveToAbsolutePos(Point3D p)
    {
        relPos = p.subtract(parent.getAbsolutePos());
        invalidateVolume();
    }
    
    public void setCreationTime(long t)
    {
        this.creationTime = t;
    }
    
    public long getCreationTime()
    {
        return creationTime;
    }
    
    public void draw(GL10 gl, int detail)
    {
        boolean isCamera = this instanceof Camera;
        
        /* Set up the projection matrix if this scn obj alters it. */
        if (isCamera)
        {
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glMultMatrixf(((Camera)this).onCalculateProjTransform(), 0);
        }
        
        /* Set up the model matrix so the onDraw can draw in the correct
         * place.
         */
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glMultMatrixf(onCalculateModelTransform(), 0);
        
        onDraw(gl, detail);
        
        for (ScnObj so : onCreateChildRenderList())
        {
            if (onCheckWillDrawChild(so))
            {
                so.draw(gl, detail);
            }
        }
        
        onDrawEnd(gl, detail);
        
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPopMatrix();
        
        if (isCamera)
        {
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glPopMatrix();
        }
    }
    
    /** Called when a scene object is added to the scene, somewhere below this
     * scene object.
     */
    protected void onDescendantAdded(ScnObj o)
    {
    }
    
    /** Called when a scene object is removed from the scene, somewhere below
     * this scene object.
     */
    protected void onDescendantRemoved(ScnObj o)
    {
    }
    
    /** Allows augmentation of the usual isVisible check. To consider isVisible
     * too, OR the return value with a super call.
     */
    protected boolean onCheckWillDrawChild(ScnObj child)
    {
        return child.isVisible();
    }
    
    protected ArrayList<ScnObj> onCreateChildRenderList()
    {
        return children;
    }
    
    /** Draw the object. Model matrix is set up such that origin is the centre
     * of where this object should render. Called before children are drawn.
     */
    protected void onDraw(GL10 gl, int detail)
    {
    }
    
    /** Draw the object. Called after children are drawn. Useful for unsetting
     * a light scnobj, an effect scnobj, etc.
     */
    protected void onDrawEnd(GL10 gl, int detail)
    {
    }
    
    /** When the bounding volume of a scene object must be recalculated this
     * method is called to do it.
     */
    protected Volume onCalculateVolume()
    {
        // TODO Calculate this based on children? Cache value and invalidate?
        return new Volume();
    }
    
    protected float[] onCalculateModelTransform()
    {
        float[] m = new float[4*4];
        Matrix.setIdentityM(m, 0);
        Matrix.translateM(m, 0, relPos.x, relPos.y, relPos.z);
        return m;
    }
    
    public interface Camera
    {
        /** Set up the camera. The projection matrix is loaded and ready to be altered
         * when this method is called.
         */
        public abstract float[] onCalculateProjTransform();
    }
    
    public interface Updatable
    {
        public abstract void onUpdate(long tDelta, long tIndex);
        
        public abstract void onPostUpdate(long tDelta, long tIndex);
    }
    
    public interface VolumeChangedListener
    {
        public void tellVolumeChanged(ScnObj o, Volume v);
    }
}
