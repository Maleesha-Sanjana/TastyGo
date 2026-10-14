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

    public static int getDrawableResForPath(Context context, String path) {
        if (path == null || path.trim().isEmpty()) {
            return R.drawable.real_food_burger;
        }
        String lower = path.toLowerCase().trim();
        if (lower.contains("pizza") || lower.contains("margherita") || lower.contains("pepperoni") || lower.contains("truffle")) {
            return R.drawable.real_food_pizza;
        }
        if (lower.contains("ramen") || lower.contains("chashu") || lower.contains("tonkotsu")) {
            return R.drawable.real_food_ramen;
        }
        if (lower.contains("padthai") || lower.contains("pad_thai") || lower.contains("asian") || lower.contains("noodle") || lower.contains("sichuan")) {
            return R.drawable.real_food_padthai;
        }
        if (lower.contains("smoothie") || lower.contains("mango") || lower.contains("juice") || lower.contains("shake")) {
            return R.drawable.real_food_smoothie;
        }
        if (lower.contains("cake") || lower.contains("dessert") || lower.contains("chocolate") || lower.contains("lava")) {
            return R.drawable.real_food_dessert;
        }
        if (lower.contains("latte") || lower.contains("coffee") || lower.contains("drink") || lower.contains("tea") || lower.contains("caramel")) {
            return R.drawable.real_food_latte;
        }
        if (lower.contains("promo") || lower.contains("dish")) {
            return R.drawable.real_promo_dish;
        }
        if (lower.contains("burger") || lower.contains("bbq") || lower.contains("veggie") || lower.contains("patty") || lower.contains("cheeseburger")) {
            return R.drawable.real_food_burger;
        }

        if (context != null) {
            try {
                int resId = context.getResources().getIdentifier(lower, "drawable", context.getPackageName());
                if (resId != 0) {
                    return resId;
                }
            } catch (Exception ignored) {}
        }
        return R.drawable.real_food_burger;
    }

    public static String mapToRealPhoto(String path) {
        if (path == null || path.trim().isEmpty() || "ic_food_placeholder".equalsIgnoreCase(path)) {
            return "real_food_burger";
        }
        if (path.startsWith("/")) {
            return path;
        }
        String lower = path.toLowerCase().trim();
        if (lower.contains("pizza") || lower.contains("margherita") || lower.contains("pepperoni") || lower.contains("truffle")) {
            return "real_food_pizza";
        }
        if (lower.contains("ramen") || lower.contains("chashu") || lower.contains("tonkotsu")) {
            return "real_food_ramen";
        }
        if (lower.contains("padthai") || lower.contains("pad_thai") || lower.contains("asian") || lower.contains("noodle") || lower.contains("sichuan")) {
            return "real_food_padthai";
        }
        if (lower.contains("smoothie") || lower.contains("mango") || lower.contains("juice") || lower.contains("shake")) {
            return "real_food_smoothie";
        }
        if (lower.contains("cake") || lower.contains("dessert") || lower.contains("chocolate") || lower.contains("lava")) {
            return "real_food_dessert";
        }
        if (lower.contains("latte") || lower.contains("coffee") || lower.contains("drink") || lower.contains("tea") || lower.contains("caramel")) {
            return "real_food_latte";
        }
        if (lower.contains("promo") || lower.contains("dish")) {
            return "real_promo_dish";
        }
        if (lower.contains("burger") || lower.contains("bbq") || lower.contains("veggie") || lower.contains("patty") || lower.contains("cheeseburger")) {
            return "real_food_burger";
        }
        return "real_food_burger";
    }

    public static void loadImage(Context context, String path, ImageView imageView, int placeholderRes) {
        if (context == null || imageView == null) {
            return;
        }

        if (context instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return;
            }
        }

        int targetResId = getDrawableResForPath(context, path);

        imageView.setImageResource(targetResId);

        if (path != null && path.startsWith("/")) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    Glide.with(context)
                            .load(file)
                            .placeholder(targetResId)
                            .error(targetResId)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(imageView);
                    return;
                } catch (Exception e) {
                    imageView.setImageResource(targetResId);
                    return;
                }
            }
        }

        try {
            Glide.with(context)
                    .load(targetResId)
                    .placeholder(targetResId)
                    .error(targetResId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(targetResId);
        }
    }
}
