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
    private String password;
    private String phone;
    private String provider;
    @SerializedName("registration_date")
    private Date registrationDate;

    public User(int id, String idGoogle, String firstname, String lastname, String email, String password, String provider, String phone, Date registrationDate) {
        setId(id);
        setIdGoogle(idGoogle);
        setFirstname(firstname);
        setLastname(lastname);
        setEmail(email);
        setPassword(password);
        setProvider(provider);
        setPhone(phone);
        setRegistrationDate(registrationDate);
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

}
