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
	int vc;// 获取当前版本号
	ACache mCache;
	RoundedImageView iv_main_left_head;
	TextView user_name;
	RelativeLayout toprl;
	ImageView login_tv;
	LinearLayout animll_id;

	private boolean isLogin = false;
	// 定时清楚缓存
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
		String[] values = new String[] { "每日一句", "年龄：0岁-1岁半", "年龄：1岁半-3岁", "年龄：3岁-6岁", "年龄：6岁-9岁", "年龄：10岁-13岁", 
				"专题一：孩子对性器官的好奇", "专题二：自慰", "专题三：问答", "专题四：幼儿在家庭中的性伤害", 
				"专题五：孩子遭受性侵害", "专题六：生殖器", "专题七：安全套",  "专题八：艾滋病", "精美卡片", "推荐应用"};
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
							setTitle("每日一句");
							break;
						case 1:
							initFragment(new OtherFragment("age0_1.json"));
							setTitle("年龄：0岁-1岁半");
							break;
						case 2:
							initFragment(new OtherFragment("age1_3.json"));
							setTitle("年龄：1岁半-3岁");
							break;
						case 3:
							initFragment(new OtherFragment("age3_6.json"));
							setTitle("年龄：3岁-6岁");
							break;
						case 4:
							initFragment(new OtherFragment("age6_9.json"));
							setTitle("年龄：6岁-9岁");
							break;
						case 5:
							initFragment(new OtherFragment("age10_13.json"));
							setTitle("年龄：10岁-13岁");
							break;
						case 6:
							initFragment(new OtherFragment("module1.json"));
							setTitle("专题一");
							break;
						case 7:
							initFragment(new OtherFragment("module2.json"));
							setTitle("专题二");
							break;
						case 8:
							initFragment(new OtherFragment("module3.json"));
							setTitle("专题三");
							break;
						case 9:
							initFragment(new OtherFragment("module4.json"));
							setTitle("专题四");
							break;
						case 10:
							initFragment(new OtherFragment("module5.json"));
							setTitle("专题五");
							break;
						case 11:
							initFragment(new OtherFragment("module6.json"));
							setTitle("专题六");
							break;
						case 12:
							initFragment(new OtherFragment("module7.json"));
							setTitle("专题七");
							break;
						case 13:
							initFragment(new OtherFragment("module8.json"));
							setTitle("专题八");
							break;
						case 14:
							initFragment(new HomeFragment());
							setTitle("精美卡片");
							break;
						case 15:
							initFragment(new AppTuiFragment());
							setTitle("推荐应用");
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
     * 显示ShortToast
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
	 * 获取版本号
	 * 
	 * @param context
	 * @return
	 */
	private int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			// 获取软件版本号
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
		MobclickAgent.onResume(this); // 统计时长
		String avatar = mCache.getAsString("avatar");
		String name = mCache.getAsString("name");
		try {
			if (avatar.equals("")) {
				login_tv.setVisibility(View.VISIBLE);
				// login_tv.setText("登录");
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
				// login_tv.setText("登录");
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
		// 只當容器，主要內容已Fragment呈現
		initFragment(new EveryDayEnglishFragment());
	}

	// 切換Fragment
	public void changeFragment(Fragment f) {
		changeFragment(f, false);
	}

	// 初始化Fragment(FragmentActivity中呼叫)
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
			// 创建一个文件夹对象，赋值为外部存储器的目录
			File sdcardDir = Environment.getExternalStorageDirectory();
			// 得到一个路径，内容是sdcard的文件夹路径和名字
			String path = sdcardDir.getPath() + "/readersex";
			File path1 = new File(path);
			if (!path1.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				path1.mkdirs();
				System.out.println("paht ok,path:" + path);
			}
		} else {
			System.out.println("false");
			return;
		}

	}

	// TODO 是否版本更新
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
						// 比较开源中国返回的code跟当前版本code是否一致
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
												.setTitleText("版本检测")
												.setContentText("发现新版本，是否更新？")
												.setCancelText("暂不更新")
												.setConfirmText("马上更新")
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
			String result = data.getExtras().getString("result");// 得到新Activity
																	// 关闭后返回的数据
			if (result.equals("exit")) {
				isLogin = false;
				// login_tv.setText("登录");
				login_tv.setVisibility(View.VISIBLE);
				user_name.setText("登录");
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

		user_name.setText("设置");
		// login_tv.setText("");
		iv_main_left_head.setVisibility(View.VISIBLE);
	}


	// 启动动画
	private void addAnimation() {

		// login_tv.startAnimation(myAnimation_Left);
		animll_id.startAnimation(myAnimation_Left);

	}

	// 动画定义
	private void myAnimation() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		displayMetrics = this.getResources().getDisplayMetrics();
		// 获得屏幕宽度
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
            EasyPermissions.requestPermissions(this, "因为功能需要，需要使用相关权限，请允许",
                    100, perms);
        }
    }
	@Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //Toast.makeText(this, "onPermissionsGranted  requestCode: " + requestCode , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(MainActivity.this, "您拒绝了相关权限，可能会导致相关功能不可用" , Toast.LENGTH_LONG).show();
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
