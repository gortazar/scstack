package es.sidelab.code.virtualbox;

import java.util.List;

import org.virtualbox_4_1.IMachine;
import org.virtualbox_4_1.IMedium;
import org.virtualbox_4_1.IProgress;
import org.virtualbox_4_1.IVirtualBox;
import org.virtualbox_4_1.VirtualBoxManager;

/**
 * Hello world!
 *
 */
public class App {
	
    public static void main( String[] args ) {
    	
    	VirtualBoxManager vbm = VirtualBoxManager.createInstance(null);
    	vbm.connect("http://localhost:18083", null, null);
    	
    	IVirtualBox vBox = vbm.getVBox();
    	List<IMedium> hds = vBox.getHardDisks();
    	for(IMedium hd : hds) {
    		System.out.println(hd.getName());
    		System.out.println(hd.getId());
    		
//    		if("SidelabCode.vdi".equals(hd.getName())) {
//    			IMedium newHD = vBox.createHardDisk("vdi", "/home/patxi/.VirtualBox/HardDisks/theCopy.vdi");
//    			getId());
//    			hd.cloneTo(newHD, MediumVariant.Standard, null);
//    		}
    	}
    	
    	List<IMachine> machines = vBox.getMachines();
    	for(IMachine m : machines) {
    		System.out.println(m.getName());
    		System.out.println(m.getId());
    		
    		if("SidelabCode".equals(m.getName())) {
    			IProgress progress = m.launchVMProcess(vbm.getSessionObject(), "gui", null);
    			progress.waitForCompletion(-1);
    			System.out.println("VM started");
    		}
    	}
        
    }
}
