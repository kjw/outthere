package tbc.scene;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class SceneActivity extends Activity
{
    private GLSurfaceView sceneView;
    
    private static final int PAUSE_KEY = KeyEvent.KEYCODE_CALL;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        sceneView = onCreateSceneView(this, onCreateScene());
        setContentView(sceneView);
    }
    
    @Override
    protected void onPause() 
    {
        super.onPause();
        sceneView.onPause();
    }

    @Override
    protected void onResume() 
    {
        super.onResume();
        sceneView.onResume();
    }
    
    protected Scene onCreateScene()
    {
        return new Scene();
    }
    
    protected SceneView onCreateSceneView(Context context, Scene scene)
    {
        return new SceneView(context, scene);
    }
    
    public class SceneView extends GLSurfaceView 
    {
        private Scene scene;
        
        public SceneView(Context context, Scene scene) 
        {
            super(context);
            this.scene = scene;
//            setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
            setRenderer(new SceneRenderer());
            setKeepScreenOn(true);
        }
        
        @Override
        public void onPause()
        {
            super.onPause();
            scene.pause();
        }
        
        @Override
        public void onResume()
        {
            super.onResume();
            scene.resume();
        }
        
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event)
        {
            if (keyCode == PAUSE_KEY)
            {
                scene.togglePause();
                return true;
            }
            return false;
        }
        
        class SceneRenderer implements GLSurfaceView.Renderer 
        {
            public int[] getConfigSpec() 
            {
                return new int[] 
                {
                    EGL10.EGL_RED_SIZE,      8,
                    EGL10.EGL_GREEN_SIZE,    8,
                    EGL10.EGL_BLUE_SIZE,     8,
                    EGL10.EGL_ALPHA_SIZE,    8,
                    EGL10.EGL_DEPTH_SIZE,   16,
                    EGL10.EGL_NONE
                };
            }
            
            @Override
            public void onDrawFrame(GL10 gl)
            {
                scene.update();
                scene.draw(gl, ScnObj.DETAIL_DEBUG | ScnObj.DETAIL_NORM);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height)
            {
                gl.glViewport(0, 0, width, height);
                scene.setViewport(new int[] {0, 0, width, height});
            }

            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config)
            {
                /*
                 * By default, OpenGL enables features that improve quality
                 * but reduce performance. One might want to tweak that
                 * especially on software renderer.
                 */
                gl.glDisable(GL10.GL_DITHER);
                gl.glEnable(GL10.GL_CULL_FACE);
                gl.glEnable(GL10.GL_DEPTH_TEST);

                /*
                 * Some one-time OpenGL initialization can be made here
                 * probably based on features of this particular context
                 */
                 gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                         GL10.GL_FASTEST);
                 
                 gl.glShadeModel(GL10.GL_SMOOTH);
                 
            }
        }
    }
}