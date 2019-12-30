package org.cloudbus.cloudsimdisk.power.models.hdd;

/**
 * Power Model based on Seagate 600 Pro  Enterprise SSD Review .
 *
 * Link: http://www.storagereview.com/seagate_600_pro_enterprise_ssd_review
 *
 * Created by SaiVishwas on 1/24/17.
 */public class PowerModelSsdSeagate600ProEnterpriseST480FP0021 extends PowerModelHdd {

    /* (non-Javadoc)
	 *
	 * @see org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd#getPowerData(int) */
    @Override
    protected Object getPowerData(int key) {

        switch (key) {
            case 0:
                return 1.25; // Idle mode, in W.
            case 1:
                return 2.8; // Active mode, in W.
            default:
                return "n/a";

            // SCALABILITY: add new mode by adding new CASE.
            //
            // case <KEY_NUMBER>:
            // return <POWER_VALUE>;
        }
    }
}
