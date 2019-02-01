# 关于
中间件下载库，属于中间件的子项目，主要为Android移动端提供下载功能，并支持对下载文件进行校验和解压，支持自定义校验、解压方式。

## 下载库框架

![structure](http://172.28.2.93/bfc/BfcDownload/raw/dev_lzy/doc/%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3/bfc-download-structure.png)


# 特性
- 简单易用
- 高效稳定
- 扩展性强，支持自定义校验方式和解压方式
- 灵活控制下载使用的网络类型
- 错误码精准定位和查询

## 版本和项目名称
- 发布版本： 2.0.1
- GitLab项目名： BfcDownload
- 库名称：bfc-download
- 需要Android API >= 15

## 功能列表
- 支持设置下载文件名称
- 支持设置文件保存路径
- 支持预设文件大小并最终进行文件大小检测 
- 支持定义下载优先级
- 支持设置网络类型
- 支持文件校验
- 支持文件解压
- 支持定义扩展字段
- 支持批量查询任务
- 支持自定义查询任务
- 支持暂停下载
- 支持恢复下载
- 支持重新下载
- 支持多种方式删除任务
- 支持动态修改网络类型
- 支持设置多个监听器监听同一个下载任务
- 支持对所有下载任务进行统一监听
- 支持任务按模块分类，各模块查询不干扰
- 支持设置进度回调间隔时间，也可不回调进度

## 升级清单文档
- 文档名称：UPDATE.md (http://172.28.2.93/bfc/BfcDownload/blob/master/UPDATE.md)

# 使用

#### 注意事项
- 使用前必须先初始化
- 如果用到外置存储卡，请声明读写权限"android.permission.WRITE_EXTERNAL_STORAGE"
- 必须配置和使用我们公司的私有maven仓库(http://172.28.1.147:8081/nexus/content/repositories/thirdparty/)
- 必须在项目主module的build.gradle配置文件中显式申明applicationId，否则数据库配置将会失败
- 旧版数据怎么在新版中获取，详细请看文档最后面“特殊情况”的第七项

## 前置条件  
#### 1. 需要申请的权限
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
> 5.0以上系统还需要在代码中动态申请权限,具体请查看Android API

#### 2. 已申请的权限
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```
> 在下载库中的AndroidManifest.xml中已申请以上权限

#### 3. 项目中如何使用下载库
##### 使用Gradle导入下载库:
```groovy
dependencies {
    compile "com.eebbk.bfc:bfc-download:+"
    // 其中“+”号也可以改成具体已发布的版本号，建议使用最新版本
}
```

## 初始化说明
#### 1.使用默认初始化
```java
DownloadController.init(this.getApplicationContext());
// 建议在Application中进行，初始化不会引起性能和时间消耗
```

#### 2.使用自定义全局配置初始化
```java
GlobalConfig config = new GlobalConfig.Builder()
                .setDebug(true) // 设置debug模式，调试模式使用
                .setSavePath(path) // 设置保存路径
                .setNetType(NetworkType.NETWORK_WIFI|NetworkType.NETWORK_BLUETOOTH) // 设置网络类型
                // 可继续添加其他配置项
                .build();
DownloadController.init(this.getApplicationContext(), config);
// 全局配置可以设置调试模式、日志保存地址、默认网络、默认下载保存路径、是否解压默认值、校验类型默认值、添加自定义校验器、添加自定义解压器、删除任务时怎么处理已下载文件、电信2g网络是否允许下载等
```

## 核心功能使用说明
### 1.创建任务
##### 1.1 使用默认参数创建任务
```java
ITask task = DownloadController.buildTask(url).build();
// 使用下载地址创建任务，其他都使用默认配置

ITask task = DownloadController.buildTask(url ,fileName ,savePath).build();
// 使用下载地址、文件名、保存地址创建任务，其他都使用默认配置，除下载地址不能为null，其他参数都可为null

ITask task = DownloadController.buildTask(url, fileName, fileSize, fileExtension, md5Code).build();
// 使用下载地址、文件名、预设文件大小、后缀名、md5校验码创建任务，其他都使用默认配置，除下载地址不能为null，其他参数都可为null
```

##### 1.2 自定义参数配置创建任务
```java
        // 创建任务并配置参数
        ITask.Builder builder = DownloadController.buildTask(mConfigView.getUrl())
                .setFileName(mConfigView.getFileName()) // 设置文件名，可包含文件后缀
                .setFileExtension(mConfigView.getFileExtension()) // 设置文件后缀 
                .setSavePath(mConfigView.getSavePath()) //设置下载文件保存路径
                .setPresetFileSize(mConfigView.getPresetFileSize()) // 设置文件预设大小，用来比较跟真实文件大小是否一致
                .setAutoCheckSize(mConfigView.isAutoCheckSize()) //设置是否自动比较文件大小（设置了文件预设大小才有效）
                .setPriority(mConfigView.getPriority()) //设置优先级
                .setCheckType(mConfigView.getCheckType()) //设置校验类型
                .setCheckCode(mConfigView.getCheckCode()) //设置校验码
                .setCheckEnable(mConfigView.isCheckEnable()) //设置校验开关，为true才会进行校验
                .setNetworkTypes(mConfigView.getNetworkTypes()) //设置下载可以使用的网络类型
                .setReserver(mConfigView.getReserver()) // 设置自定义字段
                .setMinProgressTime(mConfigView.getMinProgressTime()) // 设置进度回调时间，<0不回调(只会回调进度的第一次和最后一次)，=0实时回调，>0按设置的间隔时间回调
                .setShowRealTimeInfo(mConfigView.isShowRealTimeInfo()) // 设置显示实时速度，默认false
                .setAutoUnpack(mConfigView.isAutoUnpack()) // 设置自动解压
                .setUnpackPath(mConfigView.getUnpackPath()) // 设置解压文件保存路径，如果不设置，则会选择提取路径（下载路径+文件名称）保存
                .setDeleteNoEndTaskAndCache(mConfigView.isDeleteNoEndTaskAndCache()) // 设置默认删除任务时是否删除缓存文件（未下完的文件），默认删除
                .setDeleteEndTaskAndCache(mConfigView.isDeleteEndTaskAndCache()) // 设置默认删除任务时是否删除已下载的文件,默认不删除
                .setModuleName(mConfigView.getModuleName()) // 设置任务所属模块

                .setOnDownloadListener(mDownloadListener) // 增加下载监听
                .setOnCheckListener(mCheckListener) // 增加文件校验监听
                .setOnUnpackListener(mUnpackListener); // 增加解压监听
        ITask task = builder.build();
```
> 以上为任务所有配置参数，很多配置采用默认值即可，特殊的可根据各自需求配置
> 0.9.2版本增加任务所属模块配置，配置任务所属模块，模块名可以自定义，任务将会添加到指定的模块下。

##### 1.3 下载任务分模块
**使用下载库的应用默认有一个模块，以ApplicationId为模块名，所有默认任务都添加在此模块下。添加、查询任务或者注册监不带模块名参数时，都是查询的默认模块下的任务。只有指定模块名参数时，才会添加、查询指定模块下的任务。注册、注销监听也是一样。**

### 2.单任务下载监听(必须注册监听才会生效)
##### 2.1 创建并设置单任务下载监听
```java
mDownloadListener = new OnDownloadListener() {
            public void onDownloadWaiting(ITask task) {} // 下载等待中，排队或者缺少运行资源处于等待状态，一旦轮到或者满足条件则会立即开始
            public void onDownloadStarted(ITask task) {} // 下载已开始
            public void onDownloadConnected(ITask task, boolean resuming, long finishedSize, long totalSize) {}
            public void onDownloading(ITask task, long finishedSize, long totalSize) {}
            public void onDownloadPause(ITask task, String errorCode) {}
            public void onDownloadRetry(ITask task, int retries, String errorCode, Throwable throwable) {}
            public void onDownloadFailure(ITask task, String errorCode, Throwable throwable) {}
            public void onDownloadSuccess(ITask task) {}
        };

// 创建任务时设置下载监听
ITask.Builder builder = DownloadController.buildTask(url).setOnDownloadListener(mDownloadListener);
```
> 也可以继承SimpleDownloadListener.java类，覆写自己需要的下载相关回调方法   
> 正常下载流程回调：onDownloadWaiting -> onDownloadStarted -> onDownloadConnected -> onDownloading -> onDownloadSuccess/onDownloadFailure    
> 暂停流程回调：onDownloadSuccess 或者 onDownloadFailure之前的任何状态都可能会被暂停回调onDownloadPause
> 恢复流程回调：同正常流程回调     
> 重试流程回调：在onDownloadConnected、onDownloading之间或之后可能会回调onDownloadRetry，最后直至onDownloadSuccess/onDownloadFailure     
> 异常流程回调：onDownloadStarted、onDownloadConnected、onDownloading的任何回调之后都可能会发生异常，然后导致失败回调onDownloadFailure
>
> 注意：onDownloadPause方法，会返回错误码，此时错误码标识暂停原因。暂停原因由errorCode错误码标识，目前造成暂停的原因只有三种：
>     1.用户手动暂停，必须手动恢复下载
>     2.网络原因，网络正常后会自动恢复下载
>     3.存储空间不足造成暂停，清理后必须手动恢复下载
>     具体判断方法可以调用ITask.isPauseByUser()、ITask.isPauseByNetwork()、ITask.isPauseByOutOfSpace()

#### 2.2 创建并设置单任务文件校验监听
```java
mCheckListener = new OnCheckListener() {
            public void onCheckStarted(ITask task, long totalSize) {}
            public void onChecking(ITask task, long finishedSize, long totalSize) {}
            public void onCheckFailure(ITask task, String errorCode, Throwable throwable) {}
            public void onCheckSuccess(ITask task) {}
        };

// 创建任务时设置校验监听
ITask.Builder builder = DownloadController.buildTask(url).setOnCheckListener(mCheckListener)
```
> 也可以继承SimpleDownloadListener.java类，覆写自己需要的校验相关回调方法    
> 正常校验流程回调：onCheckStarted -> onChecking -> onCheckSuccess/onCheckFailure    
> 其中可能会遇到异常结束： onCheckStarted -> onCheckFailure

#### 2.3 创建并设置单任务文件解压监听
```java
mUnpackListener = new OnUnpackListener() {
            public void onUnpackStarted(ITask task, long totalSize) {}
            public void onUnpacking(ITask task, long finishedSize, long totalSize) {}
            public void onUnpackFailure(ITask task, String errorCode, Throwable throwable) {}
            public void onUnpackSuccess(ITask task) {}
        };

// 创建任务时设置解压监听
ITask.Builder builder = DownloadController.buildTask(url).setOnUnpackListener(mUnpackListener)
```
> 也可以继承SimpleDownloadListener.java类，覆写自己需要的解压相关回调方法    
> 正常校验流程回调：onUnpackStarted -> onUnpacking -> onUnpackSuccess/onUnpackFailure    
> 其中可能会遇到异常结束： onUnpackStarted -> onUnpackFailure

#### 2.4 对同一个任务设置多个监听
```java
// 首先通过url和保存地址查找任务，或者通过id以及其他方式查找到任务
ITask task = DownloadController.getInstance().getTask(url, savePath);
        // 创建并设置监听
        task.setOnDownloadListener(mDownloadListener) // 设置下载监听
            .setOnCheckListener(mCheckListener) // 设置校验监听，不用可不设
            .setOnUnpackListener(mUnpackListener); // 设置解压监听，不用可不设
           
        // 注册监听
        DownloadController.getInstance().registerTaskListener(task);
```
> task为设置了监听的任务，task可以通过查找任务得到

#### 2.5 注册监听
```java
// 1.注册监听
DownloadController.getInstance().registerTaskListener(task);     

// 2.注册监听，附带tag(同一tag可以与多个任务关联，注销时可按tag全部注销)
DownloadController.getInstance().registerTaskListener(tag, task);     
```
> 注意：注册了任务监听，必须在不需要时进行注销，否则将会引起内存泄露

#### 2.6 注销监听
```java
// 1.注销监听
DownloadController.getInstance().unregisterTaskListener(task);

// 2.注销监听，按tag(按tag注销关联的全部任务)
DownloadController.getInstance().unregisterTaskListener(tag);

// 3.注销监听，按id注销(注销相同id的所有监听)
DownloadController.getInstance().unregisterTaskAllListener(task);
```

#### 2.7 销毁任务
```java
if(task != null){
    task.recycle();
    task = null;
}
```

### 3.多任务下载监听
#### 3.1 创建多任务监听
```java
DownloadListener mListener = new DownloadListener();
        mListener.setOnDownloadListener(mDownloadListener); // 设置下载监听，监听同单任务
        mListener.setOnCheckListener(mCheckListener); // 设置校验监听，监听同单任务，不用可不设
        mListener.setOnUnpackListener(mUnpackListener); // 设置解压监听，监听同单任务，不用可不设
```

#### 3.2 注册多任务监听
```java
// 1.给默认模块的所有任务注册监听
DownloadController.getInstance().registerTaskListener(mListener);

// 2.给指定模块的所有任务注册监听
DownloadController.getInstance().registerTaskListener(mListener, moduleName);
```
> 注意：注册了多任务监听，必须在不需要时进行注销，否则将会引起内存泄露

#### 3.3 注销多任务监听
```java
// 1.给默认模块的所有任务注册监听
DownloadController.getInstance().unregisterTaskListener(mListener);

// 2.给指定模块的所有任务注册监听
DownloadController.getInstance().unregisterTaskListener(mListener, moduleName);
```
> 注意：注册了多任务监听，必须在不需要时进行注销，否则将会引起内存泄露

### 4.任务操作（支持批量操作）
#### 4.1 开始任务
```java
DownloadController.getInstance().addTask(task);
```
> 不存在的任务直接添加；已经存在的任务，如果没有正在下载且没有下载完成，会尝试恢复下载

#### 4.2 暂停任务
```java
DownloadController.getInstance().pauseTask(task);
```

#### 4.3 恢复任务
```java
DownloadController.getInstance().resumeTask(task);
```

#### 4.4 重新下载（将会删除所有文件，包括缓存和目标文件）
```java
DownloadController.getInstance().reloadTask(task);
```
> 任务必须存在，即以前添加过，任务存在数据库中，否则重下失败，打印任务不存在日志

#### 4.5 按配置删除任务
```java
DownloadController.getInstance().deleteTask(task);
// 按创建任务时的配置选择是否删除缓存或者目标文件
```

#### 4.6 删除任务同时删除所有文件
```java
DownloadController.getInstance().deleteTaskAndAllFile(task);
// 不按配置，直接删除缓存和目标文件
```

#### 4.7 删除任务不删除文件
```java
DownloadController.getInstance().deleteTaskWithoutFile(task);
// 不按配置，保留缓存和目标文件
```

#### 4.8 修改已开始下载的任务使用网络类型
```java
// 设置下载可以使用wifi、mobile、bluetooth三种网络
int networkTypes = NetworkType.NETWORK_WIFI | NetworkType.NETWORK_MOBILE | NetworkType.NETWORK_BLUETOOTH;
DownloadController.getInstance().setNetworkTypes(networkTypes, task);

// 添加数据网络，添加后可以使用数据网络 （注意：添加数据网络将默认添加Wifi网络，即可以数据网络就可以使用Wifi网络）
int networkTypes = NetworkParseUtil.addNetworkType(task.getNetworkTypes(), NetworkType.NETWORK_MOBILE);
DownloadController.getInstance().setNetworkTypes(networkTypes, task);

// 移除数据网络，移除后将不能使用数据网络
int networkTypes = NetworkParseUtil.removeNetworkType(task.getNetworkTypes(), NetworkType.NETWORK_MOBILE);
DownloadController.getInstance().setNetworkTypes(networkTypes, task);
```

#### 4.9  同步任务最新信息
```java
DownloadController.getInstance().refreshData(task);
```

### 5.查询任务
> 注意：所有的查询返回结果均可能返回null

#### 5.1 查找所有任务
```java
// 1.查询默认模块的所有任务
ArrayList<ITask> tasks = DownloadController.getInstance().getTask();

// 2.查询指定模块的所有任务
ArrayList<ITask> tasks = DownloadController.getInstance().getTask(moduleName);
```

#### 5.2 按状态查找任务
```java
// 1.按状态在默认模块中查询满足条件的任务
ArrayList<ITask> tasks = DownloadController.getInstance().getTaskByStatus(status);

// 2.按状态在指定模块中查询满足条件的任务   status为Status.java接口中的常量
ArrayList<ITask> tasks = DownloadController.getInstance().getTaskByStatus(status, moduleName);
```

#### 5.3 按下载地址url和保存地址savePath查询任务
```java
ITask task = DownloadController.getInstance().getTask(url, savePath);
// 下载地址和保存地址可以确定唯一的一个任务，两个下载任务的下载地址和保存地址一致则为同一个任务，id相同
```

#### 5.4 按id查找任务
```java
ITask task = DownloadController.getInstance().getTaskById(id);
// id通过将下载地址和保存地址md5生成
```

#### 5.5 通过自定义的扩展字段查找任务
```java
// 1.按扩展字段在默认模块中查询满足条件的任务
ArrayList<ITask> tasks = DownloadController.getInstance().getTaskByExtras(keys，values);

// 1.按扩展字段在指定模块中查询满足条件的任务
ArrayList<ITask> tasks = DownloadController.getInstance().getTaskByExtras(keys，values, moduleName);
```

#### 5.6 自定义查询
```java
Query query = new Query();
query.setModuleName(moduleName); // 添加模块过滤，默认可以不填
query.setFilterByStatus(state1, state2,state3...); // 添加状态过滤
query.setFilterById(id1,id2,id3...); // 添加id过滤
query.addFilterByExtras(key, value); // 添加扩展字段过滤
query.orderBy(Query.COLUMN_TASK_CREATE_TIME, Query.ORDER_ASC); // 任务排序：升序、降序， 默认按任务创建时间升序排列，支持按创建时间、文件总大小、任务在数据库中的ID
ArrayList<ITask> tasks = DownloadController.getInstance().getTask(query); // 开始查询
```

### 6.下载库版本信息查看
```java
StringBuilder sb = new StringBuilder();
sb.append("\r\n 库名称: " + SDKVersion.getLibraryName());
sb.append("\r\n 版本序号: " + SDKVersion.getSDKInt());
sb.append("\r\n 版本名称: " + SDKVersion.getVersionName());
sb.append("\r\n 构建版本: " + SDKVersion.getBuildName());
sb.append("\r\n 构建时间: " + SDKVersion.getBuildTime());
sb.append("\r\n TAG标签: " + SDKVersion.getBuildTag());
sb.append("\r\n HEAD值: " + SDKVersion.getBuildHead());
```

## 公开接口说明
### Task任务接口说明
#### 1. 配置参数相关
```java
     int INVALID_GENERATE_ID = -1;  // 无效任务ID
     int getId();  //获取下载任务ID，通过url和保存路径生成
     String getModuleName(); // 获取模块名称
     String getUrl(); // 获取下载地址
     String getRealUrl(); // 获取经过decodeUTF8转换过的url
     String getFileName(); // 获取下载文件名称
     String getFileExtension(); // 获取下载文件后缀名
     String getSavePath(); // 获取下载文件保存路径
     long getPresetFileSize(); // 获取预设的下载文件大小
     long getFileSize(); // 获取真实的下载文件大小
     boolean isAutoCheckSize(); // 是否自动检测文件大小
     int getPriority(); // 获取下载任务优先级
     String getCheckType(); // 文件校验类型，详细可查看{@link com.eebbk.bfc.sdk.download.TaskParam.CheckType}, 也可以自定义
     String getCheckCode(); // 获取文件校验码
     boolean isCheckEnable();  // 文件校验开关是否打开
     boolean needCheckFile();  // 是否需要校验文件，将根据配置的参数以及校验开关决定
     int getNetworkTypes();  // 获取网络类型
     String getReserver();  // 获取保留字段
     Throwable getException();  // 获取异常信息
     boolean isAllowMobileNet();  // 是否允许使用移动网络
     boolean isAllowWifiNet();  // 是否允许使用Wifi网络
     boolean isAllowBluetoothNet();  // 是否允许使用蓝牙网络
     String getUnpackPath();  // 获取解压文件保存路径
     boolean isShowRealTimeInfo(); // 是否显示实时状态（下载速度、剩余时间）（默认false） 设置setMinProgressTime >= 0 时此值有效，实时状态返回将跟随进度同时返回
     int getMinProgressTime(); // 下载进度回调间隔时间，单位毫秒
     boolean isAutoUnpack(); // 是否自动解压（默认否）
     boolean isDeleteNoEndTaskAndCache(); // 删除未下载完成任务时同时删除缓存文件（默认是）
     boolean isDeleteEndTaskAndCache(); // 删除已下载完成任务时同时删除文件（默认否）
     int getDownloadThreads(); // 获取下载任务开启多线程数量
     boolean isFinished(); // 下载任务是否已结束，标识任务将不会在运行，除非让任务重现开始
     ITask cloneTask(); // 将当前任务clone出一个新的任务，将不会保留当前任务的监听，深度clone数据
     ITask updateData(ITask task); // 复制指定任务的数据到当前任务中，将会保留当前任务的监听 注意：此方法不是深度copy
     OnDownloadListener getOnDownloadListener(); // 获取文件下载监听
     OnCheckListener getOnCheckListener(); // 获取文件校验监听
     OnUnpackListener getOnUnpackListener(); // 获取文件解压监听
     ITask setOnDownloadListener(OnDownloadListener downloadListener); // 设置下载监听 注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     ITask setOnCheckListener(OnCheckListener checkListener); // 设置校验监听 注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     ITask setOnUnpackListener(OnUnpackListener unpackListener); // 设置解压监听  注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     void recycle(); // 回收资源，注销监听,任务不使用时必须尽早调用此方法，避免监听没有注销导致内存泄露
```
#### 2.获取临时状态、进度、异常信息
```java
    int getState(); // 获取当前状态，包括下载状态、校验状态、解压状态
    String getSpeed(); // 获取当前速度值。下载时，显示的是下载速度；校验时，显示的是校验速度；解压时，显示的是解压速度 速度值保留小数点后面两位 如果不想转换，可以通过{@link #getSpeedNumber()}获取原始值
    long getSpeedNumber(); // 获取当前速度值。 B/S
    String getLastTime(); // 获取剩余时间。下载时，显示的是下载剩余时间；校验时，显示的是校验剩余时间；解压时，显示的是解压剩余时间 如果不想转换，可以通过{@link #getLastTimeSeconds()}获取原始值
    long getLastTimeSeconds(); // 获取剩余时间,单位秒
    long getFinishSize(); // 获取已完成大小。下载时，显示的是下载已完成大小；校验时，显示的是校验已完成大小；解压时，显示的是解压已完成大小
    String getErrorCode(); // 获取错误码
    boolean isPauseByUser(); // 是否用户手动暂停
```

### IController操作接口说明
#### 1. 任务操作相关
```java
    GlobalConfig getGlobalConfig() throws DownloadNoInitException ; // 获取全局配置
    void addTask(ITask... pTasks); // 添加任务
    void deleteTask(int... ids); // 删除任务，按配置删除文件
    void deleteTask(ITask... pTasks); // 删除任务，按配置删除文件
    void deleteTask(List<ITask> tasks); // 删除任务，按配置删除文件
    void deleteTaskAndAllFile(int... ids); // 删除任务和所有文件
    void deleteTaskAndAllFile(ITask... pTasks); // 删除任务和所有文件
    void deleteTaskAndAllFile(List<ITask> tasks); // 删除任务和所有文件
    void deleteTaskWithoutFile(int... ids); // 删除任务，不删除文件
    void deleteTaskWithoutFile(ITask... pTasks); // 删除任务，不删除文件
     void deleteTaskWithoutFile(List<ITask> tasks); // 删除任务，不删除文件
    int refreshData(ITask... pTasks); // 刷新任务状态, 返回成功刷新的数量
    ITask getTaskById(int id); // 查询任务
    ITask getTask(String url, String savePath); // 查询任务
    boolean cloneData(ITask task, ITask srcTask); // 克隆任务，监听事件不会克隆，其他参数属性全部一致 注意：监听事件不会从旧任务中继承
    ArrayList<ITask> getTaskByStatus(int status); // 查询任务
    ArrayList<ITask> getTask(); // 获取所有的下载任务
    ArrayList<ITask> getTaskByExtras(String[] keys, String[] values); // 查询任务 根据扩展字段
    void reloadTask(ITask... pTasks); // 重新下载 根据下载任务
    void pauseTask(ITask... pTasks); // 暂停下载 根据下载任务
    void pauseTaskForConnectedToMobile(ITask... pTasks); // 连接到移动网络时暂停下载
    void resumeTask(ITask... pTasks); // 恢复下载 根据下载任务
    int registerTaskListener(ITask... tasks); // 为指定的任务注册一个监听，一个任务可以注册多个监听，返回注册成功的数量
    int unregisterTaskListener(ITask... tasks); // 为指定的任务注销监听，返回注销成功的数量
    void unregisterTaskAllListener(ITask... tasks); // 注销指定任务的所有监听
    void unregisterTaskAllListener(int... ids); // 注销指定任务的所有监听
    boolean registerTaskListener(IDownloadListener listener); // 注册全局监听，可以监听所有下载任务，返回注册成功的数量
    boolean unregisterTaskListener(IDownloadListener listener); // 注销全局监听，返回注销成功的数量
```

#### 2. 网络相关操作接口
```java
	/**
	 * 动态设置可以使用的网络类型（权限）
	 * 网络类型包括：NETWORK_WIFI | NETWORK_MOBILE | NETWORK_BLUETOOTH
	 * @param networkTypes
	 * @param tasks
	 */
	void setNetworkTypes(int networkTypes, ITask... tasks);
	/**
	 * 动态设置可以使用的网络类型（权限）
	 * 网络类型包括：NETWORK_WIFI | NETWORK_MOBILE | NETWORK_BLUETOOTH
	 * @param networkTypes
	 * @param ids
	 */
	void setNetworkTypes(int networkTypes, int... ids);
```

#### 3. 带模块名参数相关操作接口
```java
	/**
	 * 查询指定模块的任务
	 * @param status 根据下载状态查询
	 * @param moduleName    模块名
	 * @return 相应状态的下载列表
	 */
	ArrayList<ITask> getTaskByStatus(int status, String moduleName);
	/**
	 * 获取指定模块的所有的下载任务
	 * @param moduleName    模块名
	 * @return 所有任务列表
	 */
	ArrayList<ITask> getTask(String moduleName);
	/**
	 * 查询指定模块的任务 根据扩展字段
	 * @param keys 键数组
	 * @param values 值数组
	 * @param moduleName    模块名
	 * @return 相应的任务列表
	 */
	ArrayList<ITask> getTaskByExtras(String[] keys, String[] values, String moduleName);
	/**
	 * 注册指定模块的全局监听，可以监听所有下载任务
	 * @param listener      监听
	 * @param moduleName 模块名
	 * @return 注册成功的数量
	 */
	boolean registerTaskListener(IDownloadListener listener, String moduleName);
	/**
	 * 注销指定模块的全局监听
	 * @param listener      监听
	 * @param moduleName 模块名
	 * @return 注销成功的数量
	 */
	boolean unregisterTaskListener(IDownloadListener listener, String moduleName);
```

## 公开全局变量说明
### 1. 状态 Status.java
```java
public interface Status {
    int DOWNLOAD_INVALID = -1; // 无效状态
    int DOWNLOAD_WAITING = 0; // 下载等待中
    int DOWNLOAD_STARTED = 1; // 下载已开始执行
    int DOWNLOAD_CONNECTED = 2; // 下载已连接
    int DOWNLOAD_PROGRESS = 3; // 下载中
    int DOWNLOAD_PAUSE = 4; // 下载暂停
    int DOWNLOAD_RETRY = 5; // 下载重试
    int DOWNLOAD_FAILURE = 6; // 下载失败
    int DOWNLOAD_SUCCESS = 7; // 下载成功

    int CHECK_STARTED = 10; // 校验初始化
    int CHECK_PROGRESS = 11; // 校验中
    int CHECK_FAILURE = 12; // 校验失败
    int CHECK_SUCCESS = 13; // 校验成功

    int UNPACK_STARTED = 20; // 解压开始
    int UNPACK_PROGRESS = 21; // 解压中
    int UNPACK_FAILURE = 22; // 解压失败
    int UNPACK_SUCCESS = 23; // 解压成功
}
```
> 下载整体流程
![demo效果图](http://172.28.2.93/bfc/BfcDownload/raw/808d152ad757c3b49d9fa9d17cebf25f74f891e1/doc/%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3/%E6%A6%82%E8%A6%81%E8%AE%BE%E8%AE%A1%E9%99%84%E4%BB%B6/%E4%B8%AD%E9%97%B4%E4%BB%B6%E4%B8%8B%E8%BD%BD%E6%95%B4%E4%BD%93%E5%A4%84%E7%90%86%E6%B5%81%E7%A8%8B%E5%9B%BE_%E5%8D%A2%E6%B5%AA%E5%B9%B3_V1.0.0_20160909.png)

### 2. 默认校验类型，也可自定义
```java
public interface CheckType {
        String NON = "NON"; // 不校验
        String MD5 = "MD5"; // MD5校验类型
        String HASH = "HASH"; // HASH校验类型
    }
```

### 3. 下载网络类型
```java
public interface NetworkType {
    int NETWORK_UNKNOWN = 0; // 未知网络
    int NETWORK_MOBILE = 1 << 0; // 移动网络（4G/3G/2G...）
    int NETWORK_WIFI = 1 << 1; // WIFI网络
    int NETWORK_BLUETOOTH = 1 << 2; // 蓝牙（暂时不支持）
    int DEFAULT_NETWORK = NETWORK_WIFI | NETWORK_BLUETOOTH; // 下载默认使用网络
}
```

## 可扩展功能说明
### 1.自定义文件校验
#### 1.1 继承BaseValidator.java类写一个自己的校验器
```java
/**
 * Desc: 自定义校验类
 * Author: llp
 * Create Time: 2016-11-07 22:28
 * Email: jacklulu29@gmail.com
 */

public class MyValidator extends BaseValidator {

    @Override
    public boolean checkFile(String fileSourcePath, String checkType, String checkCode)
            throws DownloadCheckException {
        File file = new File(fileSourcePath);
        if (!file.exists()) {
            throw new DownloadCheckException(ErrorCode.getUnpackErrorCode(1), " check source file["+fileSourcePath+"] not found ");
        }

        if (file.length() <= 0) {
            // 删除源文件
            FileUtil.deleteFile(fileSourcePath);
            throw new DownloadCheckException(ErrorCode.getUnpackErrorCode(2), " can't check file[length=" + file.length() +"], delete file[" + file + "]");
        }

        try{
            // 进度回调
            checkProgress(file.length(), 0);

            boolean result = true;
            // 校验文件代码写在这

            // 返回校验结果，true为通过，false不通过
            return result;
        } catch (Exception e){
            throw new DownloadCheckException(ErrorCode.getCheckErrorCode(2), " check error! ", e);
        }
    }

    public static class MyValidatorCreator implements BaseValidator.Creator {

        @Override
        public BaseValidator create() {
            return new MyValidator();
        }

        @Override
        public String getType() {
            // 返回校验器类型，随便自己定义
            return "MyValidatorType";
        }
    }
}
```
 
#### 1.2 下载初始化时加入校验构造器,下载服务启动时会自动生成校验器，文件校验时会根据任务的校验类型取对应的校验器
```java
 GlobalConfig config = new GlobalConfig.Builder()
        // 添加自定义校验器，可以添加多个
        .addValidator(new MyValidator.MyValidatorCreator());
        
// 初始化
DownloadController.init(this.getApplicationContext(), config);
```
 
### 2.自定义文件解压
#### 2.1 DownloadBaseUnpacker.java类写一个自己的解压器
```java
/**
 * Desc: 自定义解压器
 * Author: llp
 * Create Time: 2016-11-07 22:41
 * Email: jacklulu29@gmail.com
 */

public class MyUnpacker extends DownloadBaseUnpacker {

    @Override
    public boolean unpackByParseTargetPath(String sourceFilePath, String targetPath, boolean deleteSourceAfterUnpack)
            throws DownloadUnpackException {
        File file = new File(sourceFilePath);
        if (!file.exists()) {
            throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(1), " unpack source file["+sourceFilePath+"] not found ");
        }

        if (file.length() <= 0) {
            // 删除源文件
            FileUtil.deleteFile(sourceFilePath);
            throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(2), " can't unpack file[length=" + file.length() +"], delete file[" + file + "]");
        }

        try {
            // 回调进度
            unpackProgress(file.length(), 0);

            if (fileExtractor(sourceFilePath, targetPath)) {
                LogUtil.i("unpack success, auto delete source file[" + deleteSourceAfterUnpack + "]");
                if(deleteSourceAfterUnpack){
                    // 删除源文件
                    FileUtil.deleteFile(sourceFilePath);
                }
                return true;
            } else {
                LogUtil.e("unpack failed, delete unpack temp files");
                // 删除解压后的文件夹
                FileUtil.deleteFileAndDir(targetPath);
                return false;
            }

        } catch (Exception e) {
            LogUtil.e("unpack failed, delete unpack temp files");
            // 删除解压后的文件夹
            FileUtil.deleteFileAndDir(targetPath);
            throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(3), " unpack error ", e);
        }
    }

    private boolean fileExtractor(String sourceFilePath, String targetPath) throws DownloadUnpackException {
        // 解压代码写这里

        // 返回结果，解压成功返回true，失败返回false
        return true;
    }

    @Override
    public boolean isSupport(String filePath) {
        // 该文件是否支持解压
        return false;
    }

    public static class MyUnpackerCreator implements DownloadBaseUnpacker.Creator {

        @Override
        public DownloadBaseUnpacker create() {
            return new MyUnpacker();
        }
    }

}
```

#### 2.2 下载初始化时设置解压构造器,下载服务启动时会创建解压器，文件解压时会自动调用
```java
GlobalConfig config = new GlobalConfig.Builder()
      // 添加自定义校验器，可以添加多个
      .setUnpacker(new MyUnpacker.MyUnpackerCreator());
                
// 初始化
DownloadController.init(this.getApplicationContext(), config);
```
 

## 返回结果（回调）说明
#### 创建任务 -> 设置监听 -> 开始任务 -> 产生状态、进度、异常等信息 -> 监听回调
#### 操作任务 -> 产生操作结果 -> 监听回调结果

## 异常处理说明
下载中初始化异常将会直接抛出，重要异常信息通过封装定义错误码后通过监听传回，次要异常信息通过日志打印
 
#### 下载错误码说明
```
        03010001, 下载未知异常
        03010002, 用户手动暂停下载
        03010003, 其他原因暂停下载
        03010004, 将要运行时下载状态不为等待状态
        03010005, 权限清单没有添加网络权限
        03010006, 运行任务对象为空
        03010007, 网络没有连接，请检测网络是否畅通
        03010008, 由于文件超过大小不能使用网络
        03010009, 由于文件超过大小不建议使用网络
        03010010, 下载任务没有使用当前网络的权限，如需继续请增加相应网络使用权限
        03010011, 网络堵塞
        03010012, 缓存文件路径为空
        03010013, 缓存文件被文件夹占用
        03010014, 创建缓存文件错误
        03010015, 创建缓存文件IO异常
        03010016, 创建的缓存文件没有找到
        03010017, 存储空间不足
        03010018, 跳到指定文件指定位置时发生IO异常
        03010019, 跳到指定文件指定位置时参数错误
        03010020, 设置文件大小时参数错误
        03010021, 设置文件大小时发生IO异常
        03010022, 下载完成，但是下载文件真实大小不等于总大小
        03010023, 读写文件发生IO异常
        03010024, 删除目标文件错误
        03010025, 重命名文件错误
        03010026, 发生RemoteException错误
        03010027, 下载文件大小超过总大小
        03010028, 查询任务时cursor为空
        03010029, 网络不允许使用漫游
        03010030, 下载过程中数据请求超时
        03010031, 保存文件夹被文件占用
        03010032, 创建保存路径错误
        03010033, 重启服务恢复查询任务时cursor为空
        03010034, 文件名长度超过255
        03010035, 文件名称含有非法字符：/
        03010036, 连接异常
        03010037, 下载的缓存文件不存在，是否已经被移除
        03010038, 获取网络状态信息失败，Context为空
        03010039, 未知的网络状态错误类型
        03010040, 禁止在电信2g网络下进行下载
        03010041, 网络问题无法找到地址，don't find address
        03010042, 认证网络
        03010043, 网络问题无法解析域名，No route to host
        03010044, 下载地址连接过程中网络请求超时

        03020001, 校验未知异常
        03020002, 校验器没有找到
        03020003, 校验key无效
        03020004, 校验错误
        03020005, 校验运行时异常
        03020006, 校验失败

        03030001, 解压未知异常
        03030002, 解压器为空
        03030003, 不支持解压此文件
        03030004, 解压错误
        03030005, 解压运行时异常
        03030006, 解压源文件路径是空的

        03040001, 获取版本信息未知异常

        03050001, 其他未知异常
```

## 特殊情况
##### 1. 下载任务网络突然断开，任务暂停，过会网络又恢复的情况，任务需要自动恢复下载
> 解决方案：注册一个网络变化广播监听，网络断开时，任务自动暂停并缓存到等待网络的一个任务队列，网络恢复时，网络监听收到广播，然后轮询网络等待队列，满足条件的缓存任务恢复下载。

##### 2. 下载服务异常终止，服务重启后任务恢复情况
> 解决方案：服务重启后会从数据库加载所有未完成任务，并按数据库中的任务状态恢复任务。

##### 3. 下载服务异常终止，无法收到回调的情况
> 解决方案：服务被异常终止，监听并不会被回收掉，服务重启后即可收到监听回调。

##### 4. 单任务以及多任务都会设置和注册监听，监听会一直保存，可能存在内存泄露。
> 解决方案：不使用任务或监听时，单任务调用ITask.recycle()、多任务调用DownloadController.unregisterTaskListener(...)方法及时回收和注销。

##### 5. 应用内分多个模块，我不想多个模块之间的任务被其他模块查询以及监听到。
> 解决方案：添加任务时设置任务所属模块，使用时调用需要传模块参数的方法进行查询、监听等

##### 6. 任务暂停了，不知道具体什么原因
> 解决方案：任务被暂停后，ITask中的状态值会被改为暂停，同时错误码也会变化，此时错误码标识暂停原因。目前造成暂停的原因只有三种：
1.用户手动暂停，必须手动恢复下载
2.网络原因，网络正常后会自动恢复下载
3.存储空间不足造成暂停，清理后必须手动恢复下载
具体判断方法可以调用ITask.isPauseByUser()、ITask.isPauseByNetwork()、ITask.isPauseByOutOfSpace()

##### 7. 旧版数据在新版数据库中查询不到了
> 造成原因：旧版数据都存在共有的一个系统应用中，所有应用共享，新版数据库独立跟随使用的应用走，两个不是一个数据库。
> 解决方案：将旧版数据复制到新版下载数据库中。具体执行步骤：在http://172.28.2.93/bfc/BfcDownload项目中找到doc/新旧版本数据兼容/目录下的compatibility.rar
在自己的项目中引用解压的两个类，并按类中的说明进行使用，这两个类主要是将旧版数据中已经下载成功的记录复制到新版数据库中。

## 待处理问题
- 由于后台服务器暂时取消了文件校验，所以文件校验暂时不可用，后续会用起来
- 由于下载采用服务，很多操作可能是异步的，暂时无法直接返回操作结果
- 优化下载速度，现在数据库操作采用的是同步进制，后续会采用异步加快下载速度

## 源码保存地址
GitLab源码地址: http://172.28.2.93/bfc/BfcDownload.git

## 相关文档获取方式
可以从GitLab下载项目(http://172.28.2.93/bfc/BfcDownload.git)，项目根目录下有doc文件夹,保存有齐全的使用说明文档和项目设计文档。也可以在wiki上找到中间件栏目查看相关文档。

## 问题反馈
使用过程成遇到任何问题，有任何建议或者意见都可以使用下面这个地址反馈给我们。欢迎大家提出问题，我们将会在最短的时间内解决问题。
**地址：http://172.28.2.93/bfc/BfcDownload/issues**

# 最后
希望大家多多使用和提出宝贵意见，大家一起讨论进步，一起完善本库。
联系方式 ： 404822627@qq.com  RTX：  20253764
参与开发人员： 卢浪平

--------