package com.flinders.nguy1025.grouptasklist;

import android.net.Uri;
import androidx.core.content.FileProvider;

public class EasyFileProvider extends FileProvider {

    public EasyFileProvider(){

        super();
    }

    //Enforce image type so that image is previewable in default clients on sharing
    @Override
    public String getType(Uri uri) {
        return "image/*";
    }
}
