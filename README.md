## 简介
这是一个方便小厂鼠鼠程序员单人感恩而二次开发的基础oa项目，方便开发方便部署  
后续应该会跟着ry更新吧（tag写着：更新到 xxx，对应就是单应用项目git提交的hash前几位）  

## 项目基于 ruoyi单应用项目 二次开发得来
[原项目](https://gitee.com/y_project/RuoYi) [单应用项目](https://github.com/yangzongzhuan/RuoYi-fast)  

## 我修改的
1.  增加打印pid的工具类，因为我用着windows运行ry.bat经常关闭不了进程，打印出来方便任务管理器关闭
2.  增加hutool；用他的雪花算法生成id（！如果要使用，生成代码时，业务id要选成string）；发邮件
3.  增加简单文件上传模块，带通用界面。比较low，要用对象储存的自己改吧
5.  引入camunda工作流，有个请假流程的例子。
6.  增加角色和能够发起的工作流的关系
