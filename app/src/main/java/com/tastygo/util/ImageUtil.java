package com.tastygo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tastygo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageUtil {

    private static final String DIRECTORY_FOOD_IMAGES = "food_images";

    /**
     * Copies an image from a URI (such as picked from the gallery) into internal app storage.
     * Returns the absolute path of the saved file.
     */
    public static String saveImageToInternalStorage(Context context, Uri sourceUri) {
        if (context == null || sourceUri == null) {
            return null;
        }
        try {
            File dir = new File(context.getFilesDir(), DIRECTORY_FOOD_IMAGES);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "tastygo_" + System.currentTimeMillis() + ".jpg";
            File destinationFile = new File(dir, fileName);

            try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
                 OutputStream out = new FileOutputStream(destinationFile)) {
                if (in == null) return null;
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            return destinationFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads an image into an ImageView using Glide with caching and placeholder.
     * Supports both file paths on internal storage and drawable resource identifiers.
     */
    public static void loadImage(Context context, String path, ImageView imageView, int placeholderRes) {
        if (context == null || imageView == null) {
            return;
        }

        if (path == null || path.trim().isEmpty()) {
            imageView.setImageResource(placeholderRes);
            return;
        }

        // Check if path is a drawable resource name
        if (!path.startsWith("/")) {
            int resId = context.getResources().getIdentifier(path, "drawable", context.getPackageName());
            if (resId != 0) {
                Glide.with(context)
                        .load(resId)
                        .placeholder(placeholderRes)
                        .error(placeholderRes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(imageView);
                return;
            }
        }

        File file = new File(path);
        if (file.exists()) {
            Glide.with(context)
                    .load(file)
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(imageView);
        } else {
            imageView.setImageResource(placeholderRes);
        }
    }
}
