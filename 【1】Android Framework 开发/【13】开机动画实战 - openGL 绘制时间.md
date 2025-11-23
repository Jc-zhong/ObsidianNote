
## 前言： **drawClock() 函数** 的分析

若要实现在开机动画中绘制 **自定义的时间**
首先我们需要分析一下在 `BootAnimation.cpp` 中的 **drawClock() 函数**
**（见附件 搜索关键字 Jc-z - 【13-00】开始）**

![[13-Bootanimation.cpp]]

分析完 drawClock 方法后，接着我们来实现开机动画中绘制  **自定义的时间**

## 1、 声明时间 Font 变量

在 `BootAnimation.h` 头文件中声明 Font 变量
```c++
Font    mClockFont;
```


## 2、初始化 Font 变量 & 调用绘制方法

在 `BootAnimation.cpp` 中的 android() 方法中，为 Font 变量进行初始化
```c++
// 声明 Font 的图片路径，注意：这里需要确保 clock_font.png 文件存在
// frameworks/base/core/res/assets/images/ 此目录下查看
// clock_font.png 实际就是一张含有 “ 0123456789: ” 的图片
// 另外说明： openGL 不支持我们显示 字符123 的数据显示，它只支持图片格式的 123数字字样
// 因此才需要 clock_font.png 的存在
// 比如需要数字 0 时，可以通过 openGL 裁剪图片 获取到 0 数字的图片纹理，然后再进行操作
static const char CLOCK_FONT_ASSET[] = "images/clock_font.png";

bool BootAnimation::android(){
	....
	initTexture(xxxx);
	
	// add by test - begin
	// 初始化 Font
	bool hasInitFont = false;
	if(initFont(&mClockFont, CLOCK_FONT_ASSET) == NO_ERROR){
		hasInitFont = true;
		ALOGD("Android init Font success, font name = %u",mClockFont.texture.name);
	}
	....
	// 注意：此处是裁剪出最小绘制的面积区域，以减少 CPU 的消耗
	// 如果我们不修改的话，那就将无法显示我们添加的 时间绘制
	// 它只显示 Android logo 的 宽*高 区域
	// 而我们定义的时间绘制位于 logo 上方，不在绘制范围内，因此无法显示
	//const Rect updateRect(xc, yc, xc + mAndroid[0].w, yc + mAndroid[0].h);
	//glScissor(updateRect.left, mHeight - updateRect.bottom, 
			//updateRect.width(), updateRect.height());
	// 此时我们修改绘制区域即可，我们让其绘制高度 * 2
	const Rect updateRect(xc, yc, xc + mAndroid[0].w, yc + mAndroid[0].h * 2);
	glScissor(updateRect.left, mHeight - updateRect.bottom, 
			updateRect.width(), updateRect.height() * 2);
	....
	// 在 do while 函数中添加绘制时间的方法
	do{
		...
		// 调用现成的 drawClock 函数，实现绘制时间
		// mClockFont : 绘制的内容
		// TEXT_CENTER_VALUE : x轴 居中显示
		// yc + mAndroid[0].h : y轴 位于 Android logo 图上方
		drawClock(mClockFont, TEXT_CENTER_VALUE, yc + mAndroid[0].h);
		...
	}while(!exitPending());
	
	// 释放 Font 纹理
	if(hasInitFont){
		glDeleteTextures(1, &mClockFont.texture.name);
	}
	// add by test - end
	
	return false;
}
```

## 3、 编译&验证

最终实现的效果为： 

![[12-ep-image1.png|100x200]]
