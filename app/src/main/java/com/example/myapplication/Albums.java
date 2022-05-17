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


public class Albums extends Activity {
    private  ImageView albumsPicture;
    public static final int CHOOSE_PHOTO = 2;
    private Button pestDection=null;
    private Button pictureSave=null;
    private ProgressDialog progressDialog; //进度条
    private Intent intent2;
    private String  pic_path;
    private Uri uri;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private PyObject obj,obj0,obj1,obj2,obj3,obj4;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums);

        pestDection=super.findViewById(R.id.pestDetection);
        pictureSave=super.findViewById(R.id.pictureSave);
        albumsPicture = super.findViewById(R.id.picture);
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avi);
        //    弹出要给ProgressDialog
        progressDialog = new ProgressDialog(Albums.this); //进度显示
        progressDialog.setTitle("提示信息");
        progressDialog.setMessage("正在加强中，请稍后......");
        //    设置setCancelable(false); 表示我们不能取消这个弹出框，等下载完成之后再让弹出框消失
        progressDialog.setCancelable(false);
        //    设置ProgressDialog样式为圆圈的形式
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        //初始化python
        initPython();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CHOOSE_PHOTO);
        } else {
            openAlbum();
        }

        intent2 = new Intent(getApplicationContext(),MainActivity.class);
       // avLoadingIndicatorView.setVisibility(View.GONE);
        //receivePicturefromMainActivaty();
        pestDection.setOnClickListener(new pestDectionFuntion());
        pictureSave.setOnClickListener(new pictureSaveFunction());

    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);//打开相册

    }

   /********************图像增强*************************/

    // 初始化Python环境
    void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
    private class pestDectionFuntion implements View.OnClickListener {
        public void onClick(View view){
            Python py = Python.getInstance();
            //avLoadingIndicatorView.setVisibility(View.VISIBLE);
         //   avLoadingIndicatorView.show();
            // 在UI Thread当中实例化AsyncTask对象，并调用execute方法
            new MyAsyncTask().execute();

         //   displayImage(enhanceImg);
         //   Toast.makeText(getApplicationContext(),"图像增强成功",Toast.LENGTH_SHORT).show();
        }
    }

/**
 * 定义一个类，让其继承AsyncTask这个类
 * Params: String类型，表示传递给异步任务的参数类型是String，通常指定的是URL路径
 * Progress: Integer类型，进度条的单位通常都是Integer类型
 * Result：String类型，表示增强好的图片返回预存地址
 *
 */

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





    private class pictureSaveFunction implements View.OnClickListener {
        public void onClick(View view){
            //Toast.makeText(getApplicationContext(),"图片保存成功！",Toast.LENGTH_SHORT).show();
            //Intent intent2 = new Intent(getApplicationContext(),MainActivity.class);//创建窗口切换的Intent,MainActivity.class指切换到主界面
            //Bitmap savepicture=loadBitmapFromView(albumsPicture);
            //String name=String.valueOf(System.currentTimeMillis());
            BitmapDrawable bmpDrawable = (BitmapDrawable) albumsPicture.getDrawable();
            Bitmap bitmap = bmpDrawable.getBitmap();
            saveToSystemGallery(bitmap);//将图片保存到本地
            Toast.makeText(getApplicationContext(),"图片保存成功！",Toast.LENGTH_SHORT).show();
            startActivity(intent2);//窗口切换
        }
    }
    public void saveToSystemGallery(Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "MyAlbums");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
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
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent); // 发送广播，通知图库更新
    }


    // 使用startActivityForResult()方法开启Intent的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //返回成功，请求码（对应启动时的requestCode）
        if(resultCode == RESULT_OK && CHOOSE_PHOTO == 2)
        {
            uri = data.getData();
            ContentResolver cr = this.getContentResolver();
            try {
                //根据Uri获取流文件
                InputStream is = cr.openInputStream(uri);
                pic_path= RealPathFromUriUtils.getRealPathFromUri(this, uri);
                System.out.println(pic_path);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize =3;
                Bitmap bitmap = BitmapFactory.decodeStream(is,null,options);
                albumsPicture.setImageBitmap(bitmap);

            }
            catch(Exception e)
            {
                Log.i("lyf", e.toString());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @TargetApi(19)
    private void handleImageOnKitkat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是File类型的uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        //根据图片路径显示图片
        pic_path = imagePath;
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri=data.getData();
        String imagePath=getImagePath(uri,null);
        displayImage(imagePath);
    }
    private String getImagePath(Uri uri,String selection){
        String path=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    private void displayImage(String imagePath){
        if(imagePath!=null){
            Bitmap bitmap=BitmapFactory.decodeFile(imagePath);
            albumsPicture.setImageBitmap(bitmap);//将图片放置在控件上
        }else {
            Toast.makeText(this,"得到图片失败",Toast.LENGTH_SHORT).show();
        }
    }
}
