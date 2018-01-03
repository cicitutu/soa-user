package com.today.api.user.response
import java.util.Date

case class LoginResponse( /**
                            *
                            * *
                            * 用户名称
                            *
                            **/

                          userName: String,

                          /**
                            *
                            * *
                            * 电话号码
                            *
                            **/

                          telephone: String,

                          /**
                            *
                            * *
                            * 用户状态
                            *
                            **/

                          status:Int,

                          /**
                            *
                            * *
                            * 用户 积分
                            *
                            **/

                          integral: Int,

                          /**
                            *
                            * *
                            * 注册时间
                            *
                            **/

                          createdAt: Date,

                          /**
                            *
                            * *
                            * 更新时间
                            *
                            **/

                          updatedAt: Date,

                          /**
                            *
                            * *
                            * 用户邮箱
                            *
                            **/

                          email: Option[String] = None,

                          /**
                            *
                            * *
                            * 用户 qq
                            *
                            **/

                          qq: Option[String] = None)
