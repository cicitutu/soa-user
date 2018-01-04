package com.today.api.user.response

case class BlackResponse (
                           /**
                             *

                           用户 id

                             **/

                           userId : String, /**
                             *

                           用户状态

                             **/

                           status : Int, /**
                             *

                           操作员冻结备注

                             **/

                           remark : String
                          )
