package icet.koco.user.entity;

import icet.koco.enums.UserRole;
import icet.koco.posts.entity.Comment;
import icet.koco.posts.entity.Like;
import icet.koco.posts.entity.Post;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@Table(name = "Users")
public class User {

    @Id // PK
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //회원 이메일
    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    // 회원 이름
    @Column(name = "name", nullable = false, length = 10)
    private String name;

    // 회원 닉네임
    @Column(name = "nickname", length = 15)
    private String nickname;

    // 회원 역할, 디폴트 값: user(일반)
    @Builder.Default
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    // 회원 상태메세지
    @Column(name = "status_msg", length = 50)
    private String statusMsg;

    // 회원 프로필 이미지
    @Lob
    @Column(name = "profile_img_url")
    private String profileImgUrl;

    // 회원 계정 생성일
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 회원 계정 탈퇴일
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
