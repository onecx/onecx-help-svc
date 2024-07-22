package org.tkit.onecx.help.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "PRODUCT_RESOURCE", uniqueConstraints = {
        @UniqueConstraint(name = "PRODUCT_RESOURCE_ID", columnNames = { "ITEM_ID", "PRODUCT_NAME" })
})
@SuppressWarnings("java:S2160")
public class ProductResource extends TraceableEntity {

    @Column(name = "ITEM_ID")
    private String itemId;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    /**
     * Flag to identify permissions created by an operator
     */
    @Column(name = "OPERATOR")
    private Boolean operator;

}
