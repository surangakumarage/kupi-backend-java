package com.kupi.util.service.sms;

import com.kupi.util.domain.request.SmsRequestDto;
import com.kupi.util.domain.response.SendSmsResponseDto;
import com.kupi.util.exception.UtilServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Component
@Slf4j
public class SnsSmsGateway implements SmsGateway {

    private final SnsClient sns;
    private final String senderId;
    private final String smsType;

    public SnsSmsGateway(
            @Value("${sms.sns.region}") String region,
            @Value("${sms.sns.senderId}") String senderId,
            @Value("${sms.sns.smsType}") String smsType,
            @Value("${sms.sns.accessKeyId}") String accessKeyId,
            @Value("${sms.sns.secretAccessKey}") String secretKey,
            @Value("${sms.sns.sessionToken:}") String sessionToken) {

        AwsCredentialsProvider creds =
                (sessionToken == null || sessionToken.isBlank())
                        ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretKey))
                        : StaticCredentialsProvider.create(AwsSessionCredentials.create(accessKeyId, secretKey, sessionToken));

        this.sns = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(creds)
                .build();

        this.senderId = senderId;
        this.smsType = smsType;
    }

    @Override
    public SendSmsResponseDto sendSms(SmsRequestDto dto) {
        log.info("Preparing to send SMS via SNS. Recipient={}, MessageLength={}, SenderID={}, SMSType={}",
                dto.getRecipientNo().getNo(),
                dto.getMessage() != null ? dto.getMessage().length() : 0,
                senderId,
                smsType);

        try {
            var attrs = Map.of(
                    "AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                            .dataType("String").stringValue(senderId).build(),
                    "AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                            .dataType("String").stringValue(smsType).build()
            );

            log.debug("SNS PublishRequest attributes={}", attrs);

            var resp = sns.publish(PublishRequest.builder()
                    .phoneNumber(dto.getRecipientNo().getNo()) // must be E.164 (+94...)
                    .message(dto.getMessage())
                    .messageAttributes(attrs)
                    .build());

            log.info("SNS publish successful. MessageId={}, Recipient={}",
                    resp.messageId(), dto.getRecipientNo().getNo());

            var out = new SendSmsResponseDto();
            out.setRequest(resp.messageId());
            out.setStatus("SUCCESS");
            out.setDate(OffsetDateTime.now(ZoneOffset.UTC).toString());
            return out;
        } catch (Exception e) {
            log.error("Send SMS failed for Recipient={} with message='{}'",
                    dto.getRecipientNo().getNo(), dto.getMessage(), e);
            throw new UtilServiceException("Send SMS failed (SNS).", e);
        }
    }
}

