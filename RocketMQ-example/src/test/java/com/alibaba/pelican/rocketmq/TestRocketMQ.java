package com.alibaba.pelican.rocketmq;

import com.alibaba.pelican.deployment.junit.AbstractJUnit4PelicanTests;
import com.alibaba.pelican.deployment.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author moyun@middleware
 */

@Slf4j
public class TestRocketMQ extends AbstractJUnit4PelicanTests {

	private String ip;

	{
		Map<String, String> params = this.getTestProject().getVariables();
		ip = params.get("ip");
	}

	@Test
	public void test() throws Exception {

		DefaultMQProducer producer = new
				DefaultMQProducer("please_rename_unique_group_name");
		producer.setNamesrvAddr(ip + ":9876");
		producer.start();
		for (int i = 0; i < 100; i++) {
			Message msg = new Message("TopicTest" /* Topic */,
					"TagA" /* Tag */,
					("Hello RocketMQ " +
							i).getBytes(RemotingHelper.DEFAULT_CHARSET) /* Message body */
			);
			SendResult sendResult = producer.send(msg);
			System.out.printf("%s%n", sendResult);
		}
		producer.shutdown();

		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please_rename_unique_group_name");

		consumer.setNamesrvAddr(ip + ":9876");

		consumer.subscribe("TopicTest", "*");

		consumer.registerMessageListener(new MessageListenerConcurrently() {

			public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
															ConsumeConcurrentlyContext context) {
				System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			}
		});

		consumer.start();

		CommonUtils.waitForSeconds(30);
	}

}
