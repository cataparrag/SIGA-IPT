package cl.ipt.siga.config;

import cl.ipt.siga.model.*;
import cl.ipt.siga.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the study plans of Appendix A (plus a period, teachers and students for demos)
 * the first time the application starts with an empty database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CareerRepository careerRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final AcademicPeriodRepository periodRepository;

    /** Each line: semester | code | name | T-E-L | SCT | prerequisites (comma separated). */
    private static final String TAP_2021_3 = """
            1|TAP101|Fundamentos de Programación|2-2-2|6|
            1|TAP102|Matemática Aplicada|2-2-0|5|
            1|TAP103|Fundamentos de Computación|2-0-2|4|
            1|TAP104|Bases de Datos I|2-0-2|5|
            1|TAP105|Comunicación Efectiva|2-2-0|4|
            1|TAP106|Inglés Técnico I|2-2-0|4|
            2|TAP201|Programación Orientada a Objetos|2-2-2|6|TAP101
            2|TAP202|Estructuras de Datos|2-2-2|6|TAP101
            2|TAP203|Bases de Datos II|2-0-2|5|TAP104
            2|TAP204|Desarrollo Web I|2-0-4|6|TAP101
            2|TAP205|Sistemas Operativos|2-0-2|4|TAP103
            2|TAP206|Inglés Técnico II|2-2-0|3|TAP106
            3|TAP301|Desarrollo Web II|2-0-4|6|TAP201,TAP204
            3|TAP302|Ingeniería de Software|2-2-0|5|TAP201
            3|TAP303|Desarrollo de Aplicaciones Móviles|2-0-4|6|TAP201
            3|TAP304|Administración de Bases de Datos|2-0-2|5|TAP203
            3|TAP305|Redes y Comunicaciones|2-0-2|4|TAP205
            3|TAP306|Taller de Programación|0-0-4|4|TAP201,TAP202
            4|TAP401|Arquitectura y Desarrollo de Software|2-0-4|6|TAP301,TAP302
            4|TAP402|Desarrollo de Aplicaciones Cloud|2-0-4|6|TAP301
            4|TAP403|Seguridad de Aplicaciones|2-0-2|4|TAP301,TAP305
            4|TAP404|Calidad y Pruebas de Software|2-0-2|5|TAP302
            4|TAP405|Proyecto de Desarrollo de Software|0-0-6|6|TAP301,TAP302,TAP306
            4|TAP406|Emprendimiento y Gestión de Proyectos|2-2-0|3|TAP302
            """;

    private static final String TAD_2022_3 = """
            1|TAD101|Fundamentos de Programación|2-2-2|6|
            1|TAD102|Matemática Aplicada|2-2-0|5|
            1|TAD103|Introducción al Análisis de Datos|2-0-2|5|
            1|TAD104|Bases de Datos I|2-0-2|5|
            1|TAD105|Herramientas Computacionales|0-0-4|4|
            1|TAD106|Comunicación Efectiva|2-2-0|4|
            2|TAD201|Programación para Análisis de Datos|2-0-4|6|TAD101
            2|TAD202|Probabilidad y Estadística|2-2-2|6|TAD102
            2|TAD203|Bases de Datos II|2-0-2|5|TAD104
            2|TAD204|Preparación y Calidad de Datos|2-0-4|5|TAD103,TAD101
            2|TAD205|Visualización de Datos|2-0-2|5|TAD103
            2|TAD206|Inglés Técnico|2-2-0|3|
            3|TAD301|Estadística Aplicada|2-2-2|6|TAD202
            3|TAD302|Minería de Datos|2-0-4|6|TAD201,TAD202
            3|TAD303|Inteligencia de Negocios|2-0-4|5|TAD203,TAD205
            3|TAD304|Integración y Procesamiento de Datos|2-0-4|5|TAD201,TAD203
            3|TAD305|Visualización Avanzada de Datos|2-0-2|4|TAD205
            3|TAD306|Ética y Protección de Datos|2-2-0|4|TAD103
            4|TAD401|Fundamentos de Machine Learning|2-0-4|6|TAD301,TAD302
            4|TAD402|Big Data y Procesamiento de Datos|2-0-4|5|TAD304
            4|TAD403|Analítica de Negocios|2-0-2|5|TAD301,TAD303
            4|TAD404|Desarrollo de Soluciones de Datos|2-0-4|5|TAD302,TAD304
            4|TAD405|Proyecto de Análisis de Datos|0-0-6|6|TAD302,TAD303,TAD305
            4|TAD406|Emprendimiento y Gestión de Proyectos|2-2-0|3|
            """;

    private static final String TCS_2021_3 = """
            1|TCS101|Fundamentos de Programación|2-2-2|6|
            1|TCS102|Fundamentos de Sistemas Computacionales|2-0-2|5|
            1|TCS103|Redes de Computadores I|2-0-2|5|
            1|TCS104|Sistemas Operativos|2-0-2|5|
            1|TCS105|Matemática Aplicada|2-2-0|5|
            1|TCS106|Comunicación Efectiva|2-2-0|4|
            2|TCS201|Programación para Ciberseguridad|2-0-4|5|TCS101
            2|TCS202|Redes de Computadores II|2-0-4|6|TCS103
            2|TCS203|Administración de Sistemas Linux|2-0-4|5|TCS104
            2|TCS204|Fundamentos de Ciberseguridad|2-2-2|6|TCS102,TCS103
            2|TCS205|Bases de Datos|2-0-2|5|TCS101
            2|TCS206|Inglés Técnico|2-2-0|3|
            3|TCS301|Seguridad de Redes|2-0-4|6|TCS202,TCS204
            3|TCS302|Seguridad de Sistemas Operativos|2-0-4|5|TCS203,TCS204
            3|TCS303|Criptografía Aplicada|2-2-2|5|TCS204
            3|TCS304|Seguridad de Aplicaciones Web|2-0-4|5|TCS201,TCS204
            3|TCS305|Monitoreo y Detección de Amenazas|2-0-4|5|TCS202,TCS204
            3|TCS306|Legislación y Ética en Ciberseguridad|2-2-0|4|TCS204
            4|TCS401|Ethical Hacking y Pruebas de Penetración|2-0-4|6|TCS301,TCS302
            4|TCS402|Gestión de Incidentes de Ciberseguridad|2-0-4|5|TCS305
            4|TCS403|Seguridad en Cloud|2-0-4|5|TCS301,TCS302
            4|TCS404|Análisis Forense Digital|2-0-4|5|TCS302
            4|TCS405|Proyecto de Ciberseguridad|0-0-6|6|TCS301,TCS302,TCS305
            4|TCS406|Gestión de Riesgos de Ciberseguridad|2-2-0|3|TCS204
            """;

    @Override
    @Transactional
    public void run(String... args) {
        if (careerRepository.count() > 0) {
            return;
        }
        log.info("Empty database: loading Appendix A study plans and demo data");

        StudyPlan tap = createCareerWithPlan("10450", "Técnico Analista Programador",
                "Formación en desarrollo de software y aplicaciones.", "2021.3", TAP_2021_3);
        createCareerWithPlan("10451", "Técnico en Análisis de Datos",
                "Formación en análisis, visualización y gestión de datos.", "2022.3", TAD_2022_3);
        createCareerWithPlan("10452", "Técnico en Ciberseguridad",
                "Formación en protección de sistemas, redes y aplicaciones.", "2021.3", TCS_2021_3);

        AcademicPeriod period = new AcademicPeriod();
        period.setYear(2027);
        period.setSemester(1);
        period.setStatus(PeriodStatus.OPEN);
        periodRepository.save(period);

        createTeacher("11111111-1", "Ana Rojas", "ana.rojas@ipt.cl", "Ingeniera Informática", "Magíster");
        createTeacher("12222222-2", "Luis Pérez", "luis.perez@ipt.cl", "Ingeniero de Software", "Licenciado");

        createStudent("21111111-1", "Pedro Soto", "pedro.soto@ipt.cl", tap);
        createStudent("22222222-2", "María González", "maria.gonzalez@ipt.cl", tap);
    }

    private StudyPlan createCareerWithPlan(String code, String name, String description,
                                           String planIdentifier, String subjects) {
        Career career = new Career();
        career.setCode(code);
        career.setName(name);
        career.setDescription(description);
        career.setStatus(CareerStatus.ACTIVE);
        careerRepository.save(career);

        StudyPlan plan = new StudyPlan();
        plan.setIdentifier(planIdentifier);
        plan.setCareer(career);
        plan.setStatus(StudyPlanStatus.CURRENT);
        studyPlanRepository.save(plan);

        Map<String, Subject> byCode = new HashMap<>();
        for (String line : subjects.strip().split("\n")) {
            String[] parts = line.split("\\|", -1);
            String[] tel = parts[3].split("-");
            Subject subject = new Subject();
            subject.setSemester(Integer.parseInt(parts[0]));
            subject.setCode(parts[1]);
            subject.setName(parts[2]);
            subject.setTheoryHours(Integer.parseInt(tel[0]));
            subject.setExerciseHours(Integer.parseInt(tel[1]));
            subject.setLabHours(Integer.parseInt(tel[2]));
            subject.setSct(Integer.parseInt(parts[4]));
            subject.setStudyPlan(plan);
            if (!parts[5].isBlank()) {
                Arrays.stream(parts[5].split(","))
                        .map(String::trim)
                        .map(byCode::get)
                        .forEach(subject.getPrerequisites()::add);
            }
            byCode.put(subject.getCode(), subjectRepository.save(subject));
        }
        return plan;
    }

    private void createTeacher(String run, String fullName, String email, String title, String degree) {
        Teacher teacher = new Teacher();
        teacher.setRun(run);
        teacher.setFullName(fullName);
        teacher.setEmail(email);
        teacher.setProfessionalTitle(title);
        teacher.setAcademicDegree(degree);
        teacher.setStatus(TeacherStatus.ACTIVE);
        teacherRepository.save(teacher);
    }

    private void createStudent(String run, String fullName, String email, StudyPlan plan) {
        Student student = new Student();
        student.setRun(run);
        student.setFullName(fullName);
        student.setEmail(email);
        student.setCareer(plan.getCareer());
        student.setStudyPlan(plan);
        student.setStatus(StudentStatus.REGULAR);
        studentRepository.save(student);
    }
}