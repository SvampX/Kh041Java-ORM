package annotations.handlers;

import annotations.handlers.configuration.User;
import exceptions.DBException;
import exceptions.DataObtainingFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneratedValueHandlerTest {

    @Test
    void setGeneratedIdValue() throws DBException, IllegalAccessException {
        //given
        User user = new User();
        user.setFirstName("Ivan");
        user.setLastName("Ivanov");
        user.setAge(30);
        int userDbId = 2;
        GeneratedValueHandler gvh = new GeneratedValueHandler();
        //when
        gvh.setGeneratedIdValue(user);
        //then
        assertEquals(user.getId(), userDbId);

    }

    @Test
    void setGeneratedIdValueWithoutEntityAndTableAnnotations() {
        //given
        Admin user = new Admin();
        GeneratedValueHandler gvh = new GeneratedValueHandler();
        //when, then
        Assertions.assertThrows(DataObtainingFailureException.class, () -> gvh.setGeneratedIdValue(user));
    }

}