package com.today.user.repository

import java.util.Date
import javax.sql.DataSource

import com.today.api.user.request.{LoginUserRequest, RegisterUserRequest}
import com.today.api.user.response.{LoginResponse, LoginUserResponse}
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
  def insertUser(users:RegisterUserRequest)={
    dataSource.executeUpdate(sql"""INSERT INTO users(user_name,password,telephone,integral,created_at,created_by,updated_by) VALUES (${users.userName},${users.passWord},${users.telephone},0,${new Date},1,1)""")
  }
  /**查找手机号有没有存在*/
  def findTelephone(telephone:String)={
    val s = dataSource.row[Int](sql"""SELECT COUNT(*) FROM users WHERE telephone=${telephone}""")
    s.get
  }
  /**根据手机号码和密码查询用户*/
  def findUserByTelephoneAndPassword(users:LoginUserRequest):LoginResponse={
    val l = dataSource.rows[LoginResponse](sql"""SELECT user_name as userName,telephone,status,integral,created_at as createdAt,updated_at as updatedAt,email,qq FROM users WHERE telephone=${users.telephone} AND password=${users.passWord}""")
    l.head
  }
  override def afterPropertiesSet(): Unit = {
    println("我已经初始话了")
  }
}
