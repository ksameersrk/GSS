package org.cloudbus.cloudsimdisk.power.models.hdd;

/**
 * Storage Model based on Intel SSD DC S3500 Enterprise Review .
 *
 * Link: http://www.storagereview.com/intel_ssd_dc_s3500_enterprise_review
 *
 * Created by SaiVishwas on 1/24/17.
 */
public class PowerModelSsdIntelDCS3500EnterpriseSC2BB800G401 extends PowerModelHdd {

    /* (non-Javadoc)
     *
     * @see org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd#getPowerData(int) */
    @Override
    protected Object getPowerData(int key) {

        switch (key) {
            case 0:
                return 0.65; // Idle mode, in W.
            case 1:
                return 5.0; // Active mode, in W.
            default:
                return "n/a";

            // SCALABILITY: add new mode by adding new CASE.
            //
            // case <KEY_NUMBER>:
            // return <POWER_VALUE>;
        }
    }
}
