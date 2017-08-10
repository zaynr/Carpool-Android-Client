# 恒生拼车系统安卓客户端技术架构

## 推送
该 APP 引用了“宿主”式推送机制，若有使用同类推送服务的手机 APP 则会从那个 APP 的后台线程消息队列中获取被推送的信息，并进行消息推送，从而达到降低功耗、提高推送成功率的目的。

## 地图
使用了百度地图的 SDK。

## 算路
算路模块引用了百度地图的路径提示，并收集可能的路径点，根据 dij 图的最短路算法，进行路线选取，连接点并绘制路线。导航路径则将算路结果的路径点推入导航 activity，发起导航。

## 数据库
采用 MySql 建立数据库，后台服务未利用 redis。

# 恒生拼车系统安卓客户端使用指南
此客户端可供拼车系统的司机和乘客分别进行使用。由于只有不到一个月的时间进行开发， WEB 端还有许多 Android 端未用的预留接口，可供后续添加功能的使用。


### 侧边栏
<img src="http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205638.jpg" alt="Drawing" style="width: 100px;"/>

侧边栏提供了账户中心、查看订单、查看好友和分享的按钮。

### 登录账号
司机以手机号作为登录的账号，单个手机号只能用来注册一个对应的司机用户。

乘客以公司工号作为登录账号，单个公司工号只能用来注册一个乘客用户。

在数据库内已经预留了测试账号，乘客与司机的账号密码均为 “123”。

### 好友
由于时间关系，乘客端未添加好友验证模块，添加好友不需要对方同意即可完成。乘客的添加好友点击右下角按钮即可。

司机端的常客列表由服务次数进行降序排列。

### 个人数据更新
<img src="http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205645.jpg" alt="Drawing" style="width: 100px;"/>
<img src="http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205639.jpg" alt="Drawing" style="width: 100px;"/>

可进入“账户中心”对个人基本数据进行更新，同时提供密码的更新。

## 乘客方面操作
### 查看订单
<img src="http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205652.jpg" alt="Drawing" style="width: 100px;"/>

乘客角色查看订单，订单界面有三种颜色：
1. 绿色，待接单
2. 灰色，已完成
3. 淡蓝色，正在进行的订单

<img src="http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205635.jpg" alt="Drawing" style="width: 100px;"/>

乘客角色查看订单，如果是已经下了“立即叫车”单，只能查看未完成，并等待司机接单。

### 下订单
<img src="http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205641.jpg" alt="Drawing" style="width: 100px;"/>
<img src="http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205646.jpg" alt="Drawing" style="width: 100px;"/>

点击主界面右下角按钮即可进入下单界面，起点为目前位置，终点必须输入后进行手选，提供搜索路线功能，并可以选择下单类型。如果下的是“立即”单，会进入等待界面，并显示所有其他的也在等待的订单，点击选择拼车（未完成）。

### 司机已经接单
![](http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205633.jpg)
会从等待接单页面跳转进入司机详情页面，并展示司机位置，在地图显示导航详情和当前位置。

![](http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205648.jpg)

完成订单后（司机确认或已经到达目的地），可以对司机进行评分。

## 司机方面操作
<img src="http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205650.jpg" alt="Drawing" style="width: 100px;"/>

在司机登录入安卓端后，手机会在联网的前提下对数据库内的司机实时位置进行更新，并在接立即单后更改司机的接单状态，不会继续向司机分发立即单的数据。司机可以点击右下角的按钮进入接单模式。

### 司机接单
司机可点击主界面右下角进行接单。

### 订单状态更新
![](http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205654.jpg)

乘客在下单后，会通过安卓端向服务器发送订单数据，司机需要手动接单，可在未接订单里进行选择接单。若订单是“立即”类型，点击确定后司机会进入导航界面.

![](http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205615.jpg)
![](http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205626.jpg)

导航界面的“1”标志即为乘客的位置（我美术不好，图标画的丑，只能用这个了）。

![](http://oud04ioid.bkt.clouddn.com/image/carpool/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20170810205636.jpg)

若司机选择退出导航，则视为完成订单，进入对乘客评分界面。


完成订单后，司机、乘客可对双方进行双向评价。

### 乘客拼车
根据需求，乘客在下达订单之后，可以在自己的订单列表看到和自己的路程相似的乘客的订单，选择是否和他们拼车。
