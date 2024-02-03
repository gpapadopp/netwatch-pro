package eu.gpapadop.netwatchpro.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;

import java.io.File;

public class PathUtils {
    public static String getPathFromUri(Context context, Uri uri) {
        String folderPath = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            String[] projection = {DocumentsContract.Document.COLUMN_DOCUMENT_ID};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String documentId = cursor.getString(0);
                cursor.close();

                String[] split = documentId.split(":");
                if (split.length > 1) {
                    String storageId = split[0];
                    String documentPath = split[1];

                    if ("primary".equalsIgnoreCase(storageId)) {
                        folderPath = Environment.getExternalStorageDirectory() + File.separator + documentPath;
                    } else {
                        // Handle other storage volumes here
                    }
                }
            }
        }

        return folderPath;
    }
}
