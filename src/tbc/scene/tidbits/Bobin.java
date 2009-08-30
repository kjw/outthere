package tbc.scene.tidbits;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import tbc.scene.ScnObj;

/**
 * A bobin renders a bunch of vertices from hand-defined vertex and colour arrays.
 */
public abstract class Bobin extends ScnObj
{
    public static final int one = 0x10000;
    
    private IntBuffer vertBuff;
    private IntBuffer colsBuff;
    
    private int vertCount;
    
    public Bobin()
    {
        int[] verts = getVerts();
        int[] cols = getColors();
        vertCount = verts.length / 3;
        
        ByteBuffer vertsB = ByteBuffer.allocateDirect(verts.length * 4);
        vertsB.order(ByteOrder.nativeOrder());
        vertBuff = vertsB.asIntBuffer();
        vertBuff.put(verts);
        vertBuff.position(0);
        
        ByteBuffer colsB = ByteBuffer.allocateDirect(cols.length * 4);
        colsB.order(ByteOrder.nativeOrder());
        colsBuff = colsB.asIntBuffer();
        colsBuff.put(cols);
        colsBuff.position(0);
    }
    
    @Override
    protected void onDraw(GL10 gl, int detail)
    {
        gl.glFrontFace(gl.GL_CW);
        gl.glVertexPointer(3, gl.GL_FIXED, 0, vertBuff);
        gl.glColorPointer(4, gl.GL_FIXED, 0, colsBuff);
        gl.glDrawArrays(gl.GL_TRIANGLES, 0, vertCount);
    }
    
    public abstract int[] getVerts();
    
    public abstract int[] getColors();
    
}
