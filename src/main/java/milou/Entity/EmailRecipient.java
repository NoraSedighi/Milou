package milou.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "email_recipients")
public class EmailRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Basic(optional = false)
    @Column(name = "email_code")
    private String emailCode;

    @Basic(optional = false)
    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "is_read")
    private boolean isRead;

    @ManyToOne
    @JoinColumn(name = "email_code", referencedColumnName = "code", insertable = false, updatable = false)
    private Email email;

    public EmailRecipient() {
    }

    public EmailRecipient(Email email, User recipientUser) {
        this.emailCode = email.getCode();
        this.recipientEmail = recipientUser.getEmail();
        this.isRead = false;
    }

    public String getEmailCode() {
        return emailCode;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }
}
