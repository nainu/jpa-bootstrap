package persistence.entitymanager;

import app.entity.Person5;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.bootstrap.EntityManagerFactory;
import persistence.bootstrap.Initializer;
import persistence.entity.context.ObjectNotFoundException;
import testsupport.H2DatabaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static testsupport.EntityTestUtils.assertSamePerson;

class EntityManagerImplScenarioTest extends H2DatabaseTest {
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        Initializer initializer = new Initializer("app.entity", jdbcTemplate, dialect);
        initializer.initialize();
        EntityManagerFactory entityManagerFactory = initializer.createEntityManagerFactory();
        entityManager = entityManagerFactory.openSession();
    }

    @Test
    @DisplayName("저장하고, 읽고, 지우고 다시 찾는 시나리오")
    void scenario1() {
        Person5 person = new Person5("abc", 7, "def@example.com");
        entityManager.persist(person);
        entityManager.flush();

        Person5 fetchedPerson = entityManager.find(Person5.class, 1L);
        entityManager.find(Person5.class, 1L);
        entityManager.find(Person5.class, 1L);
        entityManager.find(Person5.class, 1L);

        entityManager.remove(fetchedPerson);
        entityManager.flush();

        assertAll(
                () -> assertSamePerson(fetchedPerson, person, false),
                () -> assertThrows(ObjectNotFoundException.class, () -> entityManager.find(Person5.class, 1L)),
                () -> assertThat(executedQueries).containsExactly(
                        "INSERT INTO users (nick_name, old, email) VALUES ('abc', 7, 'def@example.com')",
                        "SELECT id, nick_name, old, email FROM users WHERE id = 1",
                        "DELETE FROM users WHERE id = 1"
                )
        );
    }

    @Test
    void scenario2() {
        Person5 person = new Person5("abc", 7, "def@example.com");
        entityManager.persist(person);
        entityManager.flush();

        Person5 fetchedPerson = entityManager.find(Person5.class, 1L);
        entityManager.remove(fetchedPerson);
        entityManager.flush();

        assertThrows(ObjectNotFoundException.class, () -> {
                         entityManager.persist(new Person5(fetchedPerson.getId(), "newname", 8, "newemail@test.com"));
                         entityManager.flush();
                     }
        );
    }

    @Test
    @DisplayName("1st 캐시에 없는 row 를 id 로 업데이트하려고 해도 insert 됨. (IDENTITY 전략) load 후에 persist 하면 update 됨")
    void scenario3() {
        jdbcTemplate.execute("INSERT INTO users (id, nick_name, old, email) VALUES (20, '가나다', 21, 'email@test.com')");
        executedQueries.clear();

        // id 20 무시됨
        entityManager.persist(new Person5(20L, "가나다라", 22, "email2@test.com"));
        entityManager.flush();

        entityManager.find(Person5.class, 20L);
        entityManager.find(Person5.class, 20L);
        entityManager.persist(new Person5(20L, "가나다라마", 22, "email2@test.com"));
        entityManager.flush();

        assertThat(executedQueries).containsExactly(
                "INSERT INTO users (nick_name, old, email) VALUES ('가나다라', 22, 'email2@test.com')",
                "SELECT id, nick_name, old, email FROM users WHERE id = 1",
                "SELECT id, nick_name, old, email FROM users WHERE id = 20",
                "UPDATE users SET nick_name = '가나다라마', old = 22, email = 'email2@test.com' WHERE id = 20"
        );
    }

    @Test
    @DisplayName("지운 걸 또 지워보기")
    void scenario4() {
        Person5 person = new Person5("가나다라", 22, "email2@test.com");
        entityManager.persist(person);
        entityManager.flush();

        Person5 fetchedPerson = entityManager.find(Person5.class, 1L);

        entityManager.remove(fetchedPerson);
        entityManager.remove(fetchedPerson);
        entityManager.flush();

        assertThat(executedQueries).containsExactly(
                "INSERT INTO users (nick_name, old, email) VALUES ('가나다라', 22, 'email2@test.com')",
                "SELECT id, nick_name, old, email FROM users WHERE id = 1",
                "DELETE FROM users WHERE id = 1"
        );
    }

    @Test
    @DisplayName("아직 db 에 없는 ID 를 가진 객체를 persist 해본다.")
    void scenario5() {
        assertThat(entityManager.find(Person5.class, 20L)).isNull();

        Person5 person = new Person5(20L, "가나다라", 22, "email2@test.com");
        entityManager.persist(person);
        entityManager.flush();

        Person5 fetched = entityManager.find(Person5.class, 1L);

        assertAll(
                () -> assertSamePerson(fetched, person, false),
                () -> assertThat(executedQueries).containsExactly(
                        "SELECT id, nick_name, old, email FROM users WHERE id = 20",
                        "INSERT INTO users (nick_name, old, email) VALUES ('가나다라', 22, 'email2@test.com')",
                        "SELECT id, nick_name, old, email FROM users WHERE id = 1")
        );
    }
}
