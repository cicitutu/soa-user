package com.today.user

import java.util.Date

import com.today.api.user.enums.UserStatusEnum
import com.today.api.user.request._
import com.today.api.user.response._
import com.today.api.user.service.UserService
import com.today.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

object UserServiceImpl {
  val IS_TELEPHONE = """1(([3,5,8]\d{9})|(4[5,7]\d{8})|(7[0,6-8]\d{8}))""".r
  val IS_PASSWORD = """^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$""".r
  val IS_EMAIL = """^(\w)+(\.\w+)*@(\w)+((\.\w{2,3}){1,3})$""".r
  val IS_QQ = """\d{4,12}""".r
}

@Component
class UserServiceImpl extends UserService {

  import UserServiceImpl._

  @Autowired
  var userRepository: UserRepository = _

  /**
    *
    * *
    * ### 用户注册
    * *
    * #### 业务描述
    * 用户注册账户，用户密码需要加盐之后存储(加盐方案还么确定,小伙伴可以自己随意设计个简单的加解密方案)
    * *
    * #### 接口依赖
    * 无
    * #### 边界异常说明
    * 无
    * *
    * #### 输入
    *1.user_request.RegisterUserRequest
    * *
    * #### 前置检查
    *1. 手机号码规则验证
    *2. 手机号未被使用验证
    *3. 密码规则,字母数字八位混合
    * *
    * ####  逻辑处理
    *1.密码加盐处理
    *2.新增一条user记录
    *3.返回结果 user_response.RegisterUserResponse
    * *
    * #### 数据库变更
    *1. insert into user() values()
    * *
    * ####  事务处理
    * 无
    * *
    * ####  输出
    *1.user_response.RegisterUserResponse
    *
    **/
  override def registerUser(request: RegisterUserRequest): RegisterUserResponse = {
    if (preCheckRegisterUser(request)) {
      userRepository.insertUser(RegisterUserRequest(request.userName,request.passWord,request.telephone))
      RegisterUserResponse(request.userName, request.telephone, UserStatusEnum.ACTIVATED, new Date().getTime)
    }
    else null
  }

  def preCheckRegisterUser(request: RegisterUserRequest): Boolean = {
    val userName = request.userName
    val passWord = request.passWord
    val telephone = request.telephone

    //手机号码规则验证
    IS_TELEPHONE.findFirstIn(telephone) match {
      case None => throw new RuntimeException("号码输入不合法")
      case _ => true
    }
    //手机号有没有被使用验证
    if (userRepository.findTelephone(telephone) >= 1)
      throw new RuntimeException("号码被使用")
    //密码规则验证
    IS_PASSWORD.findFirstIn(passWord) match {
      case None => throw new RuntimeException("密码输入不正确")
      case _ => true
    }
    if (userName == null) throw new RuntimeException("名字必须填")

    true
  }

  /**
    *
    * *
    * ### 用户登录
    * *
    * #### 业务描述
    * 用户登录
    * *
    * #### 接口依赖
    * 无
    * #### 边界异常说明
    * 无
    * *
    * #### 输入
    *1.user_request.LoginUserRequest
    * *
    * #### 前置检查
    *1.手机号码规则验证
    *2.密码规则,字母数字八位混合
    * *
    * ####  逻辑处理
    *1. 根据手机号码和密码查询用户记录
    *2. 异常用户状态的用户登录返回 Exception
    * *
    * #### 数据库变更
    *1. select *  from user where telphone = ? and password = ?
    * *
    * ####  事务处理
    * 无
    * *
    * ####  输出
    *1.user_response.LoginUserResponse
    *
    **/
  override def login(request: LoginUserRequest)={
    if(preCheckLogin(request)){
      val user = userRepository.findUserByTelephoneAndPassword(request)
      println(user.status)
      if(user.status==1||user.status==2){
        LoginUserResponse(user.userName,user.telephone,UserStatusEnum(user.status),user.integral,user.createdAt.getTime,user.updatedAt.getTime,user.email,user.qq)
      }else{
        throw new RuntimeException("您的账号有问题，请联系管理员")
      }
    }else{
      throw new RuntimeException("账号或者密码错误")
    }
  }

  def preCheckLogin(request: LoginUserRequest) = {
    val passWord = request.passWord
    val telephone = request.telephone
    //手机号码规则验证
    IS_TELEPHONE.findFirstIn(telephone) match {
      case None => throw new RuntimeException("号码输入不合法")
      case _ => true
    }
    //手机号有没有被使用验证
    if (userRepository.findTelephone(telephone) >= 1)
      throw new RuntimeException("号码被使用")
    //密码规则验证
    IS_PASSWORD.findFirstIn(passWord) match {
      case None => throw new RuntimeException("密码输入不正确")
      case _ => true
    }
    true
  }
  /**
    *
    * *
    * ### 用户修改个人资料
    * *
    * #### 业务描述
    * 用户再注册之后完善个人资料,完善资料增加积分5
    * *
    * #### 接口依赖
    * 无
    * #### 边界异常说明
    * 无
    * *
    * #### 输入
    *1.user_request.ModifyUserRequest
    * *
    * #### 前置检查
    *1. 邮箱规则验证
    *2. qq 规则验证
    *3. 用户状态判断：只有用户状态为1或2才能验证
    * *
    * ####  逻辑处理
    *1. 根据输入的参数计算用户积分
    *2. 修改用户 email qq
    *2. 修改完成之后调用积分action增加用户积分(完善资料增加积分5) ChangeUserIntegralAction
    * *
    * #### 数据库变更
    *1. update user set email = ? , qq = ? where id = ${userId}
    * *
    * ####  事务处理
    *1. 无
    * *
    * ####  输出
    *1.user_response.ModifyUserAction
    *
    **/
  override def modifyUser(request: ModifyUserRequest): ModifyUserResponse = {
    if(preCheckModifyUser(request)){
      val user = userRepository.findUserById(request)
      userRepository.updateUserEmailAndQQ(ModifyUserRequest(request.userId,request.email,request.qq))
      ModifyUserResponse(user.userName,user.telephone,UserStatusEnum(user.status),user.updatedAt.getTime)
    }
    else null
  }

  def preCheckModifyUser(request:ModifyUserRequest)={
    IS_EMAIL.findFirstIn(request.email) match {
      case None => throw new RuntimeException("邮箱输入错误")
      case _ => true
    }
    IS_QQ.findFirstIn(request.qq) match {
      case None => throw new RuntimeException("QQ输入不合法")
      case _ => true
    }
    val st = userRepository.findUserById(request).status
    st match {
      case 1 => true
      case 2 => true
      case _ => throw new RuntimeException("您的账号存在问题，请联系管理员")
    }
    true
  }
  /**
    *
    * *
    * ### 冻结用户接口
    * *
    * #### 业务描述
    * 用户因为触犯一些游戏规则,后台自检程序或者管理员会冻结该用户
    * *
    * #### 接口依赖
    * 无
    * #### 边界异常说明
    * 无
    * *
    * #### 输入
    *1.user_request.FreezeUserRequest
    * *
    * #### 前置检查
    *1.用户状态检查(已冻结,已拉黑,已逻辑删除的用户不能冻结)
    * *
    * ####  逻辑处理
    *1. 设置用户状态为 FREEZE
    * *
    * #### 数据库变更
    *1. update user set status = ? , remark = ? where id = ${userId}
    * *
    * ####  事务处理
    *1. 无
    * *
    * ####  输出
    *1.user_response.FreezeUserResponse
    *
    **/
  override def freezeUser(request: FreezeUserRequest): FreezeUserResponse = {
    if(preCheckFreezeUser(request)){
      val user = userRepository.findUnFreezeUserById(request)
      userRepository.updateUserStatusFreeze(FreezeUserRequest(request.userId,request.remark))
      FreezeUserResponse(user.userId,UserStatusEnum(user.status),user.remark)
    }
    else null
  }
  def preCheckFreezeUser(request: FreezeUserRequest)={
    if(userRepository.findId(request.userId)==0){
      throw new RuntimeException("没有这个ID的账号")
    }
    val st = userRepository.findUnFreezeUserById(request).status
    st match {
      case 1 => true
      case 2 => true
      case _ => throw new RuntimeException("已冻结,已拉黑,已逻辑删除的用户不能冻结")
    }
  }

  /**
    *
    * *
    * ### 拉黑用户接口
    * *
    * #### 业务描述
    * 用户因为触犯一些游戏规则,后台自检程序或者管理员会拉黑该用户,拉黑用户把用户的积分置为0
    * *
    * #### 接口依赖
    * 无
    * #### 边界异常说明
    * 无
    * *
    * #### 输入
    *1.user_request.BlackUserRequest
    * *
    * #### 前置检查
    *1.用户状态检查(已冻结,已拉黑,已逻辑删除的用户不能拉黑)
    * *
    * ####  逻辑处理
    *1. 设置用户状态为  BLACK
    *2. 调用积分修改接口 ChangeUserIntegralAction
    * *
    * #### 数据库变更
    *1. update user set status = ? , remark = ? where id = ${userId}
    * *
    * ####  事务处理
    *1. 无
    * *
    * ####  输出
    *1.user_response.BlackUserResponse
    *
    **/
  override def blackUser(request: BlackUserRequest): BlackUserResponse = {
    if(preCheckBlackUser(request)){
      val user = userRepository.findUnBlackUserById(request)
      userRepository.updateUserStatusBlack(BlackUserRequest(request.userId,request.remark))
      BlackUserResponse(user.userId,UserStatusEnum(user.status),user.remark)
    }
    else null
  }
  def preCheckBlackUser(request: BlackUserRequest)={
    if(userRepository.findId(request.userId)==0){
      throw new RuntimeException("没有这个ID的账号")
    }
    val st = userRepository.findUnBlackUserById(request).status
    st match {
      case 1 => true
      case 2 => true
      case _ => throw new RuntimeException("已冻结,已拉黑,已逻辑删除的用户不能拉黑")
    }
  }

  /**
    *
    * *
    * ### 记录积分改变流水
    * *
    * #### 业务描述
    * 用户因为完成一些游戏规则或者触犯游戏规则导致积分减少或者增加,调用该接口修改用户积分
    * *
    * #### 接口依赖
    * 无
    * #### 边界异常说明
    * 无
    * *
    * #### 输入
    *1.user_request.ChangeIntegralRequest
    * *
    * #### 前置检查
    *1.用户状态检查(已冻结,已拉黑,已逻辑删除的用户不能冻结)
    * *
    * ####  逻辑处理
    *1. 设置用户状态为 FREEZE
    * *
    * #### 数据库变更
    *1. update user set integral = ?  where id = ${userId}
    *2. insert into integral_journal() values()
    * *
    * ####  事务处理
    *1. 无
    * *
    * ####  输出
    *1. i32 流水 Id
    *
    **/
  override def changeUserIntegral(request: ChangeIntegralRequest): Int = {
    if(preCheckStatus(request.userId)){
      val integral = userRepository.findIntegralById(request.userId).get
      val totalIntegral = request.integralPrice.toInt + integral
      userRepository.updateIntegralById(request.userId,totalIntegral)
      userRepository.insertIntegral_journal(request,integral)
    } else throw new RuntimeException("系统错误")
  }
  def preCheckStatus(userId:String)={
    if(userRepository.findId(userId)==0){
      throw new RuntimeException("没有这个ID的账号")
    }
    val status = userRepository.findStatusById(userId).getOrElse(-1)
    status match {
      case 1 => true
      case 2 => true
      case _ => throw new RuntimeException("非法用户")
    }
  }

}
