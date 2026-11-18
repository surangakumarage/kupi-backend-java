package com.kupi.cc.auth.wrapper;

import com.kupi.cc.auth.domain.ResourcePermission;
import com.kupi.cc.auth.domain.response.ResponseDto;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseWrapper implements ResponseDto, Serializable {

    private int id;
    private String name;
    private List<ResourcePermission> permissions;

    @Override
    public String toLogJson() {
        return toJson();
    }

}


