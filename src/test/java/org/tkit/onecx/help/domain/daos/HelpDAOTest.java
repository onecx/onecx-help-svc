package org.tkit.onecx.help.domain.daos;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HelpDAOTest {
    @Inject
    HelpDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void methodExceptionTests() {
        methodExceptionTests(() -> dao.findById(null),
                HelpDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED);
        methodExceptionTests(() -> dao.findHelpsByCriteria(null),
                HelpDAO.ErrorKeys.ERROR_GET_BY_PRODUCT_NAME_AND_ITEM_ID);
        methodExceptionTests(() -> dao.findByProductNameAndItemId(null, null),
                HelpDAO.ErrorKeys.ERROR_GET_BY_PRODUCT_NAME_AND_ITEM_ID);
        methodExceptionTests(() -> dao.findProductsWithHelpItems(),
                HelpDAO.ErrorKeys.ERROR_FIND_PRODUCTS_WITH_HELP_ITEMS);
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }
}
