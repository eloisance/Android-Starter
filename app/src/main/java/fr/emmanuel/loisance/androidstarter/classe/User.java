package fr.emmanuel.loisance.androidstarter.classe;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class User {

    private int id;
    @SerializedName("id_google")
    private String idGoogle;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String provider;
    @SerializedName("registration_date")
    private Date registrationDate;

    public User() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdGoogle() {
        return idGoogle;
    }

    public void setIdGoogle(String idGoogle) {
        this.idGoogle = idGoogle;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getDisplayName() {
        return this.firstname + " " + this.lastname;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", idGoogle='" + idGoogle + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", provider='" + provider + '\'' +
                ", registrationDate=" + registrationDate +
                '}';
    }
}
