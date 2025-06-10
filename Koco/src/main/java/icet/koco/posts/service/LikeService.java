package icet.koco.posts.service;

import icet.koco.global.exception.AlreadyLikedException;
import icet.koco.global.exception.ForbiddenException;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.posts.dto.like.LikeResponseDto;
import icet.koco.posts.entity.Like;
import icet.koco.posts.entity.Post;
import icet.koco.posts.repository.LikeRepository;
import icet.koco.posts.repository.PostRepository;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponseDto createLike(Long userId, Long postId) {
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new AlreadyLikedException("이미 좋아요를 누른 게시글입니다.");
        }

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
            .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글입니다."));

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다"));

        Like like = Like.builder()
            .post(post)
            .user(user)
            .createdAt(LocalDateTime.now())
            .build();
        likeRepository.save(like);

        // 낙관적 락으로 likeCount 증가 시도
        boolean success = false;
        int retry = 0;
        while (!success && retry < 3) {
            try {
                post.setLikeCount(post.getLikeCount() + 1);
                postRepository.save(post);
                success = true;
            } catch (ObjectOptimisticLockingFailureException e) {
                retry++;
                post = postRepository.findByIdAndDeletedAtIsNull(postId).orElseThrow();
            }
        }

        if (!success) {
            throw new RuntimeException("동시성 문제로 좋아요 등록에 실패했습니다.");
        }

        return LikeResponseDto.builder()
            .postId(postId)
            .liked(true)
            .likeCount(post.getLikeCount())
            .build();
    }

    @Transactional
    public void deleteLike(Long userId, Long postId) {
        // 사용자, 게시글 존재 확인
        userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
            .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글입니다."));

        // 좋아요 취소 확인
        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new AlreadyLikedException("이미 좋아요 취소를 한 게시글입니다.");
        }

        // 좋아요 조회
        Like like = likeRepository.findByUserIdAndPostId(userId, postId)
            .orElseThrow(() -> new ResourceNotFoundException("좋아요가 존재하지 않습니다."));

        // 본인 확인
        if (!like.getUser().getId().equals(userId)) {
            throw new ForbiddenException("본인의 좋아요만 취소할 수 있습니다.");
        }

        // 좋아요 삭제
        likeRepository.delete(like);

        // 낙관적 락으로 likeCount 감소 (retry 3번)
        boolean success = false;
        int retry = 0;
        while (!success && retry < 3) {
            try {
                post.setLikeCount(post.getLikeCount() - 1);
                postRepository.save(post);
                success = true;
            } catch (ObjectOptimisticLockingFailureException e) {
                retry++;
                postRepository.findByIdAndDeletedAtIsNull(postId)
                    .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 게시글입니다."));
            }
        }

        if (!success) {
            throw new RuntimeException("동시성 문제로 좋아요 취소에 실패했습니다.");
        }
    }
}
