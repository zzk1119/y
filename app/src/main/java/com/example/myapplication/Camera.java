package com.example.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.ContentResolver;
import android.util.Log;



import java.io.InputStream;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.Python;
import com.wang.avi.AVLoadingIndicatorView;


public class Camera extends Activity {
    private ImageView cameraPicture;
    public static final int TAKE_PHOTO = 1;
    private Button pestDection=null;
    private Button pictureSave=null;
    private ProgressDialog progressDialog; //进度条
    private Intent intent3;
    private Uri imageUri;
    private String  pic_path;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums);
        pestDection=super.findViewById(R.id.pestDetection);
        pictureSave=super.findViewById(R.id.pictureSave);
        cameraPicture = super.findViewById(R.id.picture);
        //    弹出要给ProgressDialog
        progressDialog = new ProgressDialog(Camera.this); //进度显示
        progressDialog.setTitle("提示信息");
        progressDialog.setMessage("正在加强中，请稍后......");
        //    设置setCancelable(false); 表示我们不能取消这个弹出框，等下载完成之后再让弹出框消失
        progressDialog.setCancelable(false);
        //    设置ProgressDialog样式为圆圈的形式
        //ProgressDialog.STYLE_SPINNER 环形精度条
        //ProgressDialog.STYLE_HORIZONTAL 水平样式的进度条
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        //初始化python
        initPython();
      //  callPythonCode();
        // 创建一个File对象，用于保存摄像头拍下的图片，这里把图片命名为output_image.jpg
        // 并将它存放在手机SD卡的应用关联缓存目录下
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        // 对照片的更换设置
        try {
            // 如果上一次的照片存在，就删除
            if (outputImage.exists()) {
                outputImage.delete();
            }
            // 创建一个新的文件
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 如果Android版本大于等于7.0
        if (Build.VERSION.SDK_INT >= 24) {
            // 将File对象转换成一个封装过的Uri对象
            imageUri = FileProvider.getUriForFile(this, "com.example.lenovo.cameraalbumtest.fileprovider", outputImage);
            Log.d("MainActivity", outputImage.toString() + "手机系统版本高于Android7.0");
        } else {
            // 将File对象转换为Uri对象，这个Uri标识着output_image.jpg这张图片的本地真实路径
            Log.d("MainActivity", outputImage.toString() + "手机系统版本低于Android7.0");
            imageUri = Uri.fromFile(outputImage);
        }
        // 动态申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.CAMERA}, TAKE_PHOTO);
        } else {
            // 启动相机程序
            startCamera();
        }

        intent3 = new Intent(getApplicationContext(),MainActivity.class);
        pestDection.setOnClickListener(new pestDectionFuntion());
        pictureSave.setOnClickListener(new pictureSaveFunction());

    }
    private void startCamera() {
        Intent intent4 = new Intent("android.media.action.IMAGE_CAPTURE");
        // 指定图片的输出地址为imageUri
        intent4.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent4, TAKE_PHOTO);



    }
    // 初始化Python环境
    void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
    private class pestDectionFuntion implements View.OnClickListener {
        public void onClick(View view){

            new MyAsyncTask().execute();


            //进行图像增强
           // PyObject obj1 = py.getModule("enhance").callAttr("laplacian",new Kwarg("pic_path", pic_path));
            //保存增强后的图片并返回图片地址
           // PyObject obj2 = py.getModule("enhance").callAttr("ImWrite",new Kwarg("img", obj1));
          //  String enhanceImg = obj2.toJava(String.class);
            //根据图片路径显示图片
        }
    }


    public class MyAsyncTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //    在onPreExecute()中我们让进度条显示出来
            progressDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {  //耗时任务
            Python py = Python.getInstance();

            //进行图像增强
            PyObject obj1 = py.getModule("enhance").callAttr("img_enhance",new Kwarg("pic_path", pic_path));
            //保存增强后的图片并返回图片地址
            PyObject obj2 = py.getModule("enhance").callAttr("ImWrite",new Kwarg("img", obj1));
            String enhanceImg = obj2.toJava(String.class);
            return enhanceImg;
        }
        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            //    将doInBackground方法返回的byte[]解码成要给Bitmap
            //    更新我们的ImageView控件
            System.out.println(result);
            displayImage(result);
            //    使ProgressDialog框消失
            progressDialog.dismiss();
        }
    }

    private void displayImage(String imagePath){
        if(imagePath!=null){
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            cameraPicture.setImageBitmap(bitmap);//将图片放置在控件上
        }else {
            Toast.makeText(this,"得到图片失败",Toast.LENGTH_SHORT).show();
        }
    }
    private class pictureSaveFunction implements View.OnClickListener {
        public void onClick(View view){
            BitmapDrawable bmpDrawable = (BitmapDrawable) cameraPicture.getDrawable();
            Bitmap bitmap = bmpDrawable.getBitmap();
            saveToSystemGallery(bitmap);//将图片保存到本地
            Toast.makeText(getApplicationContext(),"图片保存成功！",Toast.LENGTH_SHORT).show();
            startActivity(intent3);//窗口切换
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK ) {
                    try {
                        // 将图片解析成Bitmap对象
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        cameraPicture.setImageBitmap(bitmap);
                        saveToSystemGallery(bitmap);//将图片保存到本地
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
    public void saveToSystemGallery(Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Mycamera");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        System.out.println(file.getPath());
        pic_path = file.getPath() ;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
*/
        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);// 发送广播，通知图库更新
    }
}
