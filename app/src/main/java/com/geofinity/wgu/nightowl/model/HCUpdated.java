package com.geofinity.wgu.nightowl.model;

import com.geofinity.wgu.nightowl.ui.adapters.HomeCardAdapter;

/**
 * Created by davidbleicher on 12/12/14.
 */
public class HCUpdated implements IHomeCard {

    @Override
    public int getType() {
        return HomeCardAdapter.HC_UPDATED;
    }
}
