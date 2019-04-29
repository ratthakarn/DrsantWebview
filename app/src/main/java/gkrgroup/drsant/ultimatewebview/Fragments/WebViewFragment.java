package gkrgroup.drsant.ultimatewebview.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import gkrgroup.drsant.ultimatewebview.DetectConnection;
import gkrgroup.drsant.ultimatewebview.CustomWebview;
import gkrgroup.drsant.ultimatewebview.MainActivity;
import gkrgroup.drsant.ultimatewebview.R;


public class WebViewFragment extends Fragment {
    private final static int asw_file_req = 1;
    private final static int loc_perm = 1;
    private final static int file_perm = 2;
    public static int addTime = 1;
    View view;
    // String url;
    ProgressBar progressBar1;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    InterstitialAd mInterstitialAd;
    LinearLayout internetNotAvailable;
    SwipeRefreshLayout swipeRefreshLayout;
    CustomWebview simpleWebView;
    RelativeLayout rel_layout;
    View.OnClickListener mOnClickListener;
    private Button btnRetry;
    private String TAG = WebViewFragment.class.getName();
    private ValueCallback<Uri> uploadFileMsg;
    private ValueCallback<Uri[]> uploadFilePath;
    private boolean isAlreadyHandelPdf = false;
    private String camera_image_path;
    SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {

            if (!DetectConnection.checkInternetConnection(getActivity())) {
                internetNotAvailable.setVisibility(View.VISIBLE);
                simpleWebView.setVisibility(View.GONE);
                // cancel the Visual indication of a refresh
                swipeRefreshLayout.setRefreshing(false);
            } else {
                internetNotAvailable.setVisibility(View.GONE);
                simpleWebView.setVisibility(View.VISIBLE);
                if (simpleWebView.getUrl() != null && !TextUtils.isEmpty(simpleWebView.getUrl())) {
                    loadWebPage(simpleWebView.getUrl());
                } else {
                    loadWebPage(getArguments().getString("url"));
                }

            }

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);

    }

/*
*
* Handler to get the actual url from image src
* */


    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
           String url = (String) msg.getData().get("url");

           if(url != null){
               Intent browserIntent = new Intent(Intent.ACTION_VIEW);
               browserIntent.setData(Uri.parse(url));
               getActivity().startActivity(browserIntent);
           }
        }
    } ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_web_view, container, false);
        // ButterKnife.bind(this, view);
        //...........................
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
        MainActivity.drawerLayout.closeDrawers();
        progressBar1 = (ProgressBar) view.findViewById(R.id.progressBar1);
        simpleWebView = (CustomWebview) view.findViewById(R.id.simpleWebView);

        internetNotAvailable = (LinearLayout) view.findViewById(R.id.internetNotAvailable);

        sharedPreferences = getActivity().getSharedPreferences("rateUs", 0);
        editor = sharedPreferences.edit();

        Bundle bundle = getArguments();
        String url = bundle.getString("url");
        if (!DetectConnection.checkInternetConnection(getActivity())) {
            internetNotAvailable.setVisibility(View.VISIBLE);
            simpleWebView.setVisibility(View.GONE);
        } else {
            internetNotAvailable.setVisibility(View.GONE);
            simpleWebView.setVisibility(View.VISIBLE);
            loadWebPage(url);
        }
        MainActivity.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/*");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "WebView App Demo");

                //code to share webview link
                 shareIntent.putExtra(Intent.EXTRA_TEXT, "Check this page: " + simpleWebView.getUrl());

                //code to share playstore link
                //shareIntent.putExtra(Intent.EXTRA_TEXT, "Check this page: " + "https://gkrgroup.co.th" + getActivity().getPackageName());//abhiandroid.com.ultimatewebview");

                startActivity(Intent.createChooser(shareIntent, "Share Using"));

            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.simpleSwipeRefreshLayout);
        // implement setOnRefreshListener event on SwipeRefreshLayout
        //swipeRefreshLayout.setOnRefreshListener(onRefreshListener);


        btnRetry = (Button) view.findViewById(R.id.btn_retry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internetNotAvailable.setVisibility(View.GONE);
                simpleWebView.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(true);
                onRefreshListener.onRefresh();
            }
        });


        return view;
    }

    private void loadWebPage(String url) {
        simpleWebView.getSettings().setSupportMultipleWindows(true);
        simpleWebView.getSettings().setJavaScriptEnabled(true); // enable javascript
        simpleWebView.getSettings().setLoadWithOverviewMode(true);
        simpleWebView.getSettings().setUseWideViewPort(true);
        simpleWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        simpleWebView.getSettings().setBuiltInZoomControls(true);
        simpleWebView.getSettings().setSupportMultipleWindows(true);
        simpleWebView.getSettings().setAllowFileAccess(true);
        simpleWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        simpleWebView.getSettings().setDisplayZoomControls(true);

        // Add setting to allow upload image
        simpleWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        simpleWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        simpleWebView.getSettings().setUseWideViewPort(true);
        simpleWebView.getSettings().setDomStorageEnabled(true);
        simpleWebView.getSettings().setSaveFormData(false);

        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            simpleWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            simpleWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        } else if (Build.VERSION.SDK_INT >= 19) {
            simpleWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        simpleWebView.setVerticalScrollBarEnabled(false);
        simpleWebView.setHorizontalScrollBarEnabled(false);
        simpleWebView.setWebViewClient(new MyWebViewClient());


        simpleWebView.loadUrl(url);
        simpleWebView.requestFocus(View.FOCUS_DOWN);
        simpleWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        simpleWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                WebView.HitTestResult result = view.getHitTestResult();
                try {
                    if(result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE){
                        Message message = mHandler.obtainMessage();
                        simpleWebView.requestFocusNodeHref(message);
                    }else {
                        String data = result.getExtra();
                        Context context = view.getContext();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                        browserIntent.setData(Uri.parse(data));
                        getActivity().startActivity(browserIntent);
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Please try again.", Toast.LENGTH_LONG).show();
                }


                return false;
            }

            //Handling input[type="file"] requests for android API 16+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                uploadFileMsg = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                getActivity().startActivityForResult(Intent.createChooser(intent, "File Chooser"), asw_file_req);
            }

            //Handling input[type="file"] requests for android API 21+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                get_file();
                if (uploadFilePath != null) {
                    uploadFilePath.onReceiveValue(null);
                }
                uploadFilePath = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                Intent[] intentArray;

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = create_image();
                        takePictureIntent.putExtra("PhotoPath", camera_image_path);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        camera_image_path = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, intent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                getActivity().startActivityForResult(chooserIntent, asw_file_req);

                return true;
            }

            //Getting webview rendering progress
            @Override
            public void onProgressChanged(WebView view, int p) {
            }
        });




    }

    private void showRateDialog() {


        new AlertDialog.Builder(getActivity())
                .setTitle("Rate Us On Play Store")
                .setMessage("If you like this App. Please rate us on Play Store.")
                .setPositiveButton("Rate Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        editor.putString("rate", "Yes");
                        editor.commit();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getActivity().getPackageName())));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
                        }

                    }
                })
                .setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        editor.putString("rate", "No");
                        editor.commit();

                    }
                }).show();
    }

    private void showAd() {
        addTime = addTime + 1;
        mInterstitialAd = new InterstitialAd(getActivity());

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial));

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("0EE7E9267C3B18C46C76B07B7DE04FBE")
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == 1) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(getActivity(), "Oops, you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Create an image file
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == asw_file_req) {
                    if (null == uploadFilePath) {
                        return;
                    }
                    if (intent == null || intent.getDataString() == null) {
                        if (camera_image_path != null) {
                            results = new Uri[]{Uri.parse(camera_image_path)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }
            }
            uploadFilePath.onReceiveValue(results);
            uploadFilePath = null;
        } else {
            if (requestCode == asw_file_req) {
                if (null == uploadFileMsg) return;
                Uri result = intent == null || resultCode != getActivity().RESULT_OK ? null : intent.getData();
                uploadFileMsg.onReceiveValue(result);
                uploadFileMsg = null;
            }
        }
    }

    //Checking permission for storage and camera for writing and uploading images
    public void get_file() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        //Checking for storage permission to write images for upload
        if (!check_permission(2) && !check_permission(3)) {
            ActivityCompat.requestPermissions(getActivity(), perms, file_perm);

            //Checking for WRITE_EXTERNAL_STORAGE permission
        } else if (!check_permission(2)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, file_perm);

            //Checking for CAMERA permissions
        } else if (!check_permission(3)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, file_perm);
        }
    }

    //Creating image file for upload
    private File create_image() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name = "file_" + file_name + "_";
        File sd_directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".jpg", sd_directory);
    }

    //Checking if particular permission is given or not
    public boolean check_permission(int permission) {
        switch (permission) {
            case 1:
                return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            case 2:
                return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            case 3:
                return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        }
        return false;
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.endsWith(".pdf") && isAlreadyHandelPdf) {
                isAlreadyHandelPdf = false;
                return false;
            }
            if (addTime % 4 == 0 && !url.contains("youtube.com"))
                showAd();
            else if (addTime % 7 == 0) {
                addTime = addTime + 1;
                if (sharedPreferences.getString("rate", "No").equalsIgnoreCase("No")) {
                    showRateDialog();
                }
            } else
                addTime = addTime + 1;

            Log.d("Clicked URL", url.toString());
            if (URLUtil.isNetworkUrl(url)) {
                if (url.equalsIgnoreCase("https://www.android.com/") || url.equalsIgnoreCase("https://play.google.com/store/") || url.equalsIgnoreCase("http://abhiandroid.com/sourcecode/webview/")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (url.endsWith(".mp3")) {
                    Log.d("Download Code", "Download Start");
                    if (isReadStorageAllowed()) {
                        Uri source = Uri.parse(url);
                        // Make a new request pointing to the .mp3 url
                        DownloadManager.Request request = new DownloadManager.Request(source);
                        // appears the same in Notification bar while downloading
                        request.setDescription("Description for the DownloadManager Bar");
                        request.setTitle(url.substring(url.lastIndexOf("/") + 1));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
                        // save the file in the "Downloads" folder of SDCARD
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1));
                        // get download service and enqueue file
                        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                        Toast.makeText(getActivity(), "Downloading Started...", Toast.LENGTH_LONG).show();
                    } else {
                        requestStoragePermission();
                    }
                    return true;
                } else if (url.endsWith(".pdf")) {
                    view.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + url);
                    swipeRefreshLayout.setEnabled(false);
                    swipeRefreshLayout.setRefreshing(false);

                    isAlreadyHandelPdf = true;
                } else if (url.contains("youtube.com")) {
                    ((MainActivity) getActivity()).hideAds();
                } else {
                    ((MainActivity) getActivity()).showAds();
                }
                return false;
            }

            // Otherwise allow the OS to handle it
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;

        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.getUrl().toString().endsWith(".pdf") && isAlreadyHandelPdf) {
                isAlreadyHandelPdf = false;
                return false;
            }
            if (addTime % 4 == 0 && !request.getUrl().toString().contains("youtube.com"))
                showAd();
            else if (addTime % 7 == 0) {
                addTime = addTime + 1;
                if (sharedPreferences.getString("rate", "No").equalsIgnoreCase("No")) {
                    showRateDialog();
                }
            } else
                addTime = addTime + 1;
            String url = request.getUrl() + "";
            Log.d("Clicked URL", request.getUrl().toString());
            if (URLUtil.isNetworkUrl(url)) {
                if (url.equalsIgnoreCase("https://www.android.com/") || url.equalsIgnoreCase("https://play.google.com/store/") || url.equalsIgnoreCase("http://abhiandroid.com/sourcecode/webview/")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (url.endsWith(".mp3")) {
                    Log.d("Download Code", "Download Start");
                    if (isReadStorageAllowed()) {
                        Uri source = Uri.parse(url);
                        // Make a new request pointing to the .mp3 url
                        DownloadManager.Request request1 = new DownloadManager.Request(source);
                        // appears the same in Notification bar while downloading
                        request1.setDescription("Description for the DownloadManager Bar");
                        request1.setTitle(url.substring(url.lastIndexOf("/") + 1));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            request1.allowScanningByMediaScanner();
                            request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
                        // save the file in the "Downloads" folder of SDCARD
                        request1.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/") + 1));
                        // get download service and enqueue file
                        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request1);
                        Toast.makeText(getActivity(), "Downloading Started...", Toast.LENGTH_LONG).show();
                    } else {
                        requestStoragePermission();
                    }
                    return true;
                } else if (url.endsWith(".pdf")) {
                    isAlreadyHandelPdf = true;
                    view.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + url);
                    swipeRefreshLayout.setEnabled(false);
                    swipeRefreshLayout.setRefreshing(false);

                } else if (url.contains("youtube.com")) {
                    ((MainActivity) getActivity()).hideAds();
                } else {
                    ((MainActivity) getActivity()).showAds();
                }
                return false;
            }

            // Otherwise allow the OS to handle it
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;

        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getActivity(), description, Toast.LENGTH_SHORT).show();
        }
        //For android below API 23

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Toast.makeText(getActivity(), "Something Went Wrong!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar1.setVisibility(View.VISIBLE);
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar1.setVisibility(View.GONE);
            // cancel the Visual indication of a refresh
            swipeRefreshLayout.setRefreshing(false);
            try {
                if (!url.contains("youtube.com")) {
                    ((MainActivity) getActivity()).showAds();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

//    @Override
//    public void onPause() {
//        simpleWebView.onPause();
//        simpleWebView.pauseTimers();
//        super.onPause();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        simpleWebView.resumeTimers();
//        simpleWebView.onResume();
//    }
//
//
//    @Override
//    public void onDestroy() {
//        simpleWebView.destroy();
//        simpleWebView = null;
//        super.onDestroy();
//    }

    public boolean onBackPressed() {
        if (simpleWebView.canGoBack()) {
            simpleWebView.goBack();
            swipeRefreshLayout.setEnabled(true);
            return false;
        }
        return true;
    }
}
