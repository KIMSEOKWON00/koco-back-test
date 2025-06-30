package icet.koco.posts.service;

import icet.koco.alarm.dto.AlarmRequestDto;
import icet.koco.alarm.service.AlarmService;
import icet.koco.enums.AlarmType;
import icet.koco.enums.ErrorMessage;
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
    private final AlarmService alarmService;

    @Transactional
    public LikeResponseDto createLike(Long userId, Long postId) {
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new AlreadyLikedException(ErrorMessage.ALREADY_LIKED_ERROR);
        }

		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.POST_NOT_FOUND));

        Like like = Like.builder()
            .post(post)
            .user(user)
            .createdAt(LocalDateTime.now())
            .build();
        likeRepository.save(like);

        if (!post.getUser().getId().equals(user.getId())) {
            AlarmRequestDto alarmRequestDto = AlarmRequestDto.builder()
                .postId(post.getId())
                .senderId(user.getId())
                .alarmType(AlarmType.LIKE)
                .build();

            alarmService.createAlarmInternal(alarmRequestDto);
        }

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
            throw new RuntimeException(ErrorMessage.LIKE_CONCURRENCY_FAILURE.getMessage());
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
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.POST_NOT_FOUND));


        // 좋아요 취소 확인
        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new AlreadyLikedException(ErrorMessage.ALREADY_UNLIKED_ERROR);
        }

        // 좋아요 조회
        Like like = likeRepository.findByUserIdAndPostId(userId, postId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.LIKE_NOT_FOUND));

        // 본인 확인
        if (!like.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorMessage.NO_LIKE_PERMISSION);
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
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.POST_NOT_FOUND));
            }
        }

        if (!success) {
            throw new RuntimeException(ErrorMessage.LIKE_CONCURRENCY_FAILURE.getMessage());
        }
    }
}
