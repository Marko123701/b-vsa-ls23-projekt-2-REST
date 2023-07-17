package sk.stuba.fei.uim.vsa.pr2.service;

import sk.stuba.fei.uim.vsa.pr2.domain.*;

import javax.persistence.*;
import java.util.*;

public class ThesesService {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("vsa-project-2");

    public FinalThesis makeThesisAssignment(String email,String title, String type, String description, String registrationNumber) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        Educator educator = this.getEducatorByEmail(email);


        if(educator == null){
            return null;
        }

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
        finalThesis.setEducator(educator);
        finalThesis.setRegistrationNumber(registrationNumber);
        finalThesis.setTitle(title);
        finalThesis.setDescription(description);
        finalThesis.setInstitute("default");
        finalThesis.setDepartment("default");
        finalThesis.setPublicationDate(publicationDate);
        finalThesis.setSubmissionDeadline(submissionDeadline);
        finalThesis.setType(thesisType);
        finalThesis.setStatus(Status.FREE_TO_TAKE);

        //educator.addFinalThesis(finalThesis);

        tx.begin();
        em.persist(finalThesis);
        tx.commit();

        em.close();

        return finalThesis;
    }

    public FinalThesis assignThesisByTeacher(Long thesisId, Long teacherId){
        EntityManager em = emf.createEntityManager();
        Educator educator = this.getTeacher(teacherId);
        FinalThesis thesis = getThesis(thesisId);

        if (teacherId == null) {
            throw new IllegalArgumentException("Invalid supervisor id provided.");
        }
        if(educator == null || thesis == null){
            return null;
        }

        thesis.setEducator(educator);
        this.updateThesis(thesis);

        return thesis;
    }

    public FinalThesis assignThesis(Long thesisId, Long studentId) {

        FinalThesis thesis = this.getThesis(thesisId);

        Student student = this.getStudent(studentId);

        thesis.setStudent(student);
        thesis.setStatus(Status.FREE_TO_TAKE);

        student.setFinalThesis(thesis);
        this.updateThesis(thesis);
        this.updateStudent(student);

        return thesis;
    }

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

    public FinalThesis deleteThesis(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Final work identifier cannot be null");
        }
        EntityManager em = emf.createEntityManager();
        FinalThesis thesis = this.getThesis(id);

        if (thesis == null) {
            return null;
        }

        em.getTransaction().begin();
        FinalThesis mergedThesis = em.merge(thesis);
        em.remove(mergedThesis);
        em.getTransaction().commit();

        em.close();

        return thesis;
    }

    public List<FinalThesis> getTheses() {
        try {
            EntityManager em = emf.createEntityManager();
            return em.createNamedQuery(FinalThesis.FIND_ALL, FinalThesis.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<FinalThesis> getThesesByTeacher(Long teacherId) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<FinalThesis> query = em.createNamedQuery(
                FinalThesis.FIND_BY_TEACHER_ID, FinalThesis.class);
        query.setParameter("id", teacherId);
        List<FinalThesis> finalTheses = query.getResultList();
        if (finalTheses == null) {
            return Collections.emptyList();
        }
        em.close();
        return finalTheses;
    }

    public FinalThesis getThesisByStudent(Long studentId) {
        if (studentId == null) {
            return null;
        }
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


    public FinalThesis getThesis(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Identifier cannot be null.");
        }

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

    public FinalThesis updateThesis(FinalThesis thesis) {
        if (thesis == null || thesis.getId() == null) {
            throw new IllegalArgumentException("Invalid student entity or entity identifier.");
        }
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

    public Educator getTeacher(Long id) {

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

    public Student getStudent(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("id parameter cannot be null");
        }
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            return em.find(Student.class, id);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public Student updateStudent(Student student) {
        if (student == null || student.getId() == null) {
            throw new IllegalArgumentException("Invalid student entity or entity identifier.");
        }
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

    public Educator getEducatorByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }

        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Educator> query = em.createQuery(
                    "SELECT e FROM Educator e WHERE e.email = :email", Educator.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
