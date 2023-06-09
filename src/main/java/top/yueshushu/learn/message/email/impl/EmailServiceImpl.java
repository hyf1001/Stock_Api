package top.yueshushu.learn.message.email.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import top.yueshushu.learn.enumtype.message.VelocityTemplateType;
import top.yueshushu.learn.message.email.EmailService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

/**
 * 邮件发送信息
 *
 * @author yuejianli
 * @date 2022-06-09
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
	@Value("${spring.mail.username}")
	private String from;
	@Value("${spring.velocity.FILE_RESOURCE_LOADER_PATH}")
	private String fileResourceLoaderPath;
	
	private VelocityEngine velocityEngine;
	
	@Resource
	private JavaMailSender javaMailSender;
	
	@PostConstruct
	public void initVelocityEngine() {
		velocityEngine = new VelocityEngine();
		Properties p = new Properties();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		velocityEngine.init(p);
	}
	
	@Override
	public boolean sendSimpleMail(String[] toArr, String subject, String content) {
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
		simpleMailMessage.setTo(toArr);
		simpleMailMessage.setSubject(subject);
		simpleMailMessage.setText(content);
		simpleMailMessage.setFrom(from);
		//发送信息
		try {
			javaMailSender.send(simpleMailMessage);
			return true;
		} catch (Exception e) {
			log.error("componentAndSendReqeust simple mail error", e);
			return false;
		}
	}
	
	@Override
	public boolean sendHtmlMail(String[] toArr, String subject, String content) {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setTo(toArr);
			mimeMessageHelper.setSubject(subject);
			mimeMessageHelper.setText(content, true);
			mimeMessageHelper.setFrom(from);
			//发送信息
			javaMailSender.send(mimeMessage);
			return true;
		} catch (MessagingException e) {
            log.error("componentAndSendReqeust mail error ", e);
			return false;
		}
	}
	
	@Override
    public boolean sendVelocityMail(String[] toArr, String subject, VelocityTemplateType velocityTemplateType, Map<String, Object> dataMap) {
        try {
            String velocityMailText = getVelocityMailText(velocityTemplateType, dataMap);
            // log.info(">>>得到信息:{}", velocityMailText);
            return sendHtmlMail(toArr, subject, velocityMailText);
        } catch (Exception ex) {
            log.error(">>>componentAndSendReqeust email type {}  is error,", velocityTemplateType.getCode(), ex);
            return false;
        }
    }

    @Override
    public String getVelocityContent(VelocityTemplateType velocityTemplateType, Map<String, Object> dataMap) {
        return getVelocityMailText(velocityTemplateType, dataMap);
    }

    private String getVelocityMailText(VelocityTemplateType velocityTemplateType, Map<String, Object> dataMap) {
        VelocityContext velocityContext = new VelocityContext(dataMap);
        StringWriter writer = new StringWriter();
        String templateLocation = "stock_" + velocityTemplateType.getCode() + ".vm";
        velocityEngine.mergeTemplate(templateLocation, "UTF-8", velocityContext, writer);
        return writer.toString();
    }
}
