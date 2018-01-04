package com.today.api.user.response

import java.util.Date

case class ModifyResponse (/**
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

                           status: Int,

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
