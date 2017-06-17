package org.cloudbus.cloudsimdisk.models.hdd;

/**
 * Storage Model based on Intel SSD DC S3500 Enterprise Review .
 *
 * Link: http://www.storagereview.com/intel_ssd_dc_s3500_enterprise_review
 *
 * Created by SaiVishwas on 1/24/17.
 */
public class StorageModelSsdIntelDCS3500EnterpriseSC2BB800G401 extends StorageModelHdd {
    /* (non-Javadoc)
	 *
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int) */
    @Override
    protected Object getCharacteristic(int key) {
        switch (key) {
            case 0:
                return "Intel"; // Manufacturer
            case 1:
                return "SSDSC2BB800G401"; // Model Number
            case 2:
                //return 800000; // capacity (MB)
                return 1000; // capacity (MB)
            case 3:
                return 0.0000001; // Average Rotation Latency (s)
            case 4:
                return 0.000065; // Average Seek Time (s)
            case 5:
                return 500.0; // Maximum Internal Data Transfer Rate (MB/s)
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
