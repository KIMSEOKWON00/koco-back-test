package icet.koco.posts.service;

import icet.koco.alarm.dto.AlarmRequestDto;
import icet.koco.alarm.service.AlarmService;
import icet.koco.enums.AlarmType;
import icet.koco.enums.ErrorMessage;
import icet.koco.global.exception.ForbiddenException;
import icet.koco.global.exception.ResourceNotFoundException;
import icet.koco.posts.dto.comment.CommentCreateEditRequestDto;
import icet.koco.posts.dto.comment.CommentCreateEditResponseDto;
import icet.koco.posts.dto.comment.CommentListResponseDto;
import icet.koco.posts.entity.Comment;
import icet.koco.posts.entity.Post;
import icet.koco.posts.repository.CommentRepository;
import icet.koco.posts.repository.PostRepository;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final AlarmService alarmService;


    @Transactional
    public CommentCreateEditResponseDto createComment(Long userId, Long postId, CommentCreateEditRequestDto requestDto) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.POST_NOT_FOUND));

        Comment comment = Comment.builder()
            .user(user)
            .post(post)
            .comment(requestDto.getContent())
            .createdAt(LocalDateTime.now())
            .build();

        commentRepository.save(comment);

        // 본인이 댓글 단 거 말고 알림 생성
        if (!post.getUser().getId().equals(user.getId())) {
            AlarmRequestDto alarmRequestDto = AlarmRequestDto.builder()
                .postId(post.getId())
                .senderId(user.getId())
                .alarmType(AlarmType.COMMENT)
                .build();

            alarmService.createAlarmInternal(alarmRequestDto);
        }

        postRepository.incrementCommentCount(postId);

        return CommentCreateEditResponseDto.builder()
            .commentId(comment.getId())
            .build();
    }

    @Transactional
    public CommentCreateEditResponseDto editComment(Long userId, Long postId, Long commentId, CommentCreateEditRequestDto requestDto) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.POST_NOT_FOUND));

        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.COMMENT_NOT_FOUND));

        // 권한 체크
        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorMessage.NO_COMMENT_PERMISSION);
        }

        comment.setComment(requestDto.getContent());

        return CommentCreateEditResponseDto.builder()
            .commentId(comment.getId())
            .build();
    }

    @Transactional
    public void deleteComment(Long userId, Long postId, Long commentId) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND));

		Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.POST_NOT_FOUND));

		Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
			.orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.COMMENT_NOT_FOUND));

        // 권한 체크
		if (!comment.getUser().getId().equals(userId)) {
			throw new ForbiddenException(ErrorMessage.NO_COMMENT_PERMISSION);
		}

        comment.setDeletedAt(LocalDateTime.now());
        postRepository.decreaseCommentCount(postId);
    }

    @Transactional(readOnly = true)
    public CommentListResponseDto getComments(Long postId, Long cursorId, Integer size) {
        List<Comment> comments;

        if (cursorId == null) {
            comments = commentRepository.findTopByPostIdAndDeletedAtIsNullOrderByIdDesc(postId, size + 1);
        } else {
            comments = commentRepository.findNextPage(postId, cursorId, size + 1);
        }

        boolean hasNext = comments.size() > size;
        if (hasNext) {
            comments = comments.subList(0, size);
        }

        Long nextCursorId = hasNext ? comments.get(comments.size() - 1).getId() : null;

        List<CommentListResponseDto.CommentDetailDto> commentDtos = comments.stream()
            .map(c -> CommentListResponseDto.CommentDetailDto.builder()
                .id(c.getId())
                .comment(c.getComment())
                .createdAt(c.getCreatedAt())
                .author(CommentListResponseDto.AuthorDto.builder()
                    .userId(c.getUser().getId())
                    .nickname(c.getUser().getNickname())
                    .imgUrl(c.getUser().getProfileImgUrl())
                    .build())
                .build())
            .toList();

        return CommentListResponseDto.builder()
            .postId(postId)
            .nextCursorId(nextCursorId)
            .hasNext(hasNext)
            .comments(commentDtos)
            .build();
    }
}
