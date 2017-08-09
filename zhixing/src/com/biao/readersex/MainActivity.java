package com.biao.readersex;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.umeng.analytics.MobclickAgent;
import com.biao.readersex.activity.FeedBackActivity;
import com.biao.readersex.activity.SettingActivity;
import com.biao.readersex.activity.SplashActivity;
import com.biao.readersex.barcolor.SystemBarTintManager;
import com.biao.readersex.cache.ACache;
import com.biao.readersex.commont.DoubleClickExitHelper;
import com.biao.readersex.dialog.SweetAlertDialog;
import com.biao.readersex.fragment.AFragment;
import com.biao.readersex.fragment.AppTuiFragment;
import com.biao.readersex.fragment.EveryDayEnglishFragment;
import com.biao.readersex.fragment.HomeFragment;
import com.biao.readersex.fragment.OtherFragment;
import com.biao.readersex.fragment.OtherFragment;
import com.biao.readersex.lib.ActionBarDrawerToggle;
import com.biao.readersex.lib.DrawerArrowDrawable;
import com.biao.readersex.lib.RoundedImageView;
import com.biao.readersex.lib.StringUtil;
import com.biao.readersex.lib.toast.Crouton;
import com.biao.readersex.lib.toast.Style;
import com.biao.readersex.service.AppUpdateService;


public class MainActivity extends FragmentActivity implements EasyPermissions.PermissionCallbacks {
	private DoubleClickExitHelper mDoubleClickExitHelper;
	private TranslateAnimation myAnimation_Left;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	RelativeLayout rl;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerArrowDrawable drawerArrow;
	public static FragmentManager fm;
	private long exitTime = 0;
	Boolean openOrClose = false;
	int vc;// ��ȡ��ǰ�汾��
	ACache mCache;
	RoundedImageView iv_main_left_head;
	TextView user_name;
	RelativeLayout toprl;
	ImageView login_tv;
	LinearLayout animll_id;

	private boolean isLogin = false;
	// ��ʱ�������
	private Timer mTimer;
	private TimerTask mTimerTask;
	protected static final int UPDATE_TEXT = 0;
	private Handler mHandler;
	File sdcardDir;
	String path;
	File f;
	File[] fl;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
		}

		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.color.actionbar_color);
		mCache = ACache.get(this);
		mDoubleClickExitHelper = new DoubleClickExitHelper(this);
		toprl = (RelativeLayout) findViewById(R.id.toprl);
		animll_id = (LinearLayout) findViewById(R.id.animll_id);
		login_tv = (ImageView) findViewById(R.id.login_tv);
		user_name = (TextView) findViewById(R.id.user_name);
		iv_main_left_head = (RoundedImageView) findViewById(R.id.iv_main_left_head);
//		Animation operatingAnim = AnimationUtils
//				.loadAnimation(this, R.anim.tip);
//		LinearInterpolator lin = new LinearInterpolator();
//		operatingAnim.setInterpolator(lin);
//		if (operatingAnim != null) {
//			iv_main_left_head.startAnimation(operatingAnim);
//		}

		initPermission();
		createSDCardDir();

		vc = getVersionCode(this);
		chekedVersionCode();
		MobclickAgent.updateOnlineConfig(this);

		ActionBar ab = getActionBar();
		
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		init();
		fm = this.getSupportFragmentManager();
		rl = (RelativeLayout) findViewById(R.id.rl);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.navdrawer);

		drawerArrow = new DrawerArrowDrawable(this) {
			@Override
			public boolean isLayoutRtl() {
				return false;
			}
		};
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				drawerArrow, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				invalidateOptionsMenu();
				openOrClose = false;
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
				openOrClose = true;
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();
		String[] values = new String[] { "ÿ��һ��", "���䣺0��-1���", "���䣺1���-3��", "���䣺3��-6��", "���䣺6��-9��", "���䣺10��-13��", 
				"ר��һ�����Ӷ������ٵĺ���", "ר�������ο", "ר�������ʴ�", "ר���ģ��׶��ڼ�ͥ�е����˺�", 
				"ר���壺�����������ֺ�", "ר��������ֳ��", "ר���ߣ���ȫ��",  "ר��ˣ����̲�", "������Ƭ", "�Ƽ�Ӧ��"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.item_text, values);
		mDrawerList.setAdapter(adapter);
		mDrawerList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@SuppressLint({ "ResourceAsColor", "Recycle" })
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						switch (position) {
						case 0:
							initFragment(new EveryDayEnglishFragment());
							setTitle("ÿ��һ��");
							break;
						case 1:
							initFragment(new OtherFragment("age0_1.json"));
							setTitle("���䣺0��-1���");
							break;
						case 2:
							initFragment(new OtherFragment("age1_3.json"));
							setTitle("���䣺1���-3��");
							break;
						case 3:
							initFragment(new OtherFragment("age3_6.json"));
							setTitle("���䣺3��-6��");
							break;
						case 4:
							initFragment(new OtherFragment("age6_9.json"));
							setTitle("���䣺6��-9��");
							break;
						case 5:
							initFragment(new OtherFragment("age10_13.json"));
							setTitle("���䣺10��-13��");
							break;
						case 6:
							initFragment(new OtherFragment("module1.json"));
							setTitle("ר��һ");
							break;
						case 7:
							initFragment(new OtherFragment("module2.json"));
							setTitle("ר���");
							break;
						case 8:
							initFragment(new OtherFragment("module3.json"));
							setTitle("ר����");
							break;
						case 9:
							initFragment(new OtherFragment("module4.json"));
							setTitle("ר����");
							break;
						case 10:
							initFragment(new OtherFragment("module5.json"));
							setTitle("ר����");
							break;
						case 11:
							initFragment(new OtherFragment("module6.json"));
							setTitle("ר����");
							break;
						case 12:
							initFragment(new OtherFragment("module7.json"));
							setTitle("ר����");
							break;
						case 13:
							initFragment(new OtherFragment("module8.json"));
							setTitle("ר���");
							break;
						case 14:
							initFragment(new HomeFragment());
							setTitle("������Ƭ");
							break;
						case 15:
							initFragment(new AppTuiFragment());
							setTitle("�Ƽ�Ӧ��");
							break;

						}
						mDrawerLayout.closeDrawers();
						openOrClose = false;
					}
				});
		toprl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (isLogin == true) {
					Intent it = new Intent(getApplicationContext(),
							SettingActivity.class);
					startActivityForResult(it, 1);
//					overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
					overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
				} else {
					
				}

			}
		});
		//clearCache();
		
		UserInfo();
	}
	@TargetApi(19)
	private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}
	private void clearCache() {
		sdcardDir = Environment.getExternalStorageDirectory();
		path = sdcardDir.getPath() + "/readersex";
		f = new File(path);
		fl = f.listFiles();
		Log.e("fl.length==", fl.length + "");
		if (fl.length == 0) {

		} else {

			for (int i = 0; i < fl.length; i++) {
				if (fl[i].toString().endsWith(".mp3")
						|| fl[i].toString().endsWith(".MP3")) {
					fl[i].delete();
				}
			}
		}
	}
	  /**
     * ��ʾShortToast
     */
    public void showCustomToast(String pMsg, int view_position) {
	 Crouton.makeText(this, pMsg, Style.CONFIRM, view_position).show();
    }
	public void onClickFeedBack(View v) {
		Intent it = new Intent(this, FeedBackActivity.class);
		startActivity(it);
	}

	public void onClickSetting(View v) {
		Intent it = new Intent(this, SettingActivity.class);
		startActivity(it);
	}

	/**
	 * ��ȡ�汾��
	 * 
	 * @param context
	 * @return
	 */
	private int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			// ��ȡ����汾��
			versionCode = context.getPackageManager().getPackageInfo(
					"com.biao.readersex", 1).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this); // ͳ��ʱ��
		String avatar = mCache.getAsString("avatar");
		String name = mCache.getAsString("name");
		try {
			if (avatar.equals("")) {
				login_tv.setVisibility(View.VISIBLE);
				// login_tv.setText("��¼");
			} else {
				isLogin = true;
				login_tv.setVisibility(View.GONE);
				Ion.with(MainActivity.this).load(avatar).asBitmap()
						.setCallback(new FutureCallback<Bitmap>() {

							@Override
							public void onCompleted(Exception e, Bitmap bitmap) {
								// ivHead.setImageBitmap(bitmap);
								iv_main_left_head.setImageBitmap(bitmap);
							}
						});

			}

			if (!name.equals("")) {
				isLogin = true;
				user_name.setText(name);
				// login_tv.setText("");
				login_tv.setVisibility(View.GONE);
			} else {
				// login_tv.setText("��¼");
				login_tv.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (mDrawerLayout.isDrawerOpen(rl)) {
				mDrawerLayout.closeDrawer(rl);
				openOrClose = false;
			} else {
				mDrawerLayout.openDrawer(rl);
				openOrClose = true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private void init() {
		fm = getSupportFragmentManager();
		// ֻ����������Ҫ������Fragment�ʬF
		initFragment(new EveryDayEnglishFragment());
	}

	// �ГQFragment
	public void changeFragment(Fragment f) {
		changeFragment(f, false);
	}

	// ��ʼ��Fragment(FragmentActivity�к���)
	public void initFragment(Fragment f) {
		changeFragment(f, true);
	}

	private void changeFragment(Fragment f, boolean init) {
		FragmentTransaction ft = fm.beginTransaction().setCustomAnimations(
				R.anim.umeng_fb_slide_in_from_left,
				R.anim.umeng_fb_slide_out_from_left,
				R.anim.umeng_fb_slide_in_from_right,
				R.anim.umeng_fb_slide_out_from_right);
		;
		ft.replace(R.id.fragment_layout, f);
		if (!init)
			ft.addToBackStack(null);
		ft.commitAllowingStateLoss();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// OffersManager.getInstance(MainActivity.this).onAppExit();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {

			if (openOrClose == false) {
				showCustomToast(getString(R.string.back_exit_tips),
						R.id.fragment_layout);
				return mDoubleClickExitHelper.onKeyDown(keyCode, event);
			} else {
				mDrawerLayout.closeDrawers();
			}

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void createSDCardDir() {
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			// ����һ���ļ��ж��󣬸�ֵΪ�ⲿ�洢����Ŀ¼
			File sdcardDir = Environment.getExternalStorageDirectory();
			// �õ�һ��·����������sdcard���ļ���·��������
			String path = sdcardDir.getPath() + "/readersex";
			File path1 = new File(path);
			if (!path1.exists()) {
				// �������ڣ�����Ŀ¼��������Ӧ��������ʱ�򴴽�
				path1.mkdirs();
				System.out.println("paht ok,path:" + path);
			}
		} else {
			System.out.println("false");
			return;
		}

	}

	// TODO �Ƿ�汾����
	public void chekedVersionCode() {

		Ion.with(this, Conf.VERSION_CODE).asJsonObject()
				.setCallback(new FutureCallback<JsonObject>() {

					@Override
					public void onCompleted(Exception e, JsonObject result) {
						if (e != null) {
							return;
						}
						String code = result.get("code").getAsString();
						int jsonCode = Integer.parseInt(code);
						// �ȽϿ�Դ�й����ص�code����ǰ�汾code�Ƿ�һ��
						if (jsonCode == vc) {
							return;
						} else if (jsonCode > vc) {

							CountDownTimer timer = new CountDownTimer(12 * 100,
									100) {

								@Override
								public void onTick(long millisUntilFinished) {
									long a = millisUntilFinished / 100;

									if (a == 1) {
										new SweetAlertDialog(MainActivity.this,
												SweetAlertDialog.WARNING_TYPE)
												.setTitleText("�汾���")
												.setContentText("�����°汾���Ƿ���£�")
												.setCancelText("�ݲ�����")
												.setConfirmText("���ϸ���")
												.showCancelButton(true)
												.setCancelClickListener(
														new SweetAlertDialog.OnSweetClickListener() {
															@Override
															public void onClick(
																	SweetAlertDialog sDialog) {
																sDialog.dismiss();
															}
														})
												.setConfirmClickListener(
														new SweetAlertDialog.OnSweetClickListener() {
															@Override
															public void onClick(
																	SweetAlertDialog sDialog) {
																Intent updateIntent = new Intent(
																		MainActivity.this,
																		AppUpdateService.class);
																updateIntent
																		.putExtra(
																				"titleId",
																				R.string.app_name);
																startService(updateIntent);
																sDialog.dismiss();

															}
														}).show();
									} else {

									}
								}

								@Override
								public void onFinish() {

								}
							};
							timer.start();

						}

					}
				});

	}

	public PendingIntent getDefalutIntent(int flags) {
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
				new Intent(), flags);
		return pendingIntent;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		try {
			String result = data.getExtras().getString("result");// �õ���Activity
																	// �رպ󷵻ص�����
			if (result.equals("exit")) {
				isLogin = false;
				// login_tv.setText("��¼");
				login_tv.setVisibility(View.VISIBLE);
				user_name.setText("��¼");
				iv_main_left_head.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}



	private void UserInfo() {
		
		myAnimation();
		addAnimation();
		isLogin = true;

		user_name.setText("����");
		// login_tv.setText("");
		iv_main_left_head.setVisibility(View.VISIBLE);
	}


	// ��������
	private void addAnimation() {

		// login_tv.startAnimation(myAnimation_Left);
		animll_id.startAnimation(myAnimation_Left);

	}

	// ��������
	private void myAnimation() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		displayMetrics = this.getResources().getDisplayMetrics();
		// �����Ļ���
		int screenWidth = displayMetrics.widthPixels;
		myAnimation_Left = new TranslateAnimation(-screenWidth, 0, 0, 0);
		myAnimation_Left.setDuration(1800);
	}
	
	private void initPermission() {
        String[] perms = {Manifest.permission.READ_PHONE_STATE,
        		Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.READ_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "��Ϊ������Ҫ����Ҫʹ�����Ȩ�ޣ�������",
                    100, perms);
        }
    }
	@Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //Toast.makeText(this, "onPermissionsGranted  requestCode: " + requestCode , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(MainActivity.this, "���ܾ������Ȩ�ޣ����ܻᵼ����ع��ܲ�����" , Toast.LENGTH_LONG).show();
        /*if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, getString(R.string.rationale_ask_again))
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel), null *//* click listener *//*)
                    .setRequestCode(RC_SETTINGS_SCREEN)
                    .build()
                    .show();
        }*/
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
	

}
