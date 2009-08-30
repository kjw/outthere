package tbc.scene.tidbits;

import java.util.Random;

import tbc.scene.ScnObj;

/** Emits scene objects as children of itself (or another scene object). This class
 * is currently a bit basic. It would be nice if it could emit some uniform objects
 * that have name-indexed properties to control their appearance (particles).
 * But for now, one must provide the method that creates objects.
 */
public class Emitter extends ScnObj implements ScnObj.Updatable
{
    private ParticleMaker pMaker;
    
    private ScnObj parentage;
    
    private long randAdjustedInterlude, duration, totalTimeSoFar, tickTimeSoFar, 
                 idealInterlude;
    
    private int randomness, count;
    
    private boolean running;
    
    private static Random rand = new Random();
    
    public interface ParticleMaker
    {
        /** Make a particle. `count` is the number of times a particle has been
         * made for the emitter, and `elapsedTime` is the time elapsed since the
         * emitter was started.
         */
        public ScnObj onMakeParticle(int madeSoFar, long elapsedTime);
    }
    
    public Emitter(long interlude,
                   int randomness,
                   ScnObj parentage,
                   ParticleMaker pMaker)
    {
        this.pMaker                = pMaker;
        this.randAdjustedInterlude = interlude;
        this.idealInterlude        = interlude;
        this.duration              = 0;
        this.randomness            = randomness;
        this.parentage             = this;
        this.parentage             = parentage;
    }
    
    public Emitter(long interlude,
                   long duration,
                   int randomness,
                   ScnObj parentage,
                   ParticleMaker pMaker)
    {
        this.pMaker                = pMaker;
        this.randAdjustedInterlude = interlude;
        this.idealInterlude        = interlude;
        this.duration              = duration;
        this.randomness            = randomness;
        this.parentage             = this;
        this.parentage             = parentage;
    }
    
    public void start()
    {
        tickTimeSoFar = totalTimeSoFar = count = 0;
        running = true;
    }
    
    public void stop()
    {
        running = false;
    }
    
    @Override
    public void onUpdate(long tDelta, long tIndex)
    {
        if (running)
        {
            totalTimeSoFar += tDelta;
            tickTimeSoFar += tDelta;
            if (tickTimeSoFar >= randAdjustedInterlude)
            {
                parentage.addChild(pMaker.onMakeParticle(count++,
                                                         totalTimeSoFar));
                
                if (randomness > 0)
                {
                    if (rand.nextInt(1) == 1)
                    {
                        randAdjustedInterlude = idealInterlude + rand.nextInt(randomness);
                    }
                    else
                    {
                        randAdjustedInterlude = idealInterlude - rand.nextInt(randomness);
                    }
                }
                
                tickTimeSoFar = 0;
            }
            
            if (duration > 0 && totalTimeSoFar >= duration)
            {
                /* An emitter dies once it has finished normally. */
                stop();
                getParent().removeChild(this); 
            }
        }
    }
    
    @Override
    public void onPostUpdate(long tDelta, long tIndex)
    {
    }
}
