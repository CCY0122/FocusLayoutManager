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

<br/> 可自己监听滚动编写效果，如修改成仿MacOS文件浏览: <br/>
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

#### 调整动画效果：
```java
                new FocusLayoutManager.Builder()
                        ......
                        .simpleTrasitionListener(new FocusLayoutManager.SimpleTrasitionListener() {
                             @Override
                            public float getLayerViewMaxAlpha(int maxLayerCount) {
                                return super.getLayerViewMaxAlpha(maxLayerCount);
                            }

                            @Override
                            public float getLayerViewMinAlpha(int maxLayerCount) {
                                return super.getLayerViewMinAlpha(maxLayerCount);
                            }

                            @Override
                            public float getLayerChangeRangePercent() {
                                return super.getLayerChangeRangePercent();
                            }
                            //and more
                            
                            //更多可重写方法和释义见接口声明
                        })
                        .build();
```

#### 自定义动画/滚动监听：
如果你想在滑动时不仅仅改变item的大小、透明度，你有更多的想法，可以监听TrasitionListener,该监听暴露了很多关键布局数据，
```java
            ......
            .trasitionListener(new FocusLayoutManager.TrasitionListener() {
                            @Override
                            public void handleLayerView(FocusLayoutManager focusLayoutManager,
                                                        View view, int viewLayer,
                                                        int maxLayerCount, int position,
                                                        float fraction, float offset) {
                                
                            }

                            @Override
                            public void handleFocusingView(FocusLayoutManager focusLayoutManager,
                                                           View view, int position,
                                                           float fraction, float offset) {

                            }

                            @Override
                            public void handleNormalView(FocusLayoutManager focusLayoutManager, View view, int position, float fraction, float offset) {

                            }
                        })
```
各参数意义见接口注释。
实际上`SimpleTrasitionListener`内部就会被转为`TrasitionListener`(SimpleTrasitionListener没有getter方法）。可参考转换类是怎么做的：`TrasitionListenerConvert`

## 源码解析
待补充



