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

## 演示图

<table>
    <tr>
        <td><img src="https://github.com/user-attachments/assets/455fdf09-8efc-4801-884c-81fe63e2ecaf"/></td>
        <td><img src="https://github.com/user-attachments/assets/211f2037-759e-4af1-936a-6b14041f3eff"/></td>
    </tr>
    <tr>
        <td><img src="https://github.com/user-attachments/assets/55252505-afdb-4370-8fdc-667e9b933577"/></td>
        <td><img src="https://github.com/user-attachments/assets/a2aa3ae7-63e3-45bf-8812-3e46f857ee43"/></td>
    </tr>
    <tr>
        <td><img src="https://github.com/user-attachments/assets/a6afb379-a699-44a2-8076-eac09c7131be"/></td>
        <td><img src="https://github.com/user-attachments/assets/b5bd69ce-9c66-46e5-b2bc-59031dcbab9b"/></td>
    </tr>
	  <tr>
        <td><img src="https://github.com/user-attachments/assets/1e860a02-fddb-4f00-9a50-d469689e7362"/></td>
    </tr>
</table>