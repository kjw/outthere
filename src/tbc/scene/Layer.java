package tbc.scene;

import javax.microedition.khronos.opengles.GL10;

/** A 2d layer that has a positon on screen, and a depth. The depth determines
 * the ordering of layers.
 */
public class Layer extends ScnObj implements ScnObj.Camera
{
    private int surfaceWidth;
    private int surfaceHeight;
    
    public void setSurfaceSize(int width, int height)
    {
        this.surfaceWidth = width;
        this.surfaceHeight = height;
    }
    
    @Override
    public float[] onCalculateProjTransform()
    {
        /* Set up the projection matrix to produce an orthagonal view */
        return null;
    }
}
