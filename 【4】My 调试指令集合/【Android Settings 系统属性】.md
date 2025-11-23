
Android Settings系统属性，共分三种：

|            |                                       |
| ---------- | ------------------------------------- |
| **global** | **所有的偏好设置对系统的所有用户公开，第三方APP有读没有写的权限；** |
| **system** | **包含各种各样的用户偏好系统设置；**                  |
| **secure** | **安全性的用户偏好系统设置，第三方APP有读没有写的权限。**      |

**//System-设置**
Settings.System.putInt(getActivity().getContentResolver(), "key", 1);
  
**//System-获取**
Settings.System.getInt(getActivity().getContentResolver(), "key", 1);

**//Global**
Settings.Global.getString(ActivityThread.currentApplication().getContentResolver(), "disable_list");

  

**// 通过串口**
settings put system key 0
settings get system key

settings put global key 0
settings get global key

adb shell am start -n com.android.tv.settings/.MainSettings
adb shell am start -n com.mk.ifpd.settings/com.mk.ifpd.apps.settings.entrance.activity.SafetyLockActivity