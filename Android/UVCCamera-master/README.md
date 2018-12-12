UVCCamera
=========
https://github.com/saki4510t/UVCCamera

双摄问题解决：<br />
https://github.com/saki4510t/UVCCamera/issues/181#issuecomment-335070530 <br />
https://github.com/saki4510t/UVCCamera/issues/181#issuecomment-335543365 <br />


解决问题后，重新编译，在高分辨率下，感觉到速度慢于 <br />
https://github.com/tang5188/coding/tree/master/Android/AndroidUSBCamera-master  <br />
1.于是将【UVCCamera-master/libuvccamera/src/main/jni】文件夹删除。  <br />
2.清空【UVCCamera-master\libuvccamera\src\main\libs】内的文件夹，  <br />
3.再将【AndroidUSBCamera-master\libusbcamera\src\main\jniLibs】的到文件全部拷贝进去。  <br />
4.同时修改【UVCCamera-master/libuvccamera/build.gradle】（删除编译部分的代码，可参考【AndroidUSBCamera-master/libusbcamera/build.gradle】）  <br />
5.重新编译代码，感觉到速度快了很多。  <br />

原因可能是高版本的NDK编译出来的效果与低版本不一致，具体问题未知。  <br />
 
 <br />
速度慢问题解决的代码位于：  <br />
https://gitee.com/tang5188/UVCCamera-master
