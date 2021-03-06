package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.models.course.Course;
import br.ufal.ic.academico.models.course.CourseDAO;
import br.ufal.ic.academico.models.course.CourseDTO;
import br.ufal.ic.academico.models.discipline.Discipline;
import br.ufal.ic.academico.models.discipline.DisciplineDAO;
import br.ufal.ic.academico.models.discipline.DisciplineDTO;
import br.ufal.ic.academico.models.secretary.Secretary;
import br.ufal.ic.academico.models.secretary.SecretaryDAO;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("course")
@Slf4j
@RequiredArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
public class CourseResources {
    private final SecretaryDAO secretaryDAO;
    private final CourseDAO courseDAO;
    private final DisciplineDAO disciplineDAO;

    @GET
    @UnitOfWork
    public Response getAll() {
        log.info("getAll courses");

        return Response.ok(courseListToDTOList(courseDAO.getAll())).build();
    }

    @GET
    @Path("/{id}")
    @UnitOfWork
    public Response get(@PathParam("id") Long id) {
        log.info("get course: id={}", id);

        Course c = courseDAO.get(id);
        if (c == null) {
            return Response.status(404).entity("Course not found.").build();
        }
        return Response.ok(new CourseDTO(c)).build();
    }

    @PUT
    @Path("/{id}")
    @UnitOfWork
    @Consumes("application/json")
    public Response update(@PathParam("id") Long id, CourseDTO entity) {
        log.info("update course {} to {}", id, entity);

        Course c = courseDAO.get(id);
        if (c == null) {
            return Response.status(404).entity("Course not found.").build();
        }
        c.update(entity);
        return Response.ok(new CourseDTO(courseDAO.persist(c))).build();
    }

    @DELETE
    @Path("/{id}")
    @UnitOfWork
    public Response deleteCourse(@PathParam("id") Long id) {
        log.info("delete course {}", id);

        Course c = courseDAO.get(id);
        if (c == null) {
            return Response.status(404).entity("Course not found.").build();
        }

        Secretary s = courseDAO.getSecretary(c);
        s.deleteCourse(c);

        secretaryDAO.persist(s);
        courseDAO.delete(c);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/discipline")
    @UnitOfWork
    public Response getAllDisciplinesFromCourse(@PathParam("id") Long id) {
        log.info("getAll disciplines from course {}", id);

        Course c = courseDAO.get(id);
        if (c == null) {
            return Response.status(404).entity("Course not found.").build();
        }
        return Response.ok(c.getDisciplines().stream().map(DisciplineDTO::new).toArray()).build();
    }

    @POST
    @Path("/{id}/discipline")
    @UnitOfWork
    @Consumes("application/json")
    public Response createDiscipline(@PathParam("id") Long id, DisciplineDTO entity) {
        log.info("create discipline in course {}", id);

        Course c = courseDAO.get(id);
        if (c == null) {
            return Response.status(404).entity("Course not found.").build();
        }

        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            return Response.status(400).entity("Discipline code required.").build();
        }

        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            return Response.status(400).entity("Discipline code required.").build();
        }

        Discipline d = new Discipline(entity);
        disciplineDAO.persist(d);
        c.addDiscipline(d);
        courseDAO.persist(c);
        return Response.ok(new DisciplineDTO(d)).build();
    }

    private List<CourseDTO> courseListToDTOList(List<Course> list) {
        List<CourseDTO> dtoList = new ArrayList<>();
        list.forEach(c -> dtoList.add(new CourseDTO(c)));
        return dtoList;
    }
}
