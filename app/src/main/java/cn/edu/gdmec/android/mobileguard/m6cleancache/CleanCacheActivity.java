package cn.edu.gdmec.android.mobileguard.m6cleancache;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by lenovo on 2017/11/19.
 */

public class CleanCacheActivity extends AppCompatActivity implements View.OnClickListener {
    protected static final int CLEANNING=100;
    protected static final int CLEAN_FINISH=10;
    private AnimationDrawable animation;
    private long cacheMemory;
    private TextView mMemoryTV;
    private TextView mMemoryUnitTV;
    private PackageManager pm;
    private FrameLayout mCleanCacheFL;
    private FrameLayout mFinishCleanFL;
    private TextView mSizeTV;

    private Handler mHandler=new Handler() {
        @Override
        public void publish(LogRecord logRecord) {

        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CLEANNING:
                    long memory=(Long) msg.obj;
                    formatMemory(memory);
                    if (memory == cacheMemory) {
                        animation.stop();
                        mCleanCacheFL.setVisibility(View.GONE);
                        mFinishCleanFL.setVisibility(View.VISIBLE);
                        mSizeTV.setText("成功清理:" +
                                Formatter.formatFileSize(CleanCacheActivity.this, cacheMemory));
                    }
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_clean_cache);
        initView();
        pm.getPackageManager();
        Intent intent=getIntent();
        cacheMemory=intent.getLongExtra("cacheMemory", 0);
        initData();
    }

    private void initData() {
        cleanAll();
        new Thread() {
            public void run() {
                long memory=0;
                while (memory < cacheMemory) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Random rand=new Random();
                    int i=rand.nextInt();
                    i=rand.nextInt();
                    i=rand.nextInt(1024);
                    memory +=1024 * i;
                    if (memory > cacheMemory) {
                        memory=cacheMemory;
                    }
                    Message message=Message.obtain();
                    message.what=CLEANNING;
                    message.obj=memory;
                    mHandler.sendMessageDelayed(message, 200);
                }
            };
        }.start();
    }

    private void formatMemory(long memory) {
        String cacheMemoryStr=Formatter.formatFileSize(this, memory);
        String memoryStr;
        String memoryUnit;
        if (memory > 900) {
            memoryStr=cacheMemoryStr.substring(0, cacheMemoryStr.length() - 2);
            memoryUnit=cacheMemoryStr.substring(cacheMemoryStr.length() - 2, cacheMemoryStr.length());

        } else {
            memoryStr=cacheMemoryStr.substring(0, cacheMemoryStr.length() - 1);
            memoryUnit=cacheMemoryStr.substring(cacheMemoryStr.length() - 1, cacheMemoryStr.length());
        }
        mMemoryTV.setText(memoryStr);
        mMemoryUnitTV.setText(memoryUnit);
    }

    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(getResources().getColor(R.color.rose_red));
        ((TextView) findViewById(R.id.tv_title)).setText("缓存清理");
        ImageView mLeftImgv=(ImageView) findViewById(R.id.imgv_leftbtn);
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        animation=(AnimationDrawable) findViewById(R.id.imgv_trashbin_cacheclean).getBackground();
        animation.setOneShot(false);
        animation.start();
        mMemoryTV=(TextView) findViewById(R.id.tv_cleancache_memory);
        mMemoryUnitTV=(TextView) findViewById(R.id.tv_cleancache_memoryunit);
        mCleanCacheFL=(FrameLayout) findViewById(R.id.fl_cleancache);
        mFinishCleanFL=(FrameLayout) findViewById(R.id.fl_finishclean);
        mSizeTV=(TextView) findViewById(R.id.tv_cleanmemorysize);
        findViewById(R.id.brn_finish_cleancache).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.btn_finish_cleancache:
                finish();
                break;
        }
    }

    class CleanCacheObserver extends
            android.content.pm.IPackageStatsObserver.Stub {
        public void onRemoveCompleted(final String packageName,
                                      final boolean succeeded) {
        }

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {

        }
    }

    private void cleanAll (){
        Method[] methods=PackageManager.class.getMethods();
        for (Method method : methods) {
            if ("freeStorageAndNotify".equals(method.getName())) {
                try {
                    method.invoke(pm, "", Integer.MAX_VALUE, new CleanCacheObserver());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        Toast.makeText(this, "清理完毕", Toast.LENGTH_LONG).show();
    }
}


