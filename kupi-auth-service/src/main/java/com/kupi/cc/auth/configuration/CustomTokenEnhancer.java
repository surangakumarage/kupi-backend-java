package com.kupi.cc.auth.configuration;

import com.kupi.cc.auth.domain.Data;
import com.kupi.cc.auth.domain.ResourcePermission;
import com.kupi.cc.auth.domain.entity.Resource;
import com.kupi.cc.auth.domain.entity.User;
import com.kupi.cc.auth.enums.RoleType;
import com.kupi.cc.auth.exception.AuthServiceException;
import com.kupi.cc.auth.repository.ResourceRepository;
import com.kupi.cc.auth.service.MerchantService;
import com.kupi.cc.auth.util.ListUtils;
import com.kupi.cc.auth.wrapper.PermissionsWrapper;
import com.kupi.cc.auth.wrapper.RoleResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Binding additional properties to the token
 * Note: Uses legacy spring-security-oauth2 library for Spring Boot 3.2.6 compatibility
 */

@Slf4j
public class CustomTokenEnhancer implements TokenEnhancer {

    private static final String SUCCESS_MESSAGE = "Successfully logged-in the user";
    private static final String ERROR_TOKEN = "Error retrieving resources";
    private final ResourceRepository resourceRepository;
    private final MerchantService merchantService;

    @Autowired
    public CustomTokenEnhancer(ResourceRepository resourceRepository, MerchantService merchantService) {
        this.resourceRepository = resourceRepository;
        this.merchantService = merchantService;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken,
                                     OAuth2Authentication oAuth2Authentication) {
        User user = (User) oAuth2Authentication.getPrincipal();
        Data data = new Data();
        final Map<String, Object> additionalInfo = new HashMap<>();
        data.setFullName(user.getFullName());
        data.setImageUrl(user.getImageUrl());
        data.setLanguage(user.getLanguage());
        data.setUserId(user.getId());
        data.setApprovalStatus(user.getApprovalStatus());
        data.setActive(true);
        if (user.getRole().getId() == RoleType.MERCHANT.getId() || user.getRole().getId() == RoleType.BANK.getId()) {
            data.setActive(merchantService.isMerchantOrBankActive(user.getId()));
            data.setBusinessProfile(
                    merchantService.getMerchantOrBankLoginResponse(user.getId(), user.getRole().getName()));
        }
        setRolePermissions(data, user);
        additionalInfo.put("status", "SUCCESS");
        additionalInfo.put("message", SUCCESS_MESSAGE);
        additionalInfo.put("data", data);
        ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(additionalInfo);
        return oAuth2AccessToken;
    }

    /**
     * Set the role and permissions to login response
     *
     * @param data data
     * @param user user
     */
    private void setRolePermissions(Data data, User user) {
        if (user.getRole() != null) {
            List<Resource> allResources = getAllResource();
            RoleResponseWrapper roleResponseWrapper = new RoleResponseWrapper();
            roleResponseWrapper.setId(user.getRole().getId());
            roleResponseWrapper.setName(user.getRole().getName());
            PermissionsWrapper permissionsWrapper = new PermissionsWrapper(user.getRole().getPermissions());
            permissionsWrapper.attachResources(allResources);
            List<ResourcePermission> collect = permissionsWrapper
                    .getResourcePermissions()
                    .stream().filter(ListUtils.distinctByKey(ResourcePermission::getResourceId))
                    .collect(Collectors.toList());
            roleResponseWrapper.setPermissions(collect);
            data.setRole(roleResponseWrapper);
        }
    }

    /**
     * Get all resources
     *
     * @return list of resources
     */
    private List<Resource> getAllResource() {
        try {
            return resourceRepository.findAll();
        } catch (DataAccessException e) {
            log.error(ERROR_TOKEN, e);
            throw new AuthServiceException(ERROR_TOKEN);
        }
    }
}
