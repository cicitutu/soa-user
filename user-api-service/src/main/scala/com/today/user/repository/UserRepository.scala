package com.today.user.repository

import javax.sql.DataSource

import com.today.api.user.request._
import com.today.api.user.response._
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import wangzx.scala_commons.sql._

class UserRepository extends InitializingBean{

  @Autowired
  var dataSource: DataSource = _

  /**增加一条记录*/
  def insertUser(user:RegisterUserRequest)={
    dataSource.executeUpdate(
      sql"""INSERT INTO users
            SET user_name = ${user.userName},
                password = ${user.passWord},
                telephone = ${user.telephone},
                integral = 0,
                created_at = now(),
                created_by = 1,
                updated_by = 1""")
  }
  /**查找名字是否再数据库中存在*/
  def findName(name:String) = {
    dataSource.row[Int](sql"""SELECT COUNT(*) FROM users WHERE user_name=${name}""")
  }
  /**查找手机号有没有存在*/
  def findTelephone(telephone:String) ={
    dataSource.row[Int](sql"""SELECT COUNT(*) FROM users WHERE telephone=${telephone}""")
  }
  /**查找有没有此ID的用户*/
  def findId(id:String)={
    dataSource.row[Int](sql"""SELECT COUNT(*) FROM users WHERE id=${id}""")
  }
  /**根据手机号码和密码查询用户*/
  def findUserByTelephoneAndPassword(user:LoginUserRequest)={
    dataSource.row[LoginResponse](
      sql"""SELECT *
            FROM users
            WHERE telephone=${user.telephone}
            AND password=${user.passWord}""")
  }

  /**根据用户ID查询用户*/
  def findUserById(user: ModifyUserRequest)={
    dataSource.row[ModifyResponse](
      sql"""SELECT *
            FROM users
            WHERE id=${user.userId}""")
  }
  /**修改用户的Email和qq*/
  def updateUserEmailAndQQ(user: ModifyUserRequest)={
    dataSource.executeUpdate(sql"""UPDATE users SET qq=${user.qq},email=${user.email} WHERE id=${user.userId}""")
  }
  /**根据用户ID查询可以被冻结的用户*/
  def findUnFreezeUserById(user:FreezeUserRequest):List[FreezeResponse]={
    dataSource.rows[FreezeResponse](sql"""SELECT id as userId,status,remark FROM users WHERE id=${user.userId}""")
  }
  /**根据输入的ID修改用户状态为冻结状态*/
  def updateUserStatusFreeze(user:FreezeUserRequest)={
    dataSource.executeUpdate(sql"""UPDATE users SET status = 3 , remark = ${user.remark} where id = ${user.userId}""")
  }
  /**根据用户ID查询可以被拉黑的用户*/
  def findUnBlackUserById(user:BlackUserRequest):List[BlackResponse]={
    dataSource.rows[BlackResponse](sql"""SELECT id as userId,status,remark FROM users WHERE id=${user.userId}""")
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
      sql"""INSERT INTO integral_journal
            SET user_id = ${user.userId},
                integral_type = ${user.integralType.id},
                integral_price = ${user.integralPrice},
                integral_source = ${user.integralSource.id},
                integral = ${integral},
                created_at = now(),
                created_by = 1,
                updated_at = now(),
                updated_by = 0,
                remark = ''""")
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
