package icet.koco.posts.entity;

import icet.koco.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;


import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
@Table(name = "posts")
public class Post {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "problem_number")
    private Long problemNumber;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostCategory> postCategories = new ArrayList<>();

    public void addPostCategory(PostCategory postCategory) {
        this.postCategories.add(postCategory);
        postCategory.setPost(this);
    }

    @Version // 낙관적 락
    @Column(name = "version")
    private Long version = 0L;
}
