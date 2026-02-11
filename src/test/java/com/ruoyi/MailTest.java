package com.ruoyi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import cn.hutool.extra.mail.MailUtil;

@SpringBootTest
public class MailTest {

	@Test // 简单邮件
	public void sendSimpleEmail() {
		MailUtil.send("251064029@qq.com", "测试", "邮件来自Hutool测试", false);

		//ArrayList<String> tos = CollUtil.newArrayList("person1@bbb.com", "person2@bbb.com", "person3@bbb.com", "person4@bbb.com");

		//MailUtil.send(tos, "测试", "邮件来自Hutool群发测试", false);
	}
}
