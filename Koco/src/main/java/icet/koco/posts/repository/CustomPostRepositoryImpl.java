package icet.koco.posts.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import icet.koco.posts.dto.post.AuthorDto;
import icet.koco.posts.dto.post.CategoryDto;
import icet.koco.posts.dto.post.TopPostResponseDto;
import icet.koco.posts.entity.Post;
import icet.koco.posts.entity.QLike;
import icet.koco.posts.entity.QPost;
import icet.koco.posts.entity.QPostCategory;
import icet.koco.problemSet.entity.QCategory;
import icet.koco.user.entity.QUser;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomPostRepositoryImpl implements CustomPostRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchPosts(List<String> categoryNames, String keyword, Long cursorId, int size) {
        QPost post = QPost.post;
        QCategory category = QCategory.category;
        QPostCategory postCategory = QPostCategory.postCategory;

        JPQLQuery<Long> subQuery = queryFactory
                .select(post.id)
                .from(post)
                .where(
                        post.deletedAt.isNull(),
                        cursorId != null ? post.id.loe(cursorId) : null,
                        StringUtils.hasText(keyword)
                                ? (keyword.matches("\\d+")
                                ? post.problemNumber.eq(Long.parseLong(keyword))
                                .or(post.title.containsIgnoreCase(keyword))
                                : post.title.containsIgnoreCase(keyword))
                                : null
                );

        if (categoryNames != null && !categoryNames.isEmpty()) {
            subQuery = queryFactory
                    .select(post.id)
                    .from(post)
                    .join(post.postCategories, postCategory)
                    .join(postCategory.category, category)
                    .where(
                            post.deletedAt.isNull(),
                            cursorId != null ? post.id.loe(cursorId) : null,
                            StringUtils.hasText(keyword)
                                    ? (keyword.matches("\\d+")
                                    ? post.problemNumber.eq(Long.parseLong(keyword))
                                    .or(post.title.containsIgnoreCase(keyword))
                                    : post.title.containsIgnoreCase(keyword))
                                    : null,
                            category.name.in(categoryNames)
                    )
                    .groupBy(post.id)
                    .having(category.id.countDistinct().eq((long) categoryNames.size()));
        }

        List<Long> postIds = subQuery
                .orderBy(post.createdAt.desc())
                .limit(size)
                .fetch();

        if (postIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .selectFrom(post)
                .distinct()
                .leftJoin(post.postCategories, postCategory).fetchJoin()
                .leftJoin(postCategory.category, category).fetchJoin()
                .where(post.id.in(postIds))
                .orderBy(post.id.desc())
                .fetch();
    }


    @Override
    public List<Post> getMyPosts(Long userId, Long cursorId, int size) {
        QPost post = QPost.post;
        QCategory category = QCategory.category;
        QPostCategory postCategory = QPostCategory.postCategory;

        var subQuery = queryFactory
            .select(post.id)
            .from(post)
            .join(post.postCategories, postCategory)
            .join(postCategory.category, category)
            .where(
                post.user.id.eq(userId),
                post.deletedAt.isNull(),
                cursorId != null ? post.id.loe(cursorId) : null
            )
            .groupBy(post.id);

        List<Long> postIds = subQuery
            .orderBy(post.id.desc())
            .limit(size)
            .fetch();

        if (postIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .selectFrom(post)
            .leftJoin(post.postCategories, postCategory).fetchJoin()
            .leftJoin(postCategory.category, category).fetchJoin()
            .where(post.id.in(postIds))
            .orderBy(post.id.desc())
            .fetch();
    }

    @Override
    public List<TopPostResponseDto> findTopPostsDtoByLikesLastWeek(LocalDateTime start, LocalDateTime end, int limit) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        QLike like = QLike.like;
        QPostCategory postCategory = QPostCategory.postCategory;
        QCategory category = QCategory.category;

        // 좋아요 수 기준으로 상위 게시물 ID + Count 조회
        List<Tuple> likeCounts = queryFactory
            .select(like.post.id, like.post.id.count())
            .from(like)
            .join(like.post, post)
            .where(
                like.createdAt.between(start, end),
                post.deletedAt.isNull()
            )
            .groupBy(like.post.id)
            .orderBy(like.post.id.count().desc(), like.post.id.asc())
            .limit(limit)
            .fetch();

        if (likeCounts.isEmpty()) return List.of();

        // 선택된 postIds
        List<Long> postIds = likeCounts.stream()
            .map(t -> t.get(like.post.id))
            .collect(Collectors.toList());

        // 좋아요 수 맵핑
        Map<Long, Long> postIdToLikeCount = likeCounts.stream()
            .collect(Collectors.toMap(
                t -> t.get(like.post.id),
                t -> t.get(like.post.id.count())
            ));

        // Post, User, Category, Comment 수를 함께 조회
        List<Post> posts = queryFactory
            .selectFrom(post)
            .leftJoin(post.user, user).fetchJoin()
            .leftJoin(post.postCategories, postCategory).fetchJoin()
            .leftJoin(postCategory.category, category).fetchJoin()
            .where(post.id.in(postIds))
            .fetch();

        // ID 기준 정렬 유지
        Map<Long, Post> postMap = posts.stream()
            .collect(Collectors.toMap(Post::getId, Function.identity()));

        return postIds.stream()
            .map(id -> {
                Post p = postMap.get(id);
                return TopPostResponseDto.builder()
                    .postId(p.getId())
                    .title(p.getTitle())
                    .problemNumber(p.getProblemNumber())
                    .likeCount(postIdToLikeCount.get(id).intValue())
                    .createdAt(p.getCreatedAt().toString())
                    .categories(p.getPostCategories().stream()
                        .map(pc -> CategoryDto.from(pc.getCategory()))
                        .toList())
                    .author(AuthorDto.from(p.getUser()))
                    .commentCount(p.getCommentCount())
                    .build();
            })
            .collect(Collectors.toList());
    }
}