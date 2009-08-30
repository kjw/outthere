package tbc.data.spatial;

import java.util.ArrayList;
import java.util.Hashtable;

import tbc.supercheck.Gen;

/** 
 * Implements a loose quad tree, whose nodes have 2d bounds, and whose ordering
 * principal is two dimensional. A loose quad tree is one whose boxes are slightly
 * larger than they should be. That is to say, while an ordinary quad tree's boxes 
 * have side length l, a loose quad tree has boxes with side length kl, where k > 1.
 * 
 * @author Karl Ward
 */
public class QuadTree<A>
{
    /* TODO Remove leaves once they have no contents. Handle recreation of less
     * than 4 leaves.
     */
    
    private QuadTree<A> NE;
    private QuadTree<A> NW;
    private QuadTree<A> SE;
    private QuadTree<A> SW;
    
    private Bounds box;
    
    private ArrayList<QuadNode> contents;
    
    /** For a particular QuadTree instance that is considered the root of the
     * tree, this hash will fill up with references to all the objects tracked
     * by the quad tree. For this to be true, {@link #add(Bounds, Object)}
     * should always be called on the root QuadTree.
     */
    private Hashtable<A, QuadNode> quadNodesBelow;
    
    /** Maximum content entries per node. Once this number is reached the box is
     * split. Though a node can end up with more than this number of objects in
     * its content lists. */
    private int desiredMax;
    
    /** The multiplier for the side length of boxes. */
    private float k;
    
    /** Indicates if this node has been split and has children. */
    private boolean split;
    
    private class QuadNode
    {
        private Bounds      b;
        private A           obj;
        private QuadTree<A> parent;
        
        private QuadNode(Bounds b, A obj, QuadTree<A> parent)
        {
            this.b      = b;
            this.obj    = obj;
            this.parent = parent;
        }
    }
    
    public QuadTree(float x1, float y1, float x2, float y2, int desiredMax, float k)
    {
        this(new Bounds(x1, y1, x2, y2), desiredMax, k);
    }
    
    public QuadTree(Bounds box, int desiredMax, float k)
    {
        this.desiredMax     = desiredMax;
        this.k              = k;
        this.split          = false;
        this.box            = box;
        
        this.contents       = new ArrayList<QuadNode>(desiredMax);
        this.quadNodesBelow = new Hashtable<A, QuadNode>(); 
    }
    
    public Bounds getBox()
    {
        return box;
    }
    
    /** Adds an object somewhere in the quad tree. The new quad node created will
     * consider the quad tree section add() is called on to be its root. Thus, add,
     * remove and move calls for a particular object must be performed on the same
     * quad tree segment.
     */
    public void add(Bounds b, A obj)
    {
        addImpl(this, b, obj);
    }
    
    private void addImpl(QuadTree<A> root, Bounds b, A obj)
    {
        if (split)
        {
            /* Content objects will be placed in a single child node, or this one
             * if they don't fit any. */
            if (NE.getBox().containsBounds(b))
            {
                NE.add(b, obj);
            }
            else if (NW.getBox().containsBounds(b))
            {
                NW.add(b, obj);
            }
            else if (SE.getBox().containsBounds(b))
            {
                SE.add(b, obj);
            }
            else if (SW.getBox().containsBounds(b))
            {
                SW.add(b, obj);
            }
            else
            {
                addHere(root, b, obj); /* Can end up with more than desiredMax. */
            }
        }
        else
        {
            if (contents.size() < desiredMax)
            {
                addHere(root, b, obj);
            }
            else
            {
                split();
                add(b, obj);
            }
        }
    }
    
    /**
     * Add an object to this particular quad node. The root passed is told about
     * the new object in the tree. The root should not be this - it should be
     * the root QuadTree instance of the whole tree.
     */
    private void addHere(QuadTree<A> root, Bounds b, A obj)
    {
        QuadNode qn = new QuadNode(b.copy(), obj, this);
        contents.add(qn);
        root.quadNodesBelow.put(obj, qn);
    }
    
    public void remove(A obj)
    {
        QuadNode qn = quadNodesBelow.get(obj);
        
        if (qn != null)
        {
            qn.parent.contents.remove(qn);
            quadNodesBelow.remove(obj);
        }
    }
    
    public void move(Bounds b, A obj)
    {
        QuadNode qn = quadNodesBelow.get(obj);
        
        if (qn == null)
        {
            /* isn't yet in the tree */
            add(b, obj);
        }
        else if (qn.parent.getBox().containsBounds(b))
        {
            /* still fits in its quad tree segment */
            qn.b = b.copy();
        }
        else
        {
            /* no longer fits in its quad tree segment */
            qn.parent.contents.remove(qn);
            quadNodesBelow.remove(qn);
            add(b, obj);
        }
    }
    
    private void split()
    {
        final float childWidth  = box.getWidth() / 2;
        final float childHeight = box.getHeight() / 2;
        
        NW = new QuadTree<A>(box.x1, 
                             box.y1, 
                             box.x1 + childWidth,
                             box.y1 + childHeight,
                             desiredMax,
                             k);
        
        NE = new QuadTree<A>(box.x1 + childWidth,
                             box.y1,
                             box.x2,
                             box.y1 + childHeight,
                             desiredMax,
                             k);
        
        SW = new QuadTree<A>(box.x1,
                             box.y1 + childHeight,
                             box.x1 + childWidth,
                             box.y2,
                             desiredMax,
                             k);
        
        SE = new QuadTree<A>(box.x1 + childWidth,
                             box.y1 + childHeight,
                             box.x2,
                             box.y2,
                             desiredMax,
                             k);
        
        if (k != 1.0f)
        {
            NW.getBox().enlarge(k);
            NE.getBox().enlarge(k);
            SW.getBox().enlarge(k);
            SE.getBox().enlarge(k);
        }
        
        /* Move contents into the new children. */
        for (QuadNode qn : contents)
        {
            if (NE.getBox().containsBounds(qn.b))
            {
                contents.remove(qn);
                NE.add(qn.b, qn.obj);
            }
            else if (NW.getBox().containsBounds(qn.b))
            {
                contents.remove(qn);
                NW.add(qn.b, qn.obj);
            }
            else if (SE.getBox().containsBounds(qn.b))
            {
                contents.remove(qn);
                SE.add(qn.b, qn.obj);
            }
            else if (SW.getBox().containsBounds(qn.b))
            {
                contents.remove(qn);
                SW.add(qn.b, qn.obj);
            }
            /* otherwise leave it here */
        }
    }
    
    @Override
    public String toString()
    {
        return "QuadT(k="+k+",splitAt="+desiredMax+",split="+split+")";
    }
    
    public static QuadTree<?> arbitrary(Gen gen)
    {
        // TODO Need something in supercheck to choose a random concrete type for
        // generic QuadTree<A> for us.
        
        QuadTree<Integer> qt = new QuadTree<Integer>(Bounds.arbitrary(gen),
                                                     gen.choose(1, 30),
                                                     gen.within(1, 3));
        int contentAddAttempts = gen.choose(0, 1024);
        for (int attempt=0; attempt < contentAddAttempts; attempt++)
        {
            qt.add(Bounds.arbitrary(gen), gen.arbInt());
        }
        return qt;
    }
    
}
