package org.cloudbus.cloudsimdisk.power.models.hdd;

/**
 * Power Model based on Toshiba HG6 Enterprise SSD  Review .
 *
 * Link: http://www.storagereview.com/toshiba_hg6_ssd_review
 *
 * Created by SaiVishwas on 1/24/17.
 */
public class PowerModelSsdToshibaHG6EnterpriseTHNSNJ512GCSU extends PowerModelHdd {
    /* (non-Javadoc)
	 *
	 * @see org.cloudbus.cloudsimdisk.power.models.hdd.PowerModelHdd#getPowerData(int) */
    @Override
    protected Object getPowerData(int key) {

        switch (key) {
            case 0:
                return 0.125; // Idle mode, in W.
            case 1:
                return 3.3; // Active mode, in W.
            default:
                return "n/a";

            // SCALABILITY: add new mode by adding new CASE.
            //
            // case <KEY_NUMBER>:
            // return <POWER_VALUE>;
        }
    }
}
