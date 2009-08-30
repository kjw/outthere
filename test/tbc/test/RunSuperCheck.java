package tbc.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tbc.supercheck.TestRun;
import tbc.supercheck.Recording;

public class RunSuperCheck
{
    public static void main(String[] args) {
        TestRun r = new TestRun();
        r.runOn(tbc.test.data.spatial.Invariants.class, 10000);
        
        Recording recording = r.getRecording();
        
//        try {
//            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream("test.recording"));
//            oout.writeObject(recording);
//            oout.close();
//        } catch (IOException e) {
//            
//        }
//        
//        try {
//            ObjectInputStream oin = new ObjectInputStream(new FileInputStream("test.recording"));
//            recording = (Recording) oin.readObject();
//            oin.close();
//        } catch (IOException e) {
//            
//        } catch (ClassNotFoundException e) {
//            
//        }
//        
//        TestRun r2 = new TestRun();
//        r2.runRecording(recording);
        
        System.out.println();
        System.out.println(recording.toDescription());
    }
}
