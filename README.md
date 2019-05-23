[ ![Download](https://api.bintray.com/packages/ccy01220122/FocusLayoutManager/FocusLayoutManager/images/download.svg?version=1.0.0) ](https://bintray.com/ccy01220122/FocusLayoutManager/FocusLayoutManager/1.0.0/link)
# FocusLayoutManager
有焦点item的水平/垂直滚动RecyclerView-LayoutManager。仿Android豆瓣书影音“推荐“频道列表布局


## 效果

<br/> **截图:** <br/>

<img src="https://github.com/CCY0122/FocusLayoutManager/blob/master/pic/hor.jpg" width=700 />
<img src="https://github.com/CCY0122/FocusLayoutManager/blob/master/pic/ver.jpg" width=500 />

<br/> **GIF:** <br/>
![gif1](https://github.com/CCY0122/FocusLayoutManager/blob/master/pic/gif_hor_2.gif)
![gif2](https://github.com/CCY0122/FocusLayoutManager/blob/master/pic/gif_ver.gif)

<br/> **可以自己监听滚动并编写动画效果，如修改成仿MacOS文件浏览的动画:** <br/>
<img src="https://github.com/CCY0122/FocusLayoutManager/blob/master/pic/gif_mac_os.gif" width=660 />



## 依赖

1、根build.gradle:
```
allprojects {
    repositories {
        .....
        //add this
        maven { url 'https://dl.bintray.com/ccy01220122/FocusLayoutManager' }
    }
}
```
2、项目中:
```
api 'com.ccy:FocusLayoutManager:1.0.0'
// （or implementation）
```

## 使用


```java
 focusLayoutManager =
                new FocusLayoutManager.Builder()
                        .layerPadding(dp2px(this, 14))
                        .normalViewGap(dp2px(this, 14))
                        .focusOrientation(FocusLayoutManager.FOCUS_LEFT)
                        .isAutoSelect(true)
                        .maxLayerCount(3)
                        .setOnFocusChangeListener(new FocusLayoutManager.OnFocusChangeListener() {
                            @Override
                            public void onFocusChanged(int focusdPosition, int lastFocusdPosition) {
                                
                            }
                        })
                        .build();
recyclerView.setLayoutManager(focusLayoutManager);
```
各属性意义见图：<br/>
<img src="https://github.com/CCY0122/FocusLayoutManager/blob/master/pic/detail.png" width=600 />
<br/>
注意：因为item在不同区域随着滑动会有不同的缩放，所以实际layerPadding、normalViewGap也是经过缩放计算后的距离。

#### 修改动画效果：
```java
//待补充
```

#### 自定义监听：
```java
//待补充
```

## 源码解析
待补充



