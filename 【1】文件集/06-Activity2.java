@CallSuper
protected void onResume() {
    if (DEBUG_LIFECYCLE) Slog.v(TAG, "Resume" + this);
    getApplication().dispatchActivityResumed(this);
    mActivityTransitionState.onResume(this, isTopOfTask());
    mCalled = true;

    // add by test - begin
    ComponentName srcCom = new ComponentName("com.example.myapplication","com.example.myapplication/.SpalashActivity");
    if(srcCom.equals(getComponentName())) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() { injectClick(420, 52); }
        }, 1000); 
    }
    // add by test - end
}




// add by test - begin
void injectClick(int x, int y) {
    // Simulation ACTION_DOWN Event
    MotionEvent downMotion = MotionEvent.obtain(
        android.os.SystemClock.uptimeMillis(),
        android.os.SystemClock.uptimeMillis(),
        MotionEvent.ACTION_DOWN,x,y,0);
    dispatchTouchEvent(downMotion);

    // Simulation ACTION_UP Event after 100ms
    MotionEvent upMotion = MotionEvent.obtain(
        android.os.SystemClock.uptimeMillis(),
        android.os.SystemClock.uptimeMillis(),
        MotionEvent.ACTION_UP,x,y,0);
    mHandler.postDelayed(new Runnable(){
        @Override
        public void run() { dispatchTouchEvent(upMotion); }
    }, 100); 
}
// add by test - end