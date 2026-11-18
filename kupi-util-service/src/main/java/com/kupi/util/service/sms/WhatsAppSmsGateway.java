package com.kupi.util.service.sms;

import com.kupi.util.domain.request.SmsRequestDto;
import com.kupi.util.domain.response.SendSmsResponseDto;
import com.kupi.util.exception.UtilServiceException;
import com.kupi.util.service.whatsapp.WhatsAppApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class WhatsAppSmsGateway implements SmsGateway {

    private final WhatsAppApiClient whatsAppApiClient;
    
    // Pattern to extract OTP/token from messages like:
    // - "Please use this OTP 123456 to complete..."
    // - "Please use the token: 511167 to complete..."
    // Matches 4-8 digit numbers that appear after "OTP " or "token:" (case insensitive)
    // Pattern: (OTP|token)\s*:?\s*(\d{4,8}) - matches "OTP 123456", "token: 511167", "Token: 123456", etc.
    private static final Pattern OTP_PATTERN = Pattern.compile("(?i)(?:OTP|token)\\s*:?\\s*(\\d{4,8})\\b");
    
    // WhatsApp template parameter length limit
    private static final int WHATSAPP_PARAM_MAX_LENGTH = 15;

    public WhatsAppSmsGateway(WhatsAppApiClient whatsAppApiClient) {
        this.whatsAppApiClient = whatsAppApiClient;
    }

    /**
     * Extracts the OTP/token value from the message text.
     * For WhatsApp templates, we only need the numeric value, not the full message.
     * WhatsApp template parameters have a maximum length of 15 characters.
     * 
     * @param message the full message text (e.g., "Please use this OTP 123456 to complete..." or "Please use the token: 511167 to complete...")
     * @return the extracted OTP/token value, or the original message if pattern cannot be extracted (truncated to 15 chars if needed)
     */
    private String extractOtpFromMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return message;
        }
        
        Matcher matcher = OTP_PATTERN.matcher(message);
        if (matcher.find()) {
            String otp = matcher.group(1);
            log.debug("Extracted OTP/token from message: {}", otp);
            
            // Validate length (WhatsApp limit is 15 characters)
            if (otp.length() > WHATSAPP_PARAM_MAX_LENGTH) {
                log.warn("Extracted OTP/token length ({}) exceeds WhatsApp limit ({}), truncating", 
                        otp.length(), WHATSAPP_PARAM_MAX_LENGTH);
                otp = otp.substring(0, WHATSAPP_PARAM_MAX_LENGTH);
            }
            return otp;
        }
        
        // If OTP/token pattern not found, try to extract any numeric value as fallback
        // This handles edge cases where format is slightly different
        Pattern fallbackPattern = Pattern.compile("\\b(\\d{4,8})\\b");
        Matcher fallbackMatcher = fallbackPattern.matcher(message);
        if (fallbackMatcher.find()) {
            String fallbackOtp = fallbackMatcher.group(1);
            log.debug("Extracted numeric value using fallback pattern: {}", fallbackOtp);
            if (fallbackOtp.length() > WHATSAPP_PARAM_MAX_LENGTH) {
                fallbackOtp = fallbackOtp.substring(0, WHATSAPP_PARAM_MAX_LENGTH);
            }
            return fallbackOtp;
        }
        
        // If no pattern found, truncate original message to WhatsApp limit
        log.warn("OTP/token pattern not found in message, using truncated original message (max {} chars)", 
                WHATSAPP_PARAM_MAX_LENGTH);
        String truncated = message.length() > WHATSAPP_PARAM_MAX_LENGTH 
                ? message.substring(0, WHATSAPP_PARAM_MAX_LENGTH) 
                : message;
        return truncated;
    }

    @Override
    public SendSmsResponseDto sendSms(SmsRequestDto smsRequestDto) {
        try {
            String phone = smsRequestDto.getRecipientNo().getNo();
            String message = smsRequestDto.getMessage();
            
            // Extract OTP from message for WhatsApp template (WhatsApp template already has the message structure)
            String otpValue = extractOtpFromMessage(message);
            
            log.error("=== WHATSAPP DEBUG: Sending WhatsApp message to: {}, original message length: {}, extracted OTP: {} ===", 
                    phone, message != null ? message.length() : 0, otpValue);
            whatsAppApiClient.sendMessage(phone, otpValue);
            log.error("=== WHATSAPP DEBUG: Successfully sent WhatsApp message to: {} ===", phone);

            var out = new SendSmsResponseDto();
            out.setStatus("SUCCESS");
            out.setRequest("WHATSAPP");
            return out;

        } catch (Exception e) {
            throw new UtilServiceException("Failed to send WhatsApp message", e);
        }
    }
}
