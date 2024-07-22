package org.tkit.onecx.help.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@NamedEntityGraph(name = Help.HELP_FULL, includeAllAttributes = true)
@Table(name = "HELP", uniqueConstraints = {
        @UniqueConstraint(name = "HELP_TENANT_ID", columnNames = { "RESOURCE_REF_ID", "TENANT_ID" })
})
@SuppressWarnings("java:S2160")
public class Help extends TraceableEntity {

    public static final String HELP_FULL = "help_full";

    @Column(name = "RESOURCE_REF_ID", insertable = false, updatable = false)
    private String resourceRefId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RESOURCE_REF_ID", foreignKey = @ForeignKey(name = "HELP_RESOURCE_REF_CONSTRAINTS"))
    private ProductResource productResource;

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "CONTEXT")
    private String context;

    @Column(name = "BASE_URL")
    private String baseUrl;

    @Column(name = "RESOURCE_URL")
    private String resourceUrl;

    /**
     * Flag to identify permissions created by an operator
     */
    @Column(name = "OPERATOR")
    private Boolean operator;

    @PostPersist
    void postPersist() {
        resourceRefId = productResource.getId();
    }
}
