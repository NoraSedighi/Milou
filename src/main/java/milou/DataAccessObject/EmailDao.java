package milou.DataAccessObject;

import milou.Entity.Email;
import org.hibernate.Session;

import java.util.List;

public class EmailDao {
    public void save(Session session, Email email) {
        session.persist(email);
    }

    public void delete(Session session, Email email) {
        session.remove(email);
    }

    public Email findByCode(Session session, String code) {
        return session.createQuery("FROM Email WHERE code = :code", Email.class)
                .setParameter("code", code)
                .uniqueResult();
    }

    public List<Email> findSentEmailsBySenderEmail(Session session, String senderEmail) {
        return session.createQuery("FROM Email WHERE senderEmail = :senderEmail", Email.class)
                .setParameter("senderEmail", senderEmail)
                .getResultList();
    }
}
