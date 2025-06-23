package com.callog.callog_user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "user_table")
@NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "height")
    private Integer height;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")  // 성별 (M/F 또는 MALE/FEMALE)
    private String gender;

    //현재년도 기준으로 나이 계산
    public int getCurrentAge() {
        if (age == null) return 0;
        return LocalDate.now().getYear() - age;
    }
    //출생년도
    public Integer getBirthYear() { return age; }
    public void setBirthYear(Integer age) { this.age = age; }
}
