package com.kupi.util.service.whatsapp;

import com.kupi.util.domain.request.MobileNoRequestDto;
import com.kupi.util.domain.response.WhatsAppAvailabilityResponseDto;
import com.kupi.util.domain.response.WhatsAppCheckResponse;
import com.kupi.util.exception.UtilServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WhatsAppApiClient {

    private static final String SEPARATOR = "/";
    private final RestTemplate restTemplate;
    private final String token;
    private final String phoneNumberId;
    private final String contactsUrl;
    private final String messagesUrl;
    private final String templateName;
    private final String templateLanguageCode;

    public WhatsAppApiClient(
            RestTemplate restTemplate,
            @Value("${whatsapp.token}") String token,
            @Value("${whatsapp.phoneNumberId}") String phoneNumberId,
            @Value("${whatsapp.apiVersion}") String apiVersion,
            @Value("${whatsapp.baseUrl}") String baseUrl,
            @Value("${whatsapp.endpoints.contactsEndpoint}") String contactsEndpoint,
            @Value("${whatsapp.endpoints.messagesEndpoint}") String messagesEndpoint,
            @Value("${whatsapp.template.name}") String templateName,
            @Value("${whatsapp.template.languageCode}") String templateLanguageCode

    ) {
        this.restTemplate = restTemplate;
        this.token = token;
        this.phoneNumberId = phoneNumberId;
        this.contactsUrl = baseUrl + apiVersion + SEPARATOR + phoneNumberId + contactsEndpoint;
        this.messagesUrl = baseUrl + apiVersion + SEPARATOR + phoneNumberId + messagesEndpoint;
        this.templateName = templateName;
        this.templateLanguageCode = templateLanguageCode;
    }


    /**
     * Checks whether the given mobile number is registered on WhatsApp by calling
     * the WhatsApp Cloud API {@code /contacts} endpoint.
     * <p>
     * This method formats the number, sends it to WhatsApp for verification,
     * and processes the API response to determine whether the number is valid
     * on WhatsApp. If the API call encounters an HTTP error or an unexpected
     * exception, a {@link UtilServiceException} is thrown.
     *
     * @param mobileNoRequestDto the DTO containing the country code and local number
     *                           used to build the full E.164 phone number
     * @return a {@link WhatsAppAvailabilityResponseDto} containing the evaluation result:
     * whether the number is valid on WhatsApp and the formatted number
     * @throws UtilServiceException if the verification request fails or the API
     *                              returns an unexpected error
     */
    public WhatsAppAvailabilityResponseDto isNumberOnWhatsApp(MobileNoRequestDto mobileNoRequestDto) {

        // Remove the "+" prefix from phone number if present (WhatsApp API expects format without +)
        String phoneNumber = mobileNoRequestDto.getNo();
        String formattedPhoneNumber = phoneNumber != null && phoneNumber.startsWith("+") 
                ? phoneNumber.substring(1) 
                : phoneNumber;

        Map<String, Object> body = Map.of(
                "blocking", "wait",
                "contacts", List.of(formattedPhoneNumber)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.debug("Checking WhatsApp availability for number: {} using URL: {}", formattedPhoneNumber, contactsUrl);
        
        try {
            WhatsAppCheckResponse response = restTemplate.postForObject(
                    contactsUrl,
                    entity,
                    WhatsAppCheckResponse.class
            );

            boolean isOnWhatsApp = response != null && response.getContacts() != null &&
                    !response.getContacts().isEmpty() &&
                    "valid".equalsIgnoreCase(response.getContacts().get(0).getStatus());

            log.debug("WhatsApp availability check result for {}: {}", formattedPhoneNumber, isOnWhatsApp);
            
            // Return the original number format (with +) for consistency
            return new WhatsAppAvailabilityResponseDto(isOnWhatsApp, mobileNoRequestDto.getNo());

        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("WhatsApp availability check failed (HTTP) for number {}. URL: {}, Status: {}, Response: {}",
                    phoneNumber, contactsUrl, e.getStatusCode(), responseBody, e);
            
            // Check if it's a permissions/object not found error
            if (responseBody != null && responseBody.contains("does not exist") && responseBody.contains("missing permissions")) {
                log.error("The phoneNumberId '{}' may not have permissions for the /contacts endpoint. " +
                        "Please verify the phoneNumberId has the 'whatsapp_business_messaging' permission in your Facebook App settings.",
                        phoneNumberId);
                throw new UtilServiceException(
                        "WhatsApp availability check failed: phoneNumberId does not have permissions for contacts endpoint. " +
                        "Please verify your WhatsApp Business Account configuration.", e);
            }
            
            throw new UtilServiceException("WhatsApp availability check failed (HTTP error).", e);
        } catch (Exception e) {
            log.error("Unexpected error while checking WhatsApp availability for number {}", phoneNumber, e);
            throw new UtilServiceException("WhatsApp availability check failed due to an unexpected error.", e);
        }
    }


    /**
     * Sanitizes the message text to comply with WhatsApp template parameter requirements.
     * WhatsApp template parameters cannot have:
     * - New-line characters (\n, \r)
     * - Tab characters (\t)
     * - More than 4 consecutive spaces
     *
     * @param message the original message text
     * @return the sanitized message text
     */
    private String sanitizeMessage(String message) {
        if (message == null) {
            return "";
        }
        // Remove all newlines and carriage returns (replace with space)
        String sanitized = message.replaceAll("[\\r\\n]+", " ");
        // Remove all tabs (replace with space)
        sanitized = sanitized.replaceAll("\\t+", " ");
        // Replace sequences of 5 or more consecutive spaces with a single space
        // (WhatsApp allows up to 4 consecutive spaces, so using 1 space is safe)
        sanitized = sanitized.replaceAll(" {5,}", " ");
        // Trim leading/trailing whitespace
        return sanitized.trim();
    }

    /**
     * Sends a WhatsApp text message to the specified phone number using the
     * WhatsApp Cloud API. This method builds the required payload and submits
     * a POST request to the WhatsApp Messages endpoint.
     *
     * @param phoneNumber the destination phone number in E.164 format (e.g. +94771234567)
     * @param message     the text (for example an OTP) that will be injected into the template body
     * @throws UtilServiceException if the WhatsApp API request fails or a network/HTTP error occurs
     */
    public void sendMessage(String phoneNumber, String message) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        String formattedPhoneNumber = phoneNumber.startsWith("+") ? phoneNumber.substring(1) : phoneNumber;
        
        // Sanitize the message to comply with WhatsApp template parameter requirements
        // This removes newlines, tabs, and limits consecutive spaces (required for WhatsApp templates)
        String sanitizedMessage = sanitizeMessage(message);
        log.debug("WhatsApp message sanitization - Original: '{}', Sanitized: '{}'", 
                message, sanitizedMessage);

        Map<String, Object> language = Map.of(
                "code", templateLanguageCode
        );

        Map<String, Object> bodyParameter = Map.of(
                "type", "text",
                "text", sanitizedMessage
        );

        Map<String, Object> buttonParameter = Map.of(
                "type", "text",
                "text", sanitizedMessage
        );

        Map<String, Object> bodyComponent = Map.of(
                "type", "body",
                "parameters", List.of(bodyParameter)
        );

        Map<String, Object> buttonComponent = Map.of(
                "type", "button",
                "sub_type", "url",
                "index", "0",
                "parameters", List.of(buttonParameter)
        );

        Map<String, Object> template = Map.of(
                "name", templateName,
                "language", language,
                "components", List.of(bodyComponent, buttonComponent)
        );

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", formattedPhoneNumber,
                "type", "template",
                "template", template
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForObject(messagesUrl, entity, String.class);
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("WhatsApp message send failed (HTTP) for number {}. Status: {}, Response body: {}",
                    phoneNumber, e.getStatusCode(), responseBody, e);
            throw new UtilServiceException("WhatsApp message send failed (HTTP)", e);

        } catch (Exception e) {
            log.error("Unexpected error while sending WhatsApp message to {}", phoneNumber, e);
            throw new UtilServiceException("WhatsApp message send failed", e);
        }
    }

}
