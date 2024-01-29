package eu.gpapadop.netwatchpro.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class DrawableUtils {
    private Context parentContext;
    public DrawableUtils(Context newParentContext){
        this.parentContext = newParentContext;
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public Drawable stringToDrawable(String drawableString) {
        if (drawableString == null) {
            return null;
        }

        byte[] byteArray = Base64.decode(drawableString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return new BitmapDrawable(this.parentContext.getResources(), bitmap);
    }

    public String drawableToString(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        // Convert the Drawable to a Bitmap
        Bitmap bitmap = this.drawableToBitmap(drawable);

        // Convert the Bitmap to a base64 string
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
