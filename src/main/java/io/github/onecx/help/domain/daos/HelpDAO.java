package io.github.onecx.help.domain.daos;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;
import org.tkit.quarkus.jpa.utils.QueryCriteriaUtil;

import io.github.onecx.help.domain.criteria.HelpSearchCriteria;
import io.github.onecx.help.domain.models.Help;
import io.github.onecx.help.domain.models.Help_;

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

            if (criteria.getItemId() != null && !criteria.getItemId().isBlank()) {
                cq.where(cb.like(root.get(Help_.itemId), QueryCriteriaUtil.wildcard(criteria.getItemId())));
            }

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_HELPS_BY_CRITERIA, ex);
        }
    }

    public Help findByAppIdAndItemId(HelpSearchCriteria criteria) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            var root = cq.from(Help.class);
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getItemId() != null && !criteria.getItemId().isBlank()) {
                predicates.add(cb.like(root.get(Help_.itemId), criteria.getItemId()));
            }
            if (criteria.getAppId() != null && !criteria.getAppId().isBlank()) {
                predicates.add(cb.like(root.get(Help_.appId), criteria.getAppId()));
            }
            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }
            return this.getEntityManager().createQuery(cq).getSingleResult();

        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_HELPS_BY_CRITERIA, ex);
        }

    }

    public PageResult<Help> findAll(Integer pageNumber, Integer pageSize) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            cq.from(Help.class);
            return createPageQuery(cq, Page.of(pageNumber, pageSize)).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_ALL_HELP_PAGE, ex);
        }
    }

    public Help findHelpByItemId(String itemId) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Help.class);
            var root = cq.from(Help.class);
            cq.where(cb.equal(root.get(Help_.itemId), itemId));

            return this.getEntityManager().createQuery(cq).getSingleResult();

        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_HELP_BY_ITEM_ID, ex);
        }
    }

    public enum ErrorKeys {

        FIND_ENTITY_BY_ID_FAILED,
        ERROR_FIND_HELPS_BY_CRITERIA,
        ERROR_FIND_ALL_HELP_PAGE,
        ERROR_FIND_HELP_BY_ITEM_ID,
    }
}
