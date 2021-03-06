package br.ufal.ic.academico.models;

import br.ufal.ic.academico.models.course.Course;
import br.ufal.ic.academico.models.department.Department;
import br.ufal.ic.academico.models.department.DepartmentDAO;
import br.ufal.ic.academico.models.discipline.Discipline;
import br.ufal.ic.academico.models.person.student.Student;
import br.ufal.ic.academico.models.person.teacher.Teacher;
import br.ufal.ic.academico.models.secretary.Secretary;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(DropwizardExtensionsSupport.class)
class DepartmentCRUDTest {

    private DAOTestExtension dbTesting = DAOTestExtension.newBuilder()
            .addEntityClass(Student.class)
            .addEntityClass(Teacher.class)
            .addEntityClass(Department.class)
            .addEntityClass(Secretary.class)
            .addEntityClass(Course.class)
            .addEntityClass(Discipline.class)
            .build();

    private DepartmentDAO dao = new DepartmentDAO(dbTesting.getSessionFactory());

    @Test
    void departmentCRUD() {
        final Department d1 = create("IC");
        get(d1);

        d1.setName("FDA");
        d1.setGraduation(new Secretary());
        d1.setPostGraduation(new Secretary());
        update(d1);

        delete(d1);

        assertEquals(0, dbTesting.inTransaction(dao::getAll).size(),
                "Department não foi removido da listagem total de Department");

        final Department d2 = create("ICBS");
        get(d2);
        final Department d3 = create("COS");
        get(d3);

        assertEquals(2, dbTesting.inTransaction(dao::getAll).size(),
                "Nem todos os novos Departments estão aparecendo na listagem total de Departments");

        delete(d2);

        assertEquals(1, dbTesting.inTransaction(dao::getAll).size(),
                "Department não foi removido da listagem total de Departments");
        assertEquals(d3.getId(), dbTesting.inTransaction(dao::getAll).get(0).getId(),
                "Department3 não está na listagem de todos os Departments restantes");
    }

    private Department create(String name) {
        final Department department = new Department(name);
        final Department saved = dbTesting.inTransaction(() -> dao.persist(department));

        assertNotNull(saved, "Falhou ao salvar um novo Department");
        assertNotNull(saved.getId(), "Department não recebeu um id ao ser criado");
        assertEquals(department.getName(), saved.getName(), "Name do Department criado não corresponde com o informado");
        assertNull(saved.getGraduation(), "Department recebeu uma secretaria de graduação ao ser criado");
        assertNull(saved.getPostGraduation(), "Department recebeu uma secretaria de pós graduação ao ser criado");

        return department;
    }

    private void get(Department department) {
        Department recovered = dbTesting.inTransaction(() -> dao.get(department.getId()));

        assertEquals(department.getId(), recovered.getId(), "ID do Department recuperado não confere com o informado");
        assertEquals(department.getName(), recovered.getName(), "Name do Department recuperado não confere com o informado");
        if (department.getGraduation() != null) {
            assertEquals(department.getGraduation().getId(), recovered.getGraduation().getId(),
                    "Graduation Secretary do Department recuperado não confere com o informado");
        } else {
            assertNull(recovered.getGraduation(), "Graduation Secretary do Department recuperado não confere com o informado");
        }

        if (department.getPostGraduation() != null) {
            assertEquals(department.getPostGraduation().getId(), recovered.getPostGraduation().getId(),
                    "Post Graduation Secretary do Department recuperado não confere com o informado");
        } else {
            assertNull(recovered.getPostGraduation(), "Post Graduation Secretary do Department recuperado não confere com o informado");
        }
    }

    private void update(Department department) {
        final Department updated = dbTesting.inTransaction(() -> dao.persist(department));
        assertEquals(department.getId(), updated.getId(), "Ao ser atualizado, Department teve seu ID alterado");
        assertEquals(department.getName(), updated.getName(), "Name do Department não foi atualizado corretamente");
        assertEquals(department.getGraduation().getId(), updated.getGraduation().getId(),
                "Secretaria de graduação associada incorretamente");
        assertEquals(department.getPostGraduation().getId(), updated.getPostGraduation().getId(),
                "Secretaria de pós graduação associada incorretamente");
    }

    private void delete(Department department) {
        dbTesting.inTransaction(() -> dao.delete(department));
        assertNull(dbTesting.inTransaction(() -> dao.get(department.getId())), "Department não foi removido");
    }
}
