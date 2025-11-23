@override
public void startActivity(Intent intent, @Nullable Bundle options){
    // add by test - begin
    ComponentName srcCom = new ComponentName("com.example.myapplication","com.example.myapplication/.SpalashActivity");
    ComponentName destCom = new ComponentName("com.example.myapplication","com.example.myapplication/.MainActivity");
    if (intent.getComponent() != null && intent.getComponent.equals(srcCom)){
        intent.setComponent(destCom);
    }
    // add by test - end

    if (options != null){
        startActivityForResult(intent, -1 , options);
    } else {
        // Note we want to go through this call for compatibility with
        // application thar may have overriden the method.
        startActivityForResult(intent, -1);
    }
}
