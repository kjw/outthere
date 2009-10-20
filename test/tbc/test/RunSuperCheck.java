package tbc.test;

import tbc.data.spatial.Point2D;
import tbc.data.spatial.Point3D;
import tbc.supercheck.ParameterBunch;
import tbc.supercheck.TestRun;
import tbc.trader.junk.Cube;

public class RunSuperCheck
{
    public static void main(String[] args) {
        new TestRun().with(getTestParams())
                     .runOn(tbc.test.data.spatial.Invariants.class, 10000);
    }
    
    public static ParameterBunch getTestParams()
    {
    	ParameterBunch pb = new ParameterBunch();
    	
    	pb.setInt(Point3D.PARAM_MAX_DI, 40);
    	pb.setInt(Point2D.PARAM_MAX_DI, 40);
    	pb.setInt(Cube.PARAM_MAX_DI,    5);
    	
    	return pb;
    }
}
