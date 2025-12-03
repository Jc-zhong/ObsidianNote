    //省略

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //省略
        //注册监听系统的解锁广播
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_USER_UNLOCKED));
        maybeFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mProvisioned) {
            mHandler.postDelayed(mProgressTimeoutRunnable, PROGRESS_TIMEOUT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mProgressTimeoutRunnable);
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    //监听系统的解锁广播，触发检测是不是该结束自己
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            maybeFinish();
        }
    };

    //判断是否该结束自己的方法
    private void maybeFinish() {
        if (getSystemService(UserManager.class).isUserUnlocked()) {
            final Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME);
            final ResolveInfo homeInfo = getPackageManager().resolveActivity(homeIntent, 0);
            
            //简单判断PMS中获取到的Home类型的Activity还是不是自己，如果不是了，说明已解锁，可以启动真正Launcher了
            if (Objects.equals(getPackageName(), homeInfo.activityInfo.packageName)) {
                if (UserManager.isSplitSystemUser()
                        && UserHandle.myUserId() == UserHandle.USER_SYSTEM) {
                    // This avoids the situation where the system user has no home activity after
                    // SUW and this activity continues to throw out warnings. See b/28870689.
                    return;
                }
                Log.d(TAG, "User unlocked but no home; let's hope someone enables one soon?");
                mHandler.sendEmptyMessageDelayed(0, 500);
            } else {
                Log.d(TAG, "User unlocked and real home found; let's go!");
                getSystemService(PowerManager.class).userActivity(
                        SystemClock.uptimeMillis(), false);
                
                //这里检测到获取的Home类型Activity已经不是自己，说明系统中真正Launcher已经可以获取到了，则finish自己
                finish();
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            maybeFinish();
        }
    };
}
