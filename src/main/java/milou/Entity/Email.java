package milou.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emails")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Basic(optional = false)
    @Column(name = "code", unique = true)
    private String code;

    @Basic(optional = false)
    @Column(name = "sender_email")
    private String senderEmail;

    @Basic(optional = false)
    @Column(name = "subject")
    private String subject;

    @Column(name = "body", columnDefinition = "text")
    private String body;

    @Basic(optional = false)
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public Email() {

    }

    public Email(String code, String senderEmail, String subject, String body, LocalDateTime sentAt) {
        this.code = code;
        this.senderEmail = senderEmail;
        this.subject = subject;
        this.body = body;
        this.sentAt = sentAt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    @Override
    public String toString() {
        return "Email{" +
                "code='" + code + '\'' +
                ", senderEmail='" + senderEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}
