package sensetime.senseme.humanaction.detect;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sample.multitrack106.R;
import com.sensetime.stmobile.STCommonNative;
import com.sensetime.stmobile.STMobileHumanActionNative;
import com.sensetime.stmobile.STRotateType;
import com.sensetime.stmobile.model.STHumanAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import sensetime.senseme.humanaction.detect.mediacodec.DetectAndRender;
import sensetime.senseme.humanaction.detect.utils.FileUtils;
import sensetime.senseme.humanaction.detect.utils.LogUtils;
import sensetime.senseme.humanaction.detect.utils.STLicenseUtils;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";
    private Context mContext;
    public ImageView mImageView;
    private ImageView imageView;
    private TextView mSkeletoninfo;
    private Bitmap bitmap;
    private static final int CROP_PHOTO = 2;
    private static final int REQUEST_CODE_PICK_IMAGE=3;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 6;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE2 = 7;
    public static final String LICENSE_NAME = "labcv_test_20200301_20200415_com.bytedance.labcv.demo_labcv_test_v3.6.1.licbag";
    private File output;
    private Uri imageUri;
    private static final int degree = 90;
    private int degree1;
    private int Flag =1;


//    Button mCameraStartBtn, mImageStartBtn, mVideoStartBtn;
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 2;
    private static final int PERMISSION_REQUEST_INTERNET = 3;

    private long mHumanActionDetectConfig = STMobileHumanActionNative.ST_MOBILE_BODY_KEYPOINTS;
    private STMobileHumanActionNative mSTMobileHumanActionNative = new STMobileHumanActionNative();
    private DetectAndRender mDetector;
    private boolean mLicenseChecked = false;
    private int mIndex = 0;
    private int detectedFrame = 0;
    private int Start = 0;
    private String[] ResJson = new String[133];
    private int name = 0;
    private  Gson gson = new Gson();
    private String[] Imgaelist;
    private int[] Pointsnum = {12,10,8,9,11,13,6,4,2,3,5,7,1,1,0};  // 对应标准json中keypoints的顺序

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDetector = new DetectAndRender();
        if(Constants.ACTIVITY_MODE_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            setContentView(R.layout.activity_homepage_landscape);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            setContentView(R.layout.activity_homepage);
        }
        FileUtils.copyModelFiles(this);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission has not been granted and must be requested.
                if (shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
                    // Provide an additional rationale to the user if the permission was not granted
                    // and the user would benefit from additional context for the use of the permission.
                }
                requestPermissions(new String[]{Manifest.permission.INTERNET},
                        PERMISSION_REQUEST_INTERNET);
            }
        }

//        mCameraStartBtn = (Button) findViewById(R.id.btn_start_camera);
//        mImageStartBtn = (Button) findViewById(R.id.btn_start_image);
//        mVideoStartBtn = (Button) findViewById(R.id.btn_start_video);

        STLicenseUtils.mIsOnlineLinsense = false;
        mLicenseChecked = STLicenseUtils.checkLicense(this);
        if (!mLicenseChecked) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "License授权失败！", Toast.LENGTH_SHORT).show();
                }
            });
        }
        initView();
        try {
            Imgaelist = getAssets().list("v1");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mContext = this;
        new ImageThread().start();

//        mCameraStartBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (!mLicenseChecked) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this, "License授权失败！", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    return;
//                }
//
//                if (Build.VERSION.SDK_INT >= 23) {
//                    mIndex = 0;
//                    checkCameraPermission();
//                }else {
//                    startActivity(new Intent(MainActivity.this, HumanActionCameraActivity.class));
//                }
//            }
//        });

//        mImageStartBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (!mLicenseChecked) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "License授权失败！", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    return;
//                }
//
//                if (Build.VERSION.SDK_INT >= 23) {
//                    mIndex = 1;
//                    checkCameraPermission();
//                }else {
//                    startActivity(new Intent(MainActivity.this, HumanActionImageActivity.class));
//                }
//            }
//        });
//
//        mVideoStartBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (!mLicenseChecked) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(MainActivity.this, "License授权失败！", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    return;
//                }
//
//                if (Build.VERSION.SDK_INT >= 23) {
//                    mIndex = 2;
//                    checkWritePermission();
//                }else {
//                    startActivity(new Intent(MainActivity.this, VideoProcessActivity.class));
//                }
//            }
//        });

    }

    void initView(){
        imageView= findViewById(R.id.image);
//        pointView = findViewById(R.id.view2);
    }

    public void takePhone(View view){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);

        }else {
            takePhoto();
        }

    }

    public void choosePhone(View view){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE2);

        }else {
            choosePhoto();
        }
    }

    /**
     * 拍照
     */
    void takePhoto(){
        /**
         * 最后一个参数是文件夹的名称，可以随便起
         */
        File file=new File(Environment.getExternalStorageDirectory(),"Pictures");
        if(!file.exists()){
            file.mkdir();
        }
        /**
         * 这里将时间作为不同照片的名称
         */
        output=new File(file,System.currentTimeMillis()+".jpg");

        /**
         * 如果该文件夹已经存在，则删除它，否则创建一个
         */
        try {
            if (output.exists()) {
                output.delete();
            }
            output.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * 隐式打开拍照的Activity，并且传入CROP_PHOTO常量作为拍照结束后回调的标志
         */
        imageUri = Uri.fromFile(output);
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        ContentValues contentValues=new ContentValues(1);
        contentValues.put(MediaStore.Images.Media.DATA,output.getAbsolutePath());
        Uri uri= getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        grantUriPermission("com.example.myapplication",uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        startActivityForResult(intent, CROP_PHOTO);
    }

    /**
     * 从相册选取图片
     */
    void choosePhoto(){
        /**
         * 打开选择图片的界面
         */
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        switch (req) {
            /**
             * 拍照的请求标志
             */
            case CROP_PHOTO:
                if (res == RESULT_OK) {
                    try {
                        Bitmap bit = BitmapFactory.decodeFile(output
                                .getAbsolutePath());

                        Matrix matrix = new Matrix();
                        matrix.postRotate(degree);
                        Bitmap bit1 = Bitmap.createBitmap(bit, 0, 0, bit.getWidth(), bit.getHeight(), matrix, true);
                        Bitmap bit2 = sizeBitmap(bit1,imageView.getWidth(),imageView.getHeight());
                        imageView.setImageBitmap(bit2);
//                        BytedEffectConstants.Rotation rotation = BytedEffectConstants.Rotation.CLOCKWISE_ROTATE_0;
                        getInfo(bit2);

                    } catch (Exception e) {
                        Toast.makeText(this, "程序崩溃", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.i("tag", "失败");
                }

                break;
            /**
             * 从相册中选取图片的请求标志
             */

            case REQUEST_CODE_PICK_IMAGE:
                if (res == RESULT_OK ) {
                    try {

                        Uri uri = data.getData();
                        String s = getPath(uri);
                        degree1 = getBitmapDegree(s);
                        Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));

                        getInfo(bit);  //Init 模型
                        Start = 1;  // 开始保存数组

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("tag", e.getMessage());
                        Toast.makeText(this, "程序崩溃", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.i("Photo", "失败");
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE)
        {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                takePhoto();
            } else
            {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE2)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                choosePhoto();
            } else
            {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private int getBitmapDegree(String path) {
        int degree = 0;//被旋转的角度
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap sizeBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        return newBM;
    }
    public void getInfo(Bitmap bm){

        // TODO 模型初始化，相关参数设置
        int width = (bm.getWidth() );
        int height =  (bm.getHeight());
        ByteBuffer resizeInputBuffer =  ByteBuffer.allocateDirect((width* height * 4));
        bm.copyPixelsToBuffer(resizeInputBuffer);
        Log.i("info",resizeInputBuffer.toString());
        resizeInputBuffer.position(0);
        // 参数设置
        int mHumanActionCreateConfig = STCommonNative.ST_MOBILE_TRACKING_SINGLE_THREAD
                | STMobileHumanActionNative.ST_MOBILE_ENABLE_BODY_KEYPOINTS | STMobileHumanActionNative.ST_MOBILE_DETECT_MODE_VIDEO;

        // 初始化句柄
        int res = mSTMobileHumanActionNative.createInstanceFromAssetFile(FileUtils.LARGE_BODY_MODEL_NAME,
                mHumanActionCreateConfig, mContext.getAssets());
        LogUtils.i(TAG, "create human action handle result: %d", res);
        if (res == 0){
            Log.d("model","load model success");
        }
    }

    private class ImageThread extends Thread {
        private int mChoice =0;
        @Override
        public void run() {
            while (Flag ==1) {
                try {
                    if(mChoice == Imgaelist.length){
                        mChoice = 0;
                    }
                    InputStream is = getAssets().open("v0/"+Imgaelist[mChoice]);
                    bitmap = BitmapFactory.decodeStream(is);
                    imageView.setImageBitmap(bitmap);
                    Trackskeleton(bitmap,mChoice);
                    mChoice++;
                    try{
                        ImageThread.sleep(100);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    private void Trackskeleton(Bitmap bitmap, int ID) throws IOException {

        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            ArrayList<ArrayInfo.points> points = new ArrayList<>();
            ArrayList<ArrayInfo.annorect> ress = new ArrayList<>();
//            ByteBuffer buffer = ByteBuffer.allocate(width * height * 4);
            ByteBuffer mBuf = ByteBuffer.allocate(width * height * 4);
            bitmap.copyPixelsToBuffer(mBuf);
            mBuf.position(0);
            STHumanAction humanAction =
                    mSTMobileHumanActionNative.humanActionDetect(mBuf.array(),
                            STCommonNative.ST_PIX_FMT_RGBA8888,  mHumanActionDetectConfig, STRotateType.ST_CLOCKWISE_ROTATE_0, width, height);
            if (humanAction != null && humanAction.bodyCount>0) {
                int i,j;
                int BodyNum = humanAction.bodyCount;
                for(i = 0; i<BodyNum;i++){
                    int SkeletonPoints = humanAction.bodys[i].keyPointsCount;
                    ArrayInfo.annorect rect =  new ArrayInfo.annorect();
//
                    int num = Math.min(15,SkeletonPoints); //最大取15个点
                    for (j = 0;j<num;j++){
                        int keypoint = Pointsnum[j];  //获取对应位置点
                        boolean is_detect = true; //humanAction.bodys[i].keyPointsScore; TODO 置信度区分
                        if (is_detect){
                            points.add(new ArrayInfo.points(
                                    new int[]{j},  //ID
                                    new float[]{humanAction.bodys[i].getKeyPoints()[keypoint].getX()},
                                    new float[]{humanAction.bodys[i].getKeyPoints()[keypoint].getY()},
                                    new int[]{1})// is_visible
                            ); //得到一个body的所有keypoints信息，存在points中
                        }
                        ArrayInfo.annopoints annopoints = new ArrayInfo.annopoints(points);  //annopoints[points[],points[]]
                        ArrayList<ArrayInfo.annopoints> res = new ArrayList<>();
                        res.add(annopoints); // [annopoints[points[],points[]]]
                        /**
                         * 商汤没有body rect 全部写成1
                         */
                        rect = new ArrayInfo.annorect(
                                new int[]{1},
                                new int[]{1},
                                new int[]{1},
                                new int[]{1},
                                new int[]{-1},
                                "-1",
                                new int[]{i},
                                res
                        );
                    }
                    ress.add(rect);
                }
                Log.d("iiiiiii","图片ID是"+ ID);
//                Log.d("iiiii",befSkeletonInfo.toString());
                detectedFrame++;
            }
            if(Start ==1){
                String name = "images/bonn_5sec/008760_mpii/"+(ID+1)+".jpg";
                ArrayList<ArrayInfo.getImgName> image = new ArrayList<>();
                image.add(new ArrayInfo.getImgName(name));
                ArrayInfo ay = new ArrayInfo(image,ress,new int[]{ID+1},new int[]{1});
                String strr = gson.toJson(ay);
                ResJson[ID] = strr;
                Log.d("iiiiii",strr);
            }

            //TODO: 存储json数组
            if (ID == 132){
                SaveJson("sensetime"+name++,ResJson);
                Log.d("iiiii","lossframe rate is "+ (double)detectedFrame/133+ "detected frame is "+ detectedFrame);
                detectedFrame = 0;
//                save = true;
            }
        }
    }

    private void SaveJson(String name, String[] jsonString) throws IOException {
        String filepath =  Environment.getExternalStorageDirectory().toString()
                + "/"+name +".txt";
        //判断文件是否存在
        File file = new File(filepath);
        if (file.exists()) {
            Log.i("SaveJson", "文件存在");
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("SaveJson", "文件创建成功");
            Log.i("SaveJson",filepath);
        }

        if(jsonString.length !=0){
            try{
                FileWriter fw = new FileWriter(filepath);
                for (int i = 0; i < jsonString.length; i++) {
                    if(i == jsonString.length-1){
                        fw.write(jsonString[i]+"\r\n");
                    }
                    else{
                        fw.write(jsonString[i]+",\r\n");
                    }
                }
                fw.close();
                Log.i("SaveJson", "文件save成功");

            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e("SaveJson", "****Save Error****");
            }

        }

    }




}
