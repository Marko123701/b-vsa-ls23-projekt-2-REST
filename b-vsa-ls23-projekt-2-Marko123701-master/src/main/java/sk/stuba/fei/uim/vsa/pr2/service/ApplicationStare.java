package sk.stuba.fei.uim.vsa.pr2.service;

import sk.stuba.fei.uim.vsa.pr2.domain.*;

import javax.persistence.*;
import java.util.*;

public class ApplicationStare extends AbstractThesisService<Student, Educator, FinalThesis>{

    public static void main(String[] args) {

    }

    public ApplicationStare() {
        super();
    }

    @Override
    public Student createStudent(Long aisId, String name, String email) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TypedQuery<Long> countQuery1 = em.createQuery("SELECT COUNT(s) FROM Student s WHERE s.email = :email", Long.class);
            countQuery1.setParameter("email", email);
            Long count1 = countQuery1.getSingleResult();
            if (count1 > 0) {
                return null;
            }
            TypedQuery<Long>countQuery2 = em.createQuery("SELECT COUNT(s) FROM Student s WHERE s.aisId = :aisId", Long.class);
            countQuery2.setParameter("aisId", aisId);
            Long count2 = countQuery2.getSingleResult();
            if (count2 > 0) {
                return null;
            }

            Student student = new Student();
            student.setAisId(aisId);
            student.setName(name);
            student.setEmail(email);
            student.setYear(1);
            student.setTerm(1);
            student.setProgramme("default");
            em.persist(student);
            tx.commit();
            return student;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public Student getStudent(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("id parameter cannot be null");
        }
        EntityManager em = null;
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
            em = emf.createEntityManager();
            return em.find(Student.class, id);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public Student updateStudent(Student student) {
        if (student == null || student.getId() == null) {
            throw new IllegalArgumentException("Invalid student entity or entity identifier.");
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Student updatedStudent = em.merge(student);
            em.getTransaction().commit();
            return updatedStudent;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Student> getStudents() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        List<Student> students;

        TypedQuery<Student> query = em.createNamedQuery(Student.FIND_ALL, Student.class);
        students = query.getResultList();
        em.close();

        return students != null ? students : Collections.emptyList();
    }


    @Override
    public Student deleteStudent(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null.");
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        Student student = em.find(Student.class, id);

        if (student == null) {
            return null;
        }

        FinalThesis finalThesis = this.getThesisByStudent(id);
        if (finalThesis != null) {
            finalThesis.setStatus(Status.FREE_TO_TAKE);
            finalThesis.setStudent(null);
            em.merge(finalThesis);
        }
        try {
            em.getTransaction().begin();
            em.remove(student);
            em.getTransaction().commit();
            return student;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public Educator createTeacher(Long aisId, String name, String email, String department) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        TypedQuery<Educator> query = em.createQuery("SELECT e FROM Educator e WHERE e.email = :email", Educator.class);
        query.setParameter("email", email);
        List<Educator> existingEducators = query.getResultList();
        if (!existingEducators.isEmpty()) {
            return null;
        }
        query = em.createQuery("SELECT e FROM Educator e WHERE e.aisId = :aisId", Educator.class);
        query.setParameter("aisId", aisId);
        existingEducators = query.getResultList();
        if (!existingEducators.isEmpty()) {
            return null;
        }

        Educator educator = new Educator();
        educator.setAisId(aisId);
        educator.setName(name);
        educator.setEmail(email);
        educator.setInstitute(department);
        educator.setDepartment(department);

        try {
            em.getTransaction().begin();
            em.persist(educator);
            em.getTransaction().commit();
            return educator;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return null;
        }
    }

    @Override
    public Educator getTeacher(Long id) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");

        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null.");
        }
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Educator.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public Educator updateTeacher(Educator teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("Teacher instance cannot be null.");
        }
        if (teacher.getId() == null) {
            throw new IllegalArgumentException("Teacher identifier cannot be null.");
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Educator updatedTeacher = em.merge(teacher);
            tx.commit();
            return updatedTeacher;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Educator> getTeachers() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        List<Educator> educators;

        try {
            TypedQuery<Educator> query = em.createNamedQuery(Educator.FIND_ALL, Educator.class);
            educators = query.getResultList();
        } finally {
            em.close();
        }

        return educators != null ? educators : Collections.emptyList();
    }

    @Override
    public Educator deleteTeacher(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null");
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            Educator educator = em.find(Educator.class, id);
            if (educator != null) {
                for (FinalThesis finalThesis : this.getThesesByTeacher(id)) {
                    this.deleteThesis(finalThesis.getId());
                }
                    em.remove(educator);
            }
            em.getTransaction().commit();
            return educator;
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public FinalThesis makeThesisAssignment(Long supervisor, String title, String type, String description) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        Educator educator = this.getTeacher(supervisor);

        if (supervisor == null) {
            throw new IllegalArgumentException("Invalid supervisor id provided.");
        }
        if(educator == null){
            return null;
        }

        String registrationNumber = "FEI-" + UUID.randomUUID();

        String workplace = educator.getInstitute() + ", " + educator.getDepartment();

        Date publicationDate = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(publicationDate);
        calendar.add(Calendar.MONTH, 3);
        Date submissionDeadline = calendar.getTime();

        Type thesisType;
        try {
            thesisType = Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        FinalThesis finalThesis = new FinalThesis();
        finalThesis.setRegistrationNumber(registrationNumber);
        finalThesis.setTitle(title);
        finalThesis.setDescription(description);
        finalThesis.setPublicationDate(publicationDate);
        finalThesis.setSubmissionDeadline(submissionDeadline);
        finalThesis.setType(thesisType);
        finalThesis.setStatus(Status.FREE_TO_TAKE);
        finalThesis.setEducator(educator);

        tx.begin();
        em.persist(finalThesis);
        tx.commit();

        return finalThesis;
    }

    @Override
    public FinalThesis assignThesis(Long thesisId, Long studentId) {

        if (thesisId == null || studentId == null) {
            throw new IllegalArgumentException("Thesis and student identifiers cannot be null.");
        }

        FinalThesis thesis = this.getThesis(thesisId);
        if (thesis == null) {
            return null;
        }

        if (!thesis.getStatus().equals(Status.FREE_TO_TAKE)) {
            return null;
        }

        Date currentDate = new Date();
        if (currentDate.after(thesis.getSubmissionDeadline())) {
            return null;
        }

        Student student = this.getStudent(studentId);
        if (student == null) {
            return null;
        }

        if (student.getFinalThesis() != null) {
            return null;
        }

        thesis.setStudent(student);
        thesis.setStatus(Status.IN_PROGRESS);

        student.setFinalThesis(thesis);
        this.updateThesis(thesis);
        this.updateStudent(student);

        return thesis;
    }

    @Override
    public FinalThesis submitThesis(Long thesisId) {
        if (thesisId == null) {
            throw new IllegalArgumentException("Thesis identifier cannot be null");
        }

        FinalThesis thesis = this.getThesis(thesisId);
        if (thesis == null) {
            return null;
        }

        if (thesis.getStatus() == Status.SUBMITTED || thesis.getStatus() == Status.FREE_TO_TAKE) {
            return null;
        }

        if (thesis.getSubmissionDeadline().before(new Date())) {
            return null;
        }

        Student student = thesis.getStudent();
        if (student == null) {
            return null;
        }

        thesis.setStatus(Status.SUBMITTED);

        this.updateThesis(thesis);
        return thesis;
    }

    @Override
    public FinalThesis deleteThesis(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Final work identifier cannot be null");
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        FinalThesis thesis = this.getThesis(id);

        if (thesis == null) {
            return null;
        }

        em.getTransaction().begin();
        FinalThesis mergedThesis = em.merge(thesis);
        em.remove(mergedThesis);
        em.getTransaction().commit();

        return thesis;
    }

    @Override
    public List<FinalThesis> getTheses() {
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
            EntityManager em = emf.createEntityManager();
            return em.createNamedQuery(FinalThesis.FIND_ALL, FinalThesis.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public List<FinalThesis> getThesesByTeacher(Long teacherId) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        TypedQuery<FinalThesis> query = em.createNamedQuery(
                FinalThesis.FIND_BY_TEACHER_ID, FinalThesis.class);
        query.setParameter("id", teacherId);
        List<FinalThesis> finalTheses = query.getResultList();
        if (finalTheses == null) {
            return Collections.emptyList();
        }
        return finalTheses;
    }

    @Override
    public FinalThesis getThesisByStudent(Long studentId) {
        if (studentId == null) {
            return null;
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<FinalThesis> query = em.createQuery(
                    "SELECT ft FROM FinalThesis ft WHERE ft.student.id = :studentId", FinalThesis.class);
            query.setParameter("studentId", studentId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
        }


    @Override
    public FinalThesis getThesis(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null.");
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();

        try {
            return em.find(FinalThesis.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
        }

    @Override
    public FinalThesis updateThesis(FinalThesis thesis) {
        if (thesis == null || thesis.getId() == null) {
            throw new IllegalArgumentException("Invalid student entity or entity identifier.");
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-1");
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            FinalThesis updatedThesis = em.merge(thesis);
            em.getTransaction().commit();
            return updatedThesis;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return null;
        } finally {
            em.close();
        }
    }
}