package com.kupi.util.service.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kupi.util.domain.request.SmsRequestDto;
import com.kupi.util.domain.response.SendSmsResponseDto;
import com.kupi.util.exception.UtilServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class HttpSmsGateway implements SmsGateway {

    private static final String TO = "&to=";
    private static final String TEXT = "&text=";

    private final RestTemplate restTemplate;
    private final ObjectMapper om = new ObjectMapper();
    private final String url;

    public HttpSmsGateway(@Value("${sms.baseUrl}") String baseUrl,
                          @Value("${sms.uri}") String uri,
                          RestTemplate restTemplate) {
        this.url = baseUrl + uri;
        this.restTemplate = restTemplate;
    }

    @Override
    public SendSmsResponseDto sendSms(SmsRequestDto dto) {
        try {
            String endpoint = url + TO + dto.getRecipientNo().getNo() + TEXT + dto.getMessage();
            var res = restTemplate.postForEntity(endpoint, null, String.class);
            return om.readValue(res.getBody(), SendSmsResponseDto.class);
        } catch (HttpClientErrorException e) {
            throw new UtilServiceException("Send SMS failed (HTTP).", e);
        } catch (Exception e) {
            throw new UtilServiceException("Failed to send/parse HTTP SMS response.", e);
        }
    }
}
