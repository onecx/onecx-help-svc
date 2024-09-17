package org.tkit.onecx.help.domain.criteria;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class HelpCurrentSearchCriteria {

    private String itemId;

    private String productName;

}
