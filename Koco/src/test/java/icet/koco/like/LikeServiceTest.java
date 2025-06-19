package icet.koco.like;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import icet.koco.global.exception.AlreadyLikedException;
import icet.koco.global.exception.ForbiddenException;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.posts.dto.like.LikeResponseDto;
import icet.koco.posts.entity.Like;
import icet.koco.posts.entity.Post;
import icet.koco.posts.repository.LikeRepository;
import icet.koco.posts.repository.PostRepository;
import icet.koco.posts.service.LikeService;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private final Long userId = 1L;
    private final Long postId = 100L;

    private User user;
    private Post post;

    @BeforeEach
    void setup() {
        user = User.builder().id(userId).build();
        post = Post.builder()
            .id(postId)
            .likeCount(0)
            .version(0L)
            .build();
    }

    @Test
    @DisplayName("좋아요 등록에 성공하면 LikeResponseDto 반환")
    void createLike_성공() {
        // given
        given(likeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(false);
        given(postRepository.findByIdAndDeletedAtIsNull(postId)).willReturn(Optional.of(post));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(likeRepository.save(any(Like.class))).willReturn(null);
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        LikeResponseDto result = likeService.createLike(userId, postId);

        // then
        assertThat(result.getPostId()).isEqualTo(postId);
        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 좋아요를 누른 게시글에 다시 누르면 AlreadyLikedException 발생")
    void createLike_이미좋아요누름_예외() {
        // given
        given(likeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> likeService.createLike(userId, postId))
            .isInstanceOf(AlreadyLikedException.class)
            .hasMessage("이미 좋아요를 누른 게시글입니다.");
    }

    @Test
    @DisplayName("좋아요 등록 중 낙관적 락 충돌 3회 실패 시 예외 발생")
    void createLike_낙관적락실패_예외() {
        // given
        given(likeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(false);
        given(postRepository.findByIdAndDeletedAtIsNull(postId)).willReturn(Optional.of(post));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(likeRepository.save(any(Like.class))).willReturn(null);

        // 낙관적 락 실패 시 재시도 3회 다 실패하도록
        given(postRepository.save(any(Post.class)))
            .willThrow(ObjectOptimisticLockingFailureException.class);

        given(postRepository.findByIdAndDeletedAtIsNull(postId))
            .willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> likeService.createLike(userId, postId))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("동시성 문제로 좋아요 등록에 실패했습니다.");
    }

    @Test
    @DisplayName("좋아요 삭제에 성공하면 예외 없이 완료")
    void deleteLike_성공() {
        Like like = Like.builder()
            .id(1L)
            .user(user)
            .post(post)
            .build();

        // given
        given(postRepository.findByIdAndDeletedAtIsNull(postId)).willReturn(Optional.of(post));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        given(likeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(true);
        given(likeRepository.findByUserIdAndPostId(userId, postId)).willReturn(Optional.of(like));

        willDoNothing().given(likeRepository).delete(any(Like.class));

        given(postRepository.save(any(Post.class))).willReturn(post);

        // when & then
        assertThatCode(() -> likeService.deleteLike(userId, postId))
            .doesNotThrowAnyException();

        // 메서드 실행 검증
        verify(likeRepository).delete(any(Like.class));
        verify(postRepository, atLeastOnce()).save(post);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 좋아요 삭제 시 ResourceNotFoundException 발생")
    void deleteLike_사용자없음_예외() {
        // given
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.deleteLike(userId, postId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 좋아요 삭제 시 ResourceNotFoundException 발생")
    void deleteLike_게시글없음_예외() {
        // given
        given(postRepository.findByIdAndDeletedAtIsNull(postId)).willReturn(Optional.empty());
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> likeService.deleteLike(userId, postId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("좋아요를 누르지 않은 게시글에 대해 삭제 시 AlreadyLikedException 발생")
    void deleteLike_이미좋아요취소_예외() {
        // given
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(postRepository.findByIdAndDeletedAtIsNull(postId)).willReturn(Optional.of(post));
        given(likeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(false);

        // when
        assertThatThrownBy(() -> likeService.deleteLike(userId, postId))
            .isInstanceOf(AlreadyLikedException.class)
            .hasMessage("이미 좋아요 취소를 한 게시글입니다.");
    }

    @Test
    @DisplayName("좋아요 엔티티가 존재하지 않을 경우 ResourceNotFoundException 발생")
    void deleteLike_좋아요없음_예외() {
        // given
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(postRepository.findByIdAndDeletedAtIsNull(postId)).willReturn(Optional.of(post));
        given(likeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(true);
        given(likeRepository.findByUserIdAndPostId(userId, postId)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> likeService.deleteLike(userId, postId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("좋아요가 존재하지 않습니다.");

    }

    @Test
    @DisplayName("본인이 누른 좋아요가 아닐 경우 ForbiddenException 발생")
    void deleteLike_다른사용자좋아요_예외() {
        // given
        User otherUser = User.builder().id(999L).build(); // userId와 다름
        Like like = Like.builder().id(1L).user(otherUser).post(post).build();

        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(postRepository.findByIdAndDeletedAtIsNull(postId)).willReturn(Optional.of(post));
        given(likeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(true);
        given(likeRepository.findByUserIdAndPostId(userId, postId)).willReturn(Optional.of(like));

        // when & then
        assertThatThrownBy(() -> likeService.deleteLike(userId, postId))
            .isInstanceOf(ForbiddenException.class)
            .hasMessage("본인의 좋아요만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("좋아요 삭제 중 낙관적 락 충돌 3회 실패 시 예외 발생")
    void deleteLike_낙관적락실패_예외() {
        Like like = Like.builder()
            .id(1L)
            .user(user)
            .post(post)
            .build();

        // given
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(postRepository.findByIdAndDeletedAtIsNull(postId)).willReturn(Optional.of(post));
        given(likeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(true);
        given(likeRepository.findByUserIdAndPostId(userId, postId)).willReturn(Optional.of(like));
        willDoNothing().given(likeRepository).delete(any(Like.class));

        // 낙관적 락 실패 시 재시도 3회 다 실패하도록
        given(postRepository.save(any(Post.class)))
            .willThrow(ObjectOptimisticLockingFailureException.class);

        given(postRepository.findByIdAndDeletedAtIsNull(postId))
            .willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> likeService.deleteLike(userId, postId))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("동시성 문제로 좋아요 취소에 실패했습니다.");
    }
}
