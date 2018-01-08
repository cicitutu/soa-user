package com.today.user

import java.util.Date

import com.isuwang.dapeng.core.{SoaBaseCode, SoaException}
import com.today.api.user.enums.{IntegralSourceEnum, IntegralTypeEnum, UserStatusEnum}
import com.today.api.user.request._
import com.today.api.user.response._
import com.today.api.user.service.UserService
import com.today.user.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

object UserServiceImpl {
  val IS_TELEPHONE = "^1(3[0-9]|4[57]|5[0-35-9]|7[01678]|8[0-9])\\d{8}$".r
  val IS_PASSWORD = """^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,16}$""".r
  val IS_EMAIL = """^(\w)+(\.\w+)*@(\w)+((\.\w{2,3}){1,3})$""".r
  val IS_QQ = """\d{4,12}""".r
  val Integral = 10
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
    //检查
    preCheck(request)

    //注册
    userRepository.insertUser(RegisterUserRequest(request.userName,request.passWord,request.telephone))

    //响应
    RegisterUserResponse(request.userName, request.telephone, UserStatusEnum.ACTIVATED, new Date().getTime)
  }

  /**前置检查*/
  def preCheck(register: RegisterUserRequest)={
    //判断名字是否存在
    assert(userRepository.findName(register.userName).getOrElse(0) == 0,"你输入的名字已经存在")

    //密码规则验证
    assert(IS_PASSWORD.findFirstIn(register.passWord).nonEmpty,"密码输入不合法")

    //手机号有没有被使用验证
    assert(userRepository.findTelephone(register.telephone).getOrElse(1) <=0  , "号码被使用")

    //手机号码规则验证
    assert(IS_TELEPHONE.findFirstIn(register.telephone).nonEmpty,"号码输入不合法")

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
    //前置检查
    preCheckLogin(request)

    //查找用户
    val user = userRepository.findUserByTelephoneAndPassword(request)

    assert(user.isDefined , "手机或者密码不正确")

    //响应请求
    user.get.status match {
      case 1|2 => LoginUserResponse(user.get.userName,
                                    user.get.telephone,
                                    UserStatusEnum(user.get.status),
                                    user.get.integral,
                                    user.get.createdAt.getTime,
                                    user.get.updatedAt.getTime,
                                    user.get.email,
                                    user.get.qq)
      case _ => throw new SoaException(SoaBaseCode.NotNull,"你的账号有问题，联系管理员")
    }

  }

  def preCheckLogin(login: LoginUserRequest) = {

    //密码规则验证
    assert(IS_PASSWORD.findFirstIn(login.passWord).nonEmpty,"密码输入不合法")

    //手机号码规则验证
    assert(IS_TELEPHONE.findFirstIn(login.telephone).nonEmpty,"号码输入不合法")

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
    //前置检查
    preCheckModifyUser(request)
    assert(userRepository.findUserById(request).nonEmpty,throw new SoaException(SoaBaseCode.UnKnown,"输入错误，ID不存在"))

    //跟新信息
    val user = userRepository.findUserById(request)
      user.get.status match {
      case 1|2 => userRepository.updateUserEmailAndQQ(ModifyUserRequest(request.userId,request.email,request.qq))
      case _ => throw new SoaException(SoaBaseCode.NotNull,"你的账号有问题，联系管理员")
    }

    //跟新积分
    changeUserIntegral(ChangeIntegralRequest(userId = request.userId,
                       integralPrice = Integral.toString,
                       integralType = IntegralTypeEnum.ADD,
                       integralSource =IntegralSourceEnum.PREFECT_INFORMATION))

    //相应请求
    ModifyUserResponse(user.get.userName,user.get.telephone,UserStatusEnum(user.get.status),
      user.get.updatedAt.getTime,user.get.email,user.get.qq)

  }

  def preCheckModifyUser(request:ModifyUserRequest)={

    assert(IS_EMAIL.findFirstIn(request.email).nonEmpty, "邮箱输入错误")

    assert(IS_QQ.findFirstIn(request.qq).nonEmpty,"QQ输入不合法" )

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
    //前置检查
    preCheckFreezeUser(request)

    //跟新信息
    val user = userRepository.findUnFreezeUserById(request).head
    user.status match {
      case 1|2 => userRepository.updateUserStatusFreeze(FreezeUserRequest(request.userId,request.remark))
      case _ => throw new SoaException(SoaBaseCode.UnKnown,"已冻结,已拉黑,已逻辑删除的用户不能冻结")
    }

    //响应请求
    FreezeUserResponse(user.userId,UserStatusEnum(user.status),user.remark)

  }
  def preCheckFreezeUser(request: FreezeUserRequest)={

    assert(userRepository.findId(request.userId).nonEmpty,throw new SoaException(SoaBaseCode.UnKnown,"输入错误，ID不存在"))

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
    //前置检查
    preCheckBlackUser(request)

    //跟新信息
    val user = userRepository.findUnBlackUserById(request).head
    user.status match {
      case 1|2 => userRepository.updateUserStatusBlack(BlackUserRequest(request.userId,request.remark))
      case _ => throw new SoaException(SoaBaseCode.UnKnown,"已冻结,已拉黑,已逻辑删除的用户不能冻结")
    }

    //响应请求
    BlackUserResponse(user.userId,UserStatusEnum(user.status),user.remark)

  }
  def preCheckBlackUser(request: BlackUserRequest)={

    assert(userRepository.findId(request.userId).nonEmpty,throw new SoaException(SoaBaseCode.UnKnown,"输入错误，ID不存在"))

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
    //前置检查
    preCheckStatus(request.userId)

    //跟新信息
    userRepository.findStatusById(request.userId).get match {
      case 1|2 => val integral = userRepository.findIntegralById(request.userId).get
                  val totalIntegral = request.integralPrice.toInt + integral
                  userRepository.updateIntegralById(request.userId,totalIntegral)
                  userRepository.insertIntegral_journal(request,integral)
      case _ => throw new SoaException(SoaBaseCode.UnKnown,"非法用户")
    }

  }
  def preCheckStatus(userId:String)={

    assert(userRepository.findId(userId).nonEmpty,throw new SoaException(SoaBaseCode.UnKnown,"输入错误，ID不存在"))

  }

}
