
[Android Framework实战开发-Binder专题讲解之aidl文件的详细分析 ](https://blog.csdn.net/learnframework/article/details/119634022)

---

![[26-image1.png]]

## 1、C/S 模型

首先应用层跨进程通信时候，我们都一般会定义一个 **Service** ，因为 **Android** 中 **Service** 组件是可以运行于独立的进程即 **Service** 代码可能是在某一个 `apk` 下面，但是它运行的进程却不是这个 `apk` 的包名对应的进程，所以一般演示跨进程通信完全可以一个 `apk` 实现，这个 **Service** 就是我们真正的服务端，也是真正的业务实现端，客户端就是调用这个 **Service** 发起端，这个具体是在 **Activity** 中实现还是 **Service** 一般都是可以，一般我们为了方便就在 **Activity** 中进行跨进程通信的调用，即 **Activity** 作为客户端。

## 2、定义通信过程中的接口

（即通信要获取的信息）这个就是所说的 `aidl` 文件，它来定义好对应的通信接口接口，比如这里我们定义一个学生信息获取接口：
`IStudentInterface.aidl`
```java
interface IStudentInterface {
    int getStudentId(String name);//定义一个根据学生名字查询学号的接口
}
```

**aidl文件的转换**  
这个 `aidl` 文件写好后，点击 `make` 编译一下工程，在编译过程就会被 `aidl.exe` 转成对应的 `java` 文件，具体查看这个 `java` 文件，可以在 **android studio** 点击如下路径：

![[26-image2.png]]

如果想要直接使用 `aidl.exe` 来命令生成也是完全可以  
先到安卓 `sdk\build-tools\29.0.2` 下，确保有 `aidl.exe`

```shell
aidl.exe -IF:\binder_drivers_code\ServiceDemo\app\src\main\aidl\com\example\servicedemo  F:\binder_drivers_code\ServiceDemo\app\src\main\aidl\com\example\servicedemo\IStudentInterface.aidl

# "-I"与"F:\binder_drivers_code***"之间是没有空格的，最后就会在
# F:\binder_drivers_code\ServiceDemo\app\src\main\aidl\com\example\servicedemo
# 生成一个IStudentInterface.java

```

![[26-image3.png]]

这里相信大家已经明白了 `aidl` 是怎么一回事，它的本质目的就是为了帮助我们减少编写那些重复不变化的通信协议代码，这个完全可以让机器根据我们的描述配置文件来生成，这个描述配置文件其实就是我们的 `aidl` 文件

## 3、AIDL 生成的 java 文件

其实上面的图也提前展示了，它主要分别一部分是 **Stub 类 和 Stub .Proxy 类**

```java
  public static abstract class Stub extends android.os.Binder implements com.example.servicedemo.IStudentInterface1 {
       。。省略
    public static com.example.servicedemo.IStudentInterface1 asInterface(android.os.IBinder obj) {
         。。省略
      return new com.example.servicedemo.IStudentInterface1.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder() {
      return this;
    }
    
    //服务端，这个时候服务端驱动获取数据后，一系列调用会回调到onTransact
    @Override public boolean onTransact(int code, 
    android.os.Parcel data, android.os.Parcel reply, int flags) 
    throws android.os.RemoteException {
      java.lang.String descriptor = DESCRIPTOR;
      switch (code) {
           。。省略
        case TRANSACTION_getStudentId: {
          data.enforceInterface(descriptor);
          java.lang.String _arg0;
          _arg0 = data.readString();
          //这个地方就会真正调用Service中实现的那个getStudentId方法
          int _result = this.getStudentId(_arg0);
          reply.writeNoException();
          reply.writeInt(_result);
          return true;
        }
            。。省略
      }
    }
    
    private static class Proxy implements com.example.servicedemo.IStudentInterface1 {
     。。省略
      @Override public int getStudentId(java.lang.String name) 
      throws android.os.RemoteException {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(name);
          boolean _status = mRemote.transact(
          Stub.TRANSACTION_getStudentId, _data, _reply, 0);
          //客户端调用getStudent最后是通过mRemote调用到远程，并等待获取结果
          if (!_status && getDefaultImpl() != null) {
            return getDefaultImpl().getStudentId(name);
          }
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      public static com.example.servicedemo.IStudentInterface1 sDefaultImpl;
    }
       。。省略
  }
```

这里 `Stub` 就是远程端的具体真实实现，一般在服务端 **Serivce** 中实现，而 `Stub.Proxy` 类则是主要给客户端提供调用远程端的接口。对应的实际使用 `Stub类` 和 `Stub.Proxy类` 如下截图：  
`Stub` 的服务端实现截图：

![[26-image4.png]]

`Stub.Proxy` 的具体使用调用过程：

![[26-image5.png]]

这里的 `asInterface` 上面代码也展示，其实就是
`new com.example.servicedemo.IStudentInterface1.Stub.Proxy(obj);`
构造了一个 `Stub.Proxy` 本地对象。
最后总结一下 `aidl` 转成 `java` 文件后看到的一个跨进程调用的图：

![[26-image6.png]]

