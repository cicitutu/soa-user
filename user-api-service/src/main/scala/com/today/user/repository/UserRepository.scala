package com.today.user.repository

import java.util.Date
import javax.sql.DataSource

import com.today.api.user.request._
import com.today.api.user.response._
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.stereotype.Component
import wangzx.scala_commons.sql._
@Component
class UserRepository extends InitializingBean{

  @Autowired
  @Qualifier("dataSource")
  var dataSource: DataSource = _

  /**增加一条记录*/
  def insertUser(user:RegisterUserRequest)={
    dataSource.executeUpdate(
      sql"""INSERT INTO users(user_name,
                              password,
                              telephone,
                              integral,
                              created_at,
                              created_by,
                              updated_by)
                      VALUES (${user.userName},
                              ${user.passWord},
                              ${user.telephone},
                              0,
                              ${new Date},
                              1,
                              1)""")
  }
  /**查找手机号有没有存在*/
  def findTelephone(telephone:String)={
    val s = dataSource.row[Int](sql"""SELECT COUNT(*) FROM users WHERE telephone=${telephone}""")
    s.get
  }
  /**查找有没有此ID的用户*/
  def findId(id:String)={
    val u = dataSource.row[Int](sql"""SELECT COUNT(*) FROM users WHERE id=${id}""")
    u.get
  }
  /**根据手机号码和密码查询用户*/
  def findUserByTelephoneAndPassword(user:LoginUserRequest):LoginResponse={
    val l = dataSource.rows[LoginResponse](
      sql"""SELECT user_name as userName,
                   telephone,
                   status,
                   integral,
                   created_at as createdAt,
                   updated_at as updatedAt,
                   email,
                   qq
            FROM users
            WHERE telephone=${user.telephone}
            AND password=${user.passWord}""")
    l.head
  }
  /**根据用户ID查询用户*/
  def findUserById(user: ModifyUserRequest):ModifyResponse={
    val l = dataSource.rows[ModifyResponse](
      sql"""SELECT user_name as userName,
                               telephone,
                               status,
                               integral,
                               created_at as createdAt,
                               updated_at as updatedAt,
                               email,
                               qq
                               FROM users
                               WHERE id=${user.userId}""")
    l.head
  }
  /**修改用户的Email和qq*/
  def updateUserEmailAndQQ(user: ModifyUserRequest)={
    dataSource.executeUpdate(sql"""UPDATE users SET qq=${user.qq},email=${user.email} WHERE id=${user.userId}""")
  }
  /**根据用户ID查询可以被冻结的用户*/
  def findUnFreezeUserById(user:FreezeUserRequest):FreezeResponse={
    val l = dataSource.rows[FreezeResponse](sql"""SELECT id as userId,status,remark FROM users WHERE id=${user.userId}""")
    l.head
  }
  /**根据输入的ID修改用户状态为冻结状态*/
  def updateUserStatusFreeze(user:FreezeUserRequest)={
    dataSource.executeUpdate(sql"""UPDATE users SET status = 3 , remark = ${user.remark} where id = ${user.userId}""")
  }
  /**根据用户ID查询可以被拉黑的用户*/
  def findUnBlackUserById(user:BlackUserRequest):BlackResponse={
    val l = dataSource.rows[BlackResponse](sql"""SELECT id as userId,status,remark FROM users WHERE id=${user.userId}""")
    l.head
  }
  /**根据输入的ID修改用户状态为拉黑状态*/
  def updateUserStatusBlack(user:BlackUserRequest)={
    dataSource.executeUpdate(sql"""UPDATE users SET status = 4 , remark = ${user.remark} where id = ${user.userId}""")
  }
  /**根据用户ID查询用户状态*/
  def findStatusById(id:String)={
    dataSource.row[Int](sql"""SELECT status FROM users WHERE id=${id}""")
  }
  /**根据用户ID查询用户integral*/
  def findIntegralById(id:String)={
    dataSource.row[Int](sql"""SELECT integral FROM users WHERE id=${id}""")
  }
  def updateIntegralById(id:String,integral:Int)={
    dataSource.executeUpdate(sql"""UPDATE users SET integral=${integral} WHERE id=${id}""")
  }
//  /**根据ID查询请求流水的用户*/
//  def findChangeIntegralUserById(user:ChangeIntegralRequest):ChangeIntegralResponse={
//    val l = dataSource.rows[ChangeIntegralResponse](sql"""SELECT * FROM users WHERE id=${user.userId}""")
//    l.head
//  }
  /**根据输入的数据更新integral_journal表*/
  def insertIntegral_journal(user:ChangeIntegralRequest,integral:Int)={
    dataSource.executeUpdate(
      sql"""INSERT INTO integral_journal(user_id,
                                         integral_type,
                                         integral_price,
                                         integral_source,
                                         integral,
                                         created_at,
                                         created_by,
                                         updated_at,
                                         updated_by,
                                         remark)
                          VALUES (${user.userId},
                                  ${user.integralType.id},
                                  ${user.integralPrice},
                                  ${user.integralSource.id},
                                  ${integral},
                                  now(),
                                  1,
                                  now(),
                                  0,
                                  '')""")
  }
//  /**根据Id更新Integral*/
//  def updateIntegral(integeral:String)={
//
//  }
  /**测试是否初始化了*/
  override def afterPropertiesSet(): Unit = {
    println("我已经初始话了")
  }
}
