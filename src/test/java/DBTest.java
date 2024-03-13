import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DBTest {
    private DB db;

    @BeforeEach
    public void setup() {
        this.db = new DB();
    }

    @Test
    public void whenTableIsCalledWithEmptyArgumentThenIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> db.table(""));
    }

    @Test
    public void whenTableIsCalledThenTableSectionShouldChange() {
        db.table("transactions");
        assertEquals("[QUERY] transactions", db.getQuery());
    }

    @Test
    public void whenInsertIsGivenUnmatchableKeysAndValuesThenIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            db.insert(
                    List.of("src_account", "amount", "dest_account"),
                    List.of("123", "456")
            );
        });
    }

    @Test
    public void whenInsertAndExecuteIsCalledThenQueryShouldBeInsert() {
        db
                .table("transactions")
                .insert(List.of("src_account", "amount", "dest_account"), List.of("123", "800", "456"))
                .execute();

        assertEquals("INSERT INTO transactions(src_account, amount, dest_account) VALUES(123, 800, 456)", db.getQuery());
    }

    @Test
    public void whenSelectIsCalledThenQueryShouldBeSelect() {
        db
                .table("transactions")
                .select();

        assertEquals("SELECT * FROM transactions", db.getQuery());
    }

    @Test
    public void whenSelectAndGetIsCalledThenFetchData() {
        Assertions.assertDoesNotThrow(() -> {
            db.table("transactions");
            ResultSet res = db
                    .select()
                    .get();

            res.next();

            assertEquals(res.getString("src_account"), "123");
            assertEquals(res.getDouble("amount"), 800);
            assertEquals(res.getString("dest_account"), "456");
        });
    }

    @Test
    public void whenUpdateIsGivenUnmatchableKeysAndValuesThenIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            db.update(
                    List.of("src_account", "amount", "dest_account"),
                    List.of("123", "789")
            );
        });
    }

    @Test
    public void whenUpdateAndWhereIsCalledThenUpdateAppropriateRecord() {
        db
                .table("transactions")
                .update(List.of("src_account", "amount", "dest_account"), List.of("123", "800", "789"))
                .where("id", "1")
                .execute();

        assertEquals("UPDATE transactions SET src_account = 123, amount = 800, dest_account = 789 WHERE id = 1", db.getQuery());
    }

    @Test
    public void whenDeleteIsCalledThenDeleteAppropriateRecord() {
        Assertions.assertDoesNotThrow(() -> {
            db.table("transactions").delete().where("id", "1").execute();
        });
    }

    @Test
    public void whenAddWhereCalledANewConditionIsAdded() {
        db
                .table("transactions")
                .select()
                .where("id", "1")
                .andWhere("amount", "800");

        Assertions.assertEquals("SELECT * FROM transactions WHERE id = 1 AND amount = 800", db.getQuery());
    }

    @Test
    public void whenOrWhereCalledANewConditionIsAdded() {
        {
            db
                    .table("transactions")
                    .select()
                    .where("id", "1")
                    .orWhere("amount", "800");

            Assertions.assertEquals("SELECT * FROM transactions WHERE id = 1 OR amount = 800", db.getQuery());
        }
    }
}
