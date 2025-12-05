
**Messenger** 是基于 **AIDL** 实现的轻量级 **IPC** 方案。
对于项目过程中可能就是一些简单的跨进程数据传递，就是调用几个非常简单的方法，使用 **aidl** 成本比较大，此时可以采用 **Messenger** 

### 1、服务端对Messenger的使用

服务端需要实现一个 **Handler** 用于处理客户端发来的跨进程通信信息：

```java
 Handler messengerHandler = new Handler() {
	@Override
	public void handleMessage(@NonNull Message msg) {
		switch (msg.what) {
			case  1:
				Log.i("test",
					"MessengerService Messenger handleMessage msg = " + 
					msg + " bundle  key value = " +
					msg.getData().getString("bundleKey"));
				Messenger clientSend = msg.replyTo;
				Message toClientMsg = Message.obtain();
				toClientMsg.what = 2;
			   // toClientMsg.obj = " I am replay from Server ";
				try {
					clientSend.send(toClientMsg);
				}catch (Exception e) {
					Log.i("test","MessengerService clientSend  error ",e);
				}
				break;
		}
		super.handleMessage(msg);
	}
};
```

其次服务端构造出对应的 **Messenger**：
```java
// 服务端 - 利用 handler 构造 Messenger 对象
Messenger messenger = new Messenger(messengerHandler);
```

最后，当服务端的 `onBinder` 回调时候要返回 **Messenger** 的 `IBinder` 对象给客户端

```java
@Nullable
@Override
public IBinder onBind(Intent intent) {
	// 将 messenger 的 binder 对象传递给 客户端
	return messenger.getBinder();
}
```

## 2、客户端的使用

客户端还是和以前一样通过 `bindService` 在 **ServiceConnection** 类的 `onServiceConnected` 获取到服务端的返回的 **IBinder**，从而获取到服务端的 **Messenger** 代理类,调用 `send` **函数发送Message** 。所以 **Messenger** 能发送的信息只有 **Message** 能携带的信息。

```java
Intent intent = new Intent(MainActivity.this,MessengerService.class);
Log.i("test","MessengerService  onClick ");
bindService(intent, new ServiceConnection() {
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		try {
			Log.i("test","MessengerService  onServiceDisconnected name = " +name);
			// 利用 service 构造 Messenger
			messengerServer = new Messenger(service);
			sendMessageToServer();
		} catch (Exception e) {
			e.printStackTrace();
			Log.i("test","error " ,e);
		}
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.i("test","client onServiceDisconnected name = " +name);
	}
}, BIND_AUTO_CREATE);

void sendMessageToServer() throws RemoteException {
	Message toServer = Message.obtain();
	toServer.replyTo = messengerClientSend;
	toServer.what = 1;
	// 这里注意不可以 传递非 parcel 的对象，这个只能给 obj 赋值为 parcel 类型对象否则报错
	// toServer.obj = "hello I send from client"; 
	Bundle bundle = new Bundle();
	bundle.putString("bundleKey","bundleValue Client");
	toServer.setData(bundle);
	messengerServer.send(toServer);
}
```

大家这里注意客户端获取了服务端 **IBinder** 对象后，用它来构造客户端的 **Messenger**，
`messengerServer = new Messenger(service);`
这里注意是和服务端不一样地方

有了服务端 **Messenger** 后，就可以通过它与服务端进行通信了，通信的内容载体是属性 **Message**，它也是和 **Handler** 搭配的 **Message**，它就是具体消息体，即你需要发送什么消息，都是把内容转换成 **Message** 对象既可以

这里我们案例中传递一个 **Bundle** 的对象，**这个Bundle** 象可以利用键值对方式装载各种各样类型数据。和 **Intent** 传递数据 **Bundle** 是一样的。注意这里 **Message** 对象还有一个属性是 `replyTo` ，这个是 **Messenger** 类型的，字面意思就是说这个消息发送过去，如果对方需要回复，就可以通过消息中的 `replyTo` 的 **Messenger** 对象来进行回复，这里是不是也和我们上节课讲的 **binder** 双向通信一样，所以说 **Messenger** 这种方式本身就相当于自带了双向通信

### 3、Messenger本质原理

**Messenger** 其实本质上也是使用 **aidl** 进行实现了，只是这个 **aidl** 是在 **Framework** 层面进行写好了，不需要你写，你也就没有在意，没有看到。这里对它的源码进行分析一下：

```java
public final class Messenger implements Parcelable {
    private final IMessenger mTarget;

	/**
	 * Create a new Messenger pointing to the given Handler.  Any Message
	 * objects sent through this Messenger will appear in the Handler as if
	 * {@link Handler#sendMessage(Message) Handler.sendMessage(Message)} had
	 * been called directly.
	 * 
	 * @param target The Handler that will receive sent messages.
	 */
	public Messenger(Handler target) {
		mTarget = target.getIMessenger();//这个是handler对象获取IMessenger接口
	}
    
    /**
     * Send a Message to this Messenger's Handler.
     * 
     * @param message The Message to send.  Usually retrieved through
     * {@link Message#obtain() Message.obtain()}.
     * 
     * @throws RemoteException Throws DeadObjectException if the target
     * Handler no longer exists.
     */
    public void send(Message message) throws RemoteException {
        mTarget.send(message);//这其实调用是IMessenger接口的send
    }
    。。。。。省略
    }
```

代码中注释提到的 **handler** 对象获取 **IMessenger** 接口，**IMessenger** 接口到底又是什么呢？

```java
// Handler 的 getIMessenger
@UnsupportedAppUsage
final IMessenger getIMessenger() {
	synchronized (mQueue) {
		if (mMessenger != null) {
			return mMessenger;
		}
		mMessenger = new MessengerImpl();
		return mMessenger;
	}
}
    
private final class MessengerImpl extends IMessenger.Stub {
	public void send(Message msg) {
		msg.sendingUid = Binder.getCallingUid();
		Handler.this.sendMessage(msg);
	}
}
```

大家看这里其实就是 **MessengerImpl** ，是继承 **IMessenger.Stub**，大家看到 **IMessenger.Stub**是不是和 **aidl** 里面的接口和很熟悉。其实就是 **IMessenger.aidl** 文件生成的

```java
package android.os;

import android.os.Message;

/** @hide */
oneway interface IMessenger {
    void send(in Message msg);
}
```

这里是不是看到期待已久的 **aidl** 了,还是个 **oneway** 类型




