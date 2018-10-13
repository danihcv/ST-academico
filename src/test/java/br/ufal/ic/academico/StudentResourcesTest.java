//package br.ufal.ic.academico;
//
//import br.ufal.ic.academico.models.course.CourseDAO;
//import br.ufal.ic.academico.models.discipline.DisciplineDAO;
//import br.ufal.ic.academico.models.person.student.Student;
//import br.ufal.ic.academico.models.person.student.StudentDAO;
//import br.ufal.ic.academico.models.person.student.StudentDTO;
//import br.ufal.ic.academico.models.person.teacher.TeacherDAO;
//import br.ufal.ic.academico.resources.EnrollmentResources;
//import ch.qos.logback.classic.Level;
//import com.github.javafaker.Faker;
//import io.dropwizard.logging.BootstrapLogging;
//import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
//import io.dropwizard.testing.junit5.ResourceExtension;
//import javax.servlet.http.HttpServletRequest;
//import lombok.SneakyThrows;
//import org.apache.commons.lang3.reflect.FieldUtils;
//import org.glassfish.hk2.utilities.binding.AbstractBinder;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
///**
// *
// * @author Willy
// */
//@ExtendWith(DropwizardExtensionsSupport.class)
//class StudentResourcesTest {
//    static {
//        BootstrapLogging.bootstrap(Level.DEBUG);
//    }
//
//    static private StudentDAO studentDAO = mock(StudentDAO.class);
//    static private TeacherDAO teacherDAO = mock(TeacherDAO.class);
//    static private CourseDAO courseDAO = mock(CourseDAO.class);
//    static private DisciplineDAO disciplineDAO = mock(DisciplineDAO.class);
//
//    private final EnrollmentResources enrollmentResources = new EnrollmentResources(studentDAO, teacherDAO, courseDAO, disciplineDAO);
//
//    private ResourceExtension RULE = ResourceExtension.builder()
//            .addProvider(new MockBinder())
//            .addResource(enrollmentResources)
//            .build();
//
//    /**
//     * Caso seu resource utilize o @Context para injetar o HttpServletRequest,
//     * daí você precisará utilizar um BinderMock (ver abaixo) e adicioná-lo como
//     * um provider no ResourceExtension acima.
//     */
//    private final HttpServletRequest request = mock(HttpServletRequest.class);
//
//    public class MockBinder extends AbstractBinder {
//
//        @Override
//        protected void configure() {
//            Student requestStudent = new Student("Daniel", "Vassalo");
//
//            when(request.getAttribute(any())).thenReturn(requestStudent);
//
//            bind(request).to(HttpServletRequest.class);
//        }
//    }
//
//    static private List<Student> studentList = new ArrayList<>();
//    static private Faker faker = new Faker();
//    static private Random rand = new Random();
//
//    @BeforeAll
//    @SneakyThrows
//    static void setUp() {
//        System.out.println("setUp");
//
//        for (int i = 1; i <= 20; i++) {
//            Student newStudent = new Student(faker.name().firstName(), faker.name().lastName());
//            newStudent.setCredits(rand.nextInt());
//            studentList.add(newStudent);
//        }
//
//        for (int i = 1; i <= studentList.size(); i++) {
//            FieldUtils.writeField(studentList.get(i - 1), "id", (long) i, true);
//            when(studentDAO.get(studentList.get(i - 1).getId())).thenReturn(studentList.get(i - 1));
//        }
//    }
//
//    @Test
//    void getStudentById() {
//        for (int i = 1; i <= studentList.size(); i++) {
//            StudentDTO response = RULE.target("/enrollment/student/" + i).request().get(StudentDTO.class);
//            Student expected = studentList.get(i - 1);
//
//            assertNotNull(response);
//            assertEquals(expected.getId(), response.id, "ID do Student retornado é diferente do esperado");
//            assertEquals(studentList.get(i - 1).getFirstname(), response.firstName,
//                    "First Name do Student retornado é diferente do esperado");
//            assertEquals(studentList.get(i - 1).getLastName(), response.lastName,
//                    "Last Name do Student retornado é diferente do esperado");
//            assertEquals(studentList.get(i - 1).getCredits(), response.credits,
//                    "Credits do Student retornado é diferente do esperado");
//            assertEquals("STUDENT", response.role,
//                    "Credits do Student retornado é diferente do esperado");
//            assertNull(response.course, "Student foi associado a um Course");
//            assertEquals(0, response.getCompletedDisciplines().size(),
//                    "Student recebeu uma lista de Completed Disciplines");
//        }
//    }
//}
