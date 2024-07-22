package org.tkit.onecx.help.domain.daos;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import org.tkit.onecx.help.domain.models.ProductResource;
import org.tkit.onecx.help.domain.models.ProductResource_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;

@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class ProductResourceDAO extends AbstractDAO<ProductResource> {

    public ProductResource findByProductNameAndItemId(String productName, String itemId) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(ProductResource.class);
            var root = cq.from(ProductResource.class);
            cq.where(cb.and(
                    cb.equal(root.get(ProductResource_.productName), productName),
                    cb.equal(root.get(ProductResource_.itemId), itemId)));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException ne) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_GET_BY_PRODUCT_NAME_AND_ITEM_ID, ex);
        }
    }

    public List<String> findAllProductNames() {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<ProductResource> root = cq.from(ProductResource.class);
            cq.select(root.get(ProductResource_.PRODUCT_NAME)).distinct(true);
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_ALL_PRODUCT_NAMES, ex);
        }
    }

    public enum ErrorKeys {

        ERROR_GET_BY_PRODUCT_NAME_AND_ITEM_ID,
        ERROR_FIND_ALL_PRODUCT_NAMES
    }
}
