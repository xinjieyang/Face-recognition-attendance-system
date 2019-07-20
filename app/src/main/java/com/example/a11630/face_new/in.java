package com.example.a11630.face_new;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class in extends AppCompatActivity implements View.OnClickListener {

    String names, IDs;
    Button btn_Login;
    EditText et_name, et_ID;

    private String ImagePath = null;
    private Uri imageUri;
    private int Photo_ALBUM = 1, CAMERA = 2;
    private JSONObject res = null;
    int FLAG = 0;
    private Bitmap bp=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        btn_Login = (Button) findViewById(R.id.btn_login);
        btn_Login.setOnClickListener(this);
        et_name = (EditText) findViewById(R.id.name);
        et_ID = (EditText) findViewById(R.id.ID);
        FLAG = 0;
    }

    boolean check(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
                continue;
            else
                return false;
        }
        return true;
    }
    @Override
    public void onClick(View v) {
        names = et_name.getText().toString().trim();
        IDs = et_ID.getText().toString().trim();

        if (names.equals("") || IDs.equals("")) {
            Toast.makeText(this, "昵称和姓名不能为空", Toast.LENGTH_SHORT).show();
        } else if (check(IDs) == false) {
            Toast.makeText(this, "昵称非法", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(in.this)
                    .setTitle("系统提示")
                    // 设置对话框标题

                    .setMessage("请选择上传方式")
                    // 设置显示的内容

                    //右边按钮
                    .setPositiveButton("返回",
                            new DialogInterface.OnClickListener() {// 添加确定按钮

                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {// 确定按钮的响应事件
                                }

                            })
                    //中间按钮
                    .setNeutralButton("从相册上传", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        //    readRequest();
                            Intent in = new Intent(Intent.ACTION_PICK);      //选择数据
                            in.setType("image/*");                //选择的数据为图片
                            startActivityForResult(in, Photo_ALBUM);
                        }
                    })
                    //左边按钮
                    .setNegativeButton("拍照",
                            new DialogInterface.OnClickListener() {// 添加拍照按钮

                                @TargetApi(Build.VERSION_CODES.M)
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {// 响应事件
                                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                    StrictMode.setVmPolicy(builder.build());
                                    builder.detectFileUriExposure();            //7.0拍照必加
                                    File outputImage = new File(Environment.getExternalStorageDirectory() + File.separator + "face.jpg");     //临时照片存储地
                                    try {
                                        if (outputImage.exists()) {
                                            outputImage.delete();
                                        }
                                        outputImage.createNewFile();    ///创建临时地址
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    imageUri = Uri.fromFile(outputImage);              //获取Uri
                                    ImagePath = outputImage.getAbsolutePath();
                                    Log.i("拍照图片路径", ImagePath);         //，是传递你要保存的图片的路径，打开相机后，点击拍照按钮，系统就会根据你提供的地址进行保存图片
                                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    //跳转相机
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);                          //相片输出路径
                                    startActivityForResult(intent, CAMERA);                        //返回照片路径
                                }
                            }).show();// 在按键响应事件中显示此对话框
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相册选择图片
        if (requestCode == Photo_ALBUM) {
            if (data != null) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                cursor.moveToNext();
                ImagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));   //获得图片的绝对路径
                cursor.close();
                Log.i("图片路径", ImagePath);
                bp=getimage(ImagePath);
            }
        } else if (requestCode == CAMERA) {
            bp=getimage(ImagePath);
            runthreaad();
        }
    }

    private Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    private Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>300) { //循环判断如果压缩后图片是否大于300kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public byte[] getBytesByBitmap(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }




    void runthreaad() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
                try {
                    byte[] bytes1=getBytesByBitmap(bp);
                    String image1 = Base64Util.encode(bytes1);

                    Map<String, Object> map = new HashMap<>();
                    map.put("image", image1);
                    map.put("group_id", "face");
                    map.put("user_id", IDs);
                    map.put("user_info", "abc");
                    map.put("liveness_control", "NORMAL");
                    map.put("image_type", "BASE64");
                    map.put("quality_control", "LOW");
                    String param = GsonUtils.toJson(map);

                    // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
                    String accessToken = "24.6c369bc329b46defecfd94bc6b7544e4.2592000.1562429032.282335-16447381";
                    String result = HttpUtil.post(url, accessToken, "application/json", param);
                    System.out.println(result);

                    Gson gson = new Gson();
                    add_result_bean Result_bean = gson.fromJson(result, add_result_bean.class);

                    System.out.println("哈哈哈哈哈哈哈哈" + Result_bean.getError_code());
                    int Error_code = Result_bean.getError_code();
                    if (Error_code == 0) {
                        System.out.println("hahahahahah" + names + IDs);
                        SQLiteDatabase db;
                        MyHelper ggg = new MyHelper(in.this);
                        db = ggg.getWritableDatabase();
                        ggg.Insert(db, "name_id", names, IDs);
                        //         FLAG=1;
                        /**楼下这段话之后的语句跑不了**/
                        Looper.prepare();
                        Toast.makeText(in.this, "上传成功", Toast.LENGTH_LONG).show();
                        Looper.loop();

                    } else {
                        String error_message = "上传失败：" + Result_bean.getError_msg();
                        System.out.println("xixixixixixi" + error_message);
                        //       FLAG=1;
                        Looper.prepare();
                        Toast.makeText(in.this, error_message, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                    //       FLAG=1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

