package com.today.api.user.response

/**
         * Autogenerated by Dapeng-Code-Generator (1.2.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated

        *

 注册用户返回体

        **/
        case class RegisterUserResponse(

         /**
        *

 用户名称

        **/
        
        userName : String, /**
        *

 电话号码

        **/
        
        telephone : String, /**
        *

 用户状态

        **/
        
        status : com.today.api.user.enums.UserStatusEnum, /**
        *

 注册时间

        **/
        
        createdAt : Long
        )
      