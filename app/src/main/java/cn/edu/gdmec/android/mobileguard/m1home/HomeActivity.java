package cn.edu.gdmec.android.mobileguard.m1home;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m1home.adapter.HomeAdapter;
import cn.edu.gdmec.android.mobileguard.m2theftguard.LostFindActivity;
import cn.edu.gdmec.android.mobileguard.m2theftguard.dialog.InterPasswordDialog;
import cn.edu.gdmec.android.mobileguard.m2theftguard.dialog.SetUpPasswordDialog;
import cn.edu.gdmec.android.mobileguard.m2theftguard.receiver.MyDeviceAdminReceiver;
import cn.edu.gdmec.android.mobileguard.m2theftguard.utils.MD5Utils;
import cn.edu.gdmec.android.mobileguard.m3communicationguard.SecurityPhoneActivity;

public class HomeActivity extends AppCompatActivity {
    private GridView gv_home;
    private long mExitTime;
    private SharedPreferences mSharedPreferences;
    private DevicePolicyManager policyManager;
    private ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();
        mSharedPreferences = getSharedPreferences ( "config", MODE_PRIVATE );
        gv_home = (GridView) findViewById( R.id.gv_home);
        gv_home.setAdapter(new HomeAdapter (HomeActivity.this));
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        if (isSetUpPassword()){
                            showInterPswdDialog();
                        }else {
                            showSetUpPswdDialog();
                        }
                        break;
                    //点击通讯卫士
                    case 1:
                        startActivity ( SecurityPhoneActivity.class );
                        break;

                }
            }
        });
        policyManager=(DevicePolicyManager) getSystemService ( DEVICE_POLICY_SERVICE );
        componentName=new ComponentName ( this, MyDeviceAdminReceiver.class );
        boolean active=policyManager.isAdminActive ( componentName );
        if (!active){
            Intent intent = new Intent ( DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN );
            intent.putExtra ( DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName );
            intent.putExtra ( DevicePolicyManager.EXTRA_ADD_EXPLANATION, "获得超级管理员权限，用于远程锁屏和清除数据" );
            startActivity ( intent );
        }
    }
    public void startActivity(Class<?> cls){
        Intent intent = new Intent ( HomeActivity.this,cls );
        startActivity ( intent );
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode==KeyEvent.KEYCODE_BACK){
            if ((System.currentTimeMillis()-mExitTime)<2000){
                System.exit(0);
            }else {
                Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_LONG).show();
                mExitTime = System.currentTimeMillis();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showSetUpPswdDialog(){
        final SetUpPasswordDialog setUpPasswrodDialog = new SetUpPasswordDialog ( HomeActivity.this );
        setUpPasswrodDialog.setCallBack ( new SetUpPasswordDialog.MyCallBack (){
            @Override
            public void ok(){
                String firstPwsd = setUpPasswrodDialog.mFirstPWDET.getText ().toString ().trim ();
                String affirmPwsd = setUpPasswrodDialog.mAffirmET.getText ().toString ().trim ();
                if (!TextUtils.isEmpty ( firstPwsd )&&!TextUtils.isEmpty ( affirmPwsd )){
                    if (firstPwsd.equals ( affirmPwsd )){
                        savePswd(affirmPwsd);
                        setUpPasswrodDialog.dismiss ();
                        showInterPswdDialog();
                    }else {
                        Toast.makeText ( HomeActivity.this, "两次密码不一致！", Toast.LENGTH_LONG ).show();
                    }
                }else{
                    Toast.makeText ( HomeActivity.this, "密码不能为空！", Toast.LENGTH_LONG ).show ();
                }
            }
            @Override
            public void cancel(){
                setUpPasswrodDialog.dismiss ();
            }
        } );
        setUpPasswrodDialog.setCancelable ( true );
        setUpPasswrodDialog.show ();
    }
    private void showInterPswdDialog(){
        final String password = getPassword();
        final InterPasswordDialog mInPswdDialog = new InterPasswordDialog ( HomeActivity.this );
        mInPswdDialog.setCallBack (new InterPasswordDialog.MyCallBack (){
            @Override
            public void confirm(){
                if (TextUtils.isEmpty ( mInPswdDialog.getPassword () )){
                    Toast.makeText ( HomeActivity.this, "密码不能为空！", Toast.LENGTH_LONG ).show ();
                }else if (password.equals ( MD5Utils.encode ( mInPswdDialog.getPassword () ) )){
                    mInPswdDialog.dismiss ();
                    startActivity ( LostFindActivity.class );
                    Toast.makeText ( HomeActivity.this, "可以进入手机防盗模块",Toast.LENGTH_LONG ).show ();
                }else {
                    mInPswdDialog.dismiss ();
                    Toast.makeText ( HomeActivity.this, "密码有误，请重新输入", Toast.LENGTH_LONG ).show ();
                }
            }
            @Override
            public void cancle(){
                mInPswdDialog.dismiss ();
            }
        });
        mInPswdDialog.setCancelable ( true );
        mInPswdDialog.show ();
    }
    private void savePswd(String affirmPwsd){
        SharedPreferences.Editor edit = mSharedPreferences.edit ();
        edit.putString ("PhoneAntiTheftPWD", MD5Utils.encode ( affirmPwsd ));
        edit.commit();
    }
    private String getPassword(){
        String password = mSharedPreferences.getString ( "PhoneAntiTheftPWD", null );
        if (TextUtils.isEmpty ( password )){
            return "";
        }
        return password;
    }
    private boolean isSetUpPassword(){
        String password = mSharedPreferences.getString ( "PhoneAntiTheftPWD", null );
        if (TextUtils.isEmpty ( password )){
            return false;
        }
        return true;
    }
}
