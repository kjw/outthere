package tbc.scene;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import tbc.data.spatial.Point3D;

public class Scene extends ScnObj
{
    private ArrayList<Updatable> updatables = new ArrayList<Updatable>();
    
    private ArrayList<Updatable> recentlyAdded = new ArrayList<Updatable>();
    
    private ArrayList<Updatable> recentlyRemoved = new ArrayList<Updatable>();
    
    private long currentSceneTime           = 0l;
    
    private long initialSystemTime          = -1l;
    
    private long pauseTime                  = -1l;
    
    private boolean paused                  = false;
    
    private int[] viewport                  = new int[4];
    
    @Override
    protected void onDescendantAdded(ScnObj o)
    {
        Log.i("Scene", "Descendant added - " + o);
        if (o instanceof Updatable)
        {
            recentlyAdded.add((Updatable)o);
            o.setCreationTime(getNow());
        }
    }
    
    @Override
    protected void onDescendantRemoved(ScnObj o)
    {
        if (o instanceof Updatable)
        {
            recentlyRemoved.add((Updatable)o);
            Log.i("Scene", "Died = " + o);
        }
    }
    
    @Override
    protected void onDraw(GL10 gl, int detail)
    {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    }
    
    public void update()
    {
        long delta = 0;
        
        if (initialSystemTime == -1)
        {
            delta = 0;
            initialSystemTime = System.currentTimeMillis();
        }
        else
        {
            long newSceneTime = System.currentTimeMillis() - initialSystemTime;
            delta = newSceneTime - currentSceneTime;
            currentSceneTime = newSceneTime;
        }
        
        // TODO Should sync with onDesAdded and onDesRemoved?
        updatables.addAll(recentlyAdded);
        recentlyAdded.clear();
        
        updatables.removeAll(recentlyRemoved);
        recentlyRemoved.clear();
        
        for (Updatable so : updatables)
        {
            so.onUpdate(delta, currentSceneTime);
        }
        for (Updatable so : updatables)
        {
            so.onPostUpdate(delta, currentSceneTime);
        }
    }
    
    public void pause()
    {
        if (!paused)
        {
            pauseTime = System.currentTimeMillis();
        }
    }
    
    public void resume()
    {
        if (paused)
        {
            initialSystemTime += System.currentTimeMillis() - pauseTime;
        }
    }
    
    public void togglePause()
    {
        if (!paused)
        {
            pause();
        }
        else
        {
            resume();
        }
    }
    
    public long getNow()
    {
        return currentSceneTime;
    }
    
    @Override
    public final Point3D getAbsolutePos()
    {
        return new Point3D(0, 0, 0);
    }
    
    public int[] getViewport()
    {
        return viewport;
    }
    
    public void setViewport(int[] viewport)
    {
        Log.w("Scene", "Viewport set to " + viewport);
        this.viewport = viewport;
    }
}
