package tbc.util;

/** 
 * A later represents a stored operation. A later can have a future value whose
 * retrieval will block until the operation has been executed.
 */
public abstract class Later<F, T>
{
    private Object waitForResultLock = new Object();
    
    private T result = null;
    
    public void execute(F on)
    {
        synchronized (waitForResultLock)
        {
            if (result == null) /* only execute once */
            {
                result = onDoWithResult(on);
            }
            waitForResultLock.notify();
        }
    }
    
    public T getResult()
    {
        return getResult(0);
    }
    
    public T getResult(long timeout)
    {
        synchronized (waitForResultLock)
        {
            if (result != null)
            {
                return result;
            }
            else
            {
                while (result == null)
                {
                    try
                    {
                        waitForResultLock.wait(timeout);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
                return result;
            }
        }
    }
    
    /** This method may return null if the later's future result is not important. 
     * But if it does return null then getResult() should not be called because it 
     * will block forever. 
     */
    protected void onDo(F to)
    {
    }
    
    /** Same as {@link #onDo(Object)} but allows one to return a result. */
    protected T onDoWithResult(F to)
    {
        onDo(to);
        return null;
    }

}
