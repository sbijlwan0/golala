package com.easygo.service;

import com.easygo.domain.Organisation;
import com.easygo.domain.User;
import com.easygo.config.ConstApp;

import io.github.jhipster.config.JHipsterProperties;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

/**
 * Service for sending emails.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String USER = "user";

    private static final String BASE_URL = ConstApp.getFilePathDB();
    
    public static final String SMTP_HOST_NAME = "smtp.gmail.com";
	public static final String SMTP_PORT = "587";
	public static final String TO = "";
	public static final String TO_GETINTOUCH = "";
	public static final String FROM = "sbijlwan0@gmail.com";
	public static final String USERNAME = "levelroooms@gmail.com";
	public static final String PASSWORD = "lroomsnew";

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;

    public MailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender,
            MessageSource messageSource, SpringTemplateEngine templateEngine) {

        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Email could not be sent to user '{}'", to, e);
            } else {
                log.warn("Email could not be sent to user '{}': {}", to, e.getMessage());
            }
        }
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        Locale locale = Locale.forLanguageTag(user.getLangKey());
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);

    }

    @Async
    public void sendActivationEmail(User user) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationEmail", "email.activation.title");
    }

    @Async
    public void sendCreationEmail(User user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/creationEmail", "email.activation.title");
    }

    @Async
    public void sendPasswordResetMail(User user) {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "email.reset.title");
    }
    
    
    
    @Async
    public void userOTPMail(User user,String otp) {
		String subject = "EasyGo : WELCOMES YOU";
		String name="User";
		if(null!=user.getFirstName())
			name=user.getFirstName();
		String htmlText = "<tr>\n" + " <td style=\"padding:0 10px;\">\n"
				+ " <p>Dear "+ name+",</p>\n"
				+ " <p>Welcome to EasyGo.</p>"
				+ " <p>Following is the OTP for you to connect with EasyGo<br><h3>"+otp+"</h3></p>"
				+"<p>Regards,<br>EasyGo</p>";
		 sendMail(user.getEmail(), subject, htmlText);
		
	}
    
    
    @Async 
    public void sendOrganisationActivationMail(Organisation organisation,User user) {
    	
    	String subject = "EasyGo : "+organisation.getBusinessName()+" is activated";
    	String name="User";
		if(null!=user.getFirstName())
			name=user.getFirstName();
    	String htmlText = "<tr>\n" + " <td style=\"padding:0 10px;\">\n"
    			+ " <p>Dear "+ name+",</p>\n"
				+ " <p>Welcome to EasyGo.</p>"
				+ " <p>Your Organisation with name <h3>"+organisation.getBusinessName()+"</h3> has been activated.</p><br>"
				+ " <p>Congratulations! You can add products now.</p><br>"
				+"<p>Regards,<br>EasyGo</p>";
		 sendMail(user.getEmail(), subject, htmlText);
    }
    
    
    @Async
    public int sendMail(String mailTo, String subject,
			String htmlText) {
		Message message;

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", SMTP_HOST_NAME);
		props.put("mail.smtp.port", SMTP_PORT);
		props.put("mail.user", USERNAME);
		props.put("mail.password", PASSWORD);
		
		props.put("mail.smtp.ssl.trust", SMTP_HOST_NAME);
//		props.put("mail.smtp.socketFactory.port", SMTP_PORT);
//		props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
//		props.put("mail.smtp.socketFactory.fallback", "false");

		props.put("mail.debug", "true");

		// Get the Session object.
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(USERNAME,
								PASSWORD);
					}
				});

		try {
			// Create a default MimeMessage object.
			message = new MimeMessage(session);
			MimeMultipart multipart = new MimeMultipart("related");
			BodyPart messageBodyPart = new MimeBodyPart();

			// Set From: header field of the header.
			try {
				message.setFrom(new InternetAddress(FROM,"EasyGo"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}

			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(mailTo));

			// Set Subject: header field
			message.setSubject(subject);

			// Adding content to BodyPart Object
			messageBodyPart.setContent(htmlText, "text/html");

			// Adding the BodyPart to the MultiPart Object
			multipart.addBodyPart(messageBodyPart);

			message.setContent(multipart, "text/html");
			// Send the final Message
			Transport.send(message);
			return 1;
		} catch (MessagingException e) {
			e.printStackTrace();
			return 0;
			//Logging.error("Error in Mailer.sendmail :: ", e);

		}

	}
}
