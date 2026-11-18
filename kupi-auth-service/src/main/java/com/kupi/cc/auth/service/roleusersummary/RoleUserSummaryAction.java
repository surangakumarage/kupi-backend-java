package com.kupi.cc.auth.service.roleusersummary;


import com.kupi.cc.auth.configuration.Translator;
import com.kupi.cc.auth.domain.entity.User;
import org.springframework.data.domain.Page;

/**
 * This is used for Transaction summary actions.
 */
public interface RoleUserSummaryAction {

    /**
     * This method returns page of users based on the user summary action.
     *
     * @return page of users
     */
    Page<User> getUsers();

    /**
     * This method returns display label based on the user summary action.
     *
     * @return display label
     */
    String getDisplayLabel(Translator translator);

}
