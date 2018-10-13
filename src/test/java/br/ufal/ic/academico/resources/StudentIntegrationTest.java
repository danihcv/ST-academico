package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.AcademicoApp;
import br.ufal.ic.academico.ConfigApp;
import br.ufal.ic.academico.models.course.CourseDTO;
import br.ufal.ic.academico.models.department.DepartmentDTO;
import br.ufal.ic.academico.models.discipline.DisciplineDTO;
import br.ufal.ic.academico.models.person.student.StudentDTO;
import br.ufal.ic.academico.models.secretary.SecretaryDTO;
import ch.qos.logback.classic.Level;
import com.github.javafaker.Faker;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DropwizardExtensionsSupport.class)
class StudentIntegrationTest {
    static {
        BootstrapLogging.bootstrap(Level.DEBUG);
    }

    private List<StudentDTO> students = new ArrayList<>();

    private static DropwizardAppExtension<ConfigApp> RULE = new DropwizardAppExtension(AcademicoApp.class,
            ResourceHelpers.resourceFilePath("config-test.yml"));

    private String url;
    private Faker faker = new Faker();
    private BasicBackground background;

    @BeforeEach
    void setup() {
        url = "http://localhost:" + RULE.getLocalPort() + "/academicotest/";
        this.background = new BasicBackground(url);
    }

    @Test
    void studentResources() {
        assertEquals(new ArrayList<>(), RULE.client().target(url + "enrollment/student").request()
                        .get(new GenericType<ArrayList<StudentDTO>>() {
                        }),
                "Banco de dados inicializou com Students cadastros");

        createStudents();
        StudentDTO student = students.get(0), anotherStudent = students.get(1);
        getStudentByID(student);
        students.set(0, updateStudent(student));
        students.set(1, updateStudent(anotherStudent));

        assertEquals(students.size(), RULE.client().target(url + "enrollment/student").request()
                        .get(new GenericType<ArrayList<StudentDTO>>() {
                        }).size(),
                "Quantidade de Students listados, a partir do banco de dados, é diferente da quantidade cadastrada");

        deleteStudent();

        assertEquals(students.size(), RULE.client().target(url + "enrollment/student").request()
                        .get(new GenericType<ArrayList<StudentDTO>>() {
                        }).size(),
                "Quantidade de Students listados está diferente da quantidade esperada após remoção de Student");

        StudentDTO studentIC = students.get(0);
        StudentDTO studentFDA = students.get(1);
        // setup de Department, Secretary, Course e Discipline para testes do Student e do anotherStudent
        DepartmentDTO department = background.createDepartment(RULE, "IC"),
                anotherDepartment = background.createDepartment(RULE, "FDA");
        SecretaryDTO secretary = background.createSecretary(RULE, department, "GRADUATION"),
                anotherSecretary = background.createSecretary(RULE, anotherDepartment, "POST-GRADUATION");
        CourseDTO course = background.createCourse(RULE, secretary, "Ciência da Computação"),
                anotherCourse = background.createCourse(RULE, anotherSecretary, "Direito");
        DisciplineDTO discipline = background.createDiscipline(RULE, course, "CC001", "Programação 1", 50);
        // testes do Student e do anotherStudent
        studentIC = enrollStudentInCourse(studentIC, course);
        studentFDA = enrollStudentInCourse(studentFDA, anotherCourse);
        getAllDisciplinesFromStudentsDepartments(studentIC, studentFDA, students.get(2));
        discipline = enrollStudentInDiscipline(studentIC, discipline, studentFDA);
        getEnrollmentProof(studentIC, 1);
        getEnrollmentProof(studentFDA, 0);
        completeDisciplineFromStudent(studentIC, discipline, studentFDA);
    }

    private void createStudents() {
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "enrollment/student").request().post(Entity.json(new StudentDTO()), StudentDTO.class),
                "API não retornou status 400 ao tentar criar um Student sem First Name");

        for (int i = 1; i <= 5; i++) {
            StudentDTO entity = new StudentDTO();
            entity.firstName = faker.name().firstName();
            StudentDTO response = RULE.client().target(url + "enrollment/student").request()
                    .post(Entity.json(entity), StudentDTO.class);

            assertNotNull(response.id, "Student criado sem ID");
            assertEquals(entity.firstName, response.firstName, "First Name incorreto associado ao Student criado");
            assertNull(response.lastName);
            assertEquals("STUDENT", response.role, "Role do Student está diferente do esperado");
            assertEquals(0, (int) response.credits, "Student foi criado com credits");
            assertNull(response.course, "Student foi associado a um Course em sua criação");
            assertEquals(new ArrayList<>(), response.completedDisciplines,
                    "Student recebeu uma lista não vazia de Completed Disciplines em sua criação");
            students.add(response);
        }
    }

    private void getStudentByID(StudentDTO student) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/0").request().get(StudentDTO.class),
                "API não retornou status 404 ao tentar retornar um Student com ID inválido");

        StudentDTO response = RULE.client().target(url + "enrollment/student/" + student.id).request()
                .get(StudentDTO.class);
        assertEquals(student.id, response.id, "Student retornado tem ID diferente do informado");
    }

    private StudentDTO updateStudent(StudentDTO original) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/0").request()
                        .put(Entity.json(new StudentDTO()), StudentDTO.class),
                "API não retornou status 404 ao tentar atualizar um Student com ID inválido");

        StudentDTO entity = original;
        entity.lastName = faker.name().lastName();
        StudentDTO response = RULE.client().target(url + "enrollment/student/" + original.id).request()
                .put(Entity.json(entity), StudentDTO.class);
        assertEquals(original.id, response.id, "Student atualizado possui ID diferente do informado");
        assertEquals(entity.lastName, response.lastName, "Last Name não foi atualizado corretamente");
        assertEquals(original.firstName, response.firstName,
                "First Name foi modificado durante a atualização do Last Name");
        assertEquals(original.credits, response.credits,
                "Credits foi modificado durante a atualização do Last Name");
        assertEquals(original.course, response.course,
                "Course foi modificado durante a atualização do Last Name");
        assertEquals(original.completedDisciplines, response.completedDisciplines,
                "Completed Disciplines foi modificado durante a atualização do Last Name");

        entity = new StudentDTO();
        entity.firstName = faker.name().firstName();
        response = RULE.client().target(url + "enrollment/student/" + original.id).request()
                .put(Entity.json(entity), StudentDTO.class);
        assertEquals(original.id, response.id, "Student atualizado possui ID diferente do informado");
        assertEquals(entity.firstName, response.firstName,
                "First Name não foi atualizado corretamente");
        assertEquals(original.lastName, response.lastName,
                "Last Name foi modificado durante a atualização do First Name");
        assertEquals(original.credits, response.credits,
                "Credits foi modificado durante a atualização do First Name");
        assertEquals(original.course, response.course,
                "Course foi modificado durante a atualização do First Name");
        assertEquals(original.completedDisciplines, response.completedDisciplines,
                "Completed Disciplines foi modificado durante a atualização do First Name");
        return response;
    }

    private void deleteStudent() {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/0").request()
                        .delete(StudentDTO.class),
                "API não retornou status 404 ao tentar deletar um Student com ID inválido");

        final StudentDTO original = students.get(0);
        students.remove(original);
        assertDoesNotThrow(() -> RULE.client().target(url + "enrollment/student/" + original.id).request()
                .delete(), "Falhou ao tentar deletar um Student com ID válido");

        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/" + original.id)
                .request().get(StudentDTO.class), "API não retornou status 404 ao tentar acessar um Student removido");
    }

    private StudentDTO enrollStudentInCourse(StudentDTO student, CourseDTO course) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/0/course/" + course.getId()).request()
                .post(null, StudentDTO.class));
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/" + student.getId() + "/course/0").request()
                .post(null, StudentDTO.class), "API não retornou status 404 ao tentar cadastrar um Student válido num Course inválido");

        StudentDTO response = RULE.client().target(url + "enrollment/student/" + student.getId() + "/course/" + course.getId()).request()
                .post(null, StudentDTO.class);
        assertNotNull(response.course, "Student não foi cadastrado corretamente no Course");
        assertEquals(student.getId(), response.id, "Student retornado no cadastro num Course tem um ID diferente do informado");
        assertEquals(student.firstName, response.firstName, "Student teve seu First Name alterado ao ser cadastrado num Course");
        assertEquals(student.lastName, response.lastName, "Student teve seu Last Name alterado ao ser cadastrado num Course");
        assertEquals(student.credits, response.credits, "Student teve seus Credits alterados ao ser cadastrado num Course");
        assertEquals(student.completedDisciplines, response.completedDisciplines,
                "Student teve suas Completed Disciplines alteradas ao ser cadastrado num Course");

        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "enrollment/student/" + student.getId() + "/course/" + course.getId())
                .request().post(null, StudentDTO.class), "API não retornou status 400 ao tentar recadastrar um Student em seu curso atual");

        return response;
    }

    private void getAllDisciplinesFromStudentsDepartments(StudentDTO student, StudentDTO anotherStudent, StudentDTO studentWithoutCourse) {
        // testes para o Student, que tem Disciplines disponíveis
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/0/discipline")
                .request().get(new GenericType<List<DisciplineDTO>>(){}),
                "API não retornou status 404 ao tentar recuperar as Disciplines do Department de um Student inválido");

        List<DisciplineDTO> disciplines = RULE.client().target(url + "enrollment/student/" + student.getId() + "/discipline").request()
                .get(new GenericType<List<DisciplineDTO>>(){});
        assertEquals(1, disciplines.size(), "Quantidade inesperada de Disciplines disponíveis para o Student");

        // testes do anotherStudent, que não tem Disciplines disponíveis
        assertNull(studentWithoutCourse.course, "Student Without Course informado possui um Course associado");
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "enrollment/student/" + studentWithoutCourse.getId() + "/discipline")
                        .request().get(new GenericType<List<DisciplineDTO>>(){}),
                "API não retornou status 404 ao tentar recuperar as Disciplines do Department de um Student sem Course");
        assertEquals(0, RULE.client().target(url + "enrollment/student/" + anotherStudent.getId() + "/discipline").request()
                .get(new GenericType<List<DisciplineDTO>>(){}).size(),
                "Foram encontradas Disciplines no Department de um Student cadastrado num Course sem Disciplines");
    }

    private DisciplineDTO enrollStudentInDiscipline(StudentDTO student, DisciplineDTO discipline, StudentDTO anotherDepartmentStudent) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/0/discipline/" + discipline.getId())
                .request().post(null, DisciplineDTO.class),
                "API não retornou status 404 ao tentar matricular um Student inválido numa Discipline válida");
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/" + student.getId() + "/discipline/0")
                .request().post(null, DisciplineDTO.class),
                "API não retornou status 404 ao tentar matricular um Student válido numa Discipline inválida");
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "enrollment/student/" + anotherDepartmentStudent.getId() + "/discipline/" + discipline.getId())
                .request().post(null, DisciplineDTO.class),
                "API não retornou status 400 ao matricular um aluno que não atende os requisitos da Discipline");

        DisciplineDTO response = RULE.client().target(url + "enrollment/student/" + student.getId() + "/discipline/" + discipline.getId())
                .request().post(null, DisciplineDTO.class);
        assertTrue(response.getStudents().stream().map(s -> s.id).collect(Collectors.toList()).contains(student.getId()),
                "Student matriculado não está presente na listagem de Students da Discipline");
        assertEquals(discipline.getStudents().size() + 1, response.getStudents().size(),
                "Listagem de Students matriculados na Discipline não aumentou");

        return response;
    }

    private void completeDisciplineFromStudent(StudentDTO student, DisciplineDTO discipline, StudentDTO anotherStudent) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/0/complete/" + discipline.getId())
                .request().post(null, StudentDTO.class),
                "API não retornou status 404 ao fazer um Student inválido completar uma Discipline válida");
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/student/" + student.getId() + "/complete/0")
                .request().post(null, StudentDTO.class),
                "API não retornou status 404 ao fazer um Student válido completar uma Discipline inválida");
        assertThrows(BadRequestException.class, () -> RULE.client().target(url + "enrollment/student/" + anotherStudent.getId() + "/complete/" + discipline.getId())
                .request().post(null, StudentDTO.class),
                "API não retornou status 400 ao fazer um Student completar uma Discipline na qual não está matriculado");

        StudentDTO response = RULE.client().target(url + "enrollment/student/" + student.getId() + "/complete/" + discipline.getId())
                .request().post(null, StudentDTO.class);
        assertEquals(student.credits + discipline.getCredits(), (int) response.credits,
                "Student não recebeu os créditos devidos pela Discipline completada");
        assertTrue(response.completedDisciplines.contains(discipline.getCode()),
                "Discipline Code não está presente na listagem de Completed Disciplines do Student");
    }

    private void getEnrollmentProof(StudentDTO student, int expectedDisciplineQuantity) {
        assertThrows(NotFoundException.class, () -> RULE.client().target(url + "enrollment/proof/0").request()
                .get(EnrollmentResources.Proof.class),
                "API não retornou status 404 ao recuperar o comprovante de matrócula de um Student inválido");

        EnrollmentResources.Proof response = RULE.client().target(url + "enrollment/proof/" + student.getId()).request()
                .get(EnrollmentResources.Proof.class);
        assertEquals(student.getId(), response.getId(), "ID do Student do comprovante retornado é diferente do ID informado");
        assertEquals(expectedDisciplineQuantity, response.getDisciplines().size(), "Comprovante do Student não possui nenhuma Discipline");
    }
}
