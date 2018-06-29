package com.burnweb.rnwebview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.common.annotations.VisibleForTesting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class RNWebViewModule extends ReactContextBaseJavaModule implements ActivityEventListener {


    private final int RC_CAMERA = 1;
    private final int RC_ALBUM = 2;
    private Uri cameraCaptureURI;
    private final ReactApplicationContext reactContext;

    @VisibleForTesting
    public static final String REACT_CLASS = "RNWebViewAndroidModule";

    private RNWebViewPackage aPackage;

    /* FOR UPLOAD DIALOG */
    private final static int REQUEST_SELECT_FILE = 1001;
    private final static int REQUEST_SELECT_FILE_LEGACY = 1002;
    private final static int REQUEST_SELECT_CAMERA = 1003;

    private ValueCallback<Uri> mUploadMessage = null;
    private ValueCallback<Uri[]> mUploadMessageArr = null;

    public RNWebViewModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public void setPackage(RNWebViewPackage aPackage) {
        this.aPackage = aPackage;
    }

    public RNWebViewPackage getPackage() {
        return this.aPackage;
    }

    @SuppressWarnings("unused")
    public Activity getActivity() {
        return getCurrentActivity();
    }

    public void showAlert(String url, String message, final JsResult result) {
        AlertDialog ad = new AlertDialog.Builder(getCurrentActivity())
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                })
                .create();

        ad.show();
    }

    // For Android 4.1+
    @SuppressWarnings("unused")
    public boolean startFileChooserIntent(ValueCallback<Uri> uploadMsg, final String acceptType) {

        Log.d(REACT_CLASS, "Open old file dialog");

        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(null);
            mUploadMessage = null;
        }

        mUploadMessage = uploadMsg;

        final Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            Log.w(REACT_CLASS, "No context available");
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setTitle("请选择");
        builder.setItems(new String[]{"相机", "相册", "文件"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mAcceptType = "*/*";
                switch (which) {
                    case 0:
                        //相机
                        Intent intent_camera = new Intent("android.media.action.IMAGE_CAPTURE");
                        currentActivity.startActivityForResult(intent_camera, REQUEST_SELECT_FILE_LEGACY, new Bundle());
                        break;
                    case 1:
                        //相册
                        Intent intent_album = new Intent(Intent.ACTION_PICK);
                        intent_album.setType("image/*");
                        currentActivity.startActivityForResult(intent_album, REQUEST_SELECT_FILE_LEGACY, new Bundle());
                        break;
                    case 2:
                        if (acceptType != null && !acceptType.isEmpty()) {
                            mAcceptType = acceptType;
                        } else {
                            mAcceptType = "image/*";
                        }

                        Intent intentChoose = new Intent(Intent.ACTION_PICK);

                        intentChoose.addCategory(Intent.CATEGORY_OPENABLE);
                        intentChoose.setType(mAcceptType);

                        try {
                            currentActivity.startActivityForResult(intentChoose, REQUEST_SELECT_FILE_LEGACY, new Bundle());
                        } catch (ActivityNotFoundException e) {
                            Log.e(REACT_CLASS, "No context available");
                            e.printStackTrace();
                            if (mUploadMessage != null) {
                                mUploadMessage.onReceiveValue(null);
                                mUploadMessage = null;
                            }
                        }
                        break;
                }
            }
        });
        builder.create().show();
        return true;
    }

    // For Android 5.0+
    @SuppressLint("NewApi")
    public boolean startFileChooserIntent(ValueCallback<Uri[]> filePathCallback, final Intent intentChoose) {
        Log.d(REACT_CLASS, "Open new file dialog");

        if (mUploadMessageArr != null) {
            mUploadMessageArr.onReceiveValue(null);
            mUploadMessageArr = null;
        }

        mUploadMessageArr = filePathCallback;

        final Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            Log.w(REACT_CLASS, "No context available");
            return false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setTitle("请选择");
        builder.setItems(new String[]{"相机", "相册", "文件"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mAcceptType = "*/*";

                switch (which) {
                    case 0:
                        //相机
                        Intent intent_camera = new Intent("android.media.action.IMAGE_CAPTURE");

                        File imageFile = createNewFile();
                        cameraCaptureURI = compatUriFromFile(reactContext, imageFile);
                        if (cameraCaptureURI == null) {
                            break;
                        }

                        intent_camera.putExtra(MediaStore.EXTRA_OUTPUT, cameraCaptureURI);

                        currentActivity.startActivityForResult(intent_camera, REQUEST_SELECT_CAMERA, new Bundle());
                        break;
                    case 1:
                        //相册
                        Intent intent_album = new Intent(Intent.ACTION_PICK);
                        intent_album.setType("image/*");
                        currentActivity.startActivityForResult(intent_album, REQUEST_SELECT_FILE, new Bundle());
                        break;
                    case 2:

                        try {
                            currentActivity.startActivityForResult(intentChoose, REQUEST_SELECT_FILE, new Bundle());
                        } catch (ActivityNotFoundException e) {
                            Log.e(REACT_CLASS, "No context available");
                            e.printStackTrace();

                            if (mUploadMessage != null) {
                                mUploadMessage.onReceiveValue(null);
                                mUploadMessage = null;
                            }

                        }
                        break;
                }
            }
        });
        builder.create().show();
        return true;
    }

    @SuppressLint({"NewApi", "Deprecated"})
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i("onActivityResult", requestCode + "");
        Log.i("onActivityResult", resultCode + "");
//        Log.i("onActivityResult",data);

        if (requestCode == REQUEST_SELECT_FILE_LEGACY) {
            if (mUploadMessage == null) return;

            Uri result = ((data == null || resultCode != Activity.RESULT_OK) ? null : data.getData());

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == REQUEST_SELECT_FILE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mUploadMessageArr == null) return;

            mUploadMessageArr.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            mUploadMessageArr = null;
        } else if (requestCode == REQUEST_SELECT_CAMERA) {
//            if (mUploadMessage == null) return;
//            mUploadMessage.onReceiveValue(fileUri);
//            mUploadMessage = null;
            if (mUploadMessageArr == null) return;

            Uri uri = cameraCaptureURI;
            String realPath = getRealPathFromURI(uri);
            boolean isUrl = false;
            if (realPath != null) {
                try {
                    URL url = new URL(realPath);
                    isUrl = true;
                } catch (MalformedURLException e) {
                    // not a url
                }
            }
            if (realPath == null || isUrl) {
                try {
                    File file = createFileFromURI(uri);
                    realPath = file.getAbsolutePath();
                    uri = Uri.fromFile(file);
                } catch (Exception e) {
                    return;
                }
            }
            Intent retData = new Intent();
            retData.setData(uri);
            mUploadMessageArr.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, retData));
            mUploadMessageArr = null;
        }
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        this.onActivityResult(requestCode, resultCode, data);
    }

    public void onNewIntent(Intent intent) {
    }

    private File createNewFile() {
        String filename = new StringBuilder("image-")
                .append(UUID.randomUUID().toString())
                .append(".jpg")
                .toString();
        File path = reactContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File f = new File(path, filename);
        try {
            path.mkdirs();
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private static @Nullable
    Uri compatUriFromFile(@NonNull final Context context, @NonNull final File file) {
        Uri result = null;
        if (Build.VERSION.SDK_INT < 21) {
            result = Uri.fromFile(file);
        } else {
            final String packageName = context.getApplicationContext().getPackageName();
            final String authority = new StringBuilder(packageName).append(".provider").toString();
            try {
                result = FileProvider.getUriForFile(context, authority, file);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private @NonNull
    String getRealPathFromURI(@NonNull final Uri uri) {
        return getRealPathFromURI(reactContext, uri);
    }

    public static @Nullable
    String getRealPathFromURI(@NonNull final Context context, @NonNull final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            if (isFileProviderUri(context, uri))
                return getFileProviderPath(context, uri);

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(@NonNull final Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isFileProviderUri(@NonNull final Context context,
                                            @NonNull final Uri uri) {
        final String packageName = context.getPackageName();
        final String authority = new StringBuilder(packageName).append(".provider").toString();
        return authority.equals(uri.getAuthority());
    }

    public static @Nullable
    String getFileProviderPath(@NonNull final Context context,
                               @NonNull final Uri uri) {
        final File appDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File file = new File(appDir, uri.getLastPathSegment());
        return file.exists() ? file.toString() : null;
    }

    private File createFileFromURI(Uri uri) throws Exception {
        File file = new File(reactContext.getExternalCacheDir(), "photo-" + uri.getLastPathSegment());
        InputStream input = reactContext.getContentResolver().openInputStream(uri);
        OutputStream output = new FileOutputStream(file);

        try {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } finally {
            output.close();
            input.close();
        }

        return file;
    }
}
