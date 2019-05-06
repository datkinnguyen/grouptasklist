package com.flinders.nguy1025.grouptasklist;

import android.net.Uri;

public class EasyFileProvider extends android.support.v4.content.FileProvider {

    public EasyFileProvider(){

        super();
    }

    //Enforce image type so that image is previewable in default clients on sharing
    @Override
    public String getType(Uri uri) {
        return "image/*";
    }
}
