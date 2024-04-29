package org.tkit.onecx.help.domain.criteria;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class HelpSearchCriteria {

    private String itemId;

    private String context;

    private String baseUrl;

    private String resourceUrl;

    private String productName;

    private Integer pageNumber;

    private Integer pageSize;

}
