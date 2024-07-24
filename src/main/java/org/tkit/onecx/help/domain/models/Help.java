package org.tkit.onecx.help.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "HELP", uniqueConstraints = {
        @UniqueConstraint(name = "HELP_ITEM_ID", columnNames = { "ITEM_ID", "PRODUCT_NAME", "TENANT_ID" })
})
@SuppressWarnings("java:S2160")
public class Help extends TraceableEntity {

    @Column(name = "ITEM_ID")
    private String itemId;

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "CONTEXT")
    private String context;

    @Column(name = "BASE_URL")
    private String baseUrl;

    @Column(name = "RESOURCE_URL")
    private String resourceUrl;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "OPERATOR")
    private Boolean operator;
}
