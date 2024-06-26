package app.entity;

import jakarta.persistence.*;

@Table(name = "users")
@Entity
public class Person5 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nick_name")
    private String name;
    @Column(name = "old")
    private Integer age;
    @Column(nullable = false)
    private String email;
    @Transient
    private Integer index;

    public Person5(String name, Integer age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public Person5(Long id, String name, Integer age, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public Person5() {
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
