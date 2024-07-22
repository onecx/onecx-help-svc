package org.tkit.onecx.help.rs.internal.mappers;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.onecx.help.domain.models.ProductResource;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.help.rs.internal.model.CreateProductResourceDTO;
import gen.org.tkit.onecx.help.rs.internal.model.ProductResourceDTO;
import gen.org.tkit.onecx.help.rs.internal.model.ProductResourceItemDTO;
import gen.org.tkit.onecx.help.rs.internal.model.ProductResourceItemsDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductResourceMapper {

    ProductResourceDTO map(ProductResource pr);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "operator", constant = "false")
    ProductResource create(CreateProductResourceDTO dto);

    default ProductResourceItemsDTO map(Stream<ProductResource> stream) {
        return new ProductResourceItemsDTO().stream(createItems(stream));
    }

    default List<ProductResourceItemDTO> createItems(Stream<ProductResource> stream) {
        var tmp = stream.collect(groupingBy(ProductResource::getProductName));

        List<ProductResourceItemDTO> result = new ArrayList<>();
        tmp.forEach((k, v) -> {
            var items = v.stream().map(ProductResource::getItemId).toList();
            result.add(new ProductResourceItemDTO().productName(k).itemIds(items));
        });
        return result;
    }

}
