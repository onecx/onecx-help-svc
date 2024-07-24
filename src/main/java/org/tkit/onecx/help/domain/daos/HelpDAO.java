package org.tkit.onecx.help.domain.daos;

import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import org.tkit.onecx.help.domain.criteria.HelpSearchCriteria;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.onecx.help.domain.models.Help_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class HelpDAO extends AbstractDAO<Help> {

    // https://hibernate.atlassian.net/browse/HHH-16830#icft=HHH-16830
    @Override
    public Help findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            var root = cq.from(Help.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public PageResult<Help> findHelpsByCriteria(HelpSearchCriteria criteria) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            var root = cq.from(Help.class);

            List<Predicate> predicates = new ArrayList<>();
            addSearchStringPredicate(predicates, cb, root.get(Help_.itemId), criteria.getItemId());
            addSearchStringPredicate(predicates, cb, root.get(Help_.productName), criteria.getProductName());
            addSearchStringPredicate(predicates, cb, root.get(Help_.context), criteria.getContext());
            addSearchStringPredicate(predicates, cb, root.get(Help_.baseUrl), criteria.getBaseUrl());
            addSearchStringPredicate(predicates, cb, root.get(Help_.resourceUrl), criteria.getResourceUrl());
            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[] {}));
            }
            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));
            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_GET_BY_PRODUCT_CRITERIA, ex);
        }
    }

    public Stream<Help> findHelpsByProductNames(Set<String> productNames) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            var root = cq.from(Help.class);

            if (productNames != null && !productNames.isEmpty()) {
                cq.where(root.get(Help_.PRODUCT_NAME).in(productNames));
            }
            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));
            return getEntityManager().createQuery(cq).getResultStream();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_BY_PRODUCT_NAMES, ex);
        }
    }

    public Help findByProductNameAndItemId(String productName, String itemId) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            var root = cq.from(Help.class);
            cq.where(cb.and(
                    cb.equal(root.get(Help_.productName), productName),
                    cb.equal(root.get(Help_.itemId), itemId)));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException ne) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_GET_BY_PRODUCT_NAME_AND_ITEM_ID, ex);
        }

    }

    public List<String> findProductsWithHelpItems() {
        try {
            var cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<Help> root = cq.from(Help.class);
            cq.select(root.get(Help_.PRODUCT_NAME)).distinct(true);
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_PRODUCTS_WITH_HELP_ITEMS, ex);
        }
    }

    public enum ErrorKeys {

        ERROR_GET_BY_PRODUCT_CRITERIA,
        ERROR_FIND_BY_PRODUCT_NAMES,
        FIND_ENTITY_BY_ID_FAILED,
        ERROR_GET_BY_PRODUCT_NAME_AND_ITEM_ID,
        ERROR_FIND_PRODUCTS_WITH_HELP_ITEMS
    }
}
