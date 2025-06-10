package icet.koco.posts.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import icet.koco.posts.entity.Post;
import icet.koco.posts.entity.QPost;
import icet.koco.posts.entity.QPostCategory;
import icet.koco.problemSet.entity.QCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomPostRepositoryImpl implements CustomPostRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> searchPosts(List<String> categoryNames, String keyword, Long cursorId, int size) {
        QPost post = QPost.post;
        QCategory category = QCategory.category;
        QPostCategory postCategory = QPostCategory.postCategory;

        var subQuery = queryFactory
                .select(post.id)
                .from(post)
                .join(post.postCategories, postCategory)
                .join(postCategory.category, category)
                .where(
                        cursorId != null ? post.id.loe(cursorId) : null,
                        StringUtils.hasText(keyword)
                                ? (keyword.matches("\\d+")
                                ? post.problemNumber.eq(Long.parseLong(keyword))
                                .or(post.title.containsIgnoreCase(keyword))
                                : post.title.containsIgnoreCase(keyword))
                                : null,
                        (categoryNames != null && !categoryNames.isEmpty())
                                ? category.name.in(categoryNames)
                                : null
                )
                .groupBy(post.id);

        if (categoryNames != null && !categoryNames.isEmpty()) {
            subQuery.having(postCategory.category.id.countDistinct().eq((long) categoryNames.size()));
        }

        List<Long> postIds = subQuery
                .orderBy(post.id.desc())
                .limit(size)  // ✅ Service에서 이미 +1을 전달했으므로 그대로 사용
                .fetch();

        if (postIds.isEmpty()) {
            return List.of();
        }

        // ✅ slicing 로직 제거 - Service에서 처리하도록
        return queryFactory
                .selectFrom(post)
                .distinct()
                .leftJoin(post.postCategories, postCategory).fetchJoin()
                .leftJoin(postCategory.category, category).fetchJoin()
                .where(post.id.in(postIds))
                .orderBy(post.id.desc())
                .fetch();
    }
}

