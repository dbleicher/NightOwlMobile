package com.geofinity.wgu.nightowl.model;

import com.geofinity.wgu.nightowl.ui.adapters.HomeCardAdapter;

/**
 * Created by davidbleicher on 12/11/14.
 */
public class HCNavigate implements IHomeCard {

    @Override
    public int getType() {
        return HomeCardAdapter.HC_NAVIGATE;
    }
}
