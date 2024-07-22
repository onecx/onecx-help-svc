package org.tkit.onecx.help.domain.daos;

import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.onecx.help.domain.criteria.HelpSearchCriteria;
import org.tkit.onecx.help.domain.models.Help;
import org.tkit.onecx.help.domain.models.Help_;
import org.tkit.onecx.help.domain.models.ProductResource_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class HelpDAO extends AbstractDAO<Help> {

    public Help loadById(Object id) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            var root = cq.from(Help.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));

            return this.getEntityManager().createQuery(cq).setHint(HINT_LOAD_GRAPH,
                    this.getEntityManager().getEntityGraph(Help.HELP_FULL)).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_BY_ID, ex);
        }
    }

    public Stream<Help> loadAll() {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            cq.from(Help.class);
            return this.getEntityManager().createQuery(cq).setHint(HINT_LOAD_GRAPH,
                    this.getEntityManager().getEntityGraph(Help.HELP_FULL)).getResultStream();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ALL, ex);
        }
    }

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
            addSearchStringPredicate(predicates, cb, root.get(Help_.productResource).get(ProductResource_.itemId),
                    criteria.getItemId());
            addSearchStringPredicate(predicates, cb, root.get(Help_.productResource).get(ProductResource_.productName),
                    criteria.getProductName());
            addSearchStringPredicate(predicates, cb, root.get(Help_.context), criteria.getContext());
            addSearchStringPredicate(predicates, cb, root.get(Help_.baseUrl), criteria.getBaseUrl());
            addSearchStringPredicate(predicates, cb, root.get(Help_.resourceUrl), criteria.getResourceUrl());
            if (!predicates.isEmpty()) {
                cq.where(predicates.toArray(new Predicate[] {}));
            }
            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));
            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_GET_BY_PRODUCT_NAME_AND_ITEM_ID, ex);
        }
    }

    public Help findByProductNameAndItemId(String productName, String itemId) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            var root = cq.from(Help.class);
            cq.where(cb.and(
                    cb.equal(root.get(Help_.productResource).get(ProductResource_.productName), productName),
                    cb.equal(root.get(Help_.productResource).get(ProductResource_.itemId), itemId)));

            return this.getEntityManager().createQuery(cq).setHint(HINT_LOAD_GRAPH,
                    this.getEntityManager().getEntityGraph(Help.HELP_FULL)).getSingleResult();
        } catch (NoResultException ne) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_GET_BY_PRODUCT_NAME_AND_ITEM_ID, ex, productName, itemId);
        }
    }

    public enum ErrorKeys {

        ERROR_LOAD_ALL,
        ERROR_LOAD_BY_ID,
        FIND_ENTITY_BY_ID_FAILED,
        ERROR_GET_BY_PRODUCT_NAME_AND_ITEM_ID,
    }
}
