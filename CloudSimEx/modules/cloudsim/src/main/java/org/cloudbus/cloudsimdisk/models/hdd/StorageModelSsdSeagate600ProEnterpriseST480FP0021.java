package org.cloudbus.cloudsimdisk.models.hdd;

/**
 * Storage Model based on Seagate 600 Pro  Enterprise SSD Review .
 *
 * Link: http://www.storagereview.com/seagate_600_pro_enterprise_ssd_review
 *
 * Created by SaiVishwas on 1/24/17.
 */
public class StorageModelSsdSeagate600ProEnterpriseST480FP0021 extends StorageModelHdd {
    /* (non-Javadoc)
	 *
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int) */
    @Override
    protected Object getCharacteristic(int key) {
        switch (key) {
            case 0:
                return "Seagate Technology"; // Manufacturer
            case 1:
                return "ST240FP0021"; // Model Number
            case 2:
                return 480000; // capacity (MB)
            case 3:
                return 0.0000001; // Average Rotation Latency (s)
            case 4:
                return 0.00836; // Average Seek Time (s)
            case 5:
                return 512.0; // Maximum Internal Data Transfer Rate (MB/s)
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
