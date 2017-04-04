package org.cloudbus.cloudsimdisk.models.hdd;

/**
 * Storage Model based on Toshiba HG6 Enterprise SSD  Review .
 *
 * Link: http://www.storagereview.com/toshiba_hg6_ssd_review
 *
 * Created by SaiVishwas on 1/24/17.
 */
public class StorageModelSsdToshibaHG6EnterpriseTHNSNJ512GCSU extends StorageModelHdd {
    /* (non-Javadoc)
	 *
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int) */
    @Override
    protected Object getCharacteristic(int key) {
        switch (key) {
            case 0:
                return "Toshiba"; // Manufacturer
            case 1:
                return "THNSNJ512GCSU"; // Model Number
            case 2:
                //return 512000; // capacity (MB)
                //return 3173332;
                return 100;


            case 3:
                return 0.0000001; // Average Rotation Latency (s)
            case 4:
                return 0.064; // Average Seek Time (s)
            case 5:
                return 482.0; // Maximum Internal Data Transfer Rate (MB/s)
            default:
                return "n/a";

            // SCALABILITY: add new characteristics by adding new CASEs.
            //
            // case <KEY_NUMBER>:
            // return <PARAMETER_VALUE>;
            //
        }
    }
}
