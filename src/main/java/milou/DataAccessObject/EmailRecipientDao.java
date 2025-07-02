package milou.DataAccessObject;

import milou.Entity.EmailRecipient;
import org.hibernate.Session;

import java.util.List;

public class EmailRecipientDao {
    public void save(Session session, EmailRecipient recipient) {
        session.persist(recipient);
    }

    public void delete(Session session, EmailRecipient recipient) {
        session.remove(recipient);
    }

    public List<EmailRecipient> findByRecipientEmail(Session session, String recipientEmail) {
        return session.createQuery("FROM EmailRecipient WHERE recipientEmail = :recipientEmail", EmailRecipient.class)
                .setParameter("recipientEmail", recipientEmail)
                .getResultList();
    }

    public EmailRecipient findByEmailCodeAndRecipientEmail(Session session, String emailCode, String recipientEmail) {
        return session.createQuery("FROM EmailRecipient WHERE emailCode = :emailCode AND recipientEmail = :recipientEmail", EmailRecipient.class)
                .setParameter("emailCode", emailCode)
                .setParameter("recipientEmail", recipientEmail)
                .uniqueResult();
    }

    public List<EmailRecipient> findByEmailCode(Session session, String code) {
        return session.createQuery("FROM EmailRecipient WHERE email.code = :code", EmailRecipient.class)
                .setParameter("code", code)
                .list();
    }

}
