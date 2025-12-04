### 1、oneway 介绍

**oneway** 关键字用于修饰远程调用的行为，被 **oneway** 修饰了的方法不可以有返回值，也不可以有带 **out** 或 **inout** 的参数。
**使用 oneway 时，远程调用不会阻塞；它只是发送事务数据并立即返回**。接口的实现最终接收此调用时，是以正常远程调用形式将其作为来自 **Binder** 线程池的常规调用进行接收。

### 2、in，out，inout 介绍

**in、out、inout** 表示跨进程通信中数据的流向（基本数据类型默认是 **in** ，非基本数据类型可以使用其它数据流向 **out、inout** ）。

| 关键字       | 描述                               |                                                |
| --------- | -------------------------------- | ---------------------------------------------- |
| **in**    | **表示数据只能由 <br>客户端 -> 服务端**       | **表现为服务端修改此参数<br>不会影响客户端的对象**                  |
| **out**   | **表示数据只能由 <br>服务端 -> 客户端**       | **表现为服务端收到的参数是空对象<br>并且服务端修改对象后客户端会同步变动**      |
| **inout** | **表示数据可双向流通**<br>**服务端 <-> 客户端** | **表现为服务端能接收到客户端传来的完整对象<br>并且服务端修改对象后客户端会同步变动** |

注意如果 `aidl` 中发现对象类型参数可以不带 **in，out，inout** 任何一个，那么它一定属于默认 **in** 类型，而且也不能强制给其加上 **out** 或 **inout**
具体这里可以看 **google** 官方文档的原话：
**（注意文档要用英文来看，不要使用中文，会有翻译歧义！！）**

[Android 接口定义语言 (AIDL)  |  Background work  |  Android Developers](https://developer.android.google.cn/develop/background-work/services/aidl?hl=zh-cn#Create)

```bash
When defining your service interface, be aware that:

· Methods can take zero or more parameters, and return a value or void.
· All non-primitive parameters require a directional tag indicating which way the data goes. Either in, out, or inout (see the example below).
  Primitives, String, IBinder, and AIDL-generated interfaces are in by default, and cannot be otherwise.
```

`Primitives, String, IBinder, and AIDL-generated interfaces are in by default` —这句就说明了元数据类型，**String ，IBinder，还有 AIDL 生成的接口那默认就是 in，不能为其他**

## 3、Binder跨进程双向通信的实现

![[27-image1.png]]

这里其实一共只需要分为 3 步：
1. 首先 **client** 通过 **bindService** 方式获取到了服务端的 **IServer** 接口对象，既可以正常调用服务端的接口
2. 这里服务端接口是 **IServer** 里面有一个 `setCallback` 方法,这个方法参数是一个 **ICallbackClient** 类型实体接口对象，这个接口对象由客户端进行实现，服务端进行调用
3. 服务端在客户端 `setCallback` 之后就获取了客户端的 **ICallbackClient** 类型的对象，调用改对象的方法就可以与客户端进行通信

## 4、linktodeath介绍

死亡通知是为了让 **Bp端(客户端进程)** 进能知晓 **Bn端(服务端进程)** 的生死情况，当 **Bp端进程** 死亡后能通知到 **Bn端**。

定义：**AppDeathRecipient** 是继承 **IBinder::DeathRecipient 类**，主要需要实现其 `binderDied()` 来进行死亡通告。

注册：`binder->linkToDeath(AppDeathRecipient)` 是为了将 **AppDeathRecipient** 死亡通知注册到 **Binder** 上。

**Bp端** 只需要覆写 `binderDied()` 方法，实现一些后尾清除类的工作，则在 **Bn端** 死掉后，会回调`binderDied()` 进行相应处理。

```cpp
// 监听客户端的进程是否死亡
// 在服务端的 AIDL 绑定的回调接口方法使用 callback 参数进行监听
 @Override
public void setCallback(IChangeCallback callback) throws RemoteException {
	mCallBack = callback;
	mCallBack.asBinder().linkToDeath(new DeathRecipient() {
		@Override
		public void binderDied() {
			Log.i("test","client binderDied");
		}
	},0);
}

// 监听服务端的进程是否死亡
// 在客户端的 onServiceConnected() 回调方法中，使用传回来的 service 参数进行监听
service.linkToDeath(new IBinder.DeathRecipient() {
	 @Override
	 public void binderDied(){
		 Log.i("test","Service binderDied");
	 }
}, 0);
```








