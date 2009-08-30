package tbc.test.data.spatial;

import tbc.data.spatial.Bounds;
import tbc.data.spatial.Point2D;
import tbc.data.spatial.Point3D;

public class Invariants
{
    /* Point2D */
    
    public static boolean prop_p2dAddSubEquality(Point2D one) {
        return one.add(one).subtract(one).equals(one);
    }
    
    public static boolean prop_p2dSubAddEquality(Point2D one) {
        return one.subtract(one).add(one).equals(one);
    }
    
    public static boolean prop_p2dNegNegEquality(Point2D one) {
        return one.negative().negative().equals(one);
    }
    
    public static boolean prop_p2dDistZero(Point2D one) {
        return one.distanceFrom(one) == 0.0f;
    }
    
    public static boolean prop_p2dNormLengthOne(Point2D one) {
        return one.normalise().getLength() == 1.0f;
    }
    
    public static boolean prop_p2dSubSelfZero(Point2D one) {
        return one.subtract(one).isZero();
    }
    
    public static boolean prop_p2dCopyEquality(Point2D one) {
        return one.copy().equals(one);
    }
    
    /* Point3D */
    
    public static boolean prop_p3dAddSubEquality(Point3D one) {
        return one.add(one).subtract(one).equals(one);
    }
    
    public static boolean prop_p3dSubAddEquality(Point3D one) {
        return one.subtract(one).add(one).equals(one);
    }
    
    public static boolean prop_p3dNegNegEquality(Point3D one) {
        return one.negative().negative().equals(one);
    }
    
    public static boolean prop_p3dDistZero(Point3D one) {
        return one.distanceFrom(one) == 0.0f;
    }
    
    public static boolean prop_p3dNormLengthOne(Point3D one) {
        return one.normalise().getLength() == 1.0f;
    }
    
    public static boolean prop_p3dSubSelfZero(Point3D one) {
        return one.subtract(one).isZero();
    }
    
    public static boolean prop_p3dCopyEquality(Point3D one) {
        return one.copy().equals(one);
    }
    
    /* Bounds */
    
    public static boolean prop_bndsCopyEquality(Bounds one) {
        return one.copy().equals(one);
    }
    
    public static boolean prop_bndsEnlargeSizeMatch(Bounds one, float two) {
        Bounds oneC = one.copy();
        oneC.enlarge(two);
        return one.getWidth() == oneC.getWidth() + two
            && one.getHeight() == oneC.getHeight() + two;
    }
}
