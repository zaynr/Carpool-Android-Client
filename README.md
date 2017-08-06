# 恒生拼车系统安卓客户端
此客户端可供拼车系统的司机和乘客分别进行使用。由于只有不到一个月的时间进行开发， WEB 端还有许多 Android 端未用的预留接口，可供后续添加功能的使用。

## 登录账号
司机以手机号作为登录的账号，单个手机号只能用来注册一个对应的司机用户。

乘客以公司工号作为登录账号，单个公司工号只能用来注册一个乘客用户。

在数据库内已经预留了测试账号，乘客与司机的账号密码均为 “123”。

## 司机状态更新
在司机登录入安卓端后，手机会在联网的前提下对数据库内的司机实时位置进行更新，并在接立即单后更改司机的接单状态，不会继续向司机分发立即单的数据。

## 订单状态更新
乘客在下单后，会通过安卓端向服务器发送订单数据，司机需要手动接单，可在未接订单里进行选择接单。

完成订单后，司机、乘客可对双方进行双向评价。乘客端的评价未完善。司机可在完成行程后进行评价。

## 好友
由于时间关系，乘客端未添加好友验证模块，添加好友不需要对方同意即可完成。

司机端的常客列表由服务次数进行降序排列。

## 乘客拼车
根据需求，乘客在下达订单之后，可以在自己的订单列表看到和自己的路程相似的乘客的订单，选择是否和他们拼车。
